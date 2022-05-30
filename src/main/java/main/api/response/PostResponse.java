package main.api.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main.api.dto.CommentPostDTO;
import main.api.dto.PostDTO;
import main.api.dto.UserDTO;

@Getter
@Setter
@NoArgsConstructor
public class PostResponse extends PostDTO {
    private boolean active;
    private String text;
    private CommentPostDTO[] comments;
    private String[] tags;

    public PostResponse(int id, long timeStamp, boolean active, UserDTO user, String title, String text,
                        int likeCount, int dislikeCount, int viewCount, CommentPostDTO[] comments, String[] tags) {
        super(id, timeStamp, user, title, likeCount, dislikeCount, viewCount);
        this.active = active;
        this.text = text;
        this.comments = comments;
        this.tags = tags;
    }
}