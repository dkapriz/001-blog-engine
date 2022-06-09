package main.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.api.dto.ErrorDTO;
import main.api.dto.PostDTO;
import main.api.request.AddPostRequest;
import main.api.request.LoginRequest;
import main.api.request.VoteRequest;
import main.api.response.PostListResponse;
import main.api.response.ResultResponse;
import main.config.BlogConfig;
import main.model.Post;
import main.model.Tag;
import main.model.enums.ModerationStatusType;
import main.model.repositories.PostRepository;
import main.model.repositories.UserRepository;
import main.service.PostService;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/insert-data-post.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/delete-all.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
public class TestPostController {

    private static final int POST_COUNT = 10;
    private static final int LIMIT = 10;
    private static final int OFFSET = 0;
    private static final String SEARCH_STRING = "Поиск";
    private static final String SEARCH_DATE = "2022-05-15";
    private static final String SEARCH_TAG = "tag1";
    private static final String USER_LOGIN = "test_user@mail.ru";
    private static final String USER_MODERATOR = "test_moderator@mail.ru";
    private static final String PASSWORD = "password";
    private static final String TEST_POST_TITLE = "Не следует";
    private static final String TEST_POST_SHORT = "Не";
    private static final String TEST_POST_TEXT = "Не следует, однако, забывать о том, что постоянный количественный " +
            "рост и сфера нашей активности способствует повышению актуальности дальнейших направлений развития " +
            "проекта? Повседневная практика показывает, что выбранный нами инновационный путь играет важную роль в " +
            "формировании позиций, занимаемых участниками в отношении поставленных задач. Значимость этих проблем " +
            "настолько очевидна, что рамки и место обучения кадров играет важную роль в формировании системы " +
            "масштабного изменения ряда параметров.";

    private static final String POST_RESPONSE_BY_ID_101 = "{\"id\":101,\"user\":{\"id\":10,\"name\":\"Test\"}," +
            "\"title\":\"Заголовок 2\",\"likeCount\":1,\"dislikeCount\":0,\"commentCount\":0,\"viewCount\":1," +
            "\"active\":true,\"text\":\"Текст Текст Текст Текст Текст Текст Текст Текст Текст Текст Текст Текст\"," +
            "\"comments\":[{\"id\":15,\"text\":\"Комментарий 6\",\"user\":{\"id\":10,\"name\":\"Test\"}," +
            "\"timestamp\":1621063930}],\"tags\":[\"tag2\"],\"timestamp\":1621063930}";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private PostService postService;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(this.wac).apply(sharedHttpSession()).build();
    }

    @Test
    public void testGetPostsByModeResent() throws Exception {
        String actualJSONResponse = createPostListResponseByIDsDef(108, 109, 107, 106, 105);

        MockHttpServletRequestBuilder requestBuilder = get("/api/post")
                .param("limit", "5")
                .param("offset", "0")
                .param("mode", "recent");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(actualJSONResponse));
    }

    @Test
    public void testGetPostsByModeBest() throws Exception {
        String actualJSONResponse = createPostListResponseByIDsDef(100, 103, 101, 102, 104);

        MockHttpServletRequestBuilder requestBuilder = get("/api/post")
                .param("limit", "5")
                .param("offset", "0")
                .param("mode", "best");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(actualJSONResponse));
    }

    @Test
    public void testGetPostsByModeEarly() throws Exception {
        String actualJSONResponse = createPostListResponseByIDsDef(100, 101, 102, 103, 104);

        MockHttpServletRequestBuilder requestBuilder = get("/api/post")
                .param("limit", "5")
                .param("offset", "0")
                .param("mode", "early");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(actualJSONResponse));
    }

    @Test
    public void testGetPostsByModePopular() throws Exception {
        String actualJSONResponse = createPostListResponseByIDsDef(108, 103, 101, 102, 106);

        MockHttpServletRequestBuilder requestBuilder = get("/api/post")
                .param("limit", "5")
                .param("offset", "0")
                .param("mode", "popular");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(actualJSONResponse));
    }

    @Test
    public void testGetPostsBySearch() throws Exception {
        String actualJSONResponse = createPostListResponseByIDs(2, 106, 107);

        MockHttpServletRequestBuilder requestBuilder = get("/api/post/search")
                .param("limit", "5")
                .param("offset", "0")
                .param("query", SEARCH_STRING);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(actualJSONResponse));
    }

    @Test
    public void testGetPostsBySearchEmptyQuery() throws Exception {
        String actualJSONResponse = createPostListResponseByIDsDef(108, 109, 107, 106, 105);

        MockHttpServletRequestBuilder requestBuilder = get("/api/post/search")
                .param("limit", "5")
                .param("offset", "0")
                .param("query", "");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(actualJSONResponse));
    }

    @Test
    public void testGetPostsByDate() throws Exception {
        String actualJSONResponse = createPostListResponseByIDs(2, 108, 109);

        MockHttpServletRequestBuilder requestBuilder = get("/api/post/byDate")
                .param("limit", "10")
                .param("offset", "0")
                .param("date", SEARCH_DATE);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(actualJSONResponse));
    }

    @Test
    public void testGetPostsByTag() throws Exception {
        String actualJSONResponse = createPostListResponseByIDs(4, 100, 105, 106, 107);

        MockHttpServletRequestBuilder requestBuilder = get("/api/post/byTag")
                .param("limit", "10")
                .param("offset", "0")
                .param("tag", SEARCH_TAG);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(actualJSONResponse));
    }

    @Test
    public void testGetPostsByID() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get("/api/post/101");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(POST_RESPONSE_BY_ID_101));
    }

    @Test
    public void testGetPostsByIDNotFound() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get("/api/post/1000");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetMyPostsByStatusPublished() throws Exception {
        userLogin(USER_MODERATOR, PASSWORD);
        String actualJSONResponse = createPostListResponseByIDs(2, 108, 109);

        MockHttpServletRequestBuilder requestBuilder = get("/api/post/my")
                .param("limit", "10")
                .param("offset", "0")
                .param("status", "published");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(actualJSONResponse));
    }

    @Test
    public void testGetMyPostsByStatusInactive() throws Exception {
        userLogin(USER_LOGIN, PASSWORD);
        String actualJSONResponse = createPostListResponseByIDs(ModerationStatusType.ACCEPTED,
                (byte) 0, 1, 110);

        MockHttpServletRequestBuilder requestBuilder = get("/api/post/my")
                .param("limit", "10")
                .param("offset", "0")
                .param("status", "inactive");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(actualJSONResponse));
    }

    @Test
    public void testGetMyPostsByStatusDeclined() throws Exception {
        userLogin(USER_LOGIN, PASSWORD);
        String actualJSONResponse = createPostListResponseByIDs(ModerationStatusType.DECLINED,
                (byte) 1, 1, 111);

        MockHttpServletRequestBuilder requestBuilder = get("/api/post/my")
                .param("limit", "10")
                .param("offset", "0")
                .param("status", "declined");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(actualJSONResponse));
    }

    @Test
    public void testGetMyPostsByStatusPending() throws Exception {
        userLogin(USER_LOGIN, PASSWORD);
        String actualJSONResponse = createPostListResponseByIDs(ModerationStatusType.NEW,
                (byte) 1, 1, 112);

        MockHttpServletRequestBuilder requestBuilder = get("/api/post/my")
                .param("limit", "10")
                .param("offset", "0")
                .param("status", "pending");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(actualJSONResponse));
    }

    @Test
    public void getPostsModeration() throws Exception {
        userLogin(USER_MODERATOR, PASSWORD);
        String actualJSONResponse = createPostListResponseByIDs(ModerationStatusType.NEW,
                (byte) 1, 1, 112);

        MockHttpServletRequestBuilder requestBuilder = get("/api/post/moderation")
                .param("limit", "10")
                .param("offset", "0")
                .param("status", "new");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(actualJSONResponse));
    }

    @Test
    public void testAddPost() throws Exception {
        userLogin(USER_MODERATOR, PASSWORD);
        String[] tags = {"tag1", "tag2"};
        int pageOffset = OFFSET / LIMIT;
        ResultResponse actualResponse = new ResultResponse(true);
        AddPostRequest addPostRequest = new AddPostRequest(LocalDateTime.now().atZone(ZoneId.systemDefault())
                .toInstant().getEpochSecond(), (byte) 1, TEST_POST_TITLE, tags, TEST_POST_TEXT);

        ObjectMapper mapper = new ObjectMapper();
        MockHttpServletRequestBuilder requestBuilder = post("/api/post")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(addPostRequest));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));

        Post addedPost = postRepository.findAllByQuery(PageRequest.of(pageOffset, LIMIT, Sort.by("time").descending()),
                TEST_POST_TITLE).stream().findFirst().orElseThrow();
        Assert.assertNotNull(addedPost);
        String actualPostText = addedPost.getText();
        Assert.assertEquals(TEST_POST_TEXT, actualPostText);
        Assert.assertThat(tags, Matchers.arrayContainingInAnyOrder(
                addedPost.getTags().stream().map(Tag::getName).toArray()));
    }

    @Test
    public void testAddPostShortText() throws Exception {
        userLogin(USER_MODERATOR, PASSWORD);
        LocalDateTime date = LocalDateTime.parse("2022-05-12T10:00:00");
        ZonedDateTime zonedDate = date.atZone(ZoneId.systemDefault());
        String[] tags = {"tag1", "tag2"};
        ErrorDTO actualResponse = new ErrorDTO(false, BlogConfig.ERROR_TITLE_FRONTEND_NAME,
                BlogConfig.ERROR_SHORT_TITLE_POST_FRONTEND_MSG);
        AddPostRequest addPostRequest = new AddPostRequest(zonedDate.toInstant().toEpochMilli(),
                (byte) 1, TEST_POST_SHORT, tags, TEST_POST_TEXT);

        ObjectMapper mapper = new ObjectMapper();
        MockHttpServletRequestBuilder requestBuilder = post("/api/post")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(addPostRequest));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
    }

    @Test
    public void testChangePost() throws Exception {
        userLogin(USER_MODERATOR, PASSWORD);
        LocalDateTime date = LocalDateTime.parse("2022-05-12T10:00:00");
        ZonedDateTime zonedDate = date.atZone(ZoneId.systemDefault());
        String[] tags = {"tag1", "tag2"};
        ResultResponse actualResponse = new ResultResponse(true);
        AddPostRequest addPostRequest = new AddPostRequest(zonedDate.toInstant().toEpochMilli(),
                (byte) 1, TEST_POST_TITLE, tags, TEST_POST_TEXT);

        ObjectMapper mapper = new ObjectMapper();
        MockHttpServletRequestBuilder requestBuilder = put("/api/post/109")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(addPostRequest));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
        Post changPost = postRepository.findById(109).orElseThrow();
        Assert.assertNotNull(changPost);
        Assert.assertEquals(TEST_POST_TEXT, changPost.getText());
        Assert.assertThat(tags, Matchers.arrayContainingInAnyOrder(
                changPost.getTags().stream().map(Tag::getName).toArray()));
    }

    @Test
    public void testLikePost() throws Exception {
        userLogin(USER_LOGIN, PASSWORD);
        VoteRequest request = new VoteRequest(106);
        ResultResponse actualResponse = new ResultResponse(true);

        ObjectMapper mapper = new ObjectMapper();
        MockHttpServletRequestBuilder requestBuilder = post("/api/post/like")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
        Post post = postRepository.findById(106).orElseThrow();
        Assert.assertEquals(
                (int) post.getPostVotes()
                        .stream().filter(postVote -> postVote.getValue() == BlogConfig.POST_LIKE).count(),
                1);
    }

    @Test
    public void testLikePostAgain() throws Exception {
        userLogin(USER_LOGIN, PASSWORD);
        VoteRequest request = new VoteRequest(100);
        ResultResponse actualResponse = new ResultResponse(false);

        ObjectMapper mapper = new ObjectMapper();
        MockHttpServletRequestBuilder requestBuilder = post("/api/post/like")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
        Post post = postRepository.findById(100).orElseThrow();
        Assert.assertEquals(
                (int) post.getPostVotes()
                        .stream().filter(postVote -> postVote.getValue() == BlogConfig.POST_LIKE).count(),
                1);
    }

    @Test
    public void testLikePostReplacingLikeValue() throws Exception {
        userLogin(USER_LOGIN, PASSWORD);
        VoteRequest request = new VoteRequest(103);
        ResultResponse actualResponse = new ResultResponse(true);

        ObjectMapper mapper = new ObjectMapper();
        MockHttpServletRequestBuilder requestBuilder = post("/api/post/like")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
        Post post = postRepository.findById(103).orElseThrow();
        Assert.assertEquals(
                (int) post.getPostVotes()
                        .stream().filter(postVote -> postVote.getValue() == BlogConfig.POST_LIKE).count(),
                1);
        Assert.assertEquals(
                (int) post.getPostVotes()
                        .stream().filter(postVote -> postVote.getValue() == BlogConfig.POST_DISLIKE).count(),
                0);
    }

    @Test
    public void testDislikePost() throws Exception {
        userLogin(USER_LOGIN, PASSWORD);
        VoteRequest request = new VoteRequest(106);
        ResultResponse actualResponse = new ResultResponse(true);

        ObjectMapper mapper = new ObjectMapper();
        MockHttpServletRequestBuilder requestBuilder = post("/api/post/dislike")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
        Post post = postRepository.findById(106).orElseThrow();
        Assert.assertEquals(
                (int) post.getPostVotes()
                        .stream().filter(postVote -> postVote.getValue() == BlogConfig.POST_DISLIKE).count(),
                1);
    }

    @Test
    public void testDislikePostAgain() throws Exception {
        userLogin(USER_LOGIN, PASSWORD);
        VoteRequest request = new VoteRequest(103);
        ResultResponse actualResponse = new ResultResponse(false);

        ObjectMapper mapper = new ObjectMapper();
        MockHttpServletRequestBuilder requestBuilder = post("/api/post/dislike")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
        Post post = postRepository.findById(103).orElseThrow();
        Assert.assertEquals(
                (int) post.getPostVotes()
                        .stream().filter(postVote -> postVote.getValue() == BlogConfig.POST_DISLIKE).count(),
                1);
    }

    @Test
    public void testDislikePostReplacingValue() throws Exception {
        userLogin(USER_LOGIN, PASSWORD);
        VoteRequest request = new VoteRequest(100);
        ResultResponse actualResponse = new ResultResponse(true);

        ObjectMapper mapper = new ObjectMapper();
        MockHttpServletRequestBuilder requestBuilder = post("/api/post/dislike")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
        Post post = postRepository.findById(100).orElseThrow();
        Assert.assertEquals(
                (int) post.getPostVotes()
                        .stream().filter(postVote -> postVote.getValue() == BlogConfig.POST_DISLIKE).count(),
                1);
        Assert.assertEquals(
                (int) post.getPostVotes()
                        .stream().filter(postVote -> postVote.getValue() == BlogConfig.POST_LIKE).count(),
                0);
    }

    private String createPostListResponseByIDsDef(int... ResponsePostIDs) throws Exception {
        return createPostListResponseByIDs(POST_COUNT, ResponsePostIDs);
    }

    private String createPostListResponseByIDs(int postCount, int... ResponsePostIDs) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<PostDTO> postsExpected = Arrays.stream(ResponsePostIDs)
                .mapToObj(id -> postService.postToPostDTO(postRepository.findPostByIDIsActiveAndAccepted(id)
                        .orElseThrow(() -> new IllegalArgumentException("Пост не найден"))))
                .collect(Collectors.toList());
        PostListResponse postListResponse = new PostListResponse(postCount, postsExpected);
        return mapper.writeValueAsString(postListResponse);
    }

    private String createPostListResponseByIDs(ModerationStatusType moderationStatus, byte isActive, int postCount,
                                               int... ResponsePostIDs) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<PostDTO> postsExpected = Arrays.stream(ResponsePostIDs)
                .mapToObj(id -> postService.postToPostDTO(postRepository.findPostByID(id, isActive, moderationStatus)
                        .orElseThrow(() -> new IllegalArgumentException("Пост не найден"))))
                .collect(Collectors.toList());
        PostListResponse postListResponse = new PostListResponse(postCount, postsExpected);
        return mapper.writeValueAsString(postListResponse);
    }

    private void userLogin(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest(username, password);
        userRepository.findByEmailIgnoreCase(username).orElseThrow(null);
        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isOk()).andReturn();
    }
}
