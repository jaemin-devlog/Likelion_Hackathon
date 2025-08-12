package org.likelion.hsu.likelion_hackathon.Dto.Response;

import lombok.Getter;
import lombok.Setter;
import org.likelion.hsu.likelion_hackathon.Enum.ListingType;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class StayDetailResponse {
    private ListingType type;     // 숙박,양도 타입
    private List<String> photos;  // 등록된 모든 사진 URL
    private String buildingName;  // 건물명
    private LocalDate startDate;  // 기간 시작
    private LocalDate endDate;    // 기간 종료
    private Integer guests;       // 인원 수
    private Integer price;        // 총 금액
    private String description;   // 설명(글쓰기)
    private String address;       // 지도용 주소
}
