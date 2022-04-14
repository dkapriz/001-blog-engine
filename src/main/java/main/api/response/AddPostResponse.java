package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import main.api.dto.ErrorPostDTO;

@Getter
@Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddPostResponse {
    private boolean result;

    @JsonProperty("errors")
    private ErrorPostDTO error;

    public AddPostResponse(){
        error = null;
    }
}