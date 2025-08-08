package org.likelion.hsu.likelion_hackathon.Dto.Response;

import java.util.List;

public class TransferDetailResponse {
    private Long id;
    private List<String> photos; // 업로드된 모든 사진 URL
    private String buildingName; // 건물명
    private Integer price;       // 총 금액
    private String address;      // 주소 (프론트 지도용)
    private String description;  // 설명

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
