-- Migration: Convert issuance_date and returnDate from DATE to DATETIME for accurate timestamps
-- Also convert actualReturnDate from DATE to DATETIME

-- Update issuance_requests table
ALTER TABLE issuance_requests
  MODIFY COLUMN issuance_date DATETIME NOT NULL,
  MODIFY COLUMN return_date DATETIME;

-- Update return_records table
ALTER TABLE return_records
  MODIFY COLUMN actual_return_date DATETIME NOT NULL;
