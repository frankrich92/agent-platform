ALTER TABLE model_config
    ADD COLUMN IF NOT EXISTS connectivity_status varchar(32) NOT NULL DEFAULT 'NOT_CHECKED',
    ADD COLUMN IF NOT EXISTS connectivity_message varchar(500) NULL,
    ADD COLUMN IF NOT EXISTS last_connectivity_check timestamp NULL;

CREATE TABLE IF NOT EXISTS skill_file (
    id bigint NOT NULL,
    skill_id bigint NOT NULL,
    file_type varchar(32) NOT NULL,
    file_name varchar(255) NOT NULL,
    file_path varchar(1000) NOT NULL,
    content text NULL,
    sort int NOT NULL DEFAULT 0,
    enabled boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by bigint NULL DEFAULT NULL,
    updated_by bigint NULL DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_skill_file_skill ON skill_file(skill_id);
CREATE INDEX IF NOT EXISTS idx_skill_file_type ON skill_file(file_type);
CREATE INDEX IF NOT EXISTS idx_skill_file_path ON skill_file(skill_id, file_path);

INSERT INTO params (id, param_name, param_key, param_value)
VALUES (7, '技能包文件允许入库的扩展名', 'SKILL_FILE_ALLOWED_EXTENSIONS',
        'md,py,sh,js,ts,json,yaml,yml,xml,txt,java,cs,go,rs,rb,php,sql,html,css,scss,less,cfg,conf,toml')
ON CONFLICT (id) DO UPDATE SET
    param_name = EXCLUDED.param_name,
    param_key = EXCLUDED.param_key,
    param_value = EXCLUDED.param_value;

WITH skill_md_rows AS (
    SELECT
        (floor(extract(epoch FROM clock_timestamp()) * 1000)::bigint * 10000
            + row_number() OVER (ORDER BY sp.id)) AS id,
        sp.id AS skill_id,
        'SKILL_MD' AS file_type,
        'SKILL.md' AS file_name,
        'SKILL.md' AS file_path,
        CASE
            WHEN sp.skill_content IS NULL OR sp.skill_content = '' THEN
                '---' || chr(10)
                || 'name: ' || coalesce(sp.name, '') || chr(10)
                || 'description: ' || coalesce(sp.description, '') || chr(10)
                || '---' || chr(10)
            ELSE sp.skill_content
        END AS content,
        0 AS sort,
        sp.enabled,
        sp.created_at,
        sp.updated_at,
        sp.created_by,
        sp.updated_by
    FROM skill_package sp
    WHERE NOT EXISTS (
        SELECT 1 FROM skill_file sf WHERE sf.skill_id = sp.id AND sf.file_path = 'SKILL.md'
    )
)
INSERT INTO skill_file (id, skill_id, file_type, file_name, file_path, content, sort,
                        enabled, created_at, updated_at, created_by, updated_by)
SELECT id, skill_id, file_type, file_name, file_path, content, sort,
       enabled, created_at, updated_at, created_by, updated_by
FROM skill_md_rows;

WITH legacy_resources AS (
    SELECT sp.*, 'REFERENCES'::varchar AS file_type, 'references'::varchar AS prefix, item, ordinality
    FROM skill_package sp
         CROSS JOIN LATERAL jsonb_array_elements(
            CASE WHEN sp."references" IS NOT NULL AND btrim(sp."references") LIKE '[%'
                THEN sp."references"::jsonb ELSE '[]'::jsonb END
         ) WITH ORDINALITY AS resource(item, ordinality)
    UNION ALL
    SELECT sp.*, 'EXAMPLES'::varchar AS file_type, 'examples'::varchar AS prefix, item, ordinality
    FROM skill_package sp
         CROSS JOIN LATERAL jsonb_array_elements(
            CASE WHEN sp.examples IS NOT NULL AND btrim(sp.examples) LIKE '[%'
                THEN sp.examples::jsonb ELSE '[]'::jsonb END
         ) WITH ORDINALITY AS resource(item, ordinality)
    UNION ALL
    SELECT sp.*, 'SCRIPTS'::varchar AS file_type, 'scripts'::varchar AS prefix, item, ordinality
    FROM skill_package sp
         CROSS JOIN LATERAL jsonb_array_elements(
            CASE WHEN sp.scripts IS NOT NULL AND btrim(sp.scripts) LIKE '[%'
                THEN sp.scripts::jsonb ELSE '[]'::jsonb END
         ) WITH ORDINALITY AS resource(item, ordinality)
),
normalized AS (
    SELECT
        (floor(extract(epoch FROM clock_timestamp()) * 1000)::bigint * 10000
            + 100000
            + row_number() OVER (ORDER BY id, file_type, ordinality)) AS id,
        id AS skill_id,
        file_type,
        coalesce(item ->> 'name', 'unnamed') AS file_name,
        prefix || '/' || coalesce(item ->> 'name', 'unnamed') AS file_path,
        coalesce(item ->> 'content', '') AS content,
        ordinality::int AS sort,
        enabled,
        created_at,
        updated_at,
        created_by,
        updated_by
    FROM legacy_resources
)
INSERT INTO skill_file (id, skill_id, file_type, file_name, file_path, content, sort,
                        enabled, created_at, updated_at, created_by, updated_by)
SELECT id, skill_id, file_type, file_name, file_path, content, sort,
       enabled, created_at, updated_at, created_by, updated_by
FROM normalized n
WHERE NOT EXISTS (
    SELECT 1 FROM skill_file sf WHERE sf.skill_id = n.skill_id AND sf.file_path = n.file_path
);
