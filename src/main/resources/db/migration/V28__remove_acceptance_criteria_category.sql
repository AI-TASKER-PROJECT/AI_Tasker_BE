-- NOTE FILE: src/main/resources/db/migration/V28__remove_acceptance_criteria_category.sql
-- Đây là file gì: Migration xóa cột category khỏi bảng acceptance_criteria vì hệ thống không còn cần phân nhóm tiêu chí nghiệm thu.
-- Mục đích note: mô tả vai trò migration, không note rải vào từng câu SQL.

ALTER TABLE acceptance_criteria DROP COLUMN IF EXISTS category;
