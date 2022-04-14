package main.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Calendar;

@Setter
@Getter
@Entity
@Table(name = "post_comments")
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "parent_id", columnDefinition = "INT")
    private PostComment parent;

    @ManyToOne
    @JoinColumn(name = "post_id", columnDefinition = "INT", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id", columnDefinition = "INT", nullable = false)
    private User user;

    @Column(columnDefinition = "DATETIME", nullable = false)
    private Calendar time;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;
}
