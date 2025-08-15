package org.likelion.hsu.likelion_hackathon.Controller;

import org.likelion.hsu.likelion_hackathon.Dto.Request.CopyPolishRequest;
import org.likelion.hsu.likelion_hackathon.Dto.Response.CopyPolishResponse;
import org.likelion.hsu.likelion_hackathon.Service.CopyPolishService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ai")
public class CopyPolishController {

    private final CopyPolishService service;

    public CopyPolishController(CopyPolishService service) {
        this.service = service;
    }

    @PostMapping(value = "/polish", consumes = "application/json", produces = "application/json")
    public ResponseEntity<CopyPolishResponse> polish(@RequestBody CopyPolishRequest req) {
        try {
            return ResponseEntity.ok(service.polish(req));
        } catch (WebClientResponseException e) {
            // OpenAI가 준 상태/메시지를 그대로 전달 → Postman에서 401/429/400 등을 직접 확인 가능
            throw new ResponseStatusException(e.getStatusCode(),
                    "OpenAI error: " + e.getResponseBodyAsString(), e);
        } catch (IllegalStateException e) {
            // API 키 등 환경 설정 문제
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            // 나머지 NPE 등 일반 예외
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Server error: " + e.getMessage(), e);
        }
    }
}