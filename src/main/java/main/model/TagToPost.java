package main.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "tag2post")
public class TagToPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "post_id", columnDefinition = "INT", nullable = false)
    private int postID;

    @Column(name = "tag_id", columnDefinition = "INT", nullable = false)
    private int tagID;
}