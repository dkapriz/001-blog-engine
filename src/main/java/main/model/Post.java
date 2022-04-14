package main.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Calendar;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "is_active", columnDefinition = "TINYINT", nullable = false)
    private byte isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status",
            columnDefinition = "ENUM(\"NEW\",\"ACCEPTED\",\"DECLINED\")", nullable = false)
    private ModerationStatusType moderationStatus;

    @ManyToOne
    @JoinColumn(name = "moderator_id", columnDefinition = "INT")
    private User moderator;

    @ManyToOne
    @JoinColumn(name = "user_id", columnDefinition = "INT", nullable = false)
    private User user;

    @Column(columnDefinition = "DATETIME", nullable = false)
    private Calendar time;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(name = "view_count", columnDefinition = "INT", nullable = false)
    private int viewCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PostComment> postComments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PostVote> postVotes;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "tag2post",
            joinColumns = {@JoinColumn(name = "post_id")},
            inverseJoinColumns = {@JoinColumn(name = "tag_id")})
    private List<Tag> tags;
}
