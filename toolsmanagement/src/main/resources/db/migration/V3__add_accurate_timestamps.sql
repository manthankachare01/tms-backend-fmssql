-- Migration: Convert issuance_date and returnDate from DATE to DATETIME for accurate timestamps (MSSQL)
-- Also convert actualReturnDate from DATE to DATETIME

-- Update issuance_requests table
ALTER TABLE issuance_requests
  ALTER COLUMN issuance_date DATETIME2 NOT NULL;
ALTER TABLE issuance_requests
  ALTER COLUMN return_date DATETIME2;

-- Update return_records table
ALTER TABLE return_records
  ALTER COLUMN actual_return_date DATETIME2 NOT NULL;
