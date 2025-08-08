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

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ListingService {

    private final ListingRepository listingRepository;
    private final ListingPhotoRepository photoRepository;

    public ListingService(ListingRepository listingRepository, ListingPhotoRepository photoRepository) {
        this.listingRepository = listingRepository;
        this.photoRepository = photoRepository;
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

    /* 숙박 상세 조회*/
    @Transactional(readOnly = true)
    public StayDetailResponse getStayDetail(Long id) {
        Listing l = listingRepository.findByIdAndType(id, ListingType.STAY)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        StayDetailResponse dto = new StayDetailResponse();
        dto.setId(l.getId());
        dto.setPhotos(l.getPhotos().stream().map(ListingPhoto::getUrl).collect(Collectors.toList()));
        dto.setBuildingName(l.getDetails().getBuildingName());
        dto.setStartDate(l.getPeriod().getStartDate());
        dto.setEndDate(l.getPeriod().getEndDate());
        dto.setGuests(l.getPeriod().getGuests());
        dto.setPrice(l.getPricing().getPrice());
        dto.setDescription(l.getDetails().getDescription());
        dto.setAddress(l.getDetails().getAddress()); // 프런트에서 지도 표기용으로 사용
        return dto;
    }

    /* 양도 상세 조회*/
    @Transactional(readOnly = true)
    public TransferDetailResponse getTransferDetail(Long id) {
        Listing l = listingRepository.findByIdAndType(id, ListingType.TRANSFER)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        TransferDetailResponse dto = new TransferDetailResponse();
        dto.setId(l.getId());
        dto.setPhotos(l.getPhotos().stream().map(ListingPhoto::getUrl).collect(Collectors.toList()));
        dto.setBuildingName(l.getDetails().getBuildingName());
        dto.setPrice(l.getPricing().getPrice());
        dto.setAddress(l.getDetails().getAddress());
        dto.setDescription(l.getDetails().getDescription());
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
    public void increaseView(Long id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        listing.setViewCount(listing.getViewCount() + 1);
    }

    /** 숙박 매물 Top10 (대표사진+건물명+날짜+금액) */
    @Transactional(readOnly = true)
    public List<StayTopItem> getTop10StayListings() {
        return listingRepository.findTop10ByTypeOrderByViewCountDesc(ListingType.STAY)
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

    /** 양도 매물 Top10 (대표사진+금액) */
    @Transactional(readOnly = true)
    public List<TransferTopItem> getTop10TransferListings() {
        return listingRepository.findTop10ByTypeOrderByViewCountDesc(ListingType.TRANSFER)
                .stream()
                .map(listing -> {
                    TransferTopItem dto = new TransferTopItem();
                    dto.setId(listing.getId());
                    dto.setThumbnailUrl(
                            listing.getPhotos().isEmpty() ? null : listing.getPhotos().get(0).getUrl()
                    );
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
        res.setId(listing.getId());
        res.setType(listing.getType());
        res.setBuildingName(listing.getDetails().getBuildingName());
        res.setDescription(listing.getDetails().getDescription());
        res.setAddress(listing.getDetails().getAddress());
        res.setStartDate(listing.getPeriod().getStartDate());
        res.setEndDate(listing.getPeriod().getEndDate());
        res.setGuests(listing.getPeriod().getGuests());
        res.setPrice(listing.getPricing().getPrice());
        res.setPhotos(listing.getPhotos().stream()
                .map(ListingPhoto::getUrl)
                .collect(Collectors.toList()));
        res.setViewCount(listing.getViewCount());
        res.setCreatedAt(listing.getCreatedAt());
        res.setUpdatedAt(listing.getUpdatedAt());
        return res;
    }
}
