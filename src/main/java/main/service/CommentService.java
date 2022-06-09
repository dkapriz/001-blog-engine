package main.service;

import lombok.AllArgsConstructor;
import main.exception.DataNotFoundException;
import main.model.PostComment;
import main.model.repositories.PostCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentService {

    @Autowired
    private final PostCommentRepository postCommentRepository;

    public PostComment getCommentByID(int id) {
        return postCommentRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Комментарий с id " + id + " не найден"));
    }

    public void saveComment(PostComment postComment) {
        postCommentRepository.save(postComment);
    }
}
