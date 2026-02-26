package com.mpp.rental.repository;

import com.mpp.rental.model.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Integer>, JpaSpecificationExecutor<Facility> {

    /**
     * Check if facility name exists (case-insensitive, excluding deleted)
     */
    boolean existsByFacilityNameIgnoreCaseAndDeletedAtIsNull(String facilityName);

    /**
     * Check if facility name exists for a different facility (for updates)
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Facility f " +
            "WHERE LOWER(f.facilityName) = LOWER(:facilityName) " +
            "AND f.facilityId != :facilityId " +
            "AND f.deletedAt IS NULL")
    boolean existsByFacilityNameAndNotId(@Param("facilityName") String facilityName,
                                         @Param("facilityId") Integer facilityId);

    /**
     * Find facility by ID (excluding deleted)
     */
    Optional<Facility> findByFacilityIdAndDeletedAtIsNull(Integer facilityId);

    /**
     * Find all active facilities (excluding deleted)
     */
    List<Facility> findByFacilityStatusAndDeletedAtIsNull(String status);

    /**
     * Search facilities by name containing (case-insensitive, excluding deleted)
     */
    @Query("SELECT f FROM Facility f WHERE " +
            "LOWER(f.facilityName) LIKE LOWER(CONCAT('%', :searchQuery, '%')) " +
            "AND f.deletedAt IS NULL " +
            "ORDER BY f.facilityCreateAt DESC")
    List<Facility> searchByName(@Param("searchQuery") String searchQuery);
}