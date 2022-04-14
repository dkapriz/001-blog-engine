package main.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Calendar;

@Setter
@Getter
@Entity
@Table(name = "captcha_codes")
public class CaptchaCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "DATETIME", nullable = false)
    private Calendar time;

    @Column(columnDefinition = "TINYTEXT", nullable = false)
    private String code;

    @Column(name = "secret_code", columnDefinition = "TINYTEXT", nullable = false)
    private String secretCode;
}
