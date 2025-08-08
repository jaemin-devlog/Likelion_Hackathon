package org.likelion.hsu.likelion_hackathon.Dto.Response;

import java.time.LocalDate;

public class StayTopItem {
    private Long id;
    private String thumbnailUrl;   // 대표사진 1장
    private String buildingName;   // 건물명
    private LocalDate startDate;   // 시작일
    private LocalDate endDate;     // 종료일
    private Integer price;         // 총 금액(원)

    public StayTopItem() {}

    public StayTopItem(Long id, String thumbnailUrl, String buildingName,
                       LocalDate startDate, LocalDate endDate, Integer price) {
        this.id = id;
        this.thumbnailUrl = thumbnailUrl;
        this.buildingName = buildingName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
}
