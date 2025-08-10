package org.likelion.hsu.likelion_hackathon.Dto.Response;

import lombok.Getter;
import lombok.Setter;
import org.likelion.hsu.likelion_hackathon.Enum.ListingType;

import java.time.LocalDate;

@Getter @Setter
public class ListingSearchItem {
    private Long id;
    private ListingType type;   // STAY / TRANSFER
    private String thumbnailUrl;
    private String buildingName;
    private LocalDate startDate; // STAY일 때만 값 있음
    private LocalDate endDate;   // STAY일 때만 값 있음
    private Integer price;       // 둘 다 공통
}
