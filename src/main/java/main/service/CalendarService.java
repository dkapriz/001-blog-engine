package main.service;

import lombok.AllArgsConstructor;
import main.api.response.CalendarResponse;
import main.model.Post;
import main.model.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@AllArgsConstructor
public class CalendarService {

    @Autowired
    private final PostRepository postRepository;

    private static final String TIME_ZONE = "UTC";
    private static final String PATTERN_DATE_FORMAT = "yyyy-MM-dd";

    public CalendarResponse getCalendar(String year) {
        Calendar searchYear = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
        if (!year.trim().isEmpty()) {
            searchYear.set(Calendar.YEAR, Integer.parseInt(year));
        }
        String[] allYearsPost = postRepository.findAllYearValue();
        List<Post> postBySearchYear = postRepository.findAllByYear(searchYear.getTime());

        SimpleDateFormat formatDate = new SimpleDateFormat(PATTERN_DATE_FORMAT);
        Map<String, Integer> dateCount = new HashMap<>();
        for (Post post : postBySearchYear) {
            String postDate = formatDate.format(post.getTime().getTime());
            if (dateCount.containsKey(postDate)) {
                dateCount.put(postDate, dateCount.get(postDate) + 1);
                continue;
            }
            dateCount.put(postDate, 1);
        }
        return new CalendarResponse(allYearsPost, dateCount);
    }
}