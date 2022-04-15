package main.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class CalendarResponse {
    private String[] years;
    private Map<String, Integer> posts;
}
