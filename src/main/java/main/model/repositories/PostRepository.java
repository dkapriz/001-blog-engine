package main.model.repositories;

import main.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends CrudRepository<Post, Integer> {

    @Query(value = "SELECT p FROM Post p " +
            "LEFT JOIN User u ON u.id = p.user " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' " +
            "AND p.time <= CURRENT_TIMESTAMP()")
    Page<Post> findAll(Pageable pageable);

    @Query(value = "SELECT p FROM Post p " +
            "LEFT JOIN User u ON u.id = p.user " +
            "LEFT JOIN PostComment pc ON pc.post = p.id " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' " +
            "AND p.time <= CURRENT_TIMESTAMP() " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(pc) DESC")
    Page<Post> findAllSortByCountCommentDesc(Pageable pageable);

    @Query(value = "SELECT p FROM Post p " +
            "LEFT JOIN User u ON u.id = p.user " +
            "LEFT JOIN PostVote pv ON pv.post = p.id " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' " +
            "AND p.time <= CURRENT_TIMESTAMP() " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(pv) DESC")
    Page<Post> findAllSortByCountLikeDesc(Pageable pageable);
}
