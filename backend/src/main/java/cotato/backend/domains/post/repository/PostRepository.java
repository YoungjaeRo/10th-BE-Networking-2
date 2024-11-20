package cotato.backend.domains.post.repository;

import java.awt.print.Pageable;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import cotato.backend.domains.post.entity.Post;


public interface PostRepository extends JpaRepository<Post, Long> {
	// likes 순으로 정렬하고, Pageable을 이용해서 페이징 처리
	Page<Post> findAllByOrderByLikesDesc(Pageable pageable);
}