-- study_posts 저장 공간 측정 (인덱스 추가 전/후에 각각 실행해 비교)
-- information_schema.TABLES.INDEX_LENGTH에는 FULLTEXT 인덱스가 포함되지 않음
-- FULLTEXT 인덱스는 InnoDB 보조 테이블(fts_<table_id>_...)에 저장되므로 따로 합산함
-- 실행: mysql -u<user> -p studycrewdb < db/manual/02_measure_study_posts_storage.sql

-- 0. 통계 갱신 및 FULLTEXT 보조 테이블 정리 (정확한 측정을 위해)
ANALYZE TABLE study_posts;
SET GLOBAL innodb_optimize_fulltext_only = ON;
OPTIMIZE TABLE study_posts;
SET GLOBAL innodb_optimize_fulltext_only = OFF;

-- 1. 테이블 데이터 / 일반 인덱스 크기 (FULLTEXT 제외)
SELECT table_name,
       table_rows,
       ROUND(data_length / 1024, 1)  AS data_kb,
       ROUND(index_length / 1024, 1) AS btree_index_kb
FROM information_schema.TABLES
WHERE table_schema = DATABASE()
  AND table_name = 'study_posts';

-- 2. FULLTEXT 보조 테이블별 크기
--    fts_<table_id>_<index_id>_index_N : 인덱스별 테이블 (FULLTEXT 인덱스 1개당 6개)
--    fts_<table_id>_being_deleted 등   : 테이블 공통 테이블
SELECT ts.name,
       ROUND(ts.file_size / 1024, 1)      AS file_kb,
       ROUND(ts.allocated_size / 1024, 1) AS allocated_kb
FROM information_schema.INNODB_TABLESPACES ts
JOIN information_schema.INNODB_TABLES t
  ON t.name = CONCAT(DATABASE(), '/study_posts')
WHERE ts.name LIKE CONCAT(DATABASE(), '/fts_', LPAD(LOWER(HEX(t.table_id)), 16, '0'), '%')
ORDER BY ts.name;

-- 3. 요약: 데이터 / B-Tree 인덱스 / FULLTEXT 인덱스 / 전체 인덱스
SELECT ROUND(tb.data_length / 1024, 1)                       AS data_kb,
       ROUND(tb.index_length / 1024, 1)                      AS btree_index_kb,
       ROUND(fts.fulltext_bytes / 1024, 1)                   AS fulltext_index_kb,
       ROUND((tb.index_length + fts.fulltext_bytes) / 1024, 1) AS total_index_kb,
       ROUND((tb.data_length + tb.index_length + fts.fulltext_bytes) / 1024, 1) AS total_kb
FROM information_schema.TABLES tb
CROSS JOIN (
    SELECT COALESCE(SUM(ts.file_size), 0) AS fulltext_bytes
    FROM information_schema.INNODB_TABLESPACES ts
    JOIN information_schema.INNODB_TABLES t
      ON t.name = CONCAT(DATABASE(), '/study_posts')
    WHERE ts.name LIKE CONCAT(DATABASE(), '/fts_', LPAD(LOWER(HEX(t.table_id)), 16, '0'), '%')
) fts
WHERE tb.table_schema = DATABASE()
  AND tb.table_name = 'study_posts';

-- 증가량 = (인덱스 추가 후 total_index_kb) - (추가 전 total_index_kb)
