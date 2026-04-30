package com.tms.restapi.toolsmanagement.chatbot.repository;

import com.tms.restapi.toolsmanagement.chatbot.model.ChatbotQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatbotQARepository extends JpaRepository<ChatbotQA, Long> {
    
    @Query("SELECT c FROM ChatbotQA c WHERE c.isActive = true AND LOWER(c.question) LIKE LOWER(CONCAT('%', :question, '%'))")
    List<ChatbotQA> findByQuestionKeyword(@Param("question") String question);

    @Query("SELECT c FROM ChatbotQA c WHERE c.isActive = true AND LOWER(c.question) = LOWER(:question)")
    Optional<ChatbotQA> findByQuestionExact(@Param("question") String question);

    @Query("SELECT c FROM ChatbotQA c WHERE c.isActive = true ORDER BY c.createdAt DESC")
    List<ChatbotQA> findAllActiveQAs();

    List<ChatbotQA> findByIsActive(Boolean isActive);
}
