package com.azurlane.blyy.data.persona

import com.azurlane.blyy.data.model.PersonaConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
* SillyTavern V2 角色卡的 `data` 段。
*
* 只取人设包导入需要的字段，其余字段（creator、tags、extensions、character_book 等）
* 由 `ignoreUnknownKeys` 自动忽略。`character_book`（lorebook）与 `first_mes`（开场白）
* 在 v1 导入中不处理，见 `docs/persona-import.md` 的后续项。
*/
@Serializable
data class CharaCardV2Data(
val name: String = "",
val description: String = "",
val personality: String = "",
val scenario: String = "",
val first_mes: String = "",
val mes_example: String = "",
val system_prompt: String = "",
val post_history_instructions: String = ""
)

/**
* SillyTavern V2 角色卡顶层结构。
*/
@Serializable
data class CharaCardV2(
val spec: String = "",
val spec_version: String = "",
val data: CharaCardV2Data = CharaCardV2Data()
)

/**
* 人设包解析失败。message 均为可直接展示给用户的中文说明。
*/
class PersonaPackParseException(message: String): Exception(message)

/**
* 人设包导入器：SillyTavern V2 卡 → [PersonaConfig]。
*
* 人设包格式：单个 V2 卡对象，或 V2 卡对象的 JSON 数组。
* 字段映射与 systemPrompt 拼装规则见 `docs/persona-import.md`。
*/
object PersonaPackImporter {

/** 对话示例截断上限（字符），超出部分截断并在拼装结果中注明 */
const val MES_EXAMPLE_MAX_CHARS = 2000

private val json = Json {
ignoreUnknownKeys = true
isLenient = true
coerceInputValues = true
}

/**
* 解析人设包文本。
*
* @return 解析出的角色卡列表（已过滤掉无名卡）
* @throws PersonaPackParseException 文本为空、JSON 非法、既非单个卡对象也非卡数组、
* 或包内没有有效角色卡（data.name 为空）时抛出，message 可直接展示
*/
fun parsePack(raw: String): List<CharaCardV2> {
val text = raw.trim()
if (text.isEmpty()) throw PersonaPackParseException("人设包内容为空")
val cards: List<CharaCardV2> = try {
listOf(json.decodeFromString<CharaCardV2>(text))
} catch (singleError: Exception) {
try {
json.decodeFromString<List<CharaCardV2>>(text)
} catch (arrayError: Exception) {
throw PersonaPackParseException(
"无法解析为人设包：需要单个 SillyTavern V2 角色卡对象，或卡对象的 JSON 数组"
)
}
}
val valid = cards.filter { it.data.name.isNotBlank()}
if (valid.isEmpty()) {
throw PersonaPackParseException("人设包中没有有效的角色卡（data.name 为空）")
}
return valid
}

/**
* 按既定映射规则把一张 V2 卡转为 [PersonaConfig]。
*
* - name → name / jiuxinName；avatarUrl 留空（卡内无头像，用户在 App 内选择）；
* - voiceShipName / voiceShipAvatar 留空，voiceEnabled 保持默认 true，不碰表情包默认值；
* - systemPrompt 按 system_prompt → 角色档案 → 性格 → 当前场景 → 对话示例 → post_history_instructions
* 的顺序拼装，空字段跳过。
*/
fun toPersonaConfig(card: CharaCardV2): PersonaConfig {
val d = card.data
val name = d.name.trim()
val prompt = buildString {
val systemPrompt = d.system_prompt.trim()
if (systemPrompt.isNotEmpty()) append(systemPrompt)
if (d.description.isNotBlank()) {
append("\n\n\n")
append(d.description.trim())
}
if (d.personality.isNotBlank()) {
append("\n\n\n")
append(d.personality.trim())
}
if (d.scenario.isNotBlank()) {
append("\n\n\n")
append(d.scenario.trim())
}
if (d.mes_example.isNotBlank()) {
append("\n\n\n")
val example = d.mes_example.trim()
if (example.length > MES_EXAMPLE_MAX_CHARS) {
append(example.take(MES_EXAMPLE_MAX_CHARS))
append("\n（对话示例过长，已截断至 $MES_EXAMPLE_MAX_CHARS 字符）")
} else {
append(example)
}
}
if (d.post_history_instructions.isNotBlank()) {
append("\n\n")
append(d.post_history_instructions.trim())
}
}.trim()
return PersonaConfig(
name = name,
jiuxinName = name,
avatarUrl = "",
systemPrompt = prompt,
voiceShipName = "",
voiceShipAvatar = "",
voiceEnabled = true
)
}
}
