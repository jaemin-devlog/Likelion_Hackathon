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
import org.likelion.hsu.likelion_hackathon.Dto.Response.ListingSearchItem;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
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

    /** ★ 원샷 등록: 파일 저장 → photos URL 주입 → 기존 create 재사용 */
    public ListingResponse createWithUpload(ListingCreateRequest req,
                                            List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();
        if (files != null) {
            for (MultipartFile f : files) {
                if (f != null && !f.isEmpty()) {
                    urls.add(fileStorageService.saveImage(f)); // /images/... URL 생성
                }
            }
        }
        req.setPhotos(urls);
        return create(req);
    }

    /** 매물 등록 */
    public ListingResponse create(ListingCreateRequest req) {
        Listing listing = new Listing();
        listing.setType(req.getType());

        // Embedded 값 세팅
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
                ListingPhoto photo = new ListingPhoto();
                photo.setUrl(url);
                photo.setListing(listing);
                photoRepository.save(photo);
                listing.getPhotos().add(photo);
            }
        }

        return toResponse(listing);
    }

    /** 매물 전체 조회 */
    @Transactional(readOnly = true)
    public List<ListingResponse> findAll() {
        return listingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** 숙박 전체 리스트 (카드용) */
    @Transactional(readOnly = true)
    public List<StayTopItem> getStayList() {
        return listingRepository.findByType(ListingType.STAY)
                .stream()
                .map(l -> {
                    StayTopItem dto = new StayTopItem();
                    dto.setId(l.getId());
                    dto.setThumbnailUrl(l.getPhotos().isEmpty() ? null : l.getPhotos().get(0).getUrl());
                    dto.setBuildingName(l.getDetails().getBuildingName());
                    dto.setStartDate(l.getPeriod().getStartDate());
                    dto.setEndDate(l.getPeriod().getEndDate());
                    dto.setPrice(l.getPricing().getPrice());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /** 양도 전체 리스트 (카드용) */
    @Transactional(readOnly = true)
    public List<TransferTopItem> getTransferList() {
        return listingRepository.findByType(ListingType.TRANSFER)
                .stream()
                .map(l -> {
                    TransferTopItem dto = new TransferTopItem();
                    dto.setId(l.getId());
                    dto.setThumbnailUrl(
                            l.getPhotos().isEmpty() ? null : l.getPhotos().get(0).getUrl()
                    );
                    dto.setBuildingName(l.getDetails().getBuildingName()); // ← 여기 추가
                    dto.setPrice(l.getPricing().getPrice());
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

        // Repository에서 동적 조건 검색
        List<Listing> list = listingRepository.searchStay(
                ListingType.STAY, name, startDate, endDate, minPrice, maxPrice
        );

        // 카드 UI용 DTO 매핑 (대표사진 1장 + 건물명 + 날짜 + 금액)
        return list.stream()
                .map(l -> {
                    StayTopItem dto = new StayTopItem();
                    dto.setId(l.getId());
                    dto.setThumbnailUrl(l.getPhotos().isEmpty() ? null : l.getPhotos().get(0).getUrl());
                    dto.setBuildingName(l.getDetails().getBuildingName());
                    dto.setStartDate(l.getPeriod().getStartDate());
                    dto.setEndDate(l.getPeriod().getEndDate());
                    dto.setPrice(l.getPricing().getPrice());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /* 숙박, 양도 통합 검색 */
    @Transactional(readOnly = true)
    public List<ListingSearchItem> searchAllByName(String name) {
        return listingRepository.searchByBuildingName(name).stream().map(l -> {
            ListingSearchItem dto = new ListingSearchItem();
            dto.setId(l.getId());
            dto.setType(l.getType());
            dto.setThumbnailUrl(l.getPhotos().isEmpty() ? null : l.getPhotos().get(0).getUrl());
            dto.setBuildingName(l.getDetails().getBuildingName());
            dto.setPrice(l.getPricing().getPrice());
            if (l.getType() == ListingType.STAY) {
                dto.setStartDate(l.getPeriod().getStartDate());
                dto.setEndDate(l.getPeriod().getEndDate());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    /* 숙박 상세 조회*/
    @Transactional(readOnly = true)
    public StayDetailResponse getStayDetail(Long id) {
        Listing l = listingRepository.findByIdAndType(id, ListingType.STAY)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        StayDetailResponse dto = new StayDetailResponse();
        dto.setPhotos(l.getPhotos().stream().map(ListingPhoto::getUrl).collect(Collectors.toList()));
        dto.setBuildingName(l.getDetails().getBuildingName());
        dto.setStartDate(l.getPeriod().getStartDate());
        dto.setEndDate(l.getPeriod().getEndDate());
        dto.setGuests(l.getPeriod().getGuests());
        dto.setPrice(l.getPricing().getPrice());
        dto.setDescription(l.getDetails().getDescription());
        dto.setAddress(l.getDetails().getAddress()); // 프런트에서 지도 표기용으로 사용
        dto.setType(l.getType());

        return dto;
    }

    /* 양도 상세 조회*/
    @Transactional(readOnly = true)
    public TransferDetailResponse getTransferDetail(Long id) {
        Listing l = listingRepository.findByIdAndType(id, ListingType.TRANSFER)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        TransferDetailResponse dto = new TransferDetailResponse();
        dto.setPhotos(l.getPhotos().stream().map(ListingPhoto::getUrl).collect(Collectors.toList()));
        dto.setBuildingName(l.getDetails().getBuildingName());
        dto.setPrice(l.getPricing().getPrice());
        dto.setAddress(l.getDetails().getAddress());
        dto.setDescription(l.getDetails().getDescription());
        dto.setStartDate(l.getPeriod().getStartDate());
        dto.setEndDate(l.getPeriod().getEndDate());
        dto.setType(l.getType());

        return dto;
    }

    /** 타입별 조회 */
    @Transactional(readOnly = true)
    public List<ListingResponse> findByType(ListingType type) {
        return listingRepository.findByType(type)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** 매물 수정 */
    public ListingResponse update(Long id, ListingUpdateRequest req) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        validatePin(listing, req.getPin());

        // 과거 TRANSFER 데이터 대비: 임베디드 널 방어
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
            photoRepository.deleteByListing_Id(listing.getId());
            listing.getPhotos().clear();
            for (String url : req.getPhotos()) {
                ListingPhoto photo = new ListingPhoto();
                photo.setUrl(url);
                photo.setListing(listing);
                photoRepository.save(photo);
                listing.getPhotos().add(photo);
            }
        }

        return toResponse(listing);
    }

    /** 매물 삭제 */
    public void delete(Long id, String pin) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        validatePin(listing, pin);
        photoRepository.deleteByListing_Id(listing.getId());
        listingRepository.delete(listing);
    }

    /** 조회수 증가 */
    @Transactional
    public void increaseView(Long id) {
        if (listingRepository.increaseView(id) == 0) {
            throw new IllegalArgumentException("Listing not found");
        }
    }

    /** 숙박 매물 Top10 (대표사진+건물명+날짜+금액) */
    @Transactional(readOnly = true)
    public List<StayTopItem> getTop10StayListings() {
        return listingRepository.findTop10ByTypeOrderByViewCountDescCreatedAtDesc(ListingType.STAY)
                .stream()
                .map(listing -> {
                    StayTopItem dto = new StayTopItem();
                    dto.setId(listing.getId());
                    dto.setThumbnailUrl(
                            listing.getPhotos().isEmpty() ? null : listing.getPhotos().get(0).getUrl()
                    );
                    dto.setBuildingName(listing.getDetails().getBuildingName());
                    dto.setStartDate(listing.getPeriod().getStartDate());
                    dto.setEndDate(listing.getPeriod().getEndDate());
                    dto.setPrice(listing.getPricing().getPrice());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /** 양도 매물 Top10 (대표사진+건물명+금액) */
    @Transactional(readOnly = true)
    public List<TransferTopItem> getTop10TransferListings() {
        return listingRepository.findTop10ByTypeOrderByViewCountDescCreatedAtDesc(ListingType.TRANSFER)
                .stream()
                .map(listing -> {
                    TransferTopItem dto = new TransferTopItem();
                    dto.setId(listing.getId());
                    dto.setThumbnailUrl(
                            listing.getPhotos().isEmpty() ? null : listing.getPhotos().get(0).getUrl()
                    );
                    dto.setBuildingName(listing.getDetails().getBuildingName()); // ← 추가
                    dto.setPrice(listing.getPricing().getPrice());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ==== 내부 메서드 ====
    private void validatePin(Listing listing, String pin) {
        if (pin == null || !pin.equals(listing.getPin())) {
            throw new SecurityException("Invalid PIN");
        }
    }

    private ListingResponse toResponse(Listing listing) {
        ListingResponse res = new ListingResponse();

        // 널 가드: 임베디드가 null이어도 안전
        ListingDetails d  = (listing.getDetails()  != null) ? listing.getDetails()  : new ListingDetails();
        ListingPeriod  p  = (listing.getPeriod()   != null) ? listing.getPeriod()   : new ListingPeriod();
        ListingPricing pr = (listing.getPricing()  != null) ? listing.getPricing()  : new ListingPricing();

        res.setId(listing.getId());
        res.setType(listing.getType());
        res.setBuildingName(d.getBuildingName());
        res.setDescription(d.getDescription());
        res.setAddress(d.getAddress());
        res.setStartDate(p.getStartDate());   // TRANSFER면 null 그대로 내려가도 OK
        res.setEndDate(p.getEndDate());
        res.setGuests(p.getGuests());
        res.setPrice(pr.getPrice());
        res.setPhotos(
                listing.getPhotos().stream().map(ListingPhoto::getUrl).collect(Collectors.toList())
        );
        res.setViewCount(listing.getViewCount());
        res.setCreatedAt(listing.getCreatedAt());
        res.setUpdatedAt(listing.getUpdatedAt());
        return res;
    }
}
