package main.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserCommentDTO {
    private int id;
    private String name;
    private String photo;
}