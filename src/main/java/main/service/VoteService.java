package main.service;

import lombok.AllArgsConstructor;
import main.api.request.VoteRequest;
import main.api.response.ResultResponse;
import main.config.BlogConfig;
import main.model.Post;
import main.model.PostVote;
import main.model.User;
import main.model.repositories.PostVoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class VoteService {
    @Autowired
    private final PostVoteRepository postVoteRepository;
    @Autowired
    private final PostService postService;
    @Autowired
    private final UserService userService;

    public int countAllLikesByUser(User user) {
        return postVoteRepository.countByValueAndUser(BlogConfig.POST_LIKE, user).orElse(0);
    }

    public int countAllLikes() {
        return postVoteRepository.countByValue(BlogConfig.POST_LIKE).orElse(0);
    }

    public int countAllDislikesByUser(User user) {
        return postVoteRepository.countByValueAndUser(BlogConfig.POST_DISLIKE, user).orElse(0);
    }

    public int countAllDislikes() {
        return postVoteRepository.countByValue(BlogConfig.POST_DISLIKE).orElse(0);
    }

    public ResultResponse likePost(VoteRequest voteRequest) {
        return new ResultResponse(setPostVote(BlogConfig.POST_LIKE, voteRequest.getPostId()));
    }

    public ResultResponse dislikePost(VoteRequest voteRequest) {
        return new ResultResponse(setPostVote(BlogConfig.POST_DISLIKE, voteRequest.getPostId()));
    }

    private boolean setPostVote(byte value, int postID) {
        Post post = postService.getActiveAndAcceptedById(postID);
        User user = userService.getLoggedUser();
        PostVote postVote = postVoteRepository.findByUserAndPost(user, post).orElse(new PostVote());
        if (postVote.getValue() == value) {
            return false;
        }
        postVote.setPost(post);
        postVote.setUser(user);
        postVote.setTime(LocalDateTime.now());
        postVote.setValue(value);
        postVoteRepository.save(postVote);
        return true;
    }
}
