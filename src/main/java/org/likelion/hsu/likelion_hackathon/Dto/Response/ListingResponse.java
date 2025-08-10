package org.likelion.hsu.likelion_hackathon.Dto.Response;

import lombok.Getter;
import lombok.Setter;
import org.likelion.hsu.likelion_hackathon.Enum.ListingType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ListingResponse {
    private Long id;
    private ListingType type;
    private String buildingName;
    private String description;
    private String address;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer guests;
    private Integer price;
    private List<String> photos;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
