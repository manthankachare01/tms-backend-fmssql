-- Add issue_count column to tools table
ALTER TABLE tools ADD COLUMN issue_count INT DEFAULT 0;

-- Create index for better query performance
CREATE INDEX idx_issue_count ON tools(issue_count);
