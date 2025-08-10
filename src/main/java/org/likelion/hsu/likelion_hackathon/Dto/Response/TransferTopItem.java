package org.likelion.hsu.likelion_hackathon.Dto.Response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferTopItem {
    private Long id;
    private String thumbnailUrl;  // 대표사진 1장
    private String buildingName;
    private Integer price;        // 총 금액(원)
}
