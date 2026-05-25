# Review 检查清单模板

## Review 对象

任务编号：待填写。

所属小工程：待填写。

## 范围检查

- owned files 是否被遵守：是 / 否。
- 是否修改未授权文件：是 / 否。
- 是否存在顺手重构或无关格式化：是 / 否。

## 契约检查

- 是否修改 API DTO：是 / 否。
- 是否修改 SSE event / payload：是 / 否。
- 是否修改 RuntimeEvent / runtime-spi：是 / 否。
- 是否修改 repo-spi / 数据模型 / Flyway：是 / 否。
- `contract-notes.md` 是否记录实际参考的 API / 模型 / 代码接口 / schema / table / 字段：是 / 否。
- `contract-notes.md` 是否记录采纳点、不采纳点、参考实现差异：是 / 否。
- 是否存在“参考某项目”但没有实际 endpoint / 类 / 方法 / DTO / schema / 表 / 字段的泛泛表述：是 / 否。
- 是否需要合入主契约：是 / 否。

## 安全与治理检查

- 是否绕过 AgentScope：是 / 否。
- 是否绕过权限、Capability gate 或审计：是 / 否。
- 是否泄露密钥、token、数据库密码或敏感数据：是 / 否。
- 是否误用 MyBatis 处理 SSE 高频 checkpoint：是 / 否。

## 测试检查

- 是否执行任务单要求命令：是 / 否。
- 是否达到目标验收等级：是 / 否。
- E2E 是否使用 `testRunId` 隔离：是 / 否 / 不适用。
- Playwright 是否真实操作页面：是 / 否 / 不适用。

## 结论

```text
通过 / 需修改 / 阻断
```

## 问题清单

| 严重级别 | 文件 | 问题 | 建议 |
| --- | --- | --- | --- |
| 待填写 | 待填写 | 待填写 | 待填写 |
