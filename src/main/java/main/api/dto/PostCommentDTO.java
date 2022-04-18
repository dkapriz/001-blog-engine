package main.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostCommentDTO {
    private int id;

    @JsonProperty("timestamp")
    private long timeStamp;

    private String text;
    private UserCommentDTO user;
}
