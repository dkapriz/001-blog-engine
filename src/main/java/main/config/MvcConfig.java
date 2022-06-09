package main.config;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@AllArgsConstructor
public class MvcConfig implements WebMvcConfigurer {
    @Autowired
    private BlogConfig config;

    public MvcConfig() {
        super();
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToEnumConverter());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(config.getImagePath() + "/**")
                .addResourceLocations("file:" + config.getImagePath() + "/");
        registry.addResourceHandler("/**/" + config.getImagePath() + "/")
                .addResourceLocations(config.getImagePath() + "/");
    }
}
