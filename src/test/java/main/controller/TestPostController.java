package main.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.api.dto.PostDTO;
import main.api.response.PostListResponse;
import main.configuratoin.BlogConfig;
import main.model.repositories.PostRepository;
import main.service.PostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/insert-data-post.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/delete-all.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
public class TestPostController {

    private static final int POST_COUNT = 10;
    private static final String GET = "get";
    private static final String POST = "post";
    private static final String PUT = "put";
    private static final String DELETE = "delete";
    private static final String SEARCH_STRING = "Поиск";
    private static final String SEARCH_DATE = "2022-05-15";
    private static final String SEARCH_TAG = "tag1";

    private static final String POST_RESPONSE_BY_ID_101 = "{\"id\":101,\"user\":{\"id\":10,\"name\":\"Test\"}," +
            "\"title\":\"Заголовок 2\",\"likeCount\":0,\"dislikeCount\":0,\"commentCount\":0,\"viewCount\":0," +
            "\"active\":true,\"text\":\"Текст Текст Текст Текст Текст Текст Текст Текст Текст Текст Текст Текст\"," +
            "\"comments\":[{\"id\":15,\"text\":\"Комментарий 6\",\"user\":{\"id\":10,\"name\":\"Test\"}," +
            "\"timestamp\":1621161130}],\"tags\":[\"tag2\"],\"timestamp\":1621074730}";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BlogConfig config;
    @Autowired
    private PostService postService;
    @Autowired
    private PostRepository postRepository;

    @Test
    public void testGetPostsByModeResent() throws Exception {
        testGetPosts(status().isOk(), 5, 0, "recent",
                createPostListResponseByIDsDef(108, 109, 107, 106, 105));
    }

    @Test
    public void testGetPostsByModeBest() throws Exception {
        testGetPosts(status().isOk(), 5, 0, "best",
                createPostListResponseByIDsDef(100, 103, 101, 102, 104));
    }

    @Test
    public void testGetPostsByModeEarly() throws Exception {
        testGetPosts(status().isOk(), 5, 0, "early",
                createPostListResponseByIDsDef(100, 101, 102, 103, 104));
    }

    @Test
    public void testGetPostsByModePopular() throws Exception {
        testGetPosts(status().isOk(), 5, 0, "popular",
                createPostListResponseByIDsDef(108, 103, 101, 102, 106));
    }

    @Test
    public void testGetPostsBySearch() throws Exception {
        testGetPosts(status().isOk(), GET, "/api/post/search", 5, 0, "query",
                SEARCH_STRING, createPostListResponseByIDs(2, 106, 107));
    }

    @Test
    public void testGetPostsBySearchEmptyQuery() throws Exception {
        testGetPosts(status().isOk(), GET, "/api/post/search", 5, 0, "query",
                "", createPostListResponseByIDsDef(108, 109, 107, 106, 105));
    }

    @Test
    public void testGetPostsByDate() throws Exception {
        testGetPosts(status().isOk(), GET, "/api/post/byDate", 10, 0, "date",
                SEARCH_DATE, createPostListResponseByIDs(2, 108, 109));
    }

    @Test
    public void testGetPostsByTag() throws Exception {
        testGetPosts(status().isOk(), GET, "/api/post/byTag", 10, 0, "tag",
                SEARCH_TAG, createPostListResponseByIDs(4, 100, 105, 106, 107));
    }

    @Test
    public void testGetPostsByID() throws Exception {
        testGetPosts(status().isOk(), "/api/post/101", POST_RESPONSE_BY_ID_101);
    }

    @Test
    public void testGetPostsByIDNotFound() throws Exception {
        testGetPosts(status().isNotFound(), "/api/post/1000", null);
    }

    private void testGetPosts(ResultMatcher statusCode, String url, String executeResponse) throws Exception {
        testGetPosts(statusCode, GET, url, 0, 0, "", "", executeResponse);
    }

    private void testGetPosts(ResultMatcher statusCode, int limit, int offset,
                              String paramValue, String executeResponse) throws Exception {
        testGetPosts(statusCode, GET, "/api/post", limit, offset, "mode", paramValue, executeResponse);
    }

    private void testGetPosts(ResultMatcher statusCode, String httpMethod, String url, int limit, int offset,
                              String paramName, String paramValue, String executeResponse) throws Exception {
        MockHttpServletRequestBuilder requestBuilder;
        switch (httpMethod) {
            case GET:
                requestBuilder = get(url);
                break;
            case POST:
                requestBuilder = post(url);
                break;
            case PUT:
                requestBuilder = put(url);
                break;
            case DELETE:
                requestBuilder = delete(url);
                break;
            default:
                return;
        }
        if (limit > 0) {
            requestBuilder = requestBuilder.param("limit", String.valueOf(limit));
        }
        if (offset > 0) {
            requestBuilder = requestBuilder.param("offset", String.valueOf(offset));
        }
        if (!paramName.isEmpty()) {
            requestBuilder = requestBuilder.param(paramName, paramValue);
        }
        mockMvc.perform(requestBuilder)
                .andExpect(statusCode)
                .andExpect(executeResponse == null ? statusCode : content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(executeResponse == null ? statusCode : content().json(executeResponse));
    }

    private String createPostListResponseByIDsDef(int... ResponsePostIDs) throws Exception {
        return createPostListResponseByIDs(POST_COUNT, ResponsePostIDs);
    }

    private String createPostListResponseByIDs(int postCount, int... ResponsePostIDs) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<PostDTO> postsExpected = Arrays.stream(ResponsePostIDs)
                .mapToObj(id -> postService.postToPostDTO(postRepository.findPostByID(id)))
                .collect(Collectors.toList());
        PostListResponse postListResponse = new PostListResponse(postCount, postsExpected);
        return mapper.writeValueAsString(postListResponse);
    }
}
