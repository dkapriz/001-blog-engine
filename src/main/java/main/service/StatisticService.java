package main.service;

import lombok.AllArgsConstructor;
import main.api.response.StatisticResponse;
import main.exception.DataNotFoundException;
import main.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@AllArgsConstructor
public class StatisticService {

    @Autowired
    private final UserService userService;
    @Autowired
    private final PostService postService;
    @Autowired
    private final VoteService voteService;
    @Autowired
    private final SettingsService settingsService;

    public StatisticResponse getMyStatistic() {
        User user = userService.getLoggedUser();
        LocalDateTime date = postService.getTimeFirstPostByUser(user);
        ZonedDateTime zonedDate = date.atZone(ZoneId.systemDefault());

        StatisticResponse statisticResponse = new StatisticResponse();
        statisticResponse.setPostsCount(postService.countPostsByUser(user));
        statisticResponse.setViewsCount(postService.countViewPostsByUser(user));
        statisticResponse.setLikesCount(voteService.countAllLikesByUser(user));
        statisticResponse.setDislikesCount(voteService.countAllDislikesByUser(user));
        statisticResponse.setFirstPublication(zonedDate.toEpochSecond());
        return statisticResponse;
    }

    public StatisticResponse getAllStatistic() {
        if (!settingsService.isAllStatistic() && !userService.isModerator(userService.getLoggedUser())) {
            throw new DataNotFoundException("Статистика просмотра всего блога недоступна");
        }
        LocalDateTime date = postService.getTimeFirstPost();
        ZonedDateTime zonedDate = date.atZone(ZoneId.systemDefault());

        StatisticResponse statisticResponse = new StatisticResponse();
        statisticResponse.setPostsCount(postService.countAllPosts());
        statisticResponse.setViewsCount(postService.countViewAllPosts());
        statisticResponse.setLikesCount(voteService.countAllLikes());
        statisticResponse.setDislikesCount(voteService.countAllDislikes());
        statisticResponse.setFirstPublication(zonedDate.toEpochSecond());
        return statisticResponse;
    }
}
