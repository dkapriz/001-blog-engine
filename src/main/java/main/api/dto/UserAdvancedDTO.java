package main.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class UserAdvancedDTO extends UserDTO {
    private String email;
    private boolean moderation;
    private int moderationCount;
    private boolean settings;

    public UserAdvancedDTO(int id,
                           String name, String photo,
                           String email, boolean moderation,
                           int moderationCount, boolean settings) {
        super(id, name, photo);
        this.email = email;
        this.moderation = moderation;
        this.moderationCount = moderationCount;
        this.settings = settings;
    }
}
