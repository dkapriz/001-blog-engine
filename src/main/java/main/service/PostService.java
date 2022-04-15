package main.service;

import lombok.AllArgsConstructor;
import main.api.dto.ErrorPostDTO;
import main.api.dto.PostDTO;
import main.api.dto.UserPostDTO;
import main.api.request.AddPostRequest;
import main.api.response.AddPostResponse;
import main.api.response.PostResponse;
import main.model.*;
import main.model.repositories.PostRepository;
import main.model.repositories.TagRepository;
import main.model.repositories.TagToPostRepository;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
    private static final int MAX_LENGTH_POST_TEXT = 65000;
    private static final int MAX_ANNOUNCE_LENGTH = 150;
    private static final String ERROR_ADD_POST_RESPONSE_MES_SHORT_TITLE = "Текст заголовка слишком короткий";
    private static final String ERROR_ADD_POST_RESPONSE_MES_SHORT_TEXT = "Текст публикации слишком короткий";
    private static final String ERROR_ADD_POST_RESPONSE_MES_LONG_TITLE = "Текст заголовка слишком длинный";
    private static final String ERROR_ADD_POST_RESPONSE_MES_LONG_TEXT = "Текст публикации слишком длинный";
    private static final String ERROR_ADD_POST_RESPONSE_MES_EMPTY_TITLE = "Заголовок не установлен";
    private static final String ERROR_ADD_POST_RESPONSE_MES_EMPTY_TEXT = "Текст публикации пустой";
    private static final String TIME_ZONE = "UTC";

    @Autowired
    private final PostRepository postRepository;

    @Autowired
    private final TagToPostRepository tagToPostRepository;

    @Autowired
    private final TagRepository tagRepository;

    private final SettingsService settingsService;

    public PostResponse getPosts(int offset, int limit, String mode) {
        Page<Post> postPage;
        int pageOffset = offset / limit;
        switch (mode) {
            case (SORT_POST_TYPE_BY_DATE_PUBLICATION_DES):
                postPage = postRepository.findAll(PageRequest.of(pageOffset, limit, Sort.by("time").descending()));
                break;
            case (SORT_POST_TYPE_BY_DATE_PUBLICATION_ASC):
                postPage = postRepository.findAll(PageRequest.of(pageOffset, limit, Sort.by("time").ascending()));
                break;
            case (SORT_POST_TYPE_BY_COMMENT_DES):
                postPage = postRepository.findAllSortByCountCommentDesc(PageRequest.of(pageOffset, limit));
                break;
            case (SORT_POST_TYPE_BY_LIKE_DES):
                postPage = postRepository.findAllSortByCountLikeDesc(PageRequest.of(pageOffset, limit));
                break;
            default:
                throw new IllegalArgumentException("Invalid value of the 'mode' argument in the 'posts' request");
        }
        List<PostDTO> postDTOS = new ArrayList<>();
        postPage.forEach(post -> postDTOS.add(postToPostDTO(post)));
        return new PostResponse(postPage.getTotalElements(), postDTOS);
    }

    public PostResponse getSearchPosts(int offset, int limit, String query){
        String queryTrim = query.trim();
        if(queryTrim.isEmpty()){
            return getPosts(offset, limit, SORT_POST_TYPE_BY_DATE_PUBLICATION_DES);
        }
        int pageOffset = offset / limit;
        Page<Post> postPage = postRepository.findAllByQuery(PageRequest
                .of(pageOffset, limit, Sort.by("time").descending()), queryTrim);
        List<PostDTO> postDTOS = new ArrayList<>();
        postPage.forEach(post -> postDTOS.add(postToPostDTO(post)));
        return new PostResponse(postPage.getTotalElements(), postDTOS);
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
        List<Tag> savedTagList = getSavedTagList(postRequest.getTags());
        Post post = savePostToDB(postRequest, user, moderationStatusType);
        savedTagList.forEach(tag -> saveTagPostLinkToDB(post.getId(), tag.getId()));
        return new AddPostResponse(true, null);
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

    private Post savePostToDB(AddPostRequest postRequest, User user, ModerationStatusType moderationStatusType) {
        Post post = new Post();
        post.setIsActive(postRequest.getActive());
        post.setModerationStatus(moderationStatusType);
        post.setUser(user);
        post.setTime(checkDateCreationPost(postRequest.getTimeStamp()));
        post.setTitle(postRequest.getTitle());
        post.setText(postRequest.getText());
        post.setViewCount(0);
        post.setTags(getUnsavedTagList(postRequest.getTags()));
        return postRepository.save(post);
    }

    private List<Tag> getUnsavedTagList(String[] tagsName) {
        List<Tag> result = new ArrayList<>();
        for (String tagName : tagsName) {
            if (tagRepository.findByName(tagName).isEmpty()) {
                Tag tag = new Tag();
                tag.setName(tagName);
                result.add(tag);
            }
        }
        return result;
    }

    public List<Tag> getSavedTagList(String[] tagsName) {
        List<Tag> result = new ArrayList<>();
        for (String tagName : tagsName) {
            Optional<Tag> tag = tagRepository.findByName(tagName);
            tag.ifPresent(result::add);
        }
        return result;
    }

    private void saveTagPostLinkToDB(int postID, int tagID) {
        TagToPost tagToPost = new TagToPost();
        tagToPost.setPostID(postID);
        tagToPost.setTagID(tagID);
        tagToPostRepository.save(tagToPost);
    }

    private String removeHTMLTegFromText(String html) {
        return Jsoup.parse(html).text();
    }

    private String getAnnounceFromText(String text) {
        String modifyText = text;
        if (text.length() > MAX_ANNOUNCE_LENGTH * 5) {
            modifyText = modifyText.substring(0, (MAX_ANNOUNCE_LENGTH * 5) - 1);
        }
        modifyText = removeHTMLTegFromText(modifyText);
        int spaceIndex = modifyText.lastIndexOf(' ',
                modifyText.length() < MAX_ANNOUNCE_LENGTH ? (modifyText.length() - 1) : (MAX_ANNOUNCE_LENGTH - 1));
        modifyText = modifyText.substring(0, spaceIndex) + "...";
        return modifyText;
    }
}