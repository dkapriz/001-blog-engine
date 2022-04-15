package main.controller;

import lombok.AllArgsConstructor;
import main.api.request.AddPostRequest;
import main.api.response.AddPostResponse;
import main.api.response.PostResponse;
import main.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ApiPostController {

    private final PostService postService;

    @GetMapping("/post")
    private ResponseEntity<PostResponse> posts(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "recent") String mode) {
        return new ResponseEntity<>(postService.getPosts(offset, limit, mode), HttpStatus.OK);
    }

    @PostMapping("/post")
    private ResponseEntity<AddPostResponse> addPost(@RequestBody AddPostRequest addPostRequest) {
        return new ResponseEntity<>(postService.addPost(addPostRequest), HttpStatus.OK);
    }
}