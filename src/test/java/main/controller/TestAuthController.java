package main.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.api.request.AddUserRequest;
import main.api.request.LoginRequest;
import main.api.response.ResultResponse;
import main.api.response.UserResultResponse;
import main.config.BlogConfig;
import main.model.CaptchaCode;
import main.model.User;
import main.model.repositories.CaptchaCodeRepository;
import main.model.repositories.UserRepository;
import main.service.UserService;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/insert-data-auth.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/delete-all.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class TestAuthController {
    private static final int TEST_ID_CAPTCHA = 10;
    private static final int TEST_ID_USER = 10;
    private static final String BAD_CAPTCHA = "badCaptcha";
    private static final String TEST_GOOD_EMAIL = "test-email@mail.ru";
    private static final String TEST_REGISTER_EMAIL = "test@mail.ru";
    private static final String TEST_GOOD_PASS = "password";
    private static final String TEST_BAD_PASS = "pass";
    private static final String TEST_GOOD_NAME = "Dmitriy";
    private static final String TEST_BAD_NAME = "d";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private CaptchaCodeRepository captchaCodeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(this.wac).apply(sharedHttpSession()).build();
    }

    @Test
    public void testGenerateCaptcha() throws Exception {
        mockMvc.perform(get("/api/auth/captcha"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.secret", Matchers.notNullValue()))
                .andExpect(jsonPath("$.image", Matchers.notNullValue()));
    }

    @Test
    public void testRegisterUser() throws Exception {
        CaptchaCode captchaCode = getTestCaptchaCode();
        AddUserRequest addUserRequest = new AddUserRequest(TEST_GOOD_EMAIL, TEST_GOOD_PASS,
                TEST_GOOD_NAME, captchaCode.getCode(), captchaCode.getSecretCode());
        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).
                        content(mapper.writeValueAsBytes(addUserRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"));
    }

    @Test
    public void testRegisterUserParameterExceptionEmail() throws Exception {
        registerUserParameterException(TEST_REGISTER_EMAIL, TEST_GOOD_PASS, TEST_GOOD_NAME,
                BlogConfig.ERROR_EMAIL_FRONTEND_NAME, BlogConfig.ERROR_EMAIL_FRONTEND_MSG_REG, false);
    }

    @Test
    public void testRegisterUserParameterExceptionCaptcha() throws Exception {
        registerUserParameterException(TEST_GOOD_EMAIL, TEST_GOOD_PASS, TEST_GOOD_NAME,
                BlogConfig.ERROR_CAPTCHA_FRONTEND_NAME, BlogConfig.ERROR_CAPTCHA_FRONTEND_MSG, true);
    }

    @Test
    public void testRegisterUserParameterExceptionName() throws Exception {
        registerUserParameterException(TEST_GOOD_EMAIL, TEST_GOOD_PASS, TEST_BAD_NAME,
                BlogConfig.ERROR_NAME_FRONTEND_NAME, BlogConfig.ERROR_NAME_FRONTEND_MSG, false);
    }

    @Test
    public void testRegisterUserParameterExceptionPass() throws Exception {
        registerUserParameterException(TEST_GOOD_EMAIL, TEST_BAD_PASS, TEST_GOOD_NAME,
                BlogConfig.ERROR_PASSWORD_FRONTEND_NAME, BlogConfig.ERROR_PASSWORD_FRONTEND_MSG, false);
    }

    @Test
    public void testLogin() throws Exception {
        userLogin(TEST_REGISTER_EMAIL, TEST_GOOD_PASS, TEST_ID_USER);
    }

    @Test
    public void testLogout() throws Exception {
        userLogin(TEST_REGISTER_EMAIL, TEST_GOOD_PASS, TEST_ID_USER);
        ObjectMapper mapper = new ObjectMapper();
        ResultResponse response = new ResultResponse(true);
        mockMvc.perform(get("/api/auth/logout").accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(response)));
    }

    private void registerUserParameterException(String email, String pass, String name, String errorType,
                                                String errorMSG, boolean badCaptcha) throws Exception {
        CaptchaCode captchaCode = getTestCaptchaCode();
        AddUserRequest addUserRequest = new AddUserRequest(email, pass,
                name, badCaptcha ? BAD_CAPTCHA : captchaCode.getCode(), captchaCode.getSecretCode());
        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).
                        content(mapper.writeValueAsBytes(addUserRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("false"))
                .andExpect(jsonPath("$.errors." + errorType).value(errorMSG));
    }

    private CaptchaCode getTestCaptchaCode() {
        Optional<CaptchaCode> captchaCodeOptional = captchaCodeRepository.findById(TEST_ID_CAPTCHA);
        return captchaCodeOptional.orElse(null);
    }

    private User userLogin(String username, String password, int UserID) throws Exception {
        mockMvc = webAppContextSetup(this.wac).apply(sharedHttpSession()).build();
        LoginRequest request = new LoginRequest(username, password);
        User user = userRepository.findById(UserID).orElseThrow(null);
        ObjectMapper mapper = new ObjectMapper();
        UserResultResponse response = new UserResultResponse(true, userService.UserToUserAdvancedDTO(user));
        MvcResult result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isOk()).andReturn();
        Assert.assertArrayEquals(
                "Responses are different",
                mapper.writeValueAsBytes(response),
                result.getResponse().getContentAsByteArray());
        return user;
    }
}
