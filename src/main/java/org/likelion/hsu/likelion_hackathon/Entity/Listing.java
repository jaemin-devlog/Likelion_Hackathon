package org.likelion.hsu.likelion_hackathon.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.likelion.hsu.likelion_hackathon.Enum.ListingType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
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
}
