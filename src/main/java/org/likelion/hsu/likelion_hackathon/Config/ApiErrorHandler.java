package org.likelion.hsu.likelion_hackathon.Config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
public class ApiErrorHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<String> handleWebClient(WebClientResponseException e) {
        // OpenAI에서 내려준 상태/본문 그대로 반환
        return ResponseEntity.status(e.getStatusCode())
                .body("OpenAI error: " + e.getResponseBodyAsString());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleEtc(Exception e) {
        return ResponseEntity.internalServerError()
                .body("Server error: " + e.getMessage());
    }
}