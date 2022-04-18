package main.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main.api.dto.PostCommentDTO;
import main.api.dto.UserPostDTO;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SinglePostResponse {
    private int id;

    @JsonProperty("timestamp")
    private long timeStamp;

    private boolean active;
    private UserPostDTO user;
    private String title;
    private String text;
    private int likeCount;
    private int dislikeCount;
    private PostCommentDTO[] comments;
    private String[] tags;
}