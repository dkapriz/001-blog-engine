package main.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import main.api.dto.TagDTO;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TagResponse {
    private List<TagDTO> tags;
}
