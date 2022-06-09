package main.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.api.dto.ErrorDTO;
import main.api.request.AddUserRequest;
import main.api.request.LoginRequest;
import main.api.request.PassRecoveryRequest;
import main.api.request.PassRestoreRequest;
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
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
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
    private static final String BAD_CAPTCHA = "badCaptcha";
    private static final String TEST_CAPTCHA = "testCaptcha";
    private static final String TEST_CAPTCHA_SECRET = "vfbPFX9eVuUIVdJR";
    private static final String TEST_GOOD_EMAIL = "test-email@mail.ru";
    private static final String TEST_REGISTER_EMAIL = "test@mail.ru";
    private static final String TEST_BAD_EMAIL = "bad_test@mail.ru";
    private static final String TEST_GOOD_PASS = "password";
    private static final String TEST_BAD_PASS = "pass";
    private static final String TEST_GOOD_NAME = "Dmitriy";
    private static final String TEST_BAD_NAME = "d";

    private static final String NEW_PASS = "new_password";
    private static final String HASH = "594f7a47-038a-49d8-8868-3baa7c7be4e2";
    private static final String BAD_HASH = "594f7a47-038a-55d8-8868-3baa7c7be4e2";

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
    @Autowired
    private BlogConfig config;

    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(this.wac).apply(sharedHttpSession()).build();
    }

    @Test
    public void testGenerateCaptcha() throws Exception {
        RequestBuilder requestBuilder = get("/api/auth/captcha");

        mockMvc.perform(requestBuilder)
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
        RequestBuilder requestBuilder = post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).
                content(mapper.writeValueAsBytes(addUserRequest));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"));
    }

    @Test
    public void testRegisterUserParameterExceptionEmail() throws Exception {
        CaptchaCode captchaCode = getTestCaptchaCode();
        AddUserRequest addUserRequest = new AddUserRequest(TEST_REGISTER_EMAIL, TEST_GOOD_PASS,
                TEST_GOOD_NAME, captchaCode.getCode(), captchaCode.getSecretCode());

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).
                content(mapper.writeValueAsBytes(addUserRequest));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("false"))
                .andExpect(jsonPath("$.errors." + BlogConfig.ERROR_EMAIL_FRONTEND_NAME)
                        .value(BlogConfig.ERROR_EMAIL_FRONTEND_MSG_REG));
    }

    @Test
    public void testRegisterUserParameterExceptionCaptcha() throws Exception {
        CaptchaCode captchaCode = getTestCaptchaCode();
        AddUserRequest addUserRequest = new AddUserRequest(TEST_GOOD_EMAIL, TEST_GOOD_PASS,
                TEST_GOOD_NAME, BAD_CAPTCHA, captchaCode.getSecretCode());

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).
                content(mapper.writeValueAsBytes(addUserRequest));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("false"))
                .andExpect(jsonPath("$.errors." + BlogConfig.ERROR_CAPTCHA_FRONTEND_NAME)
                        .value(BlogConfig.ERROR_CAPTCHA_FRONTEND_MSG));
    }

    @Test
    public void testRegisterUserParameterExceptionName() throws Exception {
        CaptchaCode captchaCode = getTestCaptchaCode();
        AddUserRequest addUserRequest = new AddUserRequest(TEST_GOOD_EMAIL, TEST_GOOD_PASS,
                TEST_BAD_NAME, captchaCode.getCode(), captchaCode.getSecretCode());

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).
                content(mapper.writeValueAsBytes(addUserRequest));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("false"))
                .andExpect(jsonPath("$.errors." + BlogConfig.ERROR_NAME_FRONTEND_NAME)
                        .value(BlogConfig.ERROR_NAME_FRONTEND_MSG));
    }

    @Test
    public void testRegisterUserParameterExceptionPass() throws Exception {
        CaptchaCode captchaCode = getTestCaptchaCode();
        AddUserRequest addUserRequest = new AddUserRequest(TEST_GOOD_EMAIL, TEST_BAD_PASS,
                TEST_GOOD_NAME, captchaCode.getCode(), captchaCode.getSecretCode());

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).
                content(mapper.writeValueAsBytes(addUserRequest));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("false"))
                .andExpect(jsonPath("$.errors." + BlogConfig.ERROR_PASSWORD_FRONTEND_NAME)
                        .value(BlogConfig.ERROR_PASSWORD_FRONTEND_MSG));
    }

    @Test
    public void testLogin() throws Exception {
        LoginRequest request = new LoginRequest(TEST_REGISTER_EMAIL, TEST_GOOD_PASS);
        User user = userRepository.findByEmailIgnoreCase(TEST_REGISTER_EMAIL).orElseThrow(null);
        UserResultResponse actualResponse = new UserResultResponse(true, userService.UserToUserAdvancedDTO(user));

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request));

        MvcResult result = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk()).andReturn();
        Assert.assertArrayEquals(
                "Responses are different",
                mapper.writeValueAsBytes(actualResponse),
                result.getResponse().getContentAsByteArray());
    }

    @Test
    public void testLogout() throws Exception {
        userLogin(TEST_REGISTER_EMAIL, TEST_GOOD_PASS);
        ResultResponse actualResponse = new ResultResponse(true);

        RequestBuilder requestBuilder = get("/api/auth/logout").accept(MediaType.ALL);

        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(actualResponse)));
    }

    @Test
    public void testRestorePassword() throws Exception {
        PassRestoreRequest request = new PassRestoreRequest(TEST_REGISTER_EMAIL);
        ResultResponse actualResponse = new ResultResponse(true);

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/auth/restore")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request));

        MvcResult result = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        Assert.assertArrayEquals("Responses are different", mapper.writeValueAsBytes(actualResponse),
                result.getResponse().getContentAsByteArray());
    }

    @Test
    public void testRestorePasswordBadEmail() throws Exception {
        PassRestoreRequest request = new PassRestoreRequest(TEST_BAD_EMAIL);
        ResultResponse actualResponse = new ResultResponse(false);

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/auth/restore")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request));

        MvcResult result = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        Assert.assertArrayEquals("Responses are different", mapper.writeValueAsBytes(actualResponse),
                result.getResponse().getContentAsByteArray());
    }

    @Test
    public void testChangePassword() throws Exception {
        PassRecoveryRequest request = new PassRecoveryRequest(HASH, NEW_PASS, TEST_CAPTCHA, TEST_CAPTCHA_SECRET);
        ResultResponse actualResponse = new ResultResponse(true);

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/auth/password")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content()
                        .json(mapper.writeValueAsString(actualResponse)));
        userLogin(TEST_REGISTER_EMAIL, NEW_PASS);
    }

    @Test
    public void testChangePasswordBadCode() throws Exception {
        PassRecoveryRequest request = new PassRecoveryRequest(BAD_HASH, NEW_PASS, TEST_CAPTCHA, TEST_CAPTCHA_SECRET);
        ErrorDTO actualResponse = new ErrorDTO(false, BlogConfig.ERROR_CODE_FRONTEND_NAME,
                BlogConfig.ERROR_LINK_IS_OUTDATED_BEFORE + config.getMailRestorePasswordSubAddress() +
                        BlogConfig.ERROR_LINK_IS_OUTDATED_AFTER);

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/auth/password")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content()
                        .json(mapper.writeValueAsString(actualResponse)));
    }

    @Test
    public void testChangePasswordBadCaptcha() throws Exception {
        PassRecoveryRequest request = new PassRecoveryRequest(HASH, NEW_PASS, BAD_CAPTCHA, TEST_CAPTCHA_SECRET);
        ErrorDTO actualResponse = new ErrorDTO(false, BlogConfig.ERROR_CAPTCHA_FRONTEND_NAME,
                BlogConfig.ERROR_CAPTCHA_FRONTEND_MSG);

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/auth/password")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content()
                        .json(mapper.writeValueAsString(actualResponse)));
    }

    private CaptchaCode getTestCaptchaCode() {
        Optional<CaptchaCode> captchaCodeOptional = captchaCodeRepository.findById(TEST_ID_CAPTCHA);
        return captchaCodeOptional.orElse(null);
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
