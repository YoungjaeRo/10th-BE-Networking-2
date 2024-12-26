package cotato.backend.domains.post.service.command;

import static cotato.backend.common.exception.ErrorCode.*;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cotato.backend.common.excel.ExcelUtils;
import cotato.backend.common.exception.ApiException;
import cotato.backend.domains.post.dto.PostRequest;
import cotato.backend.domains.post.entity.Post;
import cotato.backend.domains.post.repository.PostRepository;
import jakarta.persistence.EntityManager;
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

	private final EntityManager entityManager;

	private static final int BATCH_SIZE = 1000; // 배치 사이즈 설정

	// 로컬 파일 경로로부터 엑셀 파일을 읽어 Post 엔터티로 변환하고 저장
	public void saveEstatesByExcel(String filePath) {
		try {
			// 엑셀 데이터를 읽어 Post 엔터티로 변환
			List<Post> posts = ExcelUtils.parseExcelFile(filePath).stream()
				.map(row -> {
					String title = row.get("title");
					String content = row.get("content");
					String name = row.get("name");
					return new Post(title, content, name);
				})
				.collect(Collectors.toList());

			// Bulk insert를 위한 엔티티 배치 저장
			for (int i = 0; i < posts.size(); i++) {
				entityManager.persist(posts.get(i));  // 엔티티를 persistence context에 저장

				// 배치 크기가 BATCH_SIZE에 도달하면 flush하고, persistence context를 clear
				if (i > 0 && i % BATCH_SIZE == 0) {
					entityManager.flush(); // 영속성 컨텍스트를 DB에 반영
					entityManager.clear(); // 영속성 컨텍스트 초기화 -->  메모리 절약
				}
			}

			// 마지막 남은 엔티티들 flush
			entityManager.flush();
			entityManager.clear();

		} catch (Exception e) {
			log.error("Failed to save estates by excel", e);
			throw ApiException.from(INTERNAL_SERVER_ERROR);
		}
	}

	// /**
	//  *
	//  * @ 청크 단위로 데이터를 저장함
	//  */
	// private void savePostInChunks(List<Post> posts) {
	// 	int batchSize = 100;
	// 	for(int i = 0; i < posts.size(); i += batchSize) {
	// 		List<Post> chunk = posts.subList(i, Math.min(i + batchSize, posts.size()));
	// 		postRepository.saveAll(chunk); // saveAll로 저장
	// 		postRepository.flush(); // 하이버네이트의 Batch Insert로 최적화
	// 	}
	// }

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
