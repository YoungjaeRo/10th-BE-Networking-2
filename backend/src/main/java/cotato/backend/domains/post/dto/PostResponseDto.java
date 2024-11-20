package cotato.backend.domains.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostResponseDto {
    // 게시글 ID
    private Long id;

    // 게시글 제목
    private String title;

    // 작성자 이름
    private String name;

    // 좋아요 수
    private int likes;

    // 조회수
    private int views;
}