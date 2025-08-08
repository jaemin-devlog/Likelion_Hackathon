package org.likelion.hsu.likelion_hackathon.Entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class ListingDetails {

    private String buildingName; // 건물명
    private String description;  // 설명
    private String address;      // 주소

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
