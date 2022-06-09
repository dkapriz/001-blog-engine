package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultResponse {
    protected Boolean result;
    @JsonProperty("message")
    protected String errorMessage;

    public ResultResponse(boolean result) {
        this.result = result;
        errorMessage = null;
    }

    public ResultResponse(String errorMessage) {
        this.errorMessage = errorMessage;
        result = null;
    }
}
