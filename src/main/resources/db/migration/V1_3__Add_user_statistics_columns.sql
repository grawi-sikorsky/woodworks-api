ALTER TABLE base_user
    ADD COLUMN generation_request_count BIGINT DEFAULT 0 NOT NULL,
    ADD COLUMN project_count INTEGER DEFAULT 0 NOT NULL,
    ADD COLUMN max_projects INTEGER DEFAULT 5 NOT NULL,
    ADD COLUMN max_cabinets_per_project INTEGER DEFAULT 10 NOT NULL;
