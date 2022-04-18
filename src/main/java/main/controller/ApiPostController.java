package main.controller;

import lombok.AllArgsConstructor;
import main.api.request.AddPostRequest;
import main.api.response.AddPostResponse;
import main.api.response.PostResponse;
import main.api.response.SinglePostResponse;
import main.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/api/post")
@AllArgsConstructor
public class ApiPostController {

    private final PostService postService;

    @GetMapping()
    private ResponseEntity<PostResponse> posts(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "recent") String mode) {
        return new ResponseEntity<>(postService.getPosts(offset, limit, mode), HttpStatus.OK);
    }

    @GetMapping("/search")
    private ResponseEntity<PostResponse> postSearch(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String query) {
        return new ResponseEntity<>(postService.getSearchPosts(offset, limit, query), HttpStatus.OK);
    }

    @GetMapping("/byDate")
    private ResponseEntity<PostResponse> postByDate(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String date) throws ParseException {
        return new ResponseEntity<>(postService.getPostsByDate(offset, limit, date), HttpStatus.OK);
    }

    @GetMapping("/byTag")
    private ResponseEntity<PostResponse> postByTag(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String tag) {
        return new ResponseEntity<>(postService.getPostsByTag(offset, limit, tag), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    private ResponseEntity<SinglePostResponse> postByID(@PathVariable int id) {
        SinglePostResponse singlePostRequest = postService.getPostByID(id);
        if (singlePostRequest == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(singlePostRequest, HttpStatus.OK);
    }

    @PostMapping()
    private ResponseEntity<AddPostResponse> addPost(@RequestBody AddPostRequest addPostRequest) {
        return new ResponseEntity<>(postService.addPost(addPostRequest), HttpStatus.OK);
    }
}