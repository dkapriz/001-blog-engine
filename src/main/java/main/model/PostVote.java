package main.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Calendar;

@Setter
@Getter
@Entity
@Table(name = "post_votes")
public class PostVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", columnDefinition = "INT", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id", columnDefinition = "INT", nullable = false)
    private Post post;

    @Column(columnDefinition = "DATETIME", nullable = false)
    private Calendar time;

    @Column(columnDefinition = "TINYINT", nullable = false)
    private byte value;
}