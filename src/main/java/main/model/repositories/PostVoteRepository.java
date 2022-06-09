package main.model.repositories;

import main.model.Post;
import main.model.PostVote;
import main.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostVoteRepository extends CrudRepository<PostVote, Integer> {
    Optional<Integer> countByValue(byte value);

    Optional<Integer> countByValueAndUser(byte value, User user);

    Optional<PostVote> findByUserAndPost(User user, Post post);
}
