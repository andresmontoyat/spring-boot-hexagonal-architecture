--liquibase formatted sql

-- One row per minute. The primary key is the job's idempotency key: whatever runs twice for the
-- same period (expired lock, late instance), the database keeps the first report.

--changeset andresmontoyat:20261022100000-create-customs-report-table
CREATE TABLE customs_report (
    period_start  TIMESTAMP WITH TIME ZONE PRIMARY KEY,
    total_customs BIGINT                   NOT NULL,
    generated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
--rollback DROP TABLE customs_report;
