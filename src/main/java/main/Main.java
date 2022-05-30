package main;

import main.config.BlogConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        BlogConfig.LOGGER.info(BlogConfig.MARKER_BLOG_INFO, "------- Start blog engine -------");
    }
}
