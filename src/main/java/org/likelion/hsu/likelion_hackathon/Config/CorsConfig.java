package org.likelion.hsu.likelion_hackathon.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//프론트나 다른 도메인에서 오는 요청을 허용할 지 확인
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해 CORS를 허용하겠다는 뜻
                .allowedOrigins("http://localhost:3000") // 프론트 주소
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") //프론트에서 보낼 수 있는 HTTP매서드를 허용
                .allowedHeaders("*") //요청에 어떤 헤더를 포함해도 허용
                .allowCredentials(false); // 로그인/쿠키 미사용 → false
    }
}
