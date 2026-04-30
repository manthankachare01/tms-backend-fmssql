-- V4__create_chatbot_qa_table.sql (MSSQL)
-- Create table for storing predefined chatbot questions and answers

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'chatbot_qa')
BEGIN
    CREATE TABLE chatbot_qa (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        question VARCHAR(500) NOT NULL,
        answer NVARCHAR(MAX) NOT NULL,
        is_active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
    );
    
    -- Create indexes
    CREATE INDEX idx_question ON chatbot_qa(question);
    CREATE INDEX idx_is_active ON chatbot_qa(is_active);
END;

-- Insert sample Q&A
IF NOT EXISTS (SELECT 1 FROM chatbot_qa WHERE question = 'What is system name?')
BEGIN
    INSERT INTO chatbot_qa (question, answer, is_active, created_at, updated_at) 
    VALUES ('What is system name?', 'Tools Management System', 1, GETDATE(), GETDATE());
END;
