-- Runtime treats missing tool confirmation policy as false; normalize existing rows.
UPDATE tool_config
SET need_confirm = false
WHERE need_confirm IS NULL;
