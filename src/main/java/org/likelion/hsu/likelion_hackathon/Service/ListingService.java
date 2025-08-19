package org.likelion.hsu.likelion_hackathon.Service;

import org.likelion.hsu.likelion_hackathon.Dto.Request.ListingCreateRequest;
import org.likelion.hsu.likelion_hackathon.Dto.Request.ListingUpdateRequest;
import org.likelion.hsu.likelion_hackathon.Dto.Response.*;
import org.likelion.hsu.likelion_hackathon.Entity.*;
import org.likelion.hsu.likelion_hackathon.Enum.ListingType;
import org.likelion.hsu.likelion_hackathon.Repository.ListingPhotoRepository;
import org.likelion.hsu.likelion_hackathon.Repository.ListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ListingService {

    private final ListingRepository listingRepository;
    private final ListingPhotoRepository photoRepository;
    private final FileStorageService fileStorageService;

    public ListingService(ListingRepository listingRepository,
                          ListingPhotoRepository photoRepository,
                          FileStorageService fileStorageService) {
        this.listingRepository = listingRepository;
        this.photoRepository = photoRepository;
        this.fileStorageService = fileStorageService;
    }

    /* 원샷 등록: 파일 저장 → photos URL 주입 → 기존 create 재사용 (실패시 업로드 파일 정리) */
    public ListingResponse createWithUpload(ListingCreateRequest req,
                                            List<MultipartFile> files) throws IOException {
        List<String> uploaded = new ArrayList<>();
        try {
            if (files != null) {
                for (MultipartFile f : files) {
                    if (f != null && !f.isEmpty()) {
                        uploaded.add(fileStorageService.saveImage(f)); // /images/... URL 생성
                    }
                }
            }
            req.setPhotos(uploaded);
            // DB 작업까지 시도
            return create(req);
        } catch (Exception e) {
            // DB에 저장 실패 등 예외 시, 지금까지 올린 파일은 정리
            safeDeleteFiles(uploaded);
            throw e;
        }
    }

    /* 매물 등록 */
    public ListingResponse create(ListingCreateRequest req) {
        Listing listing = new Listing();
        listing.setType(req.getType());

        // Embedded 값 세팅 (널 허용)
        ListingDetails details = new ListingDetails();
        details.setBuildingName(req.getBuildingName());
        details.setDescription(req.getDescription());
        details.setAddress(req.getAddress());
        listing.setDetails(details);

        ListingPeriod period = new ListingPeriod();
        period.setStartDate(req.getStartDate());
        period.setEndDate(req.getEndDate());
        period.setGuests(req.getGuests());
        listing.setPeriod(period);

        ListingPricing pricing = new ListingPricing();
        pricing.setPrice(req.getPrice());
        listing.setPricing(pricing);

        listing.setPin(req.getPin());

        listingRepository.save(listing);

        // 사진 URL 저장
        if (req.getPhotos() != null) {
            for (String url : req.getPhotos()) {
                if (url == null || url.isBlank()) continue;
                ListingPhoto photo = new ListingPhoto();
                photo.setUrl(url);
                photo.setListing(listing);
                photoRepository.save(photo);
                listing.getPhotos().add(photo);
            }
        }

        return toResponse(listing);
    }

    /* 매물 전체 조회 */
    @Transactional(readOnly = true)
    public List<ListingResponse> findAll() {
        return listingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /* 숙박 전체 리스트 (카드용) */
    @Transactional(readOnly = true)
    public List<StayTopItem> getStayList() {
        return listingRepository.findByType(ListingType.STAY)
                .stream()
                .map(l -> {
                    ListingDetails d  = (l.getDetails()  != null) ? l.getDetails()  : new ListingDetails();
                    ListingPeriod  p  = (l.getPeriod()   != null) ? l.getPeriod()   : new ListingPeriod();
                    ListingPricing pr = (l.getPricing()  != null) ? l.getPricing()  : new ListingPricing();

                    StayTopItem dto = new StayTopItem();
                    dto.setId(l.getId());
                    dto.setThumbnailUrl(l.getPhotos().isEmpty() ? null : l.getPhotos().get(0).getUrl());
                    dto.setBuildingName(d.getBuildingName());
                    dto.setStartDate(p.getStartDate());
                    dto.setEndDate(p.getEndDate());
                    dto.setPrice(pr.getPrice());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /* 양도 전체 리스트 (카드용) */
    @Transactional(readOnly = true)
    public List<TransferTopItem> getTransferList() {
        return listingRepository.findByType(ListingType.TRANSFER)
                .stream()
                .map(l -> {
                    ListingDetails d  = (l.getDetails()  != null) ? l.getDetails()  : new ListingDetails();
                    ListingPricing pr = (l.getPricing()  != null) ? l.getPricing()  : new ListingPricing();

                    TransferTopItem dto = new TransferTopItem();
                    dto.setId(l.getId());
                    dto.setThumbnailUrl(l.getPhotos().isEmpty() ? null : l.getPhotos().get(0).getUrl());
                    dto.setBuildingName(d.getBuildingName());
                    dto.setPrice(pr.getPrice());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /* 숙박 필터링 검색: 대표사진1, 건물명, 날짜, 금액*/
    @Transactional(readOnly = true)
    public List<StayTopItem> searchStay(String name,
                                        LocalDate startDate,
                                        LocalDate endDate,
                                        Integer minPrice,
                                        Integer maxPrice) {

        List<Listing> list = listingRepository.searchStay(
                ListingType.STAY, name, startDate, endDate, minPrice, maxPrice
        );

        return list.stream()
                .map(l -> {
                    ListingDetails d  = (l.getDetails()  != null) ? l.getDetails()  : new ListingDetails();
                    ListingPeriod  p  = (l.getPeriod()   != null) ? l.getPeriod()   : new ListingPeriod();
                    ListingPricing pr = (l.getPricing()  != null) ? l.getPricing()  : new ListingPricing();

                    StayTopItem dto = new StayTopItem();
                    dto.setId(l.getId());
                    dto.setThumbnailUrl(l.getPhotos().isEmpty() ? null : l.getPhotos().get(0).getUrl());
                    dto.setBuildingName(d.getBuildingName());
                    dto.setStartDate(p.getStartDate());
                    dto.setEndDate(p.getEndDate());
                    dto.setPrice(pr.getPrice());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /* 숙박, 양도 통합 검색 */
    @Transactional(readOnly = true)
    public List<ListingSearchItem> searchAllByName(String name) {
        return listingRepository.searchByBuildingName(name).stream().map(l -> {
            ListingDetails d  = (l.getDetails()  != null) ? l.getDetails()  : new ListingDetails();
            ListingPeriod  p  = (l.getPeriod()   != null) ? l.getPeriod()   : new ListingPeriod();
            ListingPricing pr = (l.getPricing()  != null) ? l.getPricing()  : new ListingPricing();

            ListingSearchItem dto = new ListingSearchItem();
            dto.setId(l.getId());
            dto.setType(l.getType());
            dto.setThumbnailUrl(l.getPhotos().isEmpty() ? null : l.getPhotos().get(0).getUrl());
            dto.setBuildingName(d.getBuildingName());
            dto.setPrice(pr.getPrice());
            if (l.getType() == ListingType.STAY) {
                dto.setStartDate(p.getStartDate());
                dto.setEndDate(p.getEndDate());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    /* 숙박 상세 조회 */
    @Transactional(readOnly = true)
    public StayDetailResponse getStayDetail(Long id) {
        Listing l = listingRepository.findByIdAndType(id, ListingType.STAY)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        ListingDetails d  = (l.getDetails()  != null) ? l.getDetails()  : new ListingDetails();
        ListingPeriod  p  = (l.getPeriod()   != null) ? l.getPeriod()   : new ListingPeriod();
        ListingPricing pr = (l.getPricing()  != null) ? l.getPricing()  : new ListingPricing();

        StayDetailResponse dto = new StayDetailResponse();
        dto.setType(l.getType());
        dto.setPhotos(l.getPhotos().stream().map(ListingPhoto::getUrl).toList());
        dto.setBuildingName(d.getBuildingName());
        dto.setStartDate(p.getStartDate());
        dto.setEndDate(p.getEndDate());
        dto.setGuests(p.getGuests());
        dto.setPrice(pr.getPrice());
        dto.setDescription(d.getDescription());
        dto.setAddress(d.getAddress());
        return dto;
    }

    /* 양도 상세 조회 */
    @Transactional(readOnly = true)
    public TransferDetailResponse getTransferDetail(Long id) {
        Listing l = listingRepository.findByIdAndType(id, ListingType.TRANSFER)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        ListingDetails d  = (l.getDetails()  != null) ? l.getDetails()  : new ListingDetails();
        ListingPeriod  p  = (l.getPeriod()   != null) ? l.getPeriod()   : new ListingPeriod();
        ListingPricing pr = (l.getPricing()  != null) ? l.getPricing()  : new ListingPricing();

        TransferDetailResponse dto = new TransferDetailResponse();
        dto.setType(l.getType());
        dto.setPhotos(l.getPhotos().stream().map(ListingPhoto::getUrl).toList());
        dto.setBuildingName(d.getBuildingName());
        dto.setPrice(pr.getPrice());
        dto.setStartDate(p.getStartDate());
        dto.setEndDate(p.getEndDate());
        dto.setAddress(d.getAddress());
        dto.setDescription(d.getDescription());
        return dto;
    }

    /* 타입별 조회 */
    @Transactional(readOnly = true)
    public List<ListingResponse> findByType(ListingType type) {
        return listingRepository.findByType(type)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /* 매물 수정 (사진 교체 시, 제거된 사진 파일은 커밋 후 실제 삭제) */
    public ListingResponse update(Long id, ListingUpdateRequest req) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        validatePin(listing, req.getPin());

        if (listing.getDetails() == null) listing.setDetails(new ListingDetails());
        if (listing.getPeriod()  == null) listing.setPeriod(new ListingPeriod());
        if (listing.getPricing() == null) listing.setPricing(new ListingPricing());

        if (req.getType() != null) listing.setType(req.getType());
        if (req.getBuildingName() != null) listing.getDetails().setBuildingName(req.getBuildingName());
        if (req.getDescription() != null) listing.getDetails().setDescription(req.getDescription());
        if (req.getAddress() != null) listing.getDetails().setAddress(req.getAddress());
        if (req.getStartDate() != null) listing.getPeriod().setStartDate(req.getStartDate());
        if (req.getEndDate() != null) listing.getPeriod().setEndDate(req.getEndDate());
        if (req.getGuests() != null) listing.getPeriod().setGuests(req.getGuests());
        if (req.getPrice() != null) listing.getPricing().setPrice(req.getPrice());

        // 사진 전체 교체
        if (req.getPhotos() != null) {
            // 기존 URL들 백업
            List<String> oldUrls = listing.getPhotos().stream()
                    .map(ListingPhoto::getUrl)
                    .filter(Objects::nonNull)
                    .toList();

            // DB에서 모두 교체
            photoRepository.deleteByListing_Id(listing.getId());
            listing.getPhotos().clear();
            for (String url : req.getPhotos()) {
                if (url == null || url.isBlank()) continue;
                ListingPhoto photo = new ListingPhoto();
                photo.setUrl(url);
                photo.setListing(listing);
                photoRepository.save(photo);
                listing.getPhotos().add(photo);
            }

            // 새 URL 집합
            Set<String> newSet = listing.getPhotos().stream()
                    .map(ListingPhoto::getUrl)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // 제거된 파일만 선별
            List<String> removed = oldUrls.stream()
                    .filter(u -> !newSet.contains(u))
                    .toList();

            // 트랜잭션 커밋 후 실제 파일 삭제
            runAfterCommit(() -> safeDeleteFiles(removed));
        }

        return toResponse(listing);
    }

    /* 매물 삭제 (DB 삭제 커밋 후 실제 파일 삭제) */
    public void delete(Long id, String pin) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        validatePin(listing, pin);

        // 삭제될 파일 URL 확보
        List<String> urls = listing.getPhotos().stream()
                .map(ListingPhoto::getUrl)
                .filter(Objects::nonNull)
                .toList();

        // DB 삭제
        photoRepository.deleteByListing_Id(listing.getId());
        listingRepository.delete(listing);

        // 커밋 후 실제 파일 삭제
        runAfterCommit(() -> safeDeleteFiles(urls));
    }

    /* 조회수 증가 */
    @Transactional
    public void increaseView(Long id) {
        if (listingRepository.increaseView(id) == 0) {
            throw new IllegalArgumentException("Listing not found");
        }
    }

    /* 숙박 매물 Top10 (대표사진+건물명+날짜+금액) */
    @Transactional(readOnly = true)
    public List<StayTopItem> getTop10StayListings() {
        return listingRepository.findTop10ByTypeOrderByViewCountDescCreatedAtDesc(ListingType.STAY)
                .stream()
                .map(listing -> {
                    ListingDetails d  = (listing.getDetails()  != null) ? listing.getDetails()  : new ListingDetails();
                    ListingPeriod  p  = (listing.getPeriod()   != null) ? listing.getPeriod()   : new ListingPeriod();
                    ListingPricing pr = (listing.getPricing()  != null) ? listing.getPricing()  : new ListingPricing();

                    StayTopItem dto = new StayTopItem();
                    dto.setId(listing.getId());
                    dto.setThumbnailUrl(listing.getPhotos().isEmpty() ? null : listing.getPhotos().get(0).getUrl());
                    dto.setBuildingName(d.getBuildingName());
                    dto.setStartDate(p.getStartDate());
                    dto.setEndDate(p.getEndDate());
                    dto.setPrice(pr.getPrice());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /* 양도 매물 Top10 (대표사진+건물명+금액) */
    @Transactional(readOnly = true)
    public List<TransferTopItem> getTop10TransferListings() {
        return listingRepository.findTop10ByTypeOrderByViewCountDescCreatedAtDesc(ListingType.TRANSFER)
                .stream()
                .map(listing -> {
                    ListingDetails d  = (listing.getDetails()  != null) ? listing.getDetails()  : new ListingDetails();
                    ListingPricing pr = (listing.getPricing()  != null) ? listing.getPricing()  : new ListingPricing();

                    TransferTopItem dto = new TransferTopItem();
                    dto.setId(listing.getId());
                    dto.setThumbnailUrl(listing.getPhotos().isEmpty() ? null : listing.getPhotos().get(0).getUrl());
                    dto.setBuildingName(d.getBuildingName());
                    dto.setPrice(pr.getPrice());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /* ==== 내부 메서드 ==== */
    private void validatePin(Listing listing, String pin) {
        if (pin == null || !pin.equals(listing.getPin())) {
            throw new SecurityException("Invalid PIN");
        }
    }

    private ListingResponse toResponse(Listing listing) {
        ListingResponse res = new ListingResponse();

        ListingDetails d  = (listing.getDetails()  != null) ? listing.getDetails()  : new ListingDetails();
        ListingPeriod  p  = (listing.getPeriod()   != null) ? listing.getPeriod()   : new ListingPeriod();
        ListingPricing pr = (listing.getPricing()  != null) ? listing.getPricing()  : new ListingPricing();

        res.setId(listing.getId());
        res.setType(listing.getType());
        res.setBuildingName(d.getBuildingName());
        res.setDescription(d.getDescription());
        res.setAddress(d.getAddress());
        res.setStartDate(p.getStartDate());
        res.setEndDate(p.getEndDate());
        res.setGuests(p.getGuests());
        res.setPrice(pr.getPrice());
        res.setPhotos(listing.getPhotos().stream().map(ListingPhoto::getUrl).collect(Collectors.toList()));
        res.setViewCount(listing.getViewCount());
        res.setCreatedAt(listing.getCreatedAt());
        res.setUpdatedAt(listing.getUpdatedAt());
        return res;
    }

    /* 트랜잭션 커밋 후 실행 */
    private void runAfterCommit(Runnable r) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { r.run(); }
            });
        } else {
            // 트랜잭션이 없으면 즉시 실행
            r.run();
        }
    }

    /* 파일 삭제 (예외 무시 - 로깅 필요시 확장) */
    private void safeDeleteFiles(Collection<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        try {
            fileStorageService.deleteAllByUrls(urls);
        } catch (Exception ignored) {
            // 필요하면 로거로 남기기
        }
    }
}
