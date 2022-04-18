package main.service;

import lombok.AllArgsConstructor;
import main.api.dto.*;
import main.api.request.AddPostRequest;
import main.api.response.AddPostResponse;
import main.api.response.PostResponse;
import main.api.response.SinglePostResponse;
import main.model.*;
import main.model.repositories.PostRepository;
import main.model.repositories.TagRepository;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@AllArgsConstructor
public class PostService {

    private static final String SORT_POST_TYPE_BY_DATE_PUBLICATION_DES = "recent";
    private static final String SORT_POST_TYPE_BY_COMMENT_DES = "popular";
    private static final String SORT_POST_TYPE_BY_LIKE_DES = "best";
    private static final String SORT_POST_TYPE_BY_DATE_PUBLICATION_ASC = "early";
    private static final byte POST_LIKE = 1;
    private static final byte POST_DISLIKE = -1;
    private static final int MIN_LENGTH_POST_TITLE = 3;
    private static final int MIN_LENGTH_POST_TEXT = 50;
    private static final int MAX_LENGTH_POST_TITLE = 500;
    private static final int MAX_LENGTH_POST_TEXT = 600000;
    private static final int MAX_ANNOUNCE_LENGTH = 150;
    private static final String ERROR_ADD_POST_RESPONSE_MES_SHORT_TITLE = "Текст заголовка слишком короткий";
    private static final String ERROR_ADD_POST_RESPONSE_MES_SHORT_TEXT = "Текст публикации слишком короткий";
    private static final String ERROR_ADD_POST_RESPONSE_MES_LONG_TITLE = "Текст заголовка слишком длинный";
    private static final String ERROR_ADD_POST_RESPONSE_MES_LONG_TEXT = "Текст публикации слишком длинный";
    private static final String ERROR_ADD_POST_RESPONSE_MES_EMPTY_TITLE = "Заголовок не установлен";
    private static final String ERROR_ADD_POST_RESPONSE_MES_EMPTY_TEXT = "Текст публикации пустой";
    private static final String TIME_ZONE = "UTC";
    private static final String PATTERN_DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
    private final PostRepository postRepository;

    @Autowired
    private final TagRepository tagRepository;

    private final SettingsService settingsService;

    public PostResponse getPosts(int offset, int limit, String mode) {
        int pageOffset = offset / limit;
        switch (mode) {
            case (SORT_POST_TYPE_BY_DATE_PUBLICATION_DES):
                return getPostResponse(postRepository
                        .findAll(PageRequest.of(pageOffset, limit, Sort.by("time").descending())));
            case (SORT_POST_TYPE_BY_DATE_PUBLICATION_ASC):
                return getPostResponse(postRepository
                        .findAll(PageRequest.of(pageOffset, limit, Sort.by("time").ascending())));
            case (SORT_POST_TYPE_BY_COMMENT_DES):
                return getPostResponse(postRepository.findAllSortByCountCommentDesc(PageRequest.of(pageOffset, limit)));
            case (SORT_POST_TYPE_BY_LIKE_DES):
                return getPostResponse(postRepository.findAllSortByCountLikeDesc(PageRequest.of(pageOffset, limit)));
            default:
                throw new IllegalArgumentException("Invalid value of the 'mode' argument in the 'posts' request");
        }
    }

    public PostResponse getSearchPosts(int offset, int limit, String query) {
        String queryTrim = query.trim();
        if (queryTrim.isEmpty()) {
            return getPosts(offset, limit, SORT_POST_TYPE_BY_DATE_PUBLICATION_DES);
        }
        int pageOffset = offset / limit;
        return getPostResponse(postRepository.findAllByQuery(PageRequest
                .of(pageOffset, limit, Sort.by("time").descending()), queryTrim));
    }

    public PostResponse getPostsByDate(int offset, int limit, String date) throws ParseException {
        String dateTrim = date.trim();
        if (dateTrim.isEmpty()) {
            return new PostResponse(0, new ArrayList<>());
        }
        SimpleDateFormat formatDate = new SimpleDateFormat(PATTERN_DATE_FORMAT);
        Date searchDate = formatDate.parse(dateTrim);

        int pageOffset = offset / limit;
        return getPostResponse(postRepository.findAllByDate(PageRequest
                .of(pageOffset, limit, Sort.by("time").descending()), searchDate));
    }

    public PostResponse getPostsByTag(int offset, int limit, String tag) {
        String tagTrim = tag.trim();
        if (tagTrim.isEmpty()) {
            return new PostResponse(0, new ArrayList<>());
        }

        int pageOffset = offset / limit;
        return getPostResponse(postRepository.findAllByTag(PageRequest
                .of(pageOffset, limit, Sort.by("time").descending()), tagTrim));
    }

    public SinglePostResponse getPostByID(int id) {
        Post post = postRepository.findPostByID(id);
        if (post == null) {
            return null;
        }
        incrementNumberViewPost(post);

        SinglePostResponse singlePost = new SinglePostResponse();
        long unixTime = post.getTime().getTimeInMillis() / 1000;

        singlePost.setId(post.getId());
        singlePost.setTimeStamp(unixTime);
        singlePost.setActive(byteToBool(post.getIsActive()));
        singlePost.setUser(new UserPostDTO(post.getUser().getId(), post.getUser().getName()));
        singlePost.setTitle(post.getTitle());
        singlePost.setText(post.getText());
        singlePost.setLikeCount((int) post.getPostVotes()
                .stream().filter(postVote -> postVote.getValue() == POST_LIKE).count());
        singlePost.setDislikeCount((int) post.getPostVotes()
                .stream().filter(postVote -> postVote.getValue() == POST_DISLIKE).count());
        singlePost.setComments(getPostComments(post).toArray(PostCommentDTO[]::new));
        singlePost.setTags(post.getTags().stream().map(Tag::getName).toArray(String[]::new));
        return singlePost;
    }


    public AddPostResponse addPost(AddPostRequest postRequest) {

        // TODO: добавить проверку авторизации

        if (postRequest.getTitle().isEmpty()) {
            return new AddPostResponse(false, new ErrorPostDTO(ERROR_ADD_POST_RESPONSE_MES_EMPTY_TITLE, ""));
        }
        if (postRequest.getText().isEmpty()) {
            return new AddPostResponse(false, new ErrorPostDTO("", ERROR_ADD_POST_RESPONSE_MES_EMPTY_TEXT));
        }
        if (postRequest.getTitle().length() < MIN_LENGTH_POST_TITLE) {
            return new AddPostResponse(false, new ErrorPostDTO(ERROR_ADD_POST_RESPONSE_MES_SHORT_TITLE, ""));
        }
        if (postRequest.getText().length() < MIN_LENGTH_POST_TEXT) {
            return new AddPostResponse(false, new ErrorPostDTO("", ERROR_ADD_POST_RESPONSE_MES_SHORT_TEXT));
        }
        if (postRequest.getTitle().length() > MAX_LENGTH_POST_TITLE) {
            return new AddPostResponse(false, new ErrorPostDTO(ERROR_ADD_POST_RESPONSE_MES_LONG_TITLE, ""));
        }
        if (postRequest.getText().length() > MAX_LENGTH_POST_TEXT) {
            return new AddPostResponse(false, new ErrorPostDTO("", ERROR_ADD_POST_RESPONSE_MES_LONG_TEXT));
        }

        // TODO: получение пользователя временно стоит заглушка
        // TODO: добавить заполнение поля moderation_id
        User user = new User();
        user.setId(1);

        ModerationStatusType moderationStatusType = ModerationStatusType.ACCEPTED;
        if (settingsService.getGlobalSettingByCode(SettingsService.POST_PRE_MODERATION_FIELD_NAME)) {
            moderationStatusType = ModerationStatusType.NEW;
        }

        savePostToDB(postRequest, user, moderationStatusType);
        return new AddPostResponse(true, null);
    }

    private List<PostCommentDTO> getPostComments(Post post){
        List<PostCommentDTO> result = new ArrayList<>();
        List<PostComment> postCommentList = post.getPostComments();
        for (PostComment postComment : postCommentList){
            PostCommentDTO postCommentDTO = new PostCommentDTO();
            long unixTime = postComment.getTime().getTimeInMillis() / 1000;

            postCommentDTO.setId(postComment.getId());
            postCommentDTO.setText(postComment.getText());
            postCommentDTO.setUser(new UserCommentDTO(postComment.getUser().getId(),
                    postComment.getUser().getName(), postComment.getUser().getPhoto()));
            postCommentDTO.setTimeStamp(unixTime);
            result.add(postCommentDTO);
        }
        return result;
    }

    private void incrementNumberViewPost(Post post) {
        //TODO: добавить проверку модератор авторизирован или автор авторизирован
        boolean isAuth = false; //заглушка

        if (!isAuth) {
            post.setViewCount(post.getViewCount() + 1);
        }
    }

    private PostResponse getPostResponse(Page<Post> postPage) {
        List<PostDTO> postDTOS = new ArrayList<>();
        postPage.forEach(post -> postDTOS.add(postToPostDTO(post)));
        return new PostResponse(postPage.getTotalElements(), postDTOS);
    }

    private PostDTO postToPostDTO(Post post) {
        PostDTO postDTO = new PostDTO();
        long unixTime = post.getTime().getTimeInMillis() / 1000;

        postDTO.setId(post.getId());
        postDTO.setTimeStamp(unixTime);
        postDTO.setUser(new UserPostDTO(post.getUser().getId(), post.getUser().getName()));
        postDTO.setTitle(post.getTitle());
        postDTO.setAnnounce(getAnnounceFromText(post.getText()));
        postDTO.setLikeCount((int) post.getPostVotes()
                .stream().filter(postVote -> postVote.getValue() == POST_LIKE).count());
        postDTO.setDislikeCount((int) post.getPostVotes()
                .stream().filter(postVote -> postVote.getValue() == POST_DISLIKE).count());
        postDTO.setCommentCount(post.getPostComments().size());
        postDTO.setViewCount(post.getViewCount());
        return postDTO;
    }

    private Calendar checkDateCreationPost(long time) {
        Calendar postCreationDate = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
        Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
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
        if (text.length() > MAX_ANNOUNCE_LENGTH * 100) {
            modifyText = modifyText.substring(0, (MAX_ANNOUNCE_LENGTH * 100) - 1);
        }
        modifyText = removeHTMLTegFromText(modifyText);
        int spaceIndex = modifyText.lastIndexOf(' ',
                modifyText.length() < MAX_ANNOUNCE_LENGTH ? (modifyText.length() - 1) : (MAX_ANNOUNCE_LENGTH - 1));
        modifyText = modifyText.substring(0, spaceIndex) + "...";
        return modifyText;
    }

    private boolean byteToBool(byte value) {
        return value == 1;
    }
}