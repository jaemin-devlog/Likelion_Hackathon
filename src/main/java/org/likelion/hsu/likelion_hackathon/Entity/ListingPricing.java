package org.likelion.hsu.likelion_hackathon.Entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class ListingPricing {
    private Integer price; // 총 금액(원)
}
