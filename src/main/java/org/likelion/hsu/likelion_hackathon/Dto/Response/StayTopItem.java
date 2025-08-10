package org.likelion.hsu.likelion_hackathon.Dto.Response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StayTopItem {
    private Long id;
    private String thumbnailUrl;   // 대표사진 1장
    private String buildingName;   // 건물명
    private LocalDate startDate;   // 시작일
    private LocalDate endDate;     // 종료일
    private Integer price;         // 총 금액(원)
}
