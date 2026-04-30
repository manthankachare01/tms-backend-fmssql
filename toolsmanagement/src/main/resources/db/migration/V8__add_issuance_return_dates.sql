-- V8__add_issuance_return_dates.sql (MSSQL)
-- Add issuance_date and return_date columns to tools and kits tables

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tools' AND COLUMN_NAME = 'issuance_date')
    ALTER TABLE tools ADD issuance_date DATETIME2;

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tools' AND COLUMN_NAME = 'return_date')
    ALTER TABLE tools ADD return_date DATETIME2;

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'kits' AND COLUMN_NAME = 'issuance_date')
    ALTER TABLE kits ADD issuance_date DATETIME2;

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'kits' AND COLUMN_NAME = 'return_date')
    ALTER TABLE kits ADD return_date DATETIME2;