package org.likelion.hsu.likelion_hackathon.Controller;

import lombok.RequiredArgsConstructor;
import org.likelion.hsu.likelion_hackathon.Service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/*
 * [테스트/예비용 업로드 전용 컨트롤러]

 - 현재 서비스 메인 로직은 원샷 방식(`/api/listings/with-upload`)을 사용하여
   업로드 + 매물 등록을 한 번에 처리.
 - 이 컨트롤러는 "이미지 업로드만" 단독으로 테스트하거나,
   매물 등록 전에 URL을 미리 확보해야 할 때 사용할 수 있음.

   해커톤 중에는 주로 원샷 방식을 사용하더라도,
    - 업로드 기능만 점검하고 싶을 때
    - 이미지 변경/교체 UI 구현 시
    - 다른 기능에서 이미지 업로드 재활용 시
   유용하게 사용할 수 있으므로 삭제하지 않고 유지하는 것을 권장.

   원샷 방식만 사용할 경우 이 컨트롤러는 호출되지 않아도 서비스 동작에는 영향이 없습니다.
 */

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService storage;

    /** 단일 이미지 업로드 (테스트용) */
    @PostMapping(path = "/image", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> uploadOne(@RequestPart("file") MultipartFile file) throws IOException {
        String url = storage.saveImage(file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    /** 다중 이미지 업로드 (테스트용) */
    @PostMapping(path = "/images", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> uploadMany(@RequestPart("files") MultipartFile[] files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile f : files) {
            urls.add(storage.saveImage(f));
        }
        return ResponseEntity.ok(Map.of("urls", urls));
    }
}
