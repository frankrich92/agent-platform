-- V2 kept a compatibility no-op for some escaped rows; repair them with an unambiguous prefix check.
UPDATE mcp_server
SET protocol_config = (($$"$$ || protocol_config || $$"$$)::jsonb #>> '{}')
WHERE protocol_config IS NOT NULL
  AND (left(ltrim(protocol_config), 3) = '{' || chr(92) || chr(34)
    OR left(ltrim(protocol_config), 3) = '[' || chr(92) || chr(34));
