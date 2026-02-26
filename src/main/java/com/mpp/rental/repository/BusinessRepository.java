package com.mpp.rental.repository;

import com.mpp.rental.model.Business;
import com.mpp.rental.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

    /**
     * Find all businesses owned by a specific user
     */
    List<Business> findByUser(User user);

    /**
     * Find all businesses by userId
     */
    List<Business> findByUser_UserId(Long userId);

    /**
     * Check if business name already exists (globally unique)
     */
    boolean existsByBusinessName(String businessName);

    /**
     * Check if business name exists for update (exclude current business)
     */
    boolean existsByBusinessNameAndBusinessIdNot(String businessName, Long businessId);

    /**
     * Check if SSM number already exists
     */
    boolean existsBySsmNumber(String ssmNumber);

    /**
     * Check if SSM number exists for update (exclude current business)
     */
    boolean existsBySsmNumberAndBusinessIdNot(String ssmNumber, Long businessId);

    /**
     * Find business by name
     */
    Optional<Business> findByBusinessName(String businessName);

    /**
     * Find all businesses by status
     */
    List<Business> findByBusinessStatus(String status);

    /**
     * Count businesses by user
     */
    long countByUser(User user);

    // ==================== NEW METHODS FOR MPP BUSINESS MANAGEMENT ====================

    /**
     * Find all businesses with search and filter for MPP
     * Searches by: business name, SSM number, owner name
     * Filters by: category, status, owner category, date range
     *
     * FIXED: Uses CAST to compare enum with string for ownerCategory
     */
    @Query("SELECT b FROM Business b JOIN b.user u WHERE " +
            "(:searchQuery IS NULL OR " +
            "LOWER(b.businessName) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
            "LOWER(b.ssmNumber) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
            "LOWER(u.userName) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) AND " +
            "(:businessCategory IS NULL OR LOWER(b.businessCategory) = LOWER(:businessCategory)) AND " +
            "(:businessStatus IS NULL OR UPPER(b.businessStatus) = UPPER(:businessStatus)) AND " +
            "(:ownerCategory IS NULL OR CAST(u.userCategory AS string) = :ownerCategory) AND " +
            "(:startDate IS NULL OR b.businessRegisteredAt >= :startDate) AND " +
            "(:endDate IS NULL OR b.businessRegisteredAt <= :endDate) " +
            "ORDER BY b.businessRegisteredAt DESC")
    List<Business> findAllWithFilters(
            @Param("searchQuery") String searchQuery,
            @Param("businessCategory") String businessCategory,
            @Param("businessStatus") String businessStatus,
            @Param("ownerCategory") String ownerCategory,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Get all businesses for MPP (without filters)
     * Ordered by most recent first
     */
    @Query("SELECT b FROM Business b JOIN FETCH b.user ORDER BY b.businessRegisteredAt DESC")
    List<Business> findAllWithOwnerInfo();

    /**
     * Find business by ID with user info (for MPP details view)
     * Uses JOIN FETCH to avoid N+1 query problem
     */
    @Query("SELECT b FROM Business b JOIN FETCH b.user WHERE b.businessId = :businessId")
    Optional<Business> findByIdWithOwner(@Param("businessId") Long businessId);

    /**
     * Find all businesses by owner category (STUDENT or NON_STUDENT)
     * Useful for filtering
     *
     * FIXED: Uses CAST to compare enum with string
     */
    @Query("SELECT b FROM Business b JOIN b.user u WHERE CAST(u.userCategory AS string) = :ownerCategory")
    List<Business> findByOwnerCategory(@Param("ownerCategory") String ownerCategory);

    /**
     * Count active businesses
     */
    @Query("SELECT COUNT(b) FROM Business b WHERE b.businessStatus = 'ACTIVE'")
    long countActiveBusinesses();

    /**
     * Count blocked businesses
     */
    @Query("SELECT COUNT(b) FROM Business b WHERE b.businessStatus = 'BLOCKED'")
    long countBlockedBusinesses();

    /**
     * Find businesses by category
     */
    List<Business> findByBusinessCategory(String category);

    /**
     * Check if business has approved applications (for future use)
     * This method will be implemented when FacilityApplication is created
     * For now, it returns false
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Business b WHERE b.businessId = :businessId")
    default boolean hasApprovedApplications(@Param("businessId") Long businessId) {
        // TODO: Implement when FacilityApplication entity is created
        // @Query("SELECT CASE WHEN COUNT(fa) > 0 THEN true ELSE false END " +
        //        "FROM FacilityApplication fa WHERE fa.business.businessId = :businessId " +
        //        "AND fa.applicationStatus = 'APPROVED'")
        return false;
    }
}