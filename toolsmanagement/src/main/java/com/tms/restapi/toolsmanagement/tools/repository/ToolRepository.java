package com.tms.restapi.toolsmanagement.tools.repository;

import com.tms.restapi.toolsmanagement.tools.model.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolRepository extends JpaRepository<Tool, Long> {

    List<Tool> findByLocation(String location);

    List<Tool> findByDescriptionContainingIgnoreCaseOrToolNoContainingIgnoreCase(
            String description,
            String toolNo
    );

    boolean existsBySiNoAndLocation(String siNo, String location);

    @org.springframework.data.jpa.repository.Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Tool t WHERE LOWER(TRIM(t.siNo)) = LOWER(TRIM(:siNo)) AND LOWER(TRIM(t.location)) = LOWER(TRIM(:location))")
    boolean existsByBySiNoAndLocationIgnoreCaseAndTrim(@org.springframework.data.repository.query.Param("siNo") String siNo,
                                                       @org.springframework.data.repository.query.Param("location") String location);

    List<Tool> findByToolNoIn(List<String> toolNos);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Tool t WHERE t.location = :location AND (LOWER(t.description) LIKE CONCAT('%', LOWER(:keyword), '%') OR LOWER(t.toolNo) LIKE CONCAT('%', LOWER(:keyword), '%'))")
    List<Tool> searchByLocationAndKeyword(@org.springframework.data.repository.query.Param("location") String location,
                                          @org.springframework.data.repository.query.Param("keyword") String keyword);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Tool t WHERE LOWER(TRIM(t.siNo)) = LOWER(TRIM(:siNo)) AND LOWER(TRIM(t.location)) = LOWER(TRIM(:location))")
    Tool findBySiNoAndLocationIgnoreCaseAndTrim(@org.springframework.data.repository.query.Param("siNo") String siNo,
                                                @org.springframework.data.repository.query.Param("location") String location);

    List<Tool> findByToolNoAndLocation(String toolNo, String location);
}
