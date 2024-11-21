package cotato.backend.domains.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Post {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Integer views = 0; // 조회수를 0으로 초기화

	private int likes; // 좋아요 수

	@Builder
	public Post(String title, String content, String name) {
		this.title = title;
		this.content = content;
		this.name = name;
	}


	//SOLID 중 SRP(단일 책임 원칙)에 부합하게 설계 --> 조회수 증가 로직을 엔터티 내부에 구현
	public void incrementViews() {
		this.views++;
	}

}
