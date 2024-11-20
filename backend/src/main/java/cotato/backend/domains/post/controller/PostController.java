package cotato.backend.domains.post.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cotato.backend.common.dto.DataResponse;
import cotato.backend.domains.post.dto.PostRequest;
import cotato.backend.domains.post.dto.PostResponseDto;
import cotato.backend.domains.post.entity.Post;
import cotato.backend.domains.post.service.command.PostCommandService;
import cotato.backend.domains.post.dto.SavePostsByExcelRequest;
import cotato.backend.domains.post.service.query.PostQueryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_SIZE = 10;

	private final PostCommandService postService;
	private final PostQueryService postQueryService;

	@Operation(summary = "게시글 생성 Api")
	@PostMapping("/add")
	public ResponseEntity<DataResponse<Post>> createPost(@RequestBody PostRequest postRequest) {
		Post post = postService.createPost(postRequest);

		return ResponseEntity.ok(DataResponse.from(post));
	}


	@Operation(summary = "게시글 다중 생성 기능 Api")
	@PostMapping("/excel")
	public ResponseEntity<DataResponse<Void>> savePostsByExcel(@RequestBody SavePostsByExcelRequest request) {
		postService.saveEstatesByExcel(request.getPath());

		return ResponseEntity.ok(DataResponse.ok());
	}


	@Operation(summary = "게시글 조회 기능 Api")
	@GetMapping("/{id}") //Get 메서드는 JSON 형식으로 오는게 아니므로 @PathVariable을 사용: variable이랑 "매핑되는 변수의 이름"이 같아야 함
	public ResponseEntity<DataResponse<Post>> getPostById(@PathVariable Long id) {
		Post post = postService.getPostById(id);
		return ResponseEntity.ok(DataResponse.from(post));
	}


	@Operation(summary = "게시글 목록 조회 기능 Api", description = "좋아요 순으로 정렬된 게시글 목록을 조회(페이지네이션 적용)")
	@GetMapping("/list")
	public ResponseEntity<DataResponse<Page<PostResponseDto>>> getPostsByLikes (
		@RequestParam(defaultValue = DEFAULT_PAGE + "") int page,
		@RequestParam(defaultValue = DEFAULT_SIZE + "") int size) { // 기본적으로 페이지 내 10개의 데이터를 표시할 예정

		//Pageable 객체 생성 -> 페이지 번호, 크기, 좋아요 기준으로 DESC 정렬
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("likes")));

		Page<PostResponseDto> posts = postQueryService.getPostByLikes(pageable);

		return ResponseEntity.ok(DataResponse.from(posts));
	}
}
