package com.mpp.rental.repository;

import com.mpp.rental.model.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Integer> {

    /** All active items for a group, ordered by display_order */
    List<SystemConfig> findByConfigGroupAndIsActiveTrueOrderByDisplayOrderAsc(String configGroup);

    /** All items for a group (including inactive) — for Super Admin management */
    List<SystemConfig> findByConfigGroupOrderByDisplayOrderAsc(String configGroup);

    /** Check if value already exists in a group */
    boolean existsByConfigGroupAndConfigValue(String configGroup, String configValue);

    /** Check duplicate excluding current record (for update) */
    @Query("SELECT COUNT(s) > 0 FROM SystemConfig s WHERE s.configGroup = :group AND s.configValue = :value AND s.configId != :id")
    boolean existsByConfigGroupAndConfigValueAndConfigIdNot(
            @Param("group") String group,
            @Param("value") String value,
            @Param("id") Integer id);
}