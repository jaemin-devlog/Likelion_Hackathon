package org.likelion.hsu.likelion_hackathon.Controller;

import org.likelion.hsu.likelion_hackathon.Dto.*;
import org.likelion.hsu.likelion_hackathon.Dto.Request.ListingCreateRequest;
import org.likelion.hsu.likelion_hackathon.Dto.Request.ListingUpdateRequest;
import org.likelion.hsu.likelion_hackathon.Dto.Response.*;
import org.likelion.hsu.likelion_hackathon.Enum.ListingType;
import org.likelion.hsu.likelion_hackathon.Service.ListingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    /* 매물 등록 */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ListingResponse> create(@RequestBody ListingCreateRequest req) {
        return ResponseEntity.ok(listingService.create(req));
    }

    /* 전체/타입별 조회 (?type=STAY | TRANSFER) */
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<ListingResponse>> list(@RequestParam(required = false) ListingType type) {
        if (type == null) return ResponseEntity.ok(listingService.findAll());
        return ResponseEntity.ok(listingService.findByType(type));
    }

    /* 숙박 상세 조회*/
    @GetMapping("/stay/{id}")
    public ResponseEntity<StayDetailResponse> getStayDetail(@PathVariable Long id) {
        return ResponseEntity.ok(listingService.getStayDetail(id));
    }

    /* 양도 상세 조회*/
    @GetMapping("/transfer/{id}")
    public ResponseEntity<TransferDetailResponse> getTransferDetail(@PathVariable Long id) {
        return ResponseEntity.ok(listingService.getTransferDetail(id));
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

    /* 상세 진입 시 조회수 증가 */
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> increaseView(@PathVariable Long id) {
        listingService.increaseView(id);
        return ResponseEntity.noContent().build();
    }

    /* 조회수 상위 숙박 Top10: 대표사진+건물명+날짜+금액 */
    @GetMapping("/top/stay")
    public ResponseEntity<List<StayTopItem>> topStay() {
        return ResponseEntity.ok(listingService.getTop10StayListings());
    }

    /* 조회수 상위 양도 Top10: 대표사진+금액 */
    @GetMapping("/top/transfer")
    public ResponseEntity<List<TransferTopItem>> topTransfer() {
        return ResponseEntity.ok(listingService.getTop10TransferListings());
    }
}
