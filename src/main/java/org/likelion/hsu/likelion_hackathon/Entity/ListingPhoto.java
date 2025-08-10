package org.likelion.hsu.likelion_hackathon.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "listing_photos")
public class ListingPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url; // 사진 URL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    private Listing listing;
}
