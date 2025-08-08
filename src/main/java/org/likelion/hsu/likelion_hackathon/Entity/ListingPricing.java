package org.likelion.hsu.likelion_hackathon.Entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class ListingPricing {

    private Integer price; // 총 금액(원)

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
}
