package main.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import main.api.response.ResultResponse;

@Setter
@Getter
public class UserResultDTO extends ResultResponse {

    @JsonProperty("user")
    private UserAdvancedDTO userAdvancedDTO;

    public UserResultDTO(boolean result, UserAdvancedDTO userAdvancedDTO) {
        super(result);
        this.userAdvancedDTO = userAdvancedDTO;
    }
}