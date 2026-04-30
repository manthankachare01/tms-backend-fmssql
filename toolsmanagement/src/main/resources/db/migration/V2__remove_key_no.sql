-- Migration: remove legacy key_no column from key_issuance
-- Safe approach: make the column nullable first (if you prefer non-destructive),
-- then drop it when ready. Two options provided below; run one.

-- OPTION A (recommended, non-destructive): make column nullable so inserts succeed
-- Use this if you want to keep the column but allow new inserts without value.
ALTER TABLE key_issuance
  MODIFY COLUMN key_no VARCHAR(255) NULL;

-- OPTION B (destructive): drop the column entirely
-- Use this if you are sure the column is no longer needed and you have backups.
-- ALTER TABLE key_issuance
--   DROP COLUMN key_no;
