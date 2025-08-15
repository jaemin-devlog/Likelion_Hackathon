package org.likelion.hsu.likelion_hackathon.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.likelion.hsu.likelion_hackathon.Dto.Request.CopyPolishRequest;
import org.likelion.hsu.likelion_hackathon.Dto.Response.CopyPolishResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Service
public class CopyPolishService {

    private final WebClient openAiWebClient;
    private final String model;

    public CopyPolishService(WebClient openAiWebClient,
                             @Value("${openai.model}") String model) {
        this.openAiWebClient = openAiWebClient;
        this.model = model;
    }

    public CopyPolishResponse polish(CopyPolishRequest req) {
        String tone = (req.getTone() == null || req.getTone().isBlank())
                ? "따뜻하고 담백한 톤" : req.getTone();

        String typeHints = "STAY".equalsIgnoreCase(req.getType())
                ? "- 기간/인원/위치 장점 강조\n- 주변 편의시설 간단 언급\n"
                : "- 가성비/입지/채광·소음 등 장점 선명하게\n- 과장 없이 사실 기반 홍보\n";

        String system = """
                너는 한국어 카피라이터다. 맞춤법과 문장을 자연스럽게 다듬고,
                과장 없이 장점을 요약해 2~4문장 한 단락으로만 출력하라.
                이모지/해시태그/불필요한 문구는 넣지 말고, 한국어로만 답하라.
                """;

        String user = """
                [타입]
                %s

                [가이드]
                %s

                [톤]
                %s

                [원문]
                %s
                """.formatted(req.getType(), typeHints, tone, req.getRawText());

        ChatRequest payload = new ChatRequest(
                model,
                List.of(new Message("system", system), new Message("user", user)),
                0.4, 300
        );

        ChatResponse res = openAiWebClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                // 4xx/5xx 시 예외 던지되 응답 바디를 살려서 전달
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                        r -> r.bodyToMono(String.class).flatMap(body ->
                                r.createException().map(ex -> new WebClientResponseException(
                                        ex.getMessage(),
                                        r.statusCode().value(),
                                        r.statusCode().toString(),
                                        null, body.getBytes(), null
                                ))
                        )
                )
                .bodyToMono(ChatResponse.class)
                // 429 Too Many Requests만 백오프 재시도(3회)
                .retryWhen(
                        Retry.backoff(3, Duration.ofMillis(400))
                                .filter(ex -> ex instanceof WebClientResponseException w
                                        && w.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS)
                )
                .block();

        String content = (res != null && res.choices != null && !res.choices.isEmpty()
                && res.choices.get(0).message != null && res.choices.get(0).message.content != null)
                ? res.choices.get(0).message.content.trim()
                : "(응답 없음)";

        return new CopyPolishResponse(content);
    }

    /* ===== OpenAI Chat API DTOs ===== */
    public record ChatRequest(
            String model,
            List<Message> messages,
            @JsonProperty("temperature") double temperature,
            @JsonProperty("max_tokens") int maxTokens
    ) {}

    public record Message(String role, String content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatResponse {
        public List<Choice> choices;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Choice {
            public Msg message;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Msg {
            public String role;
            public String content;
        }
    }
}