package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ModeratePostRequest {
    @JsonProperty("post_id")
    private final int postId;
    private final String decision;
}
