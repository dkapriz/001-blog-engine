package main.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.api.request.AddUserRequest;
import main.configuratoin.BlogConfig;
import main.model.CaptchaCode;
import main.model.repositories.CaptchaCodeRepository;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/insert-data-auth.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/delete-all.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class TestAuthController {
    private static final int TEST_ID_CAPTCHA = 10;
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
    private CaptchaCodeRepository captchaCodeRepository;

    @AfterEach
    public void resetDb() {
        captchaCodeRepository.deleteAll();
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
}
