package cotato.backend.domains.post.service.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cotato.backend.domains.post.dto.PostResponseDto;
import cotato.backend.domains.post.entity.Post;
import cotato.backend.domains.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 조회를 담당하는 서비스 계층이기 때문에, readOnly = true로 설정
public class PostQueryService {

	private final PostRepository postRepository;
	// 좋아요 순으로 게시글을 조회
	public Page<PostResponseDto> getPostByLikes(Pageable pageable) {
		// PostRepository에서 좋아요 순으로 정렬된 게시글 페이지를 가져옴
		Page<Post> posts = postRepository.findAllByOrderByLikesDesc(pageable);

		// 리스트 안에 있는 각각의 Post --> PostResponseDto로 변환
		return posts.map(post -> new PostResponseDto(
			post.getId(),
			post.getTitle(),
			post.getName(),
			post.getLikes(),
			post.getViews()
		));
	}
}
