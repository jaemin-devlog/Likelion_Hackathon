package org.likelion.hsu.likelion_hackathon.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class OpenAIClientConfig {

    @Bean
    public WebClient openAiWebClient(
            @Value("${openai.api.key:}") String apiKey,
            @Value("${openai.api.url}") String baseUrl
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY가 설정되지 않았습니다. 환경변수 또는 application.properties를 확인하세요."
            );
        }

        // (선택) 네트워크 타임아웃 설정
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(30));

        return WebClient.builder()
                .baseUrl(baseUrl) // ex) https://api.openai.com/v1/chat/completions
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeaders(h -> {
                    h.setBearerAuth(apiKey);
                    h.set(HttpHeaders.CONTENT_TYPE, "application/json");
                })
                .build();
    }
}