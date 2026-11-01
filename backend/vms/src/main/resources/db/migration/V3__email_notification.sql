CREATE TABLE email_log (
    email_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    org_id      BIGINT NOT NULL REFERENCES organization(org_id),
    visit_id    BIGINT NOT NULL REFERENCES visit(visit_id),
    type        VARCHAR(20) NOT NULL CHECK (type IN ('PASS', 'INVITATION', 'REJECTION')),
    recipient   VARCHAR(150) NOT NULL,
    status      VARCHAR(10) NOT NULL CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    attempts    INTEGER NOT NULL DEFAULT 0,
    last_error  VARCHAR(500),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    sent_at     TIMESTAMPTZ
);

CREATE TABLE notification (
    notification_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    org_id          BIGINT NOT NULL REFERENCES organization(org_id),
    type            VARCHAR(20) NOT NULL CHECK (type IN ('NEW_REQUEST', 'OVERDUE')),
    message         VARCHAR(500) NOT NULL,
    visit_id        BIGINT REFERENCES visit(visit_id),
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_email_log_org_status ON email_log(org_id, status);
CREATE INDEX idx_notification_org_read_created ON notification(org_id, is_read, created_at);
