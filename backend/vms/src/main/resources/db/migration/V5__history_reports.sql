ALTER TABLE organization ADD COLUMN timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Kolkata';

CREATE INDEX idx_visit_org_checkin ON visit(org_id, check_in_time);
