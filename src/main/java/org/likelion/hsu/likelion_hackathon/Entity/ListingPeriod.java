package org.likelion.hsu.likelion_hackathon.Entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class ListingPeriod {
    private LocalDate startDate; // 시작일
    private LocalDate endDate;   // 종료일
    private Integer guests;      // 인원 수
}
