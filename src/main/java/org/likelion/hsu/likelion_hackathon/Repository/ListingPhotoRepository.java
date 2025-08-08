package org.likelion.hsu.likelion_hackathon.Repository;

import org.likelion.hsu.likelion_hackathon.Entity.ListingPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListingPhotoRepository extends JpaRepository<ListingPhoto, Long> {
    void deleteByListing_Id(Long listingId); // ← 언더스코어!
}
