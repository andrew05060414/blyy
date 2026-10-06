# 从 URL 导入人设包

> 创建日期：2026-10-05 ｜ 最后更新：2026-10-05 ｜ 版本：v1.0
>
> 对应 Multica：PX-868。入口：设置页 → 舰娘人格分区 →「已保存舰娘人格」→「从 URL 导入人设包」。

## 1. 人设包格式

人设包就是 **SillyTavern V2 角色卡** 的 JSON，两种形态都接受：

1. 单个卡对象：`{"spec":"chara_card_v2","spec_version":"2.0","data":{...}}`
2. 卡对象的 JSON 数组：`[{...}, {...}]`

App 只读 `data` 段内的以下字段，其余字段（`creator`、`tags`、`extensions`、`character_book` 等）会被忽略：

| V2 `data` 字段 | 说明 |
|---|---|
| `name` | 角色名（必填，为空的卡会被跳过） |
| `system_prompt` | 核心扮演提示词 |
| `description` | 角色档案 |
| `personality` | 性格 |
| `scenario` | 当前场景 |
| `mes_example` | 对话示例 |
| `post_history_instructions` | 追加在历史之后、回复之前的指令 |

## 2. V2 → PersonaConfig 字段映射表

| V2 `data` 字段 | PersonaConfig 字段 | 备注 |
|---|---|---|
| `name` | `name`、`jiuxinName` | 同填角色名，用户可在 App 内改 `jiuxinName` |
| — | `avatarUrl` | 留空；卡内无头像，用户在 App 内选 blyy 自带头像 |
| 拼装结果（见第 3 节） | `systemPrompt` | 空字段跳过 |
| — | `voiceShipName`、`voiceShipAvatar` | 留空 |
| — | `voiceEnabled` | 保持默认 `true` |
| — | `voiceRandomChance`、`voiceKeywords`、`stickersEnabled`、`stickerChance` | 不碰，保持 PersonaConfig 默认值 |

每次导入生成新的 `PersonaConfig`（新 `id`），追加到已保存人格列表，不覆盖已有配置。

## 3. systemPrompt 拼装规则

按以下顺序拼接，空字段跳过（实现：`PersonaPackImporter.toPersonaConfig`）：

1. `data.system_prompt`（核心扮演提示词，直接放开头）
2. `\n\n\n` + `data.description`
3. `\n\n\n` + `data.personality`
4. `\n\n\n` + `data.scenario`
5. `\n\n\n` + `data.mes_example` —— 超过 2000 字符时截断至 2000 字符并追加 `（对话示例过长，已截断至 2000 字符）`
6. `\n\n` + `data.post_history_instructions`

## 4. 错误处理

| 情况 | App 内提示 |
|---|---|
| URL 为空 | 「请输入人设包 URL」 |
| URL 格式非法 | 「URL 格式错误：…」 |
| DNS 失败 / 连不上 | 「无法连接服务器（host），请检查网络和 URL 地址」 |
| HTTP 非 2xx | 「API 错误 (code)：…」 |
| 空响应体 | 「服务器返回空响应」 |
| JSON 非法 / 既非单个卡也非数组 | 「无法解析为人设包：需要单个 SillyTavern V2 角色卡对象，或卡对象的 JSON 数组」 |
| 包内无有效卡（`data.name` 为空） | 「人设包中没有有效的角色卡（data.name 为空）」 |

## 5. 安全与隐私说明

- URL 由用户在 App 内输入，**代码中不硬编码任何私有地址**。
- 下载走普通 HTTP GET，不带鉴权头；需要鉴权的私链请确认服务端允许匿名 GET 或自行扩展。
- 人设包内容只写入本机 DataStore，不上传到任何地方。

## 6. 后续项（v1 不做）

- **`character_book`（lorebook）**：V2 卡的 `character_book.entries` 尚未导入。后续服务端角色注册表 / ADR-0004 的 Lorebook 检索落地后，再决定客户端是否需要只读展示或关键词触发。
- **`first_mes`（开场白）**：blyy 当前没有"创建会话后自动发开场白"的路径（见源码审计报告第 2.1 节），v1 不做。后续如需，入口在 `JiuxinViewModel` 的会话创建流程。
- **导入去重**：重复导入同一人设包会产生重复人格，暂由用户手动删除；后续可按 `name` 去重或提示覆盖。
