-- Normalize legacy MCP seed data that stored JSON object text with escaped quotes.
UPDATE mcp_server
SET protocol_config = (($$"$$ || protocol_config || $$"$$)::jsonb #>> '{}')
WHERE protocol_config IS NOT NULL
  AND (left(ltrim(protocol_config), 3) = E'{\\\\\"'
    OR left(ltrim(protocol_config), 3) = E'[\\\\\"');
