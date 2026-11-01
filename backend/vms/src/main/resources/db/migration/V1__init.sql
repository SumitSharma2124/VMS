CREATE SEQUENCE visit_code_seq START 1;

CREATE TABLE organization (
    org_id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    slug           VARCHAR(40)  NOT NULL UNIQUE,      -- public id used in the QR URL
    name           VARCHAR(150) NOT NULL,
    address        VARCHAR(255),
    contact_number VARCHAR(20),
    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE admin (
    admin_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    org_id        BIGINT NOT NULL REFERENCES organization(org_id),
    admin_name    VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    phone_number  VARCHAR(20),
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE guard (
    guard_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    org_id        BIGINT NOT NULL REFERENCES organization(org_id),
    created_by    BIGINT REFERENCES admin(admin_id),
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    phone_number  VARCHAR(20),
    status        VARCHAR(10) NOT NULL DEFAULT 'ACTIVE'
                  CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE registration_qr (
    qr_id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    org_id        BIGINT NOT NULL REFERENCES organization(org_id),
    qr_code_value VARCHAR(255) NOT NULL UNIQUE,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    generated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE visitor (
    visitor_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    org_id         BIGINT NOT NULL REFERENCES organization(org_id),
    full_name      VARCHAR(100) NOT NULL,
    email          VARCHAR(150) NOT NULL,
    contact_number VARCHAR(20)  NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (org_id, email)                 -- visitor data is isolated per org
);

CREATE TABLE visit (
    visit_id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    org_id              BIGINT NOT NULL REFERENCES organization(org_id),
    visit_code          VARCHAR(20) UNIQUE,
    visitor_id          BIGINT NOT NULL REFERENCES visitor(visitor_id),
    host_name           VARCHAR(100) NOT NULL,
    purpose             VARCHAR(255) NOT NULL,
    status              VARCHAR(15) NOT NULL DEFAULT 'REQUESTED'
                        CHECK (status IN ('REQUESTED','APPROVED','REJECTED',
                                          'CHECKED_IN','OVERDUE','CHECKED_OUT')),
    requested_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    approved_by         BIGINT REFERENCES admin(admin_id),
    approved_at         TIMESTAMPTZ,
    expected_end_time   TIMESTAMPTZ,
    check_in_time       TIMESTAMPTZ,
    check_in_guard_id   BIGINT REFERENCES guard(guard_id),
    check_out_time      TIMESTAMPTZ,
    check_out_guard_id  BIGINT REFERENCES guard(guard_id),
    photo_url           VARCHAR(255)
);

CREATE TABLE visitor_pass_qr (
    qr_id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    visit_id      BIGINT NOT NULL UNIQUE REFERENCES visit(visit_id),
    qr_code_value VARCHAR(64) NOT NULL UNIQUE,
    is_valid      BOOLEAN NOT NULL DEFAULT TRUE,
    generated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE vehicle (
    vehicle_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    visit_id       BIGINT NOT NULL UNIQUE REFERENCES visit(visit_id),
    vehicle_number VARCHAR(20) NOT NULL,
    vehicle_type   VARCHAR(30)
);

CREATE INDEX idx_admin_org          ON admin(org_id);
CREATE INDEX idx_guard_org          ON guard(org_id);
CREATE INDEX idx_visit_org_status   ON visit(org_id, status);
CREATE INDEX idx_visit_org_requested ON visit(org_id, requested_at);
CREATE INDEX idx_visit_active       ON visit(expected_end_time)
    WHERE status IN ('CHECKED_IN', 'OVERDUE');