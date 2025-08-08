package org.likelion.hsu.likelion_hackathon.Dto.Request;

import org.likelion.hsu.likelion_hackathon.Enum.ListingType;
import java.time.LocalDate;
import java.util.List;

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

    public ListingType getType() { return type; }
    public void setType(ListingType type) { this.type = type; }
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getGuests() { return guests; }
    public void setGuests(Integer guests) { this.guests = guests; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
}
