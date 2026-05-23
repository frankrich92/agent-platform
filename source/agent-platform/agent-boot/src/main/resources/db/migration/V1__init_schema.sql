-- PostgreSQL schema for agent_platform.
-- Database creation is managed outside Flyway.


-- ----------------------------
-- 创建数据库（不存在则创建）
-- ----------------------------

-- ----------------------------
-- Table structure for account
-- ----------------------------
DROP TABLE IF EXISTS account;
CREATE TABLE account  (
id bigint NOT NULL,
nickname varchar(10) NULL DEFAULT NULL,
email varchar(100) NULL DEFAULT NULL,
username varchar(40) NULL DEFAULT NULL,
password varchar(100) NULL DEFAULT NULL,
enabled boolean NOT NULL DEFAULT true,
created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by bigint NULL DEFAULT NULL,
updated_by bigint NULL DEFAULT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_email UNIQUE (email),
CONSTRAINT uk_username UNIQUE (username)
);

INSERT INTO account (id, nickname, email, username, password, enabled, created_at, updated_at, created_by, updated_by) VALUES (1111111111111111111, '管理员', 'admin@gmail.com', 'admin', '277fc0217db5d364b3b886a9672ea9d3', true, '2026-02-07 18:50:51', '2026-02-12 22:26:50', NULL, 1111111111111111111);

-- ----------------------------
-- Table structure for account_role
-- ----------------------------
DROP TABLE IF EXISTS account_role;
CREATE TABLE account_role  (
id bigint NOT NULL,
account_id bigint NOT NULL,
role varchar(50) NOT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_account_role UNIQUE (account_id, role)
);

INSERT INTO account_role (id, account_id, role) VALUES (1111111111111111111, 1111111111111111111, 'ADMIN');

-- ----------------------------
-- Table structure for agent_a2a
-- ----------------------------
DROP TABLE IF EXISTS agent_a2a;
CREATE TABLE agent_a2a  (
id bigint NOT NULL,
agent_definition_id bigint NOT NULL,
a2a_type varchar(40) NOT NULL,
a2a_config text NOT NULL,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for agent_chat_key
-- ----------------------------
DROP TABLE IF EXISTS agent_chat_key;
CREATE TABLE agent_chat_key  (
agent_code varchar(100) NOT NULL,
chat_key varchar(100) NOT NULL,
CONSTRAINT uniq_agent_code_chat_key UNIQUE (agent_code, chat_key)
);

-- ----------------------------
-- Table structure for agent_code_execution
-- ----------------------------
DROP TABLE IF EXISTS agent_code_execution;
CREATE TABLE agent_code_execution  (
id bigint NOT NULL,
agent_definition_id bigint NOT NULL,
code_execution_id bigint NOT NULL,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for agent_definition
-- ----------------------------
DROP TABLE IF EXISTS agent_definition;
CREATE TABLE agent_definition  (
id bigint NOT NULL,
agent_type varchar(100) NULL DEFAULT NULL,
name varchar(100) NOT NULL,
agent_code varchar(100) NOT NULL,
description text NULL,
model_config_id bigint NULL DEFAULT NULL,
model_params_override text NULL,
tool_choice_strategy varchar(50) NULL DEFAULT 'AUTO',
specific_tool_name varchar(100) NULL DEFAULT NULL,
system_prompt_template_id bigint NULL DEFAULT NULL,
follow_template boolean NOT NULL DEFAULT true,
system_prompt text NULL,
sensitive_word_config_id bigint NULL DEFAULT NULL,
sensitive_filter_enabled boolean NOT NULL DEFAULT true,
max_iterations int NULL DEFAULT 10,
enable_planning boolean NOT NULL DEFAULT false,
show_tool_process boolean NOT NULL DEFAULT true,
max_subtasks int NULL DEFAULT 10,
require_plan_confirmation boolean NOT NULL DEFAULT false,
enable_memory boolean NOT NULL DEFAULT false,
enable_memory_compression boolean NOT NULL DEFAULT false,
memory_compression_config text NULL,
structured_output_enabled boolean NOT NULL DEFAULT false,
structured_output_reminder varchar(50) NULL DEFAULT NULL,
structured_output_schema text NULL,
enabled boolean NOT NULL DEFAULT true,
version varchar(20) NULL DEFAULT '1.0.0',
tag varchar(100) NULL DEFAULT NULL,
created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by bigint NULL DEFAULT NULL,
updated_by bigint NULL DEFAULT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_agent_code UNIQUE (agent_code),
CONSTRAINT uk_agent_name UNIQUE (name)
);

-- ----------------------------
-- Table structure for agent_hooks
-- ----------------------------
DROP TABLE IF EXISTS agent_hooks;
CREATE TABLE agent_hooks  (
id bigint NOT NULL,
agent_definition_id bigint NOT NULL,
hook_config_id bigint NOT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_agent_hook UNIQUE (agent_definition_id, hook_config_id)
);

-- ----------------------------
-- Table structure for agent_knowledge_bases
-- ----------------------------
DROP TABLE IF EXISTS agent_knowledge_bases;
CREATE TABLE agent_knowledge_bases  (
id bigint NOT NULL,
agent_definition_id bigint NOT NULL,
knowledge_base_config_id bigint NOT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_agent_kb UNIQUE (agent_definition_id, knowledge_base_config_id)
);

-- ----------------------------
-- Table structure for agent_mcp_servers
-- ----------------------------
DROP TABLE IF EXISTS agent_mcp_servers;
CREATE TABLE agent_mcp_servers  (
id bigint NOT NULL,
agent_definition_id bigint NOT NULL,
mcp_server_id bigint NOT NULL,
exposure_mode varchar(50) NOT NULL DEFAULT 'ALL_GLOBAL',
PRIMARY KEY (id),
CONSTRAINT uk_agent_mcp UNIQUE (agent_definition_id, mcp_server_id)
);

-- ----------------------------
-- Table structure for agent_mcp_tool
-- ----------------------------
DROP TABLE IF EXISTS agent_mcp_tool;
CREATE TABLE agent_mcp_tool  (
id bigint NOT NULL,
agent_definition_id bigint NOT NULL,
mcp_tool_id bigint NOT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_agent_mcp_tool UNIQUE (agent_definition_id, mcp_tool_id)
);

-- ----------------------------
-- Table structure for agent_skill_packages
-- ----------------------------
DROP TABLE IF EXISTS agent_skill_packages;
CREATE TABLE agent_skill_packages  (
id bigint NOT NULL,
agent_definition_id bigint NOT NULL,
skill_package_id bigint NOT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_agent_skill UNIQUE (agent_definition_id, skill_package_id)
);

-- ----------------------------
-- Table structure for agent_studio
-- ----------------------------
DROP TABLE IF EXISTS agent_studio;
CREATE TABLE agent_studio  (
id bigint NOT NULL,
agent_definition_id bigint NOT NULL,
studio_id bigint NOT NULL,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for agent_sub_agents
-- ----------------------------
DROP TABLE IF EXISTS agent_sub_agents;
CREATE TABLE agent_sub_agents  (
id bigint NOT NULL,
parent_agent_id bigint NOT NULL,
sub_agent_id bigint NOT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_parent_sub_agent UNIQUE (parent_agent_id, sub_agent_id)
);

-- ----------------------------
-- Table structure for agent_tools
-- ----------------------------
DROP TABLE IF EXISTS agent_tools;
CREATE TABLE agent_tools  (
id bigint NOT NULL,
agent_definition_id bigint NOT NULL,
tool_id bigint NOT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_agent_tool UNIQUE (agent_definition_id, tool_id)
);

-- ----------------------------
-- Table structure for agentscope_sessions
-- ----------------------------
DROP TABLE IF EXISTS agentscope_sessions;
CREATE TABLE agentscope_sessions  (
session_id varchar(255) NOT NULL,
state_key varchar(255) NOT NULL,
item_index int NOT NULL DEFAULT 0,
state_data text NOT NULL,
created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (session_id, state_key, item_index)
);

-- ----------------------------
-- Table structure for attach
-- ----------------------------
DROP TABLE IF EXISTS attach;
CREATE TABLE attach  (
id bigint NOT NULL,
file_id bigint NULL DEFAULT NULL,
link varchar(1000) NULL DEFAULT NULL,
domain varchar(500) NULL DEFAULT NULL,
name varchar(500) NULL DEFAULT NULL,
original_name varchar(500) NULL DEFAULT NULL,
extension varchar(12) NULL DEFAULT NULL,
attach_size bigint NULL DEFAULT NULL,
path varchar(255) NULL DEFAULT NULL,
create_by bigint NULL DEFAULT NULL,
create_at timestamp NULL DEFAULT NULL,
update_by bigint NULL DEFAULT NULL,
update_at timestamp NULL DEFAULT NULL,
protocol varchar(40) NULL DEFAULT NULL,
status int NULL DEFAULT NULL,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for attach_chunk
-- ----------------------------
DROP TABLE IF EXISTS attach_chunk;
CREATE TABLE attach_chunk  (
id bigint NOT NULL,
chunk_hash varchar(40) NULL DEFAULT NULL,
chunk_index int NULL DEFAULT NULL,
chunk_totals int NULL DEFAULT NULL,
file_key varchar(40) NULL DEFAULT NULL,
file_total_size int NULL DEFAULT NULL,
file_name varchar(255) NULL DEFAULT NULL,
create_by bigint NULL DEFAULT NULL,
create_at timestamp NULL DEFAULT NULL,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for attach_log
-- ----------------------------
DROP TABLE IF EXISTS attach_log;
CREATE TABLE attach_log  (
id bigint NOT NULL,
file_id bigint NULL DEFAULT NULL,
original_name varchar(500) NULL DEFAULT NULL,
extension varchar(12) NULL DEFAULT NULL,
attach_size bigint NULL DEFAULT NULL,
opt_user bigint NULL DEFAULT NULL,
opt_user_name varchar(40) NULL DEFAULT NULL,
opt_time timestamp NULL DEFAULT NULL,
opt_ip varchar(20) NULL DEFAULT NULL,
opt_type varchar(10) NULL DEFAULT NULL,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS chat_message;
CREATE TABLE chat_message  (
id integer GENERATED BY DEFAULT AS IDENTITY,
session_id bigint NOT NULL,
role varchar(20) NOT NULL,
content text NOT NULL,
parent_id int NULL DEFAULT NULL,
path text NULL DEFAULT NULL,
depth int NULL DEFAULT NULL,
created_at timestamp NULL DEFAULT NULL,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for chat_session
-- ----------------------------
DROP TABLE IF EXISTS chat_session;
CREATE TABLE chat_session  (
id bigint NOT NULL,
user_id bigint NOT NULL,
agent_id bigint NOT NULL,
current_message_id int NULL DEFAULT NULL,
title varchar(255) NULL DEFAULT NULL,
is_pinned boolean NULL DEFAULT false,
pin_time timestamp NULL DEFAULT NULL,
created_at timestamp NULL DEFAULT NULL,
updated_at timestamp NULL DEFAULT NULL,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for code_execution_config
-- ----------------------------
DROP TABLE IF EXISTS code_execution_config;
CREATE TABLE code_execution_config  (
id bigint NOT NULL,
config_name varchar(128) NOT NULL,
work_dir varchar(512) NULL DEFAULT NULL,
upload_dir varchar(512) NULL DEFAULT NULL,
auto_upload boolean NOT NULL DEFAULT false,
enable_shell boolean NOT NULL DEFAULT true,
enable_read boolean NOT NULL DEFAULT false,
enable_write boolean NOT NULL DEFAULT false,
command varchar(300) NOT NULL,
enabled boolean NOT NULL DEFAULT true,
created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by bigint NULL DEFAULT NULL,
updated_by bigint NULL DEFAULT NULL,
PRIMARY KEY (id),
CONSTRAINT config_name UNIQUE (config_name)
);

-- ----------------------------
-- Table structure for hook_config
-- ----------------------------
DROP TABLE IF EXISTS hook_config;
CREATE TABLE hook_config  (
id bigint NOT NULL,
name varchar(100) NOT NULL,
hook_type varchar(50) NOT NULL,
description varchar(500) NULL DEFAULT NULL,
class_path varchar(255) NULL DEFAULT NULL,
code text NULL,
enabled boolean NOT NULL DEFAULT true,
priority int NOT NULL DEFAULT 0,
created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by bigint NULL DEFAULT NULL,
updated_by bigint NULL DEFAULT NULL,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for knowledge_base_config
-- ----------------------------
DROP TABLE IF EXISTS knowledge_base_config;
CREATE TABLE knowledge_base_config  (
id bigint NOT NULL,
name varchar(100) NOT NULL,
kb_type varchar(50) NOT NULL,
rag_mode varchar(50) NOT NULL DEFAULT 'GENERIC',
description varchar(500) NULL DEFAULT NULL,
connection_config text NOT NULL,
endpoint_config text NULL,
retrieval_config text NULL,
reranking_config text NULL,
query_rewrite_config text NULL,
metadata_filters text NULL,
http_config text NULL,
enabled boolean NOT NULL DEFAULT true,
health_status varchar(50) NULL DEFAULT 'UNKNOWN',
last_sync_time timestamp NULL DEFAULT NULL,
created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by bigint NULL DEFAULT NULL,
updated_by bigint NULL DEFAULT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_kb_name UNIQUE (name)
);

-- ----------------------------
-- Table structure for mcp_server
-- ----------------------------
DROP TABLE IF EXISTS mcp_server;
CREATE TABLE mcp_server  (
id bigint NOT NULL,
name varchar(100) NOT NULL,
protocol varchar(50) NOT NULL,
enabled boolean NOT NULL DEFAULT true,
mode varchar(50) NOT NULL DEFAULT 'SYNC',
timeout int NULL DEFAULT 30,
protocol_config text NULL,
description varchar(500) NULL DEFAULT NULL,
tool_schemas text NULL,
activation_status varchar(50) NOT NULL DEFAULT 'NOT_ACTIVATED',
activation_message varchar(500) NULL DEFAULT NULL,
failure_source varchar(50) NOT NULL DEFAULT 'NONE',
activation_status_changed_at timestamp NULL DEFAULT NULL,
last_activation_time timestamp NULL DEFAULT NULL,
last_tool_sync_time timestamp NULL DEFAULT NULL,
tool_count int NOT NULL DEFAULT 0,
runtime_fail_threshold int NOT NULL DEFAULT 3,
activation_revision bigint NOT NULL DEFAULT 0,
config_hash varchar(64) NULL DEFAULT NULL,
needs_sync boolean NOT NULL DEFAULT true,
activation_request_id varchar(64) NULL DEFAULT NULL,
health_status varchar(50) NULL DEFAULT 'UNKNOWN',
last_health_check timestamp NULL DEFAULT NULL,
created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by bigint NULL DEFAULT NULL,
updated_by bigint NULL DEFAULT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_mcp_name UNIQUE (name)
);

INSERT INTO mcp_server (id, name, protocol, enabled, mode, timeout, protocol_config, description, health_status, last_health_check, created_at, updated_at, created_by, updated_by) VALUES (2024821058360176641, 'lbs-amap-http-mcp', 'HTTP', true, 'SYNC', 30000, '{\"url\":\"https://mcp.amap.com/mcp\",\"queryParams\":[{\"key\":\"key\",\"value\":\"yourself key\"}],\"headers\":[]}', '高度地图 HTTP MCP', 'UNKNOWN', NULL, '2026-02-20 20:18:54', '2026-02-20 23:52:37', 1111111111111111111, 1111111111111111111);
INSERT INTO mcp_server (id, name, protocol, enabled, mode, timeout, protocol_config, description, health_status, last_health_check, created_at, updated_at, created_by, updated_by) VALUES (2024825210448453633, 'lbs-amap-sse-mcp', 'SSE', true, 'SYNC', 30000, '{\"url\":\"https://mcp.amap.com/sse\",\"queryParams\":[{\"key\":\"key\",\"value\":\"yourself key\"}],\"headers\":[]}', '高度地图 SSE MCP', 'UNKNOWN', NULL, '2026-02-20 20:35:24', '2026-02-20 23:52:51', 1111111111111111111, 1111111111111111111);
INSERT INTO mcp_server (id, name, protocol, enabled, mode, timeout, protocol_config, description, health_status, last_health_check, created_at, updated_at, created_by, updated_by) VALUES (2024829036727439361, 'daidu-map-http-mcp', 'HTTP', false, 'SYNC', 30000, '{\"url\":\"https://mcp.map.baidu.com/mcp\",\"queryParams\":[{\"key\":\"ak\",\"value\":\"yourself key\"}],\"headers\":[]}', '百度地图 HTTP MCP', 'UNKNOWN', NULL, '2026-02-20 20:50:37', '2026-02-22 00:11:27', 1111111111111111111, 1111111111111111111);
INSERT INTO mcp_server (id, name, protocol, enabled, mode, timeout, protocol_config, description, health_status, last_health_check, created_at, updated_at, created_by, updated_by) VALUES (2024829212489748481, 'baidu-amp-sse-mcp', 'SSE', false, 'SYNC', 30000, '{\"url\":\"https://mcp.map.baidu.com/sse\",\"queryParams\":[{\"key\":\"ak\",\"value\":\"yourself key\"}],\"headers\":[]}', '百度地图 SSE MCP', 'UNKNOWN', NULL, '2026-02-20 20:51:18', '2026-02-22 00:11:32', 1111111111111111111, 1111111111111111111);
INSERT INTO mcp_server (id, name, protocol, enabled, mode, timeout, protocol_config, description, health_status, last_health_check, created_at, updated_at, created_by, updated_by) VALUES (2024830349192269825, 'baidu-maps', 'STDIO', true, 'SYNC', 30000, '{\"command\":\"D:\\\\\\\\environment\\\\\\\\nvm\\\\\\\\nodejs\\\\\\\\npx.cmd\",\"args\":[\"-y\",\"@baidumap/mcp-server-baidu-map\"],\"env\":[{\"key\":\"BAIDU_MAP_API_KEY\",\"value\":\"yourself key\"}],\"encoding\":\"UTF-8\"}', '百度地图 STDIO MCP', 'UNKNOWN', NULL, '2026-02-20 20:55:49', '2026-02-20 21:02:07', 1111111111111111111, 1111111111111111111);

-- ----------------------------
-- Table structure for model_config
-- ----------------------------
DROP TABLE IF EXISTS model_config;
CREATE TABLE model_config  (
id bigint NOT NULL,
provider_id bigint NOT NULL,
name varchar(100) NOT NULL,
model_id varchar(100) NOT NULL,
model_type varchar(100) NULL DEFAULT NULL,
description varchar(500) NULL DEFAULT NULL,
streaming boolean NOT NULL DEFAULT true,
thinking boolean NULL DEFAULT NULL,
context_window int NULL DEFAULT 2048,
max_tokens int NULL DEFAULT 2000,
temperature decimal(3, 2) NULL DEFAULT 0.70,
top_p decimal(3, 2) NULL DEFAULT 0.90,
top_k int NULL DEFAULT 40,
repeat_penalty decimal(3, 2) NULL DEFAULT 1.10,
seed bigint NULL DEFAULT 42,
extend_config text NULL,
enabled boolean NOT NULL DEFAULT true,
created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by bigint NULL DEFAULT NULL,
updated_by bigint NULL DEFAULT NULL,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for model_provider
-- ----------------------------
DROP TABLE IF EXISTS model_provider;
CREATE TABLE model_provider  (
id bigint NOT NULL,
type varchar(50) NOT NULL,
name varchar(100) NOT NULL,
description varchar(500) NULL DEFAULT NULL,
base_url varchar(500) NULL DEFAULT NULL,
auth_type varchar(50) NOT NULL DEFAULT 'CONFIG',
api_key varchar(500) NULL DEFAULT NULL,
env_var_name varchar(100) NULL DEFAULT NULL,
enabled boolean NOT NULL DEFAULT true,
config_meta text NULL,
created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by bigint NULL DEFAULT NULL,
updated_by bigint NULL DEFAULT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_provider_name UNIQUE (name)
);

-- ----------------------------
-- Table structure for params
-- ----------------------------
DROP TABLE IF EXISTS params;
CREATE TABLE params  (
id bigint NOT NULL,
param_name varchar(40) NULL DEFAULT NULL,
param_key varchar(40) NULL DEFAULT NULL,
param_value varchar(255) NULL DEFAULT NULL,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for quartz_job_info
-- ----------------------------
DROP TABLE IF EXISTS quartz_job_info;
CREATE TABLE quartz_job_info  (
id varchar(64) NOT NULL,
type varchar(100) NULL DEFAULT NULL,
biz_id varchar(64) NULL DEFAULT NULL,
cron varchar(64) NULL DEFAULT NULL,
job_class varchar(100) NULL DEFAULT NULL,
data_map text NULL,
enabled boolean NULL DEFAULT NULL,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for quartz_job_log
-- ----------------------------
DROP TABLE IF EXISTS quartz_job_log;
CREATE TABLE quartz_job_log  (
id varchar(64) NOT NULL,
identity varchar(255) NULL DEFAULT NULL,
start_time timestamp NULL DEFAULT NULL,
end_time timestamp NULL DEFAULT NULL,
content text NULL,
status varchar(64) NULL DEFAULT NULL,
duration decimal(11, 0) NULL DEFAULT NULL,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for rag_document
-- ----------------------------
DROP TABLE IF EXISTS rag_document;
CREATE TABLE rag_document  (
id bigint NOT NULL,
knowledge_base_config_id bigint NOT NULL,
file_name varchar(500) NOT NULL,
file_path varchar(1000) NOT NULL,
file_size bigint NOT NULL DEFAULT 0,
file_type varchar(50) NOT NULL,
chunk_count int NOT NULL DEFAULT 0,
status varchar(50) NOT NULL DEFAULT 'PENDING',
error_message text NULL,
created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by bigint NULL DEFAULT NULL,
updated_by bigint NULL DEFAULT NULL,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for rag_document_chunk
-- ----------------------------
DROP TABLE IF EXISTS rag_document_chunk;
CREATE TABLE rag_document_chunk (
id bigint NOT NULL,
document_id bigint NOT NULL,
file_name varchar(500) NOT NULL DEFAULT '',
chunk_index int NOT NULL,
content text NOT NULL,
token_count int DEFAULT NULL,
start_offset int DEFAULT NULL,
end_offset int DEFAULT NULL,
metadata text,
created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for secret_key
-- ----------------------------
DROP TABLE IF EXISTS secret_key;
CREATE TABLE secret_key  (
id bigint NOT NULL,
name varchar(100) NOT NULL,
value varchar(500) NULL DEFAULT NULL,
enabled boolean NULL DEFAULT true,
expire_time timestamp NULL DEFAULT NULL,
created_by bigint NOT NULL,
created_at timestamp NOT NULL,
updated_by bigint NULL DEFAULT NULL,
updated_at timestamp NULL DEFAULT NULL,
remark varchar(255) NULL DEFAULT NULL,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for sensitive_word_config
-- ----------------------------
DROP TABLE IF EXISTS sensitive_word_config;
CREATE TABLE sensitive_word_config  (
id bigint NOT NULL,
category varchar(100) NULL DEFAULT NULL,
name varchar(100) NOT NULL,
description varchar(500) NULL DEFAULT NULL,
words text NOT NULL,
action varchar(50) NOT NULL DEFAULT 'BLOCK',
replacement varchar(50) NULL DEFAULT '***',
enabled boolean NOT NULL DEFAULT true,
created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by bigint NULL DEFAULT NULL,
updated_by bigint NULL DEFAULT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_sw_name_category UNIQUE (category, name)
);

-- ----------------------------
-- Table structure for skill_package
-- ----------------------------
DROP TABLE IF EXISTS skill_package;
CREATE TABLE skill_package  (
id bigint NOT NULL,
name varchar(500) NOT NULL,
description text NOT NULL,
skill_content text NULL,
category varchar(100) NULL DEFAULT NULL,
"references" text NULL,
examples text NULL,
scripts text NULL,
enabled boolean NOT NULL DEFAULT true,
created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by bigint NULL DEFAULT NULL,
updated_by bigint NULL DEFAULT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_skill_name UNIQUE (name)
);



-- ----------------------------
-- Table structure for skill_tools
-- ----------------------------
DROP TABLE IF EXISTS skill_tools;
CREATE TABLE skill_tools  (
id bigint NOT NULL,
skill_id bigint NOT NULL,
tool_id bigint NOT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_skill_tool UNIQUE (skill_id, tool_id)
);

-- ----------------------------
-- Table structure for storage_protocol
-- ----------------------------
DROP TABLE IF EXISTS storage_protocol;
CREATE TABLE storage_protocol  (
id bigint NOT NULL,
name varchar(255) NULL DEFAULT NULL,
protocol varchar(255) NULL DEFAULT NULL,
protocol_config varchar(1000) NULL DEFAULT NULL,
create_by varchar(40) NULL DEFAULT NULL,
create_at timestamp NULL DEFAULT NULL,
update_by varchar(40) NULL DEFAULT NULL,
update_at timestamp NULL DEFAULT NULL,
remark varchar(255) NULL DEFAULT NULL,
valid int NULL DEFAULT 1,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for studio_config
-- ----------------------------
DROP TABLE IF EXISTS studio_config;
CREATE TABLE studio_config  (
id bigint NOT NULL,
url varchar(40) NOT NULL,
project varchar(60) NOT NULL,
PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for system_prompt_template
-- ----------------------------
DROP TABLE IF EXISTS system_prompt_template;
CREATE TABLE system_prompt_template  (
id bigint NOT NULL,
category varchar(100) NULL DEFAULT NULL,
name varchar(100) NOT NULL,
description varchar(500) NULL DEFAULT NULL,
content text NOT NULL,
enabled boolean NOT NULL DEFAULT true,
usage_count int NULL DEFAULT 0,
created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by bigint NULL DEFAULT NULL,
updated_by bigint NULL DEFAULT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_template_name_category UNIQUE (category, name)
);

-- ----------------------------
-- Table structure for tool_config
-- ----------------------------
DROP TABLE IF EXISTS tool_config;
CREATE TABLE tool_config  (
id bigint NOT NULL,
name varchar(100) NOT NULL,
tool_id varchar(100) NOT NULL,
description text NOT NULL,
category varchar(100) NULL DEFAULT NULL,
tool_type varchar(50) NOT NULL,
input_schema text NULL,
output_schema text NULL,
class_path varchar(255) NULL DEFAULT NULL,
language varchar(50) NULL DEFAULT NULL,
code text NULL,
need_confirm boolean NULL DEFAULT NULL,
enabled boolean NOT NULL DEFAULT true,
version varchar(20) NULL DEFAULT '1.0.0',
created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by bigint NULL DEFAULT NULL,
updated_by bigint NULL DEFAULT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_tool_id UNIQUE (tool_id)
);


-- ----------------------------
-- Table structure for mcp_tool
-- ----------------------------
DROP TABLE IF EXISTS mcp_tool;
CREATE TABLE mcp_tool  (
id bigint NOT NULL,
mcp_server_id bigint NOT NULL,
tool_name varchar(200) NOT NULL,
description varchar(1000) NULL DEFAULT NULL,
input_schema text NULL,
output_schema text NULL,
raw_schema text NULL,
schema_hash varchar(64) NULL DEFAULT NULL,
missing boolean NOT NULL DEFAULT false,
sort int NOT NULL DEFAULT 0,
enabled boolean NOT NULL DEFAULT true,
last_discovered_at timestamp NULL DEFAULT NULL,
last_seen_at timestamp NULL DEFAULT NULL,
created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by bigint NULL DEFAULT NULL,
updated_by bigint NULL DEFAULT NULL,
PRIMARY KEY (id),
CONSTRAINT uk_mcp_tool_name UNIQUE (mcp_server_id, tool_name)
);
