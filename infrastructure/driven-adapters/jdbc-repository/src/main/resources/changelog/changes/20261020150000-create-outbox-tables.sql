--liquibase formatted sql

-- Copied from io.codehunters:sdk-event-messaging-jdbc:9.1.0
-- (db/migration/outbox/postgresql/outbox_message_create.sql). Owned by this service from now on:
-- the SDK never runs DDL, and including the file from the jar would change checksums on upgrade.
-- seq = insertion order; the claim publishes a key's rows in seq order. send_started_at and
-- traceparent are required by the 9.x adapter.

--changeset andresmontoyat:20261020150000-create-outbox-message
CREATE TABLE outbox_message (
    id UUID PRIMARY KEY,
    seq BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL UNIQUE,
    origin VARCHAR(255) NOT NULL,
    routing_key VARCHAR(255),
    topic VARCHAR(255) NOT NULL,
    message_key VARCHAR(255),
    payload TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    error VARCHAR(512),
    occurred_at TIMESTAMP NOT NULL,
    next_attempt_at TIMESTAMP,
    published_at TIMESTAMP,
    claimed_at TIMESTAMP,
    claimed_by VARCHAR(255),
    send_started_at TIMESTAMP,
    traceparent VARCHAR(55)
);
--rollback DROP TABLE outbox_message;

--changeset andresmontoyat:20261020150000-create-outbox-message-indexes
CREATE INDEX idx_outbox_claimable ON outbox_message (origin, seq)
    WHERE status IN ('PENDING', 'IN_FLIGHT');
CREATE INDEX idx_outbox_key_order ON outbox_message (origin, topic, message_key, seq)
    WHERE status IN ('PENDING', 'IN_FLIGHT', 'FAILED');
CREATE INDEX idx_outbox_published ON outbox_message (published_at)
    WHERE status = 'PUBLISHED';
--rollback DROP INDEX idx_outbox_published;
--rollback DROP INDEX idx_outbox_key_order;
--rollback DROP INDEX idx_outbox_claimable;

--changeset andresmontoyat:20261020150000-create-shedlock
CREATE TABLE shedlock (
    name VARCHAR(64) PRIMARY KEY,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL
);
--rollback DROP TABLE shedlock;
