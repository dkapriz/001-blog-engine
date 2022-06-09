package main.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.api.dto.ErrorDTO;
import main.api.dto.TagDTO;
import main.api.request.*;
import main.api.response.*;
import main.config.BlogConfig;
import main.model.GlobalSetting;
import main.model.Post;
import main.model.User;
import main.model.enums.ModerationStatusType;
import main.model.repositories.GlobalSettingRepository;
import main.model.repositories.PostRepository;
import main.model.repositories.UserRepository;
import main.service.SettingsService;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
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
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/insert-data-general.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/delete-all.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class TestGeneralController {

    private static final String INIT_BLOG_TITLE = "DevPub";
    private static final String INIT_BLOG_SUBTITLE = "Заметки разработчика";
    private static final String INIT_BLOG_PHONE = "+7 (906) 764-73-71";
    private static final String INIT_BLOG_EMAIL = "dkapriz@mail.ru";
    private static final String INIT_BLOG_COPYRIGHT = "Дмитрий Каприз";
    private static final String INIT_BLOG_COPYRIGHT_FROM = "2022";

    private static final String[] TAG_NAME = {"tag_test1", "tag_test2", "tag3", "tag4", "tag5"};
    private static final double[] TAG_WEIGHT = {1.0, 0.75, 0.25, 0.25, 0.25};
    private static final String TAG_TESTED_NAME = "tag_test";
    private static final String[] CALENDAR_YEARS_LIST = {"2021", "2022"};
    private static final String CALENDAR_EXPECTED_POST_DATE = "2022-05-15";
    private static final int CALENDAR_EXPECTED_POST_COUNT = 3;
    private static final String CALENDAR_TESTED_YEAR = "2022";
    private static final String CALENDAR_EMPTY_YEAR = "";
    private static final String POST_COMMENT_TEXT = "Метод фиксирует действие модератора по посту";

    private static final String USER_LOGIN = "test_user@mail.ru";
    private static final String USER_MODERATOR = "test_moderator@mail.ru";
    private static final String PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private GlobalSettingRepository globalSettingRepository;

    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(this.wac).apply(sharedHttpSession()).build();
    }

    @Test
    public void testInit() throws Exception {
        RequestBuilder requestBuilder = get("/api/init");

        mockMvc.perform(requestBuilder)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", Matchers.is(INIT_BLOG_TITLE)))
                .andExpect(jsonPath("$.subtitle", Matchers.is(INIT_BLOG_SUBTITLE)))
                .andExpect(jsonPath("$.phone", Matchers.is(INIT_BLOG_PHONE)))
                .andExpect(jsonPath("$.email", Matchers.is(INIT_BLOG_EMAIL)))
                .andExpect(jsonPath("$.copyright", Matchers.is(INIT_BLOG_COPYRIGHT)))
                .andExpect(jsonPath("$.copyrightFrom", Matchers.is(INIT_BLOG_COPYRIGHT_FROM)));
    }

    @Test
    public void testGetGlobalSettings() throws Exception {
        SettingsResponse ActualResponse = settingsService.getGlobalSettings();

        RequestBuilder requestBuilder = get("/api/settings");

        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(ActualResponse)));
    }

    @Test
    public void testSaveGlobalSettings() throws Exception {
        userLogin(USER_MODERATOR, PASSWORD);
        SettingsRequest request = new SettingsRequest(false, true, true);

        ObjectMapper mapper = new ObjectMapper();
        MockHttpServletRequestBuilder requestBuilder = put("/api/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
        Iterable<GlobalSetting> iterable = globalSettingRepository.findAll();
        for (GlobalSetting globalSetting : iterable) {
            if (globalSetting.getCode().equals(BlogConfig.MULTI_USER_MODE_FIELD_NAME)) {
                Assert.assertEquals(globalSetting.getValue(), "NO");
            }
            if (globalSetting.getCode().equals(BlogConfig.POST_PRE_MODERATION_FIELD_NAME) ||
                    globalSetting.getCode().equals(BlogConfig.STATISTIC_IS_PUBLIC_FIELD_NAME)) {
                Assert.assertEquals(globalSetting.getValue(), "YES");
            }
        }
    }

    @Test
    public void testGetTagsWithQuery() throws Exception {
        TagResponse actualResponse = createTagResponse(2);

        RequestBuilder requestBuilder = get("/api/tag").param("query", TAG_TESTED_NAME)
                .contentType(MediaType.APPLICATION_JSON);

        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
    }

    @Test
    public void testGetTagsEmptyQuery() throws Exception {
        TagResponse actualResponse = createTagResponse(5);

        RequestBuilder requestBuilder = get("/api/tag").param("query", "")
                .contentType(MediaType.APPLICATION_JSON);

        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
    }

    @Test
    public void testGetTagsWithoutQuery() throws Exception {
        TagResponse actualResponse = createTagResponse(5);

        RequestBuilder requestBuilder = get("/api/tag")
                .contentType(MediaType.APPLICATION_JSON);

        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
    }

    @Test
    public void testGetTagsWithQueryBadTag() throws Exception {
        TagResponse actualResponse = createTagResponse(0);

        RequestBuilder requestBuilder = get("/api/tag").param("query", "badTag")
                .contentType(MediaType.APPLICATION_JSON);

        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
    }

    @Test
    public void testCalendarSetYear() throws Exception {
        Map<String, Integer> posts = new HashMap<>();
        posts.put(CALENDAR_EXPECTED_POST_DATE, CALENDAR_EXPECTED_POST_COUNT);
        CalendarResponse actualResponse = new CalendarResponse(CALENDAR_YEARS_LIST, posts);

        RequestBuilder requestBuilder = get("/api/calendar").param("year", CALENDAR_TESTED_YEAR)
                .contentType(MediaType.APPLICATION_JSON);

        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
    }

    @Test
    public void testCalendarEmptyYear() throws Exception {
        Map<String, Integer> posts = new HashMap<>();
        posts.put(CALENDAR_EXPECTED_POST_DATE, CALENDAR_EXPECTED_POST_COUNT);
        CalendarResponse actualResponse = new CalendarResponse(CALENDAR_YEARS_LIST, posts);

        RequestBuilder requestBuilder = get("/api/calendar").param("year", CALENDAR_EMPTY_YEAR)
                .contentType(MediaType.APPLICATION_JSON);

        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
    }

    @Test
    public void testAddPostComment() throws Exception {
        userLogin(USER_LOGIN, PASSWORD);
        CommentRequest request = new CommentRequest(0, 103, POST_COMMENT_TEXT);

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/comment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.notNullValue()));
    }

    @Test
    public void testAddPostCommentEmptyText() throws Exception {
        userLogin(USER_LOGIN, PASSWORD);
        CommentRequest request = new CommentRequest(0, 103, "");

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/comment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", Matchers.is(false)))
                .andExpect(jsonPath("$.errors.text",
                        Matchers.is(BlogConfig.ERROR_EMPTY_TEXT_POST_COMMENT_FRONTEND_MSG)));
    }

    @Test
    public void testAddPostCommentBadPostID() throws Exception {
        userLogin(USER_LOGIN, PASSWORD);
        CommentRequest request = new CommentRequest(0, 0, POST_COMMENT_TEXT);

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/comment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testModeratePostAccept() throws Exception {
        userLogin(USER_MODERATOR, PASSWORD);
        ModeratePostRequest request = new ModeratePostRequest(110, "accept");

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/moderation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", Matchers.is(true)));
        Post post = postRepository.findById(110).orElseThrow();
        Assert.assertEquals(ModerationStatusType.ACCEPTED, post.getModerationStatus());
    }

    @Test
    public void testModeratePostDecline() throws Exception {
        userLogin(USER_MODERATOR, PASSWORD);
        ModeratePostRequest request = new ModeratePostRequest(110, "decline");

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/moderation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", Matchers.is(true)));
        Post post = postRepository.findById(110).orElseThrow();
        Assert.assertEquals(ModerationStatusType.DECLINED, post.getModerationStatus());
    }

    @Test
    public void testModeratePostEmptyParameters() throws Exception {
        userLogin(USER_MODERATOR, PASSWORD);
        ModeratePostRequest request = new ModeratePostRequest(110, "11");

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/moderation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", Matchers.is(false)));
    }

    @Test
    public void testEditProfile() throws Exception {
        userLogin(USER_LOGIN, PASSWORD);
        ProfileRequest request = new ProfileRequest("newName", "newEmail@mail.ru",
                "newPassword", 0);
        ResultResponse actualResponse = new ResultResponse(true);

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/profile/my")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
        User user = userRepository.findByEmailIgnoreCase("newEmail@mail.ru").orElseThrow();
        Assert.assertNotNull(user);
        Assert.assertEquals("newName", user.getName());
        Assert.assertEquals("newEmail@mail.ru", user.getEmail());
    }

    @Test
    public void testEditProfileBadEmail() throws Exception {
        userLogin(USER_LOGIN, PASSWORD);
        ProfileRequest request = new ProfileRequest("newName", USER_MODERATOR,
                "newPassword", 0);
        ErrorDTO actualResponse = new ErrorDTO(false, BlogConfig.ERROR_EMAIL_FRONTEND_NAME,
                BlogConfig.ERROR_EMAIL_FRONTEND_MSG_REG);

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/profile/my")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
    }

    @Test
    public void testEditProfileBadName() throws Exception {
        userLogin(USER_LOGIN, PASSWORD);
        ProfileRequest request = new ProfileRequest("-", "newEmail@mail.ru",
                "newPassword", 0);
        ErrorDTO actualResponse = new ErrorDTO(false, BlogConfig.ERROR_NAME_FRONTEND_NAME,
                BlogConfig.ERROR_NAME_FRONTEND_MSG);

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/profile/my")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
    }

    @Test
    public void testEditProfileBadPass() throws Exception {
        userLogin(USER_LOGIN, PASSWORD);
        ProfileRequest request = new ProfileRequest("newName", "newEmail@mail.ru",
                "Pass", 0);
        ErrorDTO actualResponse = new ErrorDTO(false, BlogConfig.ERROR_PASSWORD_FRONTEND_NAME,
                BlogConfig.ERROR_PASSWORD_FRONTEND_MSG);

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/profile/my")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
    }

    @Test
    public void testGetMyStatistics() throws Exception {
        userLogin(USER_LOGIN, PASSWORD);
        StatisticResponse actualResponse = new StatisticResponse(10, 3, 2,
                10, 1615792210L);

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = get("/api/statistics/my");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
    }

    @Test
    public void testGetAllStatistics() throws Exception {
        userLogin(USER_MODERATOR, PASSWORD);
        StatisticResponse actualResponse = new StatisticResponse(11, 3, 3,
                13, 1615792210L);

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = get("/api/statistics/all");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
    }

    private TagResponse createTagResponse(int countTagInResponse) {
        List<TagDTO> tags = new ArrayList<>();
        for (int i = 0; i < countTagInResponse; i++) {
            tags.add(new TagDTO(TAG_NAME[i], TAG_WEIGHT[i]));
        }
        return new TagResponse(tags);
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
