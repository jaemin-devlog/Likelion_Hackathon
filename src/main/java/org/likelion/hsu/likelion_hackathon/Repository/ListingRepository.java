package org.likelion.hsu.likelion_hackathon.Repository;

import org.likelion.hsu.likelion_hackathon.Entity.Listing;
import org.likelion.hsu.likelion_hackathon.Enum.ListingType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    /** 특정 타입 매물 전체 조회 */
    List<Listing> findByType(ListingType type);

    /** 특정 타입의 매물 조회수 순 Top10 */
    List<Listing> findTop10ByTypeOrderByViewCountDesc(ListingType type);

    @EntityGraph(attributePaths = {"photos", "details", "period", "pricing"})
    Optional<Listing> findByIdAndType(Long id, ListingType type);
}
