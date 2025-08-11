package org.likelion.hsu.likelion_hackathon.Controller;

import org.likelion.hsu.likelion_hackathon.Dto.Request.ListingCreateRequest;
import org.likelion.hsu.likelion_hackathon.Dto.Request.ListingUpdateRequest;
import org.likelion.hsu.likelion_hackathon.Dto.Response.*;
import org.likelion.hsu.likelion_hackathon.Service.ListingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    /* 숙박,양도 매물 등록 */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ListingResponse> create(@RequestBody ListingCreateRequest req) {
        return ResponseEntity.ok(listingService.create(req));
    }

    /* 숙박 전체 리스트 (대표사진1, 건물명, 기간, 금액) */
    @GetMapping("/stay")
    public ResponseEntity<List<StayTopItem>> listStay() {
        return ResponseEntity.ok(listingService.getStayList());
    }

    /* 양도 전체 리스트 (대표사진1, 건물명, 금액) */
    @GetMapping("/transfer")
    public ResponseEntity<List<TransferTopItem>> listTransfer() {
        return ResponseEntity.ok(listingService.getTransferList());
    }

    /* 숙박 상세 조회*/
    @GetMapping("/stay/{id}")
    public ResponseEntity<StayDetailResponse> getStayDetail(@PathVariable Long id) {
        listingService.increaseView(id); // 조회수 증가
        return ResponseEntity.ok(listingService.getStayDetail(id));
    }

    /* 양도 상세 조회*/
    @GetMapping("/transfer/{id}")
    public ResponseEntity<TransferDetailResponse> getTransferDetail(@PathVariable Long id) {
        listingService.increaseView(id); // 조회수 증가
        return ResponseEntity.ok(listingService.getTransferDetail(id));
    }

    /* 숙박 필터링 검색: 대표사진1, 건물명, 날짜, 금액*/
    @GetMapping("/stay/search")
    public ResponseEntity<List<StayTopItem>> searchStay(
            @RequestParam(required = false) String name, // 건물명 키워드
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice
    ) {
        return ResponseEntity.ok(
                listingService.searchStay(name, startDate, endDate, minPrice, maxPrice)
        );
    }

    /* 숙박, 양도 통합 검색 */
    @GetMapping("/search")
    public ResponseEntity<List<ListingSearchItem>> searchAll(@RequestParam(required = false) String name) {
        return ResponseEntity.ok(listingService.searchAllByName(name));
    }

    /* 매물 수정 (PIN 검증 필요) */
    @PatchMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ListingResponse> update(@PathVariable Long id,
                                                  @RequestBody ListingUpdateRequest req) {
        return ResponseEntity.ok(listingService.update(id, req));
    }

    /* 매물 삭제 (PIN 쿼리파라미터) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam String pin) {
        listingService.delete(id, pin);
        return ResponseEntity.noContent().build();
    }

    /* 조회수 상위 숙박 Top10: 대표사진+건물명+날짜+금액 */
    @GetMapping("/top/stay")
    public ResponseEntity<List<StayTopItem>> topStay() {
        return ResponseEntity.ok(listingService.getTop10StayListings());
    }

    /* 조회수 상위 양도 Top10: 대표사진+건물명+금액 */
    @GetMapping("/top/transfer")
    public ResponseEntity<List<TransferTopItem>> topTransfer() {
        return ResponseEntity.ok(listingService.getTop10TransferListings());
    }
}
