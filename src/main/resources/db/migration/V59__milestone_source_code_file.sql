-- Allow milestone source code to be handed off by repository URL, ZIP archive, or both.

ALTER TABLE deliverables
    ADD COLUMN IF NOT EXISTS source_code_file_url TEXT;

ALTER TABLE milestone_progress_reports
    ADD COLUMN IF NOT EXISTS source_code_file_url TEXT;
