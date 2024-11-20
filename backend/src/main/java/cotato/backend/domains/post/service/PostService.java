package cotato.backend.domains.post.service;

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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional
public class PostService {

	// MVC 패턴이므로 Service계층에서 Repository를 참조
	private final PostRepository postRepository;

	// 로컬 파일 경로로부터 엑셀 파일을 읽어 Post 엔터티로 변환하고 저장
	public void saveEstatesByExcel(String filePath) {
		try {
			// 엑셀 파일을 읽어 데이터 프레임 형태로 변환
			List<Post> posts = ExcelUtils.parseExcelFile(filePath).stream()
				.map(row -> { // 스트림 방식
					String title = row.get("title");
					String content = row.get("content");
					String name = row.get("name");

					return new Post(title, content, name);
				})
				.collect(Collectors.toList());

			// stream 으로 처리해서 가져온 List<Post>의 데이터를 DB에 저장 (JPA Repository 인터페이스가 제공하는 saveAll 메서드를 이용)
			postRepository.saveAll(posts);

		} catch (Exception e) {
			log.error("Failed to save estates by excel", e);
			throw ApiException.from(INTERNAL_SERVER_ERROR);
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
}
