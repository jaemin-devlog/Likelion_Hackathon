package org.likelion.hsu.likelion_hackathon.Entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class ListingDetails {
    private String buildingName; // 건물명
    private String description;  // 설명
    private String address;      // 주소
}
