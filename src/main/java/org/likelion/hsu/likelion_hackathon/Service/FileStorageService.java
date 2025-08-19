package org.likelion.hsu.likelion_hackathon.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir; // 예: /var/www/uploads

    @Value("${app.upload.url-prefix:/images}")
    private String urlPrefix; // 예: /images

    // 허용 MIME 타입 (HEIC/HEIF 추가)
    private static final Set<String> ALLOWED_MIME = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif",
            "image/heic", "image/heif"
    );

    // 허용 확장자 (HEIC/HEIF 추가)
    private static final Set<String> ALLOWED_EXT = Set.of(
            "jpg", "jpeg", "png", "webp", "gif", "heic", "heif"
    );

    /**
     * 단일 이미지 저장 → 공개 URL 반환 (예: /images/2025-08/xxxx.jpg)
     */
    public String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }

        final String contentType = file.getContentType();            // 예: image/jpeg, application/octet-stream
        final String ext = getExtension(file.getOriginalFilename()); // 예: jpg, heic ...

        // MIME 또는 확장자 둘 중 하나라도 허용되면 통과
        final boolean mimeOk = contentType != null &&
                (contentType.startsWith("image/") || ALLOWED_MIME.contains(contentType));
        final boolean extOk = ext != null && !ext.isBlank() && ALLOWED_EXT.contains(ext);

        if (!(mimeOk || extOk)) {
            throw new IllegalArgumentException(
                    "Unsupported file type: contentType=" + contentType + ", ext=" + ext);
        }

        // 월 단위 디렉터리 생성 (예: 2025-08)
        final String yyyymm = LocalDate.now().toString().substring(0, 7);
        final Path dir = normalizeLocal(Path.of(uploadDir, yyyymm));
        Files.createDirectories(dir);

        // 안전한 파일명
        String safeName = UUID.randomUUID().toString().replace("-", "");
        if (ext != null && !ext.isBlank()) safeName += "." + ext.toLowerCase(Locale.ROOT);

        final Path target = normalizeLocal(dir.resolve(safeName));
        // 디렉토리 탈출 방지
        if (!target.startsWith(dir)) {
            throw new SecurityException("Invalid path");
        }

        file.transferTo(target.toFile());

        // 공개 URL 반환 (예: /images/2025-08/xxxxx.jpg)
        return joinUrlPath(normalizeUrlPrefix(urlPrefix), yyyymm + "/" + safeName);
    }

    /**
     * 여러 장 저장 → 공개 URL 리스트 반환
     */
    public List<String> saveImages(Collection<MultipartFile> files) throws IOException {
        if (files == null) return List.of();
        List<String> urls = new ArrayList<>();
        for (MultipartFile f : files) {
            if (f != null && !f.isEmpty()) {
                urls.add(saveImage(f));
            }
        }
        return urls;
    }

    /**
     * 공개 URL(예: /images/2025-08/xxx.jpg, 혹은 http://.../images/2025-08/xxx.jpg) → 로컬 경로로 매핑
     * urlPrefix 하위가 아니면 Optional.empty()
     */
    public Optional<Path> toLocalPathFromUrl(String url) {
        if (url == null || url.isBlank()) return Optional.empty();

        String normalizedPrefix = normalizeUrlPrefix(urlPrefix); // "/images"
        String pathPart = url;

        // 절대 URL인 경우 path만 뽑는다.
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                pathPart = URI.create(url).getPath();
            }
        } catch (Exception ignored) {}

        if (pathPart == null || pathPart.isBlank()) return Optional.empty();

        // 항상 슬래시로 시작하게
        if (!pathPart.startsWith("/")) pathPart = "/" + pathPart;

        // prefix 미스매치면 외부 URL로 간주
        if (!pathPart.startsWith(normalizedPrefix + "/")) return Optional.empty();

        // /images/ 이후의 상대 경로
        String sub = pathPart.substring((normalizedPrefix + "/").length());

        // uploads 루트와 합치고 안전하게 정규화
        Path local = normalizeLocal(Path.of(uploadDir).resolve(sub));
        // 업로드 루트 밖이면 차단
        Path base = normalizeLocal(Path.of(uploadDir));
        if (!local.startsWith(base)) return Optional.empty();

        return Optional.of(local);
    }

    /**
     * 공개 URL → 실제 파일 삭제 (성공/실패 boolean 반환)
     * urlPrefix 하위가 아니거나 파일이 없으면 false
     */
    public boolean deleteByUrl(String url) {
        try {
            Optional<Path> p = toLocalPathFromUrl(url);
            if (p.isEmpty()) return false;
            return Files.deleteIfExists(p.get());
        } catch (IOException ignored) {
            return false;
        }
    }

    /**
     * 공개 URL들 일괄 삭제(존재하는 것만)
     */
    public void deleteAllByUrls(Collection<String> urls) {
        if (urls == null) return;
        for (String u : urls) {
            deleteByUrl(u);
        }
    }

    /**
     * 공개 URL이 실제로 존재하는지
     */
    public boolean existsByUrl(String url) {
        return toLocalPathFromUrl(url)
                .map(Files::exists)
                .orElse(false);
    }

    /**
     * (선택) 로컬 경로 → 공개 URL로 역변환
     * uploadDir 하위일 때만 URL 생성
     */
    public Optional<String> toPublicUrl(Path localFile) {
        if (localFile == null) return Optional.empty();
        Path base = normalizeLocal(Path.of(uploadDir));
        Path norm = normalizeLocal(localFile);
        if (!norm.startsWith(base)) return Optional.empty();

        String rel = base.relativize(norm).toString().replace('\\', '/'); // OS 구분자 → URL 구분자
        return Optional.of(joinUrlPath(normalizeUrlPrefix(urlPrefix), rel));
    }

    /* ========== 내부 유틸 ========== */

    private String getExtension(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        if (idx < 0 || idx == name.length() - 1) return "";
        return name.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    /** 업로드 루트 기준으로 경로 정규화 */
    private Path normalizeLocal(Path p) {
        return p.toAbsolutePath().normalize();
    }

    /** urlPrefix를 항상 "/something" 형태로 정규화 */
    private String normalizeUrlPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) return "/images";
        String s = prefix.trim();
        if (!s.startsWith("/")) s = "/" + s;
        // 뒤에 슬래시는 제거 ("/images/")
        while (s.length() > 1 && s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    /** URL 경로 조인 (여분 슬래시 제거) */
    private String joinUrlPath(String base, String tail) {
        String b = base == null ? "" : base.trim();
        String t = tail == null ? "" : tail.trim();
        if (!b.startsWith("/")) b = "/" + b;
        while (b.endsWith("/") && b.length() > 1) b = b.substring(0, b.length() - 1);
        if (t.startsWith("/")) t = t.substring(1);
        return (b + "/" + t).replaceAll("/{2,}", "/");
    }
}
