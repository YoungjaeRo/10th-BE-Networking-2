package cotato.backend.domains.post.service.command;

import static cotato.backend.common.exception.ErrorCode.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cotato.backend.common.excel.ExcelUtils;
import cotato.backend.common.exception.ApiException;
import cotato.backend.domains.post.dto.PostRequest;
import cotato.backend.domains.post.entity.Post;
import cotato.backend.domains.post.repository.PostRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional
public class PostCommandService {

	// MVC 패턴이므로 Service계층에서 Repository를 참조
	private final PostRepository postRepository;

	// Bulk insert 방식, 특히 대량 데이터 삽입 최적화를 우해서는 JDBC template이 적절함
	private final JdbcTemplate jdbcTemplate;

	private static final int BATCH_SIZE = 1000; // 배치 사이즈 설정

	// 로컬 파일 경로로부터 엑셀 파일을 읽어 Post 엔터티로 변환하고 저장
	public void saveEstatesByExcel(String filePath) {
		try {
			// 엑셀 데이터를 읽어 Post 엔터티로 변환
			List<Post> posts = ExcelUtils.parseExcelFile(filePath).stream()
				.map(row -> new Post(
					row.get("title"),
					row.get("content"),
					row.get("name")
				))
				.collect(Collectors.toList());

			// 데이터베이스에 배치 저장
			batchInsert(posts);

		} catch (Exception e) {
			log.error("Failed to save estates by excel", e);
			throw ApiException.from(INTERNAL_SERVER_ERROR);
		}
	}

	public void batchInsert(List<Post> posts) {
		String sql = "INSERT INTO post (title, content, name, views, likes) VALUES (?, ?, ?, ?, ?)";

		List<Object[]> batchArgs = new ArrayList<>();
		for (Post post : posts) {
			batchArgs.add(new Object[]{
				post.getTitle(),
				post.getContent(),
				post.getName(),
				post.getViews(),
				post.getLikes()
			});

			// 배치 크기에 도달하면 실행
			if (batchArgs.size() == BATCH_SIZE) {
				jdbcTemplate.batchUpdate(sql, batchArgs);
				batchArgs.clear(); // 리스트 초기화
			}
		}

		// 남은 데이터 처리
		if (!batchArgs.isEmpty()) {
			jdbcTemplate.batchUpdate(sql, batchArgs);
		}
	}

	//게시글 생성 서비스 로직
	public Post createPost(PostRequest postRequest) {
		// dto로 받아와 생성한 객체에서 필드값을 추출해 Post 생성
		Post post = new Post(postRequest.getTitle(), postRequest.getContent(), postRequest.getName());
		// 저장후, 저장된 객체 반환
		return postRepository.save(post);
	}

	// 게시글 조회 비즈니스 로직
	public Post getPostById(Long id) {

		//반환 값이 Optional<>이기 떄문에 orElseThrow로 예외처리를 꼭해줘야 함
		Post post = postRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "해당 게시물을 찾을 수 없습니다.", "POST_NOT_FOUND"));

		//조회를 했기 때문에 --> 조회수 증가
		post.incrementViews();
		return postRepository.save(post);
	}

	// 게시글 삭제 비즈니스 로직
	public void deletePostById(Long id) {
		// 게시글이 있는지 확인 (없다면)
		if(!postRepository.existsById(id)) {
			throw new IllegalArgumentException("해당 ID의 게시글이 존재하지 않습니다!");
		}
		// 게시글이 존재한다면 --> 삭제 진행
		postRepository.deleteById(id);
	}
}
