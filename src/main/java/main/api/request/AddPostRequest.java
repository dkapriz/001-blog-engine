package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddPostRequest {
    @JsonProperty("timestamp")
    private long timeStamp;
    private byte active;
    private String title;
    private String[] tags;
    private String text;
}
