package main.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class UserAuthDTO {
    private int id;
    private String name;
    private String photo;
    private String email;
    private boolean moderation;
    private int moderationCount;
    private boolean settings;
}