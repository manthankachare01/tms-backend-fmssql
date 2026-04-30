package com.tms.restapi.toolsmanagement.kit.repository;

import com.tms.restapi.toolsmanagement.kit.model.Kit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KitRepository extends JpaRepository<Kit, Long> {

    List<Kit> findByLocationIgnoreCase(String location);

    @Query("SELECT k FROM Kit k " +
            "WHERE LOWER(k.location) = LOWER(:location) " +
            "AND (" +
            "LOWER(k.kitName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(k.trainingName) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    List<Kit> searchByLocationAndKeyword(@Param("location") String location,
                                         @Param("keyword") String keyword);
}
