-- V4__create_chatbot_qa_table.sql
-- Create table for storing predefined chatbot questions and answers

CREATE TABLE IF NOT EXISTS chatbot_qa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question VARCHAR(500) NOT NULL,
    answer LONGTEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_question (question),
    INDEX idx_is_active (is_active)
);

-- Insert sample Q&A
INSERT INTO chatbot_qa (question, answer, is_active, created_at, updated_at) 
VALUES ('What is system name?', 'Tools Management System', true, NOW(), NOW());
