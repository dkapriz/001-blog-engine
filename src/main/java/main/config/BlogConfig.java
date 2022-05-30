package main.config;

import lombok.Data;
import main.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "config")
@Data
public class BlogConfig {
    public static final byte B_CRYPT_STRENGTH = 12;

    public static final byte USER_MODERATOR = 1;
    public static final byte POST_LIKE = 1;
    public static final byte POST_DISLIKE = -1;

    public static final String POST_SORT_PARAMETER_NAME_BY_DATE_PUBLICATION_DES = "recent";
    public static final String POST_SORT_PARAMETER_NAME_BY_COMMENT_DES = "popular";
    public static final String POST_SORT_PARAMETER_NAME_BY_LIKE_DES = "best";
    public static final String POST_SORT_PARAMETER_NAME_BY_DATE_PUBLICATION_ASC = "early";

    public static final int LENGTH_CAPTCHA_SECRET_CODE = 22;
    public static final String MULTI_USER_MODE_FIELD_NAME = "MULTIUSER_MODE";
    public static final String POST_PRE_MODERATION_FIELD_NAME = "POST_PRE_MODERATION";
    public static final String STATISTIC_IS_PUBLIC_FIELD_NAME = "STATISTICS_IS_PUBLIC";
    public static final String TRUE_VALUE = "YES";
    public static final String FALSE_VALUE = "NO";

    public static final String ERROR_CAPTCHA_FRONTEND_NAME = "captcha";
    public static final String ERROR_EMAIL_FRONTEND_NAME = "email";
    public static final String ERROR_NAME_FRONTEND_NAME = "name";
    public static final String ERROR_PASSWORD_FRONTEND_NAME = "password";
    public static final String ERROR_TITLE_FRONTEND_NAME = "title";
    public static final String ERROR_TEXT_FRONTEND_NAME = "text";
    public static final String ERROR_CAPTCHA_FRONTEND_MSG = "Код с картинки введён неверно";
    public static final String ERROR_EMAIL_FRONTEND_MSG_REG = "Этот e-mail уже зарегистрирован";
    public static final String ERROR_EMAIL_FRONTEND_MSG_FORMAT = "E-mail указан неверно";
    public static final String ERROR_NAME_FRONTEND_MSG = "Имя указано неверно";
    public static final String ERROR_PASSWORD_FRONTEND_MSG = "Пароль короче 6-ти символов";
    public static final String ERROR_SHORT_TITLE_POST_FRONTEND_MSG = "Текст заголовка слишком короткий";
    public static final String ERROR_SHORT_TEXT_POST_FRONTEND_MSG = "Текст публикации слишком короткий";
    public static final String ERROR_LONG_TITLE_POST_FRONTEND_MSG = "Текст заголовка слишком длинный";
    public static final String ERROR_LONG_TEXT_POST_FRONTEND_MSG = "Текст публикации слишком длинный";
    public static final String ERROR_EMPTY_TITLE_POST_FRONTEND_MSG = "Заголовок не установлен";
    public static final String ERROR_EMPTY_TEXT_POST_FRONTEND_MSG = "Текст публикации пустой";

    public static final Logger LOGGER = LogManager.getLogger(Main.class);
    public static final Marker MARKER_BLOG_INFO = MarkerManager.getMarker("BLOG_INFO");
    public static final Marker MARKER_UNSUCCESSFUL_REQUEST = MarkerManager.getMarker("UNSUCCESSFUL_REQUEST");

    private String timeZone;
    private String timeDateFormat;

    private int userMinLengthPassword;

    private long captchaTimeLive;
    private int captchaHeight;
    private int captchaWight;
    private String captchaFormat;
    private String captchaURL;

    private int postMinLengthTitle;
    private int postMinLengthText;
    private int postMaxLengthTitle;
    private int postMaxLengthText;
    private int postMaxLengthAnnounce;
}