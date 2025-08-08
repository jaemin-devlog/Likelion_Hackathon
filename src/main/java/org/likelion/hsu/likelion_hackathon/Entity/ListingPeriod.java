package org.likelion.hsu.likelion_hackathon.Entity;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;

@Embeddable
public class ListingPeriod {

    private LocalDate startDate; // 시작일
    private LocalDate endDate;   // 종료일
    private Integer guests;      // 인원 수

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getGuests() { return guests; }
    public void setGuests(Integer guests) { this.guests = guests; }
}
