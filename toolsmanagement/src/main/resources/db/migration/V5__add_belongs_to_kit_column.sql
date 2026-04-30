-- Migration: Add belongsToKit flag column to tools table
-- Flag: 0 = standalone tool, 1 = tool belongs to a kit

ALTER TABLE tools
  ADD COLUMN belongs_to_kit INT NOT NULL DEFAULT 0;
