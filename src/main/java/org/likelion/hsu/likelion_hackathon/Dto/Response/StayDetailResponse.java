package org.likelion.hsu.likelion_hackathon.Dto.Response;

import java.time.LocalDate;
import java.util.List;

public class StayDetailResponse {
    private Long id;
    private List<String> photos;   // 등록된 모든 사진 URL
    private String buildingName;   // 건물명
    private LocalDate startDate;   // 기간 시작
    private LocalDate endDate;     // 기간 종료
    private Integer guests;        // 인원 수
    private Integer price;         // 총 금액
    private String description;    // 설명(글쓰기)
    private String address;        // 지도용 주소

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getGuests() { return guests; }
    public void setGuests(Integer guests) { this.guests = guests; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}