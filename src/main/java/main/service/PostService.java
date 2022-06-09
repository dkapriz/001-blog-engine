package main.service;

import lombok.AllArgsConstructor;
import main.api.dto.CommentPostDTO;
import main.api.dto.PostDTO;
import main.api.dto.UserDTO;
import main.api.request.AddPostRequest;
import main.api.request.CommentRequest;
import main.api.request.ModeratePostRequest;
import main.api.response.*;
import main.config.BlogConfig;
import main.exception.DataNotFoundException;
import main.exception.IllegalParameterException;
import main.exception.ResultIllegalParameterException;
import main.model.Post;
import main.model.PostComment;
import main.model.Tag;
import main.model.User;
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

import javax.naming.AuthenticationException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @Autowired
    private final CommentService commentService;
    @Autowired
    private final TimeService timeService;

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
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Пост с id: " + id + " не найден"));
        if (post == null) {
            throw new DataNotFoundException("Запрашиваемый пост с id: " + id + " не найден");
        }

        PostResponse postResponse = new PostResponse();
        postResponse.setId(post.getId());
        postResponse.setTimeStamp(timeService.getTimestampFromLocalDateTime(post.getTime()));
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
        checkPostTitleAndText(postRequest.getTitle(), postRequest.getText());
        User user = userService.getLoggedUser();

        ModerationStatusType moderationStatusType = ModerationStatusType.ACCEPTED;
        if (settingsService.getGlobalSettingByCode(BlogConfig.POST_PRE_MODERATION_FIELD_NAME)) {
            moderationStatusType = ModerationStatusType.NEW;
        }

        savePostToDB(postRequest, user, moderationStatusType);
        BlogConfig.LOGGER.info(BlogConfig.MARKER_BLOG_INFO, "Добавлен пост  - " + postRequest.getText());
        return new ResultResponse(true);
    }

    public ResultResponse changePost(int id, AddPostRequest postRequest) throws AuthenticationException {
        Post post = postRepository.findById(id).orElseThrow(() ->
                new DataNotFoundException("Запрашиваемый пост с id: " + id + " не найден"));
        User user = userService.getLoggedUser();
        checkPostTitleAndText(postRequest.getTitle(), postRequest.getText());
        if (userService.isModerator(user)) {
            post.setModeratorID(user.getId());
        } else {
            if (post.getUser().getId() != user.getId()) {
                throw new AuthenticationException("Пользовватель с id " + user.getId() +
                        " не является создателем поста с id " + post.getId());
            }
            post.setModerationStatus(ModerationStatusType.NEW);
        }
        post.setIsActive(postRequest.getActive());
        post.setTime(timeService.checkDateCreationPost(postRequest.getTimeStamp()));
        post.setTitle(postRequest.getTitle());
        post.setText(postRequest.getText());
        addTagsToPost(post, postRequest.getTags());
        postRepository.save(post);
        BlogConfig.LOGGER.info(BlogConfig.MARKER_BLOG_INFO, "Пост с id " + post.getId() +
                " изменен пользователем с id " + user.getId());
        return new ResultResponse(true);
    }

    public CalendarResponse getCalendar(String year) {
        int requestYear = year.isEmpty() ? LocalDateTime.now().getYear() : Integer.parseInt(year);
        String[] allYearsPost = postRepository.findAllYearValue();
        List<Post> postBySearchYear = postRepository.findAllByYear(requestYear);

        DateTimeFormatter formatDate = DateTimeFormatter.ofPattern(config.getTimeDateFormat());
        Map<String, Integer> dateCount = new HashMap<>();
        for (Post post : postBySearchYear) {
            String postDate = post.getTime().format(formatDate);
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
            postCommentDTO.setId(postComment.getId());
            postCommentDTO.setText(postComment.getText());
            postCommentDTO.setUser(new UserDTO(postComment.getUser().getId(),
                    postComment.getUser().getName(), postComment.getUser().getPhoto()));
            postCommentDTO.setTimeStamp(timeService.getTimestampFromLocalDateTime(postComment.getTime()));
            result.add(postCommentDTO);
        }
        return result;
    }

    private int incrementNumberViewPost(Post post, Principal principal) {
        int viewCount = post.getViewCount();
        if (principal != null) {
            User user = userService.getLoggedUser();
            if (userService.isModerator(user) || userService.isAuthor(user, post)) {
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
        postDTO.setId(post.getId());
        postDTO.setTimeStamp(timeService.getTimestampFromLocalDateTime(post.getTime()));
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

    public IDResponse addComment(CommentRequest commentRequest) {
        checkPostComment(commentRequest.getText());
        Post post = postRepository.findPostByIDIsActiveAndAccepted(commentRequest.getPostId())
                .orElseThrow(() -> new IllegalParameterException("ID поста указан не верно"));
        User user = userService.getLoggedUser();
        PostComment currentComment = new PostComment();
        PostComment parentComment;
        if (commentRequest.getParentId() != 0) {
            parentComment = commentService.getCommentByID(commentRequest.getParentId());
            currentComment.setParent(parentComment);
        }
        currentComment.setPost(post);
        currentComment.setUser(user);
        currentComment.setText(commentRequest.getText());
        currentComment.setTime(LocalDateTime.now());
        commentService.saveComment(currentComment);
        return new IDResponse(currentComment.getId());
    }

    public ResultResponse moderate(ModeratePostRequest moderatePostRequest) {
        User user = userService.getLoggedUser();
        Post post = postRepository.findById(moderatePostRequest.getPostId())
                .orElseThrow(() -> new IllegalParameterException("ID поста указан не верно"));
        post.setModeratorID(user.getId());
        post.setModerationStatus(getModerationStatus(moderatePostRequest.getDecision()));
        postRepository.save(post);
        if (post.getModerationStatus().equals(ModerationStatusType.ACCEPTED)) {
            BlogConfig.LOGGER.info(BlogConfig.MARKER_BLOG_INFO, "Пост с id " + post.getId() +
                    " принят модератором с id " + user.getId());
        }
        if (post.getModerationStatus().equals(ModerationStatusType.DECLINED)) {
            BlogConfig.LOGGER.info(BlogConfig.MARKER_BLOG_INFO, "Пост с id " + post.getId() +
                    " отклонен модератором с id " + user.getId());
        }
        return new ResultResponse(true);
    }

    public int countPostsByUser(User user) {
        return postRepository.countAllActiveAndAcceptedByUser(user).orElse(0);
    }

    public int countAllPosts() {
        return postRepository.countAllActiveAndAccepted().orElse(0);
    }

    public int countViewPostsByUser(User user) {
        return postRepository.countViewsAllPostsByUser(user).orElse(0);
    }

    public int countViewAllPosts() {
        return postRepository.countViewsAllPosts().orElse(0);
    }

    public LocalDateTime getTimeFirstPostByUser(User user) {
        return postRepository.getTimeFirstPostByUser(user.getId());
    }

    public LocalDateTime getTimeFirstPost() {
        return postRepository.getTimeFirstPost();
    }

    public Post getActiveAndAcceptedById(int postID) {
        return postRepository.findPostByIDIsActiveAndAccepted(postID).orElseThrow(
                () -> new IllegalParameterException("ID поста указан не верно"));
    }

    private void savePostToDB(AddPostRequest postRequest, User user, ModerationStatusType moderationStatusType) {
        Post post = new Post();
        post.setIsActive(postRequest.getActive());
        post.setModerationStatus(moderationStatusType);
        post.setUser(user);
        post.setTime(timeService.checkDateCreationPost(postRequest.getTimeStamp()));
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

    private void checkPostTitleAndText(String title, String text) {
        if (title.isEmpty()) {
            throw new ResultIllegalParameterException(BlogConfig.ERROR_TITLE_FRONTEND_NAME,
                    BlogConfig.ERROR_EMPTY_TITLE_POST_FRONTEND_MSG);
        }
        if (text.isEmpty()) {
            throw new ResultIllegalParameterException(BlogConfig.ERROR_TEXT_FRONTEND_NAME,
                    BlogConfig.ERROR_EMPTY_TEXT_POST_FRONTEND_MSG);
        }
        if (title.length() < config.getPostMinLengthTitle()) {
            throw new ResultIllegalParameterException(BlogConfig.ERROR_TITLE_FRONTEND_NAME,
                    BlogConfig.ERROR_SHORT_TITLE_POST_FRONTEND_MSG);
        }
        if (removeHTMLTegFromText(text).length() < config.getPostMinLengthText()) {
            throw new ResultIllegalParameterException(BlogConfig.ERROR_TEXT_FRONTEND_NAME,
                    BlogConfig.ERROR_SHORT_TEXT_POST_FRONTEND_MSG);
        }
        if (title.length() > config.getPostMaxLengthTitle()) {
            throw new ResultIllegalParameterException(BlogConfig.ERROR_TITLE_FRONTEND_NAME,
                    BlogConfig.ERROR_LONG_TITLE_POST_FRONTEND_MSG);
        }
        if (text.length() > config.getPostMaxLengthText()) {
            throw new ResultIllegalParameterException(BlogConfig.ERROR_TEXT_FRONTEND_NAME,
                    BlogConfig.ERROR_LONG_TEXT_POST_FRONTEND_MSG);
        }
    }

    private void checkPostComment(String text) {
        if (text.isEmpty()) {
            throw new ResultIllegalParameterException(BlogConfig.ERROR_TEXT_FRONTEND_NAME,
                    BlogConfig.ERROR_EMPTY_TEXT_POST_COMMENT_FRONTEND_MSG);
        }
        if (removeHTMLTegFromText(text).length() < config.getPostCommentMinLength()) {
            throw new ResultIllegalParameterException(BlogConfig.ERROR_TEXT_FRONTEND_NAME,
                    BlogConfig.ERROR_SHORT_TEXT_POST_COMMENT_FRONTEND_MSG);
        }
        if (text.length() > config.getPostCommentMaxLength()) {
            throw new ResultIllegalParameterException(BlogConfig.ERROR_TEXT_FRONTEND_NAME,
                    BlogConfig.ERROR_LONG_TEXT_POST_COMMENT_FRONTEND_MSG);
        }
    }

    private ModerationStatusType getModerationStatus(String status) {
        switch (status) {
            case BlogConfig.POST_MODERATION_STATUS_ACCEPT:
                return ModerationStatusType.ACCEPTED;
            case BlogConfig.POST_MODERATION_STATUS_DECLINE:
                return ModerationStatusType.DECLINED;
        }
        throw new ResultIllegalParameterException(BlogConfig.ERROR_MODERATION_DECISION_MSG);
    }
}