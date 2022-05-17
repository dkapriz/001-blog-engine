package main.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostDTO {
    private int id;

    @JsonProperty("timestamp")
    private long timeStamp;

    private UserDTO user;
    private String title;
    private String announce;
    private int likeCount;
    private int dislikeCount;
    private int commentCount;
    private int viewCount;

    public PostDTO(int id, long timeStamp, UserDTO user, String title, int likeCount, int dislikeCount) {
        this.id = id;
        this.timeStamp = timeStamp;
        this.user = user;
        this.title = title;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
    }
}
