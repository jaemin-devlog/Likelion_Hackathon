package org.likelion.hsu.likelion_hackathon.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.url-prefix:/images}")
    private String urlPrefix;

    // 허용 MIME 타입 (HEIC/HEIF 추가)
    private static final Set<String> ALLOWED_MIME = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif",
            "image/heic", "image/heif"
    );

    // 허용 확장자 (HEIC/HEIF 추가)
    private static final Set<String> ALLOWED_EXT = Set.of(
            "jpg", "jpeg", "png", "webp", "gif", "heic", "heif"
    );

    public String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }

        final String contentType = file.getContentType();            // 예: image/jpeg, application/octet-stream
        final String ext = getExtension(file.getOriginalFilename()); // 예: jpg, heic ...

        // MIME 또는 확장자 둘 중 하나라도 허용되면 통과
        final boolean mimeOk = contentType != null &&
                (contentType.startsWith("image/") || ALLOWED_MIME.contains(contentType));
        final boolean extOk = ext != null && ALLOWED_EXT.contains(ext);

        if (!(mimeOk || extOk)) {
            throw new IllegalArgumentException(
                    "Unsupported file type: contentType=" + contentType + ", ext=" + ext);
        }

        // 월 단위 디렉터리 생성 (예: 2025-08)
        String yyyymm = LocalDate.now().toString().substring(0, 7);
        Path dir = Path.of(uploadDir, yyyymm)
                .toAbsolutePath() // 절대경로로 변환
                .normalize();
        Files.createDirectories(dir);

        // 안전한 파일명
        String safeName = UUID.randomUUID().toString().replace("-", "");
        if (ext != null && !ext.isBlank()) safeName += "." + ext;

        Path target = dir.resolve(safeName)
                .toAbsolutePath() // 절대경로로 변환
                .normalize();

        // 디렉토리 탈출 방지
        if (!target.startsWith(dir)) {
            throw new SecurityException("Invalid path");
        }

        file.transferTo(target.toFile());

        // 공개 URL 반환
        return urlPrefix + "/" + yyyymm + "/" + safeName;
    }

    private String getExtension(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        if (idx < 0 || idx == name.length() - 1) return "";
        return name.substring(idx + 1).toLowerCase();
    }
}
