package main.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingsResponse {
    @JsonProperty("MULTIUSER_MODE")
    private boolean multiUserMode;

    @JsonProperty("POST_PREMODERATION")
    private boolean postPreModeration;

    @JsonProperty("STATISTICS_IS_PUBLIC")
    private boolean statisticsIsPublic;
}
