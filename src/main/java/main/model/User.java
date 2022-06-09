package main.model;

import lombok.Getter;
import lombok.Setter;
import main.model.enums.Role;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "is_moderator", columnDefinition = "TINYINT", nullable = false)
    private byte isModerator;

    @Column(name = "reg_time", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime regTime;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String name;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String email;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String password;

    @Column(columnDefinition = "VARCHAR(255)")
    private String code;

    @Column(columnDefinition = "TEXT")
    private String photo;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PostComment> postComment;

    public Role getRole() {
        return isModerator == 1 ? Role.MODERATOR : Role.USER;
    }
}
