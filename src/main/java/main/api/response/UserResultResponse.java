package main.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import main.api.dto.UserAdvancedDTO;

@Setter
@Getter
public class UserResultResponse extends ResultResponse {

    @JsonProperty("user")
    private UserAdvancedDTO userAdvancedDTO;

    public UserResultResponse(boolean result, UserAdvancedDTO userAdvancedDTO) {
        super(result);
        this.userAdvancedDTO = userAdvancedDTO;
    }
}