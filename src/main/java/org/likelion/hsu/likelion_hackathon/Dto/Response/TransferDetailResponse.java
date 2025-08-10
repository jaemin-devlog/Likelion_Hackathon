package org.likelion.hsu.likelion_hackathon.Dto.Response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TransferDetailResponse {
    private Long id;
    private List<String> photos; // 업로드된 모든 사진 URL
    private String buildingName; // 건물명
    private Integer price;       // 총 금액
    private String address;      // 주소 (프론트 지도용)
    private String description;  // 설명
}
