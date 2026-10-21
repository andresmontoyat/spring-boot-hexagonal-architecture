--liquibase formatted sql

--changeset andresmontoyat:20261020120000-create-custom-table
CREATE TABLE custom (
    id      UUID         PRIMARY KEY,
    name    VARCHAR(20)  NOT NULL,
    version BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_custom_name ON custom (name);
--rollback DROP TABLE custom;
