package main.model.repositories;

import main.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

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

    @Query(value = "SELECT p FROM Post p " +
            "LEFT JOIN User u ON u.id = p.user " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' " +
            "AND p.time <= CURRENT_TIMESTAMP() " +
            "AND UPPER(p.title) LIKE CONCAT('%',UPPER(:query),'%')")
    Page<Post> findAllByQuery(Pageable pageable, @Param("query") String query);

    @Query(value = "SELECT p FROM Post p " +
            "LEFT JOIN User u ON u.id = p.user " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' " +
            "AND p.time <= CURRENT_TIMESTAMP() " +
            "AND DATE(p.time) = :date")
    Page<Post> findAllByDate(Pageable pageable, @Param("date") Date date);

    @Query(value = "SELECT p FROM Post p " +
            "LEFT JOIN User u ON u.id = p.user " +
            "LEFT JOIN p.tags t " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' " +
            "AND p.time <= CURRENT_TIMESTAMP() " +
            "AND t.name = :tagName")
    Page<Post> findAllByTag(Pageable pageable, @Param("tagName") String tagName);

    @Query(value = "SELECT p FROM Post p " +
            "LEFT JOIN User u ON u.id = p.user " +
            "LEFT JOIN PostComment pc ON pc.post = p.id " +
            "LEFT JOIN PostVote pv ON pv.post = p.id " +
            "LEFT JOIN p.tags " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' " +
            "AND p.time <= CURRENT_TIMESTAMP() " +
            "AND p.id = :id")
    Post findPostByID(@Param("id") int id);

    @Query(value = "SELECT p FROM Post p " +
            "LEFT JOIN User u ON u.id = p.user " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' " +
            "AND p.time <= CURRENT_TIMESTAMP() " +
            "AND YEAR(p.time) = YEAR(:year) " +
            "ORDER BY p.time")
    List<Post> findAllByYear(@Param("year") Date year);

    @Query(value = "SELECT YEAR(p.time) FROM Post p " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' " +
            "AND p.time <= CURRENT_TIMESTAMP() " +
            "GROUP BY YEAR(p.time) " +
            "ORDER BY YEAR(p.time)")
    String[] findAllYearValue();
}
