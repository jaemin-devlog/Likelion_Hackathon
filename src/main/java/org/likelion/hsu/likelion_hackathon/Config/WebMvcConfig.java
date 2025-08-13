package org.likelion.hsu.likelion_hackathon.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.url-prefix:/images}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /images/**  ->  file:/var/www/uploads/
        String location = Path.of(uploadDir).toUri().toString();
        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations(location);
    }
}
