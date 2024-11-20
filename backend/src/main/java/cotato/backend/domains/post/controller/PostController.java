package cotato.backend.domains.post.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cotato.backend.common.dto.DataResponse;
import cotato.backend.domains.post.dto.PostRequest;
import cotato.backend.domains.post.entity.Post;
import cotato.backend.domains.post.service.PostService;
import cotato.backend.domains.post.dto.SavePostsByExcelRequest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@Operation(summary = "게시글 생성 Api")
	@PostMapping
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
	@GetMapping("/{id}") //Get 메서드는 JSON 형식으로 오는게 아니므로 @PathVariable을 사용: variable이랑 "매핑 변수의 이름"이 같아야 함
	public ResponseEntity<DataResponse<Post>> getPostById(@PathVariable Long id) {
		Post post = postService.getPostById(id);
		return ResponseEntity.ok(DataResponse.from(post));
	}

}
