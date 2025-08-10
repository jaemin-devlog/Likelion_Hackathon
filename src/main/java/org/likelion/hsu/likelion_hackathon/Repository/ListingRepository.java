package org.likelion.hsu.likelion_hackathon.Repository;

import org.likelion.hsu.likelion_hackathon.Entity.Listing;
import org.likelion.hsu.likelion_hackathon.Enum.ListingType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    /** 특정 타입 매물 전체 조회 (카드용 필드 즉시 로딩) */
    @EntityGraph(attributePaths = {"photos", "details", "period", "pricing"})
    List<Listing> findByType(ListingType type);

    /** 특정 타입의 매물 조회수 순 Top10 (카드에 필요한 연관 전부 즉시 로딩) */
    @EntityGraph(attributePaths = {"photos", "details", "period", "pricing"})
    List<Listing> findTop10ByTypeOrderByViewCountDesc(ListingType type);

    /** 상세 조회 (모든 임베디드/사진 즉시 로딩) */
    @EntityGraph(attributePaths = {"photos", "details", "period", "pricing"})
    Optional<Listing> findByIdAndType(Long id, ListingType type);

    /* 숙박 필터링 검색: 대표사진1, 건물명, 날짜, 금액*/
    @EntityGraph(attributePaths = {"photos", "details", "period", "pricing"})
    @Query("""
        SELECT l FROM Listing l
        WHERE l.type = :type
          AND (:name IS NULL OR LOWER(l.details.buildingName) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (
                (:start IS NULL AND :end IS NULL)
             OR (:start IS NULL AND l.period.endDate >= :end)
             OR (:end IS NULL AND l.period.startDate <= :start)
             OR (l.period.startDate <= :end AND l.period.endDate >= :start)
          )
          AND (:minPrice IS NULL OR l.pricing.price >= :minPrice)
          AND (:maxPrice IS NULL OR l.pricing.price <= :maxPrice)
        """)
    List<Listing> searchStay(@Param("type") ListingType type,
                             @Param("name") String name,
                             @Param("start") LocalDate startDate,
                             @Param("end") LocalDate endDate,
                             @Param("minPrice") Integer minPrice,
                             @Param("maxPrice") Integer maxPrice);

    /* 숙박, 양도 통합 검색 */
    @EntityGraph(attributePaths = {"photos", "details", "period", "pricing"})
    @Query("""
    SELECT l FROM Listing l
    WHERE (:name IS NULL OR LOWER(l.details.buildingName) LIKE LOWER(CONCAT('%', :name, '%')))
    """)
    List<Listing> searchByBuildingName(@Param("name") String name);
}
