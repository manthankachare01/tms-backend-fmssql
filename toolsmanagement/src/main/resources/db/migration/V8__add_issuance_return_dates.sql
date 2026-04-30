-- V8__add_issuance_return_dates.sql
-- Add issuance_date and return_date columns to tools and kits tables

ALTER TABLE tools ADD COLUMN issuance_date TIMESTAMP;
ALTER TABLE tools ADD COLUMN return_date TIMESTAMP;

ALTER TABLE kits ADD COLUMN issuance_date TIMESTAMP;
ALTER TABLE kits ADD COLUMN return_date TIMESTAMP;