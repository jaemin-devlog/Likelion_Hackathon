package org.likelion.hsu.likelion_hackathon.Dto.Request;

import lombok.Getter;
import lombok.Setter;
import org.likelion.hsu.likelion_hackathon.Enum.ListingType;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ListingUpdateRequest {
    private ListingType type;
    private String buildingName;
    private String description;
    private String address;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer guests;
    private Integer price;
    private List<String> photos;
    private String pin; // 필수
}