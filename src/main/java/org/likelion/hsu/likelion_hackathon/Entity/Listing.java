package org.likelion.hsu.likelion_hackathon.Entity;

import jakarta.persistence.*;
import org.likelion.hsu.likelion_hackathon.Enum.ListingType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingType type; // STAY | TRANSFER

    @Embedded
    private ListingDetails details;

    @Embedded
    private ListingPeriod period;

    @Embedded
    private ListingPricing pricing;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListingPhoto> photos = new ArrayList<>();

    @Column(nullable = false)
    private Long viewCount = 0L;

    @Column(nullable = false, length = 10)
    private String pin;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===== Getters and Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ListingType getType() { return type; }
    public void setType(ListingType type) { this.type = type; }

    public ListingDetails getDetails() { return details; }
    public void setDetails(ListingDetails details) { this.details = details; }

    public ListingPeriod getPeriod() { return period; }
    public void setPeriod(ListingPeriod period) { this.period = period; }

    public ListingPricing getPricing() { return pricing; }
    public void setPricing(ListingPricing pricing) { this.pricing = pricing; }

    public List<ListingPhoto> getPhotos() { return photos; }
    public void setPhotos(List<ListingPhoto> photos) { this.photos = photos; }

    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
