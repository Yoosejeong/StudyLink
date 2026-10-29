-- 제목+본문 FULLTEXT 인덱스 추가 (로컬/테스트 DB 전용, 운영 DB에 직접 실행 금지)
-- 프로젝트에 Flyway/Liquibase가 없어 수동 실행용으로 작성
-- 기존 ft_study_posts_title 인덱스는 유지하며, 이미 존재하면 아무 작업도 하지 않음
-- 실행: mysql -u<user> -p studycrewdb < db/manual/01_add_ft_study_posts_title_content.sql

SET @index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE table_schema = DATABASE()
      AND table_name = 'study_posts'
      AND index_name = 'ft_study_posts_title_content'
);

SET @ddl := IF(
    @index_exists = 0,
    'ALTER TABLE study_posts ADD FULLTEXT INDEX ft_study_posts_title_content (title, content) WITH PARSER ngram',
    'SELECT ''ft_study_posts_title_content already exists'' AS message'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SHOW INDEX FROM study_posts WHERE Index_type = 'FULLTEXT';
