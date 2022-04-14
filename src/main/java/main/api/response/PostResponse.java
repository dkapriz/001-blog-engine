package main.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import main.api.dto.PostDTO;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PostResponse {
    private long count;
    private List<PostDTO> posts;
}