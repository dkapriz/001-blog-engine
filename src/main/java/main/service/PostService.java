package main.service;

import lombok.AllArgsConstructor;
import main.api.dto.CommentPostDTO;
import main.api.dto.PostDTO;
import main.api.dto.UserDTO;
import main.api.request.AddPostRequest;
import main.api.response.CalendarResponse;
import main.api.response.PostListResponse;
import main.api.response.PostResponse;
import main.api.response.ResultResponse;
import main.config.BlogConfig;
import main.exception.IllegalParameterException;
import main.exception.PageNotFoundException;
import main.model.*;
import main.model.enums.ModerationStatusType;
import main.model.repositories.PostRepository;
import main.model.repositories.TagRepository;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@AllArgsConstructor
public class PostService {

    private static final String MSG_EXCEPTION_INVALID_PARAM_MODE_VALUE = "Invalid value of the 'mode' " +
            "argument in the '/post' request";
    private static final String MSG_EXCEPTION_INVALID_PARAM_STATUS_VALUE = "Invalid value of the 'status' " +
            "argument in the '/moderation' request";

    @Autowired
    private final PostRepository postRepository;
    @Autowired
    private final TagRepository tagRepository;
    @Autowired
    private final UserService userService;
    @Autowired
    private final BlogConfig config;
    @Autowired
    private final SettingsService settingsService;

    public PostListResponse getPosts(int offset, int limit, String mode) {
        int pageOffset = offset / limit;
        switch (mode) {
            case (BlogConfig.POST_SORT_PARAMETER_NAME_BY_DATE_PUBLICATION_DES):
                return getPostResponse(postRepository
                        .findAll(PageRequest.of(pageOffset, limit, Sort.by("time").descending())));
            case (BlogConfig.POST_SORT_PARAMETER_NAME_BY_DATE_PUBLICATION_ASC):
                return getPostResponse(postRepository
                        .findAll(PageRequest.of(pageOffset, limit, Sort.by("time").ascending())));
            case (BlogConfig.POST_SORT_PARAMETER_NAME_BY_COMMENT_DES):
                return getPostResponse(postRepository.findAllSortByCountCommentDesc(PageRequest.of(pageOffset, limit)));
            case (BlogConfig.POST_SORT_PARAMETER_NAME_BY_LIKE_DES):
                return getPostResponse(postRepository.findAllSortByCountLikeDesc(PageRequest.of(pageOffset, limit)));
            default:
                throw new IllegalParameterException(MSG_EXCEPTION_INVALID_PARAM_MODE_VALUE);
        }
    }

    public PostListResponse getSearchPosts(int offset, int limit, String query) {
        String queryTrim = query.trim();
        if (queryTrim.isEmpty()) {
            return getPosts(offset, limit, BlogConfig.POST_SORT_PARAMETER_NAME_BY_DATE_PUBLICATION_DES);
        }
        int pageOffset = offset / limit;
        return getPostResponse(postRepository.findAllByQuery(PageRequest
                .of(pageOffset, limit, Sort.by("time").descending()), queryTrim));
    }

    public PostListResponse getPostsByDate(int offset, int limit, String date) throws ParseException {
        String dateTrim = date.trim();
        if (dateTrim.isEmpty()) {
            return new PostListResponse(0, new ArrayList<>());
        }
        SimpleDateFormat formatDate = new SimpleDateFormat(config.getTimeDateFormat());
        Date searchDate = formatDate.parse(dateTrim);

        int pageOffset = offset / limit;
        return getPostResponse(postRepository.findAllByDate(PageRequest
                .of(pageOffset, limit, Sort.by("time").descending()), searchDate));
    }

    public PostListResponse getPostsByTag(int offset, int limit, String tag) {
        String tagTrim = tag.trim();
        if (tagTrim.isEmpty()) {
            return new PostListResponse(0, new ArrayList<>());
        }

        int pageOffset = offset / limit;
        return getPostResponse(postRepository.findAllByTag(PageRequest
                .of(pageOffset, limit, Sort.by("time").descending()), tagTrim));
    }

    public PostListResponse getPostsModeration(int offset, int limit, ModerationStatusType status) {
        User user = userService.getLoggedUser();
        int pageOffset = offset / limit;
        if (status == ModerationStatusType.NEW) {
            return getPostResponse(postRepository.findAllByModerationStatus(PageRequest
                    .of(pageOffset, limit, Sort.by("time").descending()), status));
        }
        if (status == ModerationStatusType.DECLINED || status == ModerationStatusType.ACCEPTED) {
            return getPostResponse(postRepository.findAllByModerationStatusAndModerationID(PageRequest
                    .of(pageOffset, limit, Sort.by("time").descending()), status, user.getId()));
        }
        throw new IllegalParameterException(MSG_EXCEPTION_INVALID_PARAM_STATUS_VALUE);
    }

    public PostListResponse getMyPosts(int offset, int limit, ModerationStatusType status) {
        User user = userService.getLoggedUser();
        int pageOffset = offset / limit;
        Pageable pageable = PageRequest.of(pageOffset, limit, Sort.by("time").descending());
        switch (status) {
            case INACTIVE:
                return getPostResponse(postRepository.findAllIsNotActiveByUserID(
                        pageable, user.getId()));
            case PENDING:
                return getPostResponse(postRepository.findAllByModerationStatusAndUserID(
                        pageable, ModerationStatusType.NEW, user.getId()));
            case DECLINED:
                return getPostResponse(postRepository.findAllByModerationStatusAndUserID(
                        pageable, ModerationStatusType.DECLINED, user.getId()));
            case PUBLISHED:
                return getPostResponse(postRepository.findAllByModerationStatusAndUserID(
                        pageable, ModerationStatusType.ACCEPTED, user.getId()));
            default:
                throw new IllegalParameterException(MSG_EXCEPTION_INVALID_PARAM_STATUS_VALUE);
        }
    }

    public PostResponse getPostByID(int id, Principal principal) {
        Post post = postRepository.findPostByIDIsActiveAndAccepted(id);
        if (post == null) {
            throw new PageNotFoundException("Page with id " + id + " not found");
        }

        PostResponse postResponse = new PostResponse();
        long unixTime = post.getTime().getTimeInMillis() / 1000;

        postResponse.setId(post.getId());
        postResponse.setTimeStamp(unixTime);
        postResponse.setActive(byteToBool(post.getIsActive()));
        postResponse.setUser(new UserDTO(post.getUser().getId(), post.getUser().getName()));
        postResponse.setTitle(post.getTitle());
        postResponse.setText(post.getText());
        postResponse.setLikeCount((int) post.getPostVotes()
                .stream().filter(postVote -> postVote.getValue() == BlogConfig.POST_LIKE).count());
        postResponse.setDislikeCount((int) post.getPostVotes()
                .stream().filter(postVote -> postVote.getValue() == BlogConfig.POST_DISLIKE).count());
        postResponse.setViewCount(incrementNumberViewPost(post, principal));
        postResponse.setComments(getPostComments(post).toArray(CommentPostDTO[]::new));
        postResponse.setTags(post.getTags().stream().map(Tag::getName).toArray(String[]::new));
        return postResponse;
    }

    public ResultResponse addPost(AddPostRequest postRequest) {
        if (postRequest.getTitle().isEmpty()) {
            throw new IllegalParameterException(BlogConfig.ERROR_TITLE_FRONTEND_NAME,
                    BlogConfig.ERROR_EMPTY_TITLE_POST_FRONTEND_MSG);
        }
        if (postRequest.getText().isEmpty()) {
            throw new IllegalParameterException(BlogConfig.ERROR_TEXT_FRONTEND_NAME,
                    BlogConfig.ERROR_EMPTY_TEXT_POST_FRONTEND_MSG);
        }
        if (postRequest.getTitle().length() < config.getPostMinLengthTitle()) {
            throw new IllegalParameterException(BlogConfig.ERROR_TITLE_FRONTEND_NAME,
                    BlogConfig.ERROR_SHORT_TITLE_POST_FRONTEND_MSG);
        }
        if (postRequest.getText().length() < config.getPostMinLengthText()) {
            throw new IllegalParameterException(BlogConfig.ERROR_TEXT_FRONTEND_NAME,
                    BlogConfig.ERROR_SHORT_TEXT_POST_FRONTEND_MSG);
        }
        if (postRequest.getTitle().length() > config.getPostMaxLengthTitle()) {
            throw new IllegalParameterException(BlogConfig.ERROR_TITLE_FRONTEND_NAME,
                    BlogConfig.ERROR_LONG_TITLE_POST_FRONTEND_MSG);
        }
        if (postRequest.getText().length() > config.getPostMaxLengthText()) {
            throw new IllegalParameterException(BlogConfig.ERROR_TEXT_FRONTEND_NAME,
                    BlogConfig.ERROR_LONG_TEXT_POST_FRONTEND_MSG);
        }

        User user = userService.getLoggedUser();

        ModerationStatusType moderationStatusType = ModerationStatusType.ACCEPTED;
        if (settingsService.getGlobalSettingByCode(BlogConfig.POST_PRE_MODERATION_FIELD_NAME)) {
            moderationStatusType = ModerationStatusType.NEW;
        }

        savePostToDB(postRequest, user, moderationStatusType);
        BlogConfig.LOGGER.info(BlogConfig.MARKER_BLOG_INFO,"Добавлен пост  - " + postRequest.getText());
        return new ResultResponse(true);
    }

    public CalendarResponse getCalendar(String year) {
        Calendar searchYear = Calendar.getInstance(TimeZone.getTimeZone(config.getTimeZone()));
        if (!year.trim().isEmpty()) {
            searchYear.set(Calendar.YEAR, Integer.parseInt(year));
        }
        String[] allYearsPost = postRepository.findAllYearValue();
        List<Post> postBySearchYear = postRepository.findAllByYear(searchYear.getTime());

        SimpleDateFormat formatDate = new SimpleDateFormat(config.getTimeDateFormat());
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

    private List<CommentPostDTO> getPostComments(Post post) {
        List<CommentPostDTO> result = new ArrayList<>();
        List<PostComment> postCommentList = post.getPostComments();
        for (PostComment postComment : postCommentList) {
            CommentPostDTO postCommentDTO = new CommentPostDTO();
            long unixTime = postComment.getTime().getTimeInMillis() / 1000;

            postCommentDTO.setId(postComment.getId());
            postCommentDTO.setText(postComment.getText());
            postCommentDTO.setUser(new UserDTO(postComment.getUser().getId(),
                    postComment.getUser().getName(), postComment.getUser().getPhoto()));
            postCommentDTO.setTimeStamp(unixTime);
            result.add(postCommentDTO);
        }
        return result;
    }

    private int incrementNumberViewPost(Post post, Principal principal) {
        int viewCount = post.getViewCount();
        if (principal != null) {
            User user = userService.getLoggedUser();
            if (userService.isModerator(user) && userService.isAuthor(user, post)) {
                return viewCount;
            }
        }
        post.setViewCount(++viewCount);
        postRepository.save(post);
        return viewCount;
    }

    private PostListResponse getPostResponse(Page<Post> postPage) {
        List<PostDTO> postDTOS = new ArrayList<>();
        postPage.forEach(post -> postDTOS.add(postToPostDTO(post)));
        return new PostListResponse(postPage.getTotalElements(), postDTOS);
    }

    public PostDTO postToPostDTO(Post post) {
        PostDTO postDTO = new PostDTO();
        long unixTime = post.getTime().getTimeInMillis() / 1000;

        postDTO.setId(post.getId());
        postDTO.setTimeStamp(unixTime);
        postDTO.setUser(new UserDTO(post.getUser().getId(), post.getUser().getName()));
        postDTO.setTitle(post.getTitle());
        postDTO.setAnnounce(getAnnounceFromText(post.getText()));
        postDTO.setLikeCount((int) post.getPostVotes()
                .stream().filter(postVote -> postVote.getValue() == BlogConfig.POST_LIKE).count());
        postDTO.setDislikeCount((int) post.getPostVotes()
                .stream().filter(postVote -> postVote.getValue() == BlogConfig.POST_DISLIKE).count());
        postDTO.setCommentCount(post.getPostComments().size());
        postDTO.setViewCount(post.getViewCount());
        return postDTO;
    }

    private Calendar checkDateCreationPost(long time) {
        Calendar postCreationDate = Calendar.getInstance(TimeZone.getTimeZone(config.getTimeZone()));
        Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone(config.getTimeZone()));
        postCreationDate.setTimeInMillis(time);
        if (postCreationDate.before(currentDate)) {
            return currentDate;
        }
        return postCreationDate;
    }

    private void savePostToDB(AddPostRequest postRequest, User user, ModerationStatusType moderationStatusType) {
        Post post = new Post();
        post.setIsActive(postRequest.getActive());
        post.setModerationStatus(moderationStatusType);
        post.setUser(user);
        post.setTime(checkDateCreationPost(postRequest.getTimeStamp()));
        post.setTitle(postRequest.getTitle());
        post.setText(postRequest.getText());
        post.setViewCount(0);
        addTagsToPost(post, postRequest.getTags());
        postRepository.save(post);
    }

    private void addTagsToPost(Post post, String[] strTags) {
        for (String strTag : strTags) {
            String strLowerCaseTag = strTag.toLowerCase();
            Optional<Tag> tagOptional = tagRepository.findByName(strLowerCaseTag);
            Tag tag = tagOptional.orElseGet(() -> new Tag(strLowerCaseTag));
            post.addTag(tag);
        }
    }

    private String removeHTMLTegFromText(String html) {
        return Jsoup.parse(html).text();
    }

    private String getAnnounceFromText(String text) {
        String modifyText = text;
        if (text.length() > config.getPostMaxLengthAnnounce() * 100) {
            modifyText = modifyText.substring(0, (config.getPostMaxLengthAnnounce() * 100) - 1);
        }
        modifyText = removeHTMLTegFromText(modifyText);
        int spaceIndex = modifyText.lastIndexOf(' ',
                modifyText.length() < config.getPostMaxLengthAnnounce() ?
                        (modifyText.length() - 1) : (config.getPostMaxLengthAnnounce() - 1));
        modifyText = modifyText.substring(0, spaceIndex) + "...";
        return modifyText;
    }

    private boolean byteToBool(byte value) {
        return value == 1;
    }
}