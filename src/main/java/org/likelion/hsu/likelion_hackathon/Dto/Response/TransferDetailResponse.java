package org.likelion.hsu.likelion_hackathon.Dto.Response;

import lombok.Getter;
import lombok.Setter;
import org.likelion.hsu.likelion_hackathon.Enum.ListingType;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class TransferDetailResponse {
    private ListingType type;    // 숙박,양도 타입
    private List<String> photos; // 업로드된 모든 사진 URL
    private String buildingName; // 건물명
    private Integer price;       // 총 금액
    private LocalDate startDate; // 입실 날짜
    private LocalDate endDate;   // 퇴실 날짜
    private String address;      // 주소 (프론트 지도용)
    private String description;  // 설명
}
