package main.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.api.dto.TagDTO;
import main.api.response.CalendarResponse;
import main.api.response.SettingsResponse;
import main.api.response.TagResponse;
import main.service.SettingsService;
import org.hamcrest.Matchers;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private static final int CALENDAR_EXPECTED_POST_COUNT = 2;
    private static final String CALENDAR_TESTED_YEAR = "2022";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SettingsService settingsService;

    @Test
    public void testInit() throws Exception {
        mockMvc.perform(get("/api/init"))
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
        ObjectMapper mapper = new ObjectMapper();
        SettingsResponse response = settingsService.getGlobalSettings();
        mockMvc.perform(get("/api/settings")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(response)));
    }

    @Test
    public void testGetTagsWithQuery() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(get("/api/tag").param("query", TAG_TESTED_NAME)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(createTagResponse(2))));
    }

    @Test
    public void testGetTagsEmptyQuery() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(get("/api/tag").param("query", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(createTagResponse(5))));
    }

    @Test
    public void testGetTagsWithoutQuery() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(get("/api/tag")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(createTagResponse(5))));
    }

    @Test
    public void testGetTagsWithQueryBadTag() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(
                        get("/api/tag").param("query", "badTag")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(createTagResponse(0))));
    }

    @Test
    public void testCalendarSetYear() throws Exception {
        testCalendar(CALENDAR_TESTED_YEAR);
    }

    @Test
    public void testCalendarEmptyYear() throws Exception {
        testCalendar("");
    }

    private TagResponse createTagResponse(int countTagInResponse) {
        List<TagDTO> tags = new ArrayList<>();
        for (int i = 0; i < countTagInResponse; i++) {
            tags.add(new TagDTO(TAG_NAME[i], TAG_WEIGHT[i]));
        }
        return new TagResponse(tags);
    }

    private void testCalendar(String year) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Integer> posts = new HashMap<>();
        posts.put(CALENDAR_EXPECTED_POST_DATE, CALENDAR_EXPECTED_POST_COUNT);
        CalendarResponse response = new CalendarResponse(CALENDAR_YEARS_LIST, posts);
        mockMvc.perform(get("/api/calendar").param("year", year)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(response)));
    }
}
