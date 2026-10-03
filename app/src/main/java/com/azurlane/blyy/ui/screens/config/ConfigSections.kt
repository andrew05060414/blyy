package com.azurlane.blyy.ui.screens.config

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import androidx.compose.material3.AlertDialog
import com.azurlane.blyy.data.model.ApiConfig
import com.azurlane.blyy.data.model.JiuxinPreset
import com.azurlane.blyy.data.model.PersonaConfig
import com.azurlane.blyy.data.model.Ship
import com.azurlane.blyy.ui.components.AdaptiveScreenBackground
import com.azurlane.blyy.ui.components.BlyyBottomSheet
import com.azurlane.blyy.ui.components.BlyyPanel
import com.azurlane.blyy.ui.components.BlyyPrimaryButton
import com.azurlane.blyy.ui.components.BlyyEntrance
import com.azurlane.blyy.ui.components.RobustAvatar
import com.azurlane.blyy.ui.screens.chat.AvatarPickerSheet
import com.azurlane.blyy.ui.components.BlyySectionPanel
import com.azurlane.blyy.ui.components.BlyyTopBar
import com.azurlane.blyy.ui.components.StableOutlinedTextField
import com.azurlane.blyy.ui.theme.AppSpacing
import com.azurlane.blyy.ui.theme.AppTypography
import com.azurlane.blyy.ui.theme.ChatColors
import com.azurlane.blyy.ui.theme.LocalIsDark
import com.azurlane.blyy.util.findActivityViewModelStoreOwner
import com.azurlane.blyy.viewmodel.ConnectionTestState
import com.azurlane.blyy.viewmodel.JiuxinViewModel
import com.azurlane.blyy.viewmodel.ModelListState
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.coroutines.launch

// ════════════════════════════════════════════════════════════
// 配置分区导航：主菜单 + 各分区详情页
// ════════════════════════════════════════════════════════════

internal enum class AccentColorRole { PRIMARY, SECONDARY, TERTIARY }

/**
 * 配置分区枚举：每个分区对应一个独立配置页
 */
internal enum class ConfigSection(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val description: String,
    val accentColorRole: AccentColorRole
) {
    PRESETS(
        title = "舰娘预设",
        subtitle = "完整配置快照",
        icon = Icons.Rounded.Bookmark,
        description = "保存 API + 人格 + 语音 + 表情包为完整预设，一键应用",
        accentColorRole = AccentColorRole.TERTIARY
    ),
    API(
        title = "API 配置",
        subtitle = "大模型连接",
        icon = Icons.Rounded.Key,
        description = "管理 API URL、Key、Model，可保存多套供组合使用",
        accentColorRole = AccentColorRole.PRIMARY
    ),
    PERSONA(
        title = "舰娘人格",
        subtitle = "头像 · 名称 · 语音 · 表情",
        icon = Icons.Rounded.Psychology,
        description = "舰娘头像、名称、人格提示词、语音触发、表情包等完整人格配置",
        accentColorRole = AccentColorRole.SECONDARY
    )
}

@Composable
internal fun AccentColorRole.toColor(): Color = when (this) {
    AccentColorRole.PRIMARY -> MaterialTheme.colorScheme.primary
    AccentColorRole.SECONDARY -> MaterialTheme.colorScheme.secondary
    AccentColorRole.TERTIARY -> MaterialTheme.colorScheme.tertiary
}

/**
 * 主菜单：分区卡片列表
 *
 * 每张卡片显示分区图标、标题、描述和当前状态徽章，
 * 点击进入对应分区详情页。状态徽章实时反映该分区配置的可用性。
 */
@Composable
internal fun ConfigMainMenu(
    presets: List<JiuxinPreset>,
    apiConfigs: List<ApiConfig>,
    personaConfigs: List<PersonaConfig>,
    isApiReady: Boolean,
    isPersonaReady: Boolean,
    voiceEnabled: Boolean,
    stickersEnabled: Boolean,
    onSectionClick: (ConfigSection) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.Screen.Horizontal)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Md)
    ) {
        Spacer(modifier = Modifier.height(AppSpacing.Sm))

        // 顶部概览卡片
        BlyyPanel(accentColor = MaterialTheme.colorScheme.primary) {
            Column(modifier = Modifier.padding(AppSpacing.Lg).fillMaxWidth()) {
                Text(
                    "配置中心",
                    style = AppTypography.TitleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(AppSpacing.Xs))
                Text(
                    "按分区管理啾信配置。API 配置与舰娘人格解耦，可独立保存多套并在新建对话时自由组合。",
                    style = AppTypography.BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(AppSpacing.Md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm)
                ) {
                    ConfigStatPill(label = "预设", count = presets.size, color = MaterialTheme.colorScheme.tertiary)
                    ConfigStatPill(label = "API", count = apiConfigs.size, color = MaterialTheme.colorScheme.primary)
                    ConfigStatPill(label = "人格", count = personaConfigs.size, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        // 分区卡片
        ConfigSection.entries.forEach { section ->
            val statusText = when (section) {
                ConfigSection.PRESETS -> if (presets.isNotEmpty()) "${presets.size} 套" else "未配置"
                ConfigSection.API -> if (isApiReady) "已配置" else "未配置"
                ConfigSection.PERSONA -> buildString {
                    if (isPersonaReady) append("已配置") else append("未配置")
                    val extras = mutableListOf<String>()
                    if (voiceEnabled) extras.add("语音")
                    if (stickersEnabled) extras.add("表情")
                    if (extras.isNotEmpty()) append(" · ${extras.joinToString("/")}")
                }
            }
            val statusColor = when (section) {
                ConfigSection.PRESETS -> if (presets.isNotEmpty()) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                ConfigSection.API -> if (isApiReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ConfigSection.PERSONA -> if (isPersonaReady) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
            }
            ConfigSectionCard(
                section = section,
                statusText = statusText,
                statusColor = statusColor,
                onClick = { onSectionClick(section) }
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.Xl))
    }
}

@Composable
internal fun ConfigStatPill(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(AppSpacing.Corner.Sm))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = AppSpacing.Md, vertical = AppSpacing.Xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "$count",
            style = AppTypography.TitleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = AppTypography.LabelSmall,
            color = color.copy(alpha = 0.8f)
        )
    }
}

/**
 * 分区导航卡片：图标 + 标题 + 描述 + 状态徽章 + 右箭头
 */
@Composable
internal fun ConfigSectionCard(
    section: ConfigSection,
    statusText: String,
    statusColor: Color,
    onClick: () -> Unit
) {
    val accentColor = section.accentColorRole.toColor()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppSpacing.Corner.Lg))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(AppSpacing.Corner.Lg))
            .clickable(onClick = onClick)
            .padding(AppSpacing.Lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Md)
    ) {
        // 图标徽章
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(AppSpacing.Corner.Md))
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                section.icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        // 标题 + 描述
        Column(modifier = Modifier.weight(1f)) {
            Text(
                section.title,
                style = AppTypography.TitleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                section.description,
                style = AppTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        // 状态徽章
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(AppSpacing.Corner.Sm))
                .background(statusColor.copy(alpha = 0.12f))
                .padding(horizontal = AppSpacing.Sm, vertical = AppSpacing.Xs)
        ) {
            Text(
                statusText,
                style = AppTypography.LabelSmall,
                fontWeight = FontWeight.Medium,
                color = statusColor
            )
        }
        // 右箭头
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = "进入",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

// ════════════════════════════════════════════════════════════
// 分区详情页：预设管理
// ════════════════════════════════════════════════════════════

@Composable
internal fun PresetsSection(
    presets: List<JiuxinPreset>,
    jiuxinName: String,
    selectedModel: String,
    voiceShipName: String,
    apiUrl: String,
    apiKey: String,
    onSavePreset: () -> Unit,
    onApplyPreset: (JiuxinPreset) -> Unit,
    onEditPreset: (JiuxinPreset) -> Unit,
    onDeletePreset: (JiuxinPreset) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.Screen.Horizontal)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Md)
    ) {
        Spacer(modifier = Modifier.height(AppSpacing.Sm))

        BlyyEntrance(index = 0) {
        BlyySectionPanel(
                    title = "舰娘预设",
                    icon = Icons.Rounded.Bookmark,
                    accentColor = MaterialTheme.colorScheme.tertiary
                ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(AppSpacing.Lg),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.Md)
                ) {
                    Text(
                        "预设是完整的配置快照，包含 API、人格、语音、表情包等全部设置。新建对话时可一键应用，无需重复配置。",
                        style = AppTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // 当前配置摘要
                    BlyyPanel(accentColor = MaterialTheme.colorScheme.tertiary) {
                        Column(modifier = Modifier.padding(AppSpacing.Md).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.Xs)) {
                            Text("当前将保存的配置", style = AppTypography.LabelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            Text(
                                buildString {
                                    if (jiuxinName.isNotBlank()) append("舰娘: $jiuxinName")
                                    if (selectedModel.isNotBlank()) {
                                        if (isNotEmpty()) append("\n")
                                        append("模型: $selectedModel")
                                    }
                                    if (voiceShipName.isNotBlank()) {
                                        if (isNotEmpty()) append("\n")
                                        append("语音: $voiceShipName")
                                    }
                                    if (apiUrl.isNotBlank()) {
                                        if (isNotEmpty()) append("\n")
                                        append("API: ${apiUrl.take(30)}")
                                    }
                                    if (isEmpty()) append("（当前无配置）")
                                },
                                style = AppTypography.BodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    BlyyPrimaryButton(
                        text = "保存当前配置为预设",
                        onClick = onSavePreset,
                        icon = Icons.Rounded.Bookmark,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (presets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(AppSpacing.Xs))
                        Text(
                            "已保存预设 (${presets.size})",
                            style = AppTypography.LabelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        presets.forEach { preset ->
                            PresetCard(
                                preset = preset,
                                onApply = { onApplyPreset(preset) },
                                onEdit = { onEditPreset(preset) },
                                onDelete = { onDeletePreset(preset) }
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(AppSpacing.Lg),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm)) {
                                Icon(Icons.Rounded.Bookmark, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(40.dp))
                                Text("暂无保存的预设", style = AppTypography.BodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("保存当前配置为预设后，可在此查看和管理", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
            }
        Spacer(modifier = Modifier.height(AppSpacing.Xl))
    }
}

// ════════════════════════════════════════════════════════════
// 分区详情页：API 配置
// ════════════════════════════════════════════════════════════

@Composable
internal fun ApiSection(
    apiUrl: String,
    apiKey: String,
    selectedModel: String,
    availableModels: List<String>,
    modelListState: ModelListState,
    connectionState: ConnectionTestState,
    showApiKey: Boolean,
    isModelExpanded: Boolean,
    apiConfigs: List<ApiConfig>,
    successColor: Color,
    onSaveApiUrl: (String) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onSaveModel: (String) -> Unit,
    onToggleShowApiKey: () -> Unit,
    onToggleModelExpanded: () -> Unit,
    onFetchModels: () -> Unit,
    onTestConnection: () -> Unit,
    buildFullApiUrl: (String) -> String,
    onSaveApiConfig: () -> Unit,
    onApplyApiConfig: (ApiConfig) -> Unit,
    onEditApiConfig: (ApiConfig) -> Unit,
    onDeleteApiConfig: (ApiConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.Screen.Horizontal)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Md)
    ) {
        Spacer(modifier = Modifier.height(AppSpacing.Sm))

        BlyyEntrance(index = 1) {
        BlyySectionPanel(title = "当前 API 配置", icon = Icons.Rounded.Key, accentColor = MaterialTheme.colorScheme.primary) {
                Column(modifier = Modifier.fillMaxWidth().padding(AppSpacing.Lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.Md)) {
                    StableOutlinedTextField(
                        value = apiUrl, onValueChange = onSaveApiUrl, modifier = Modifier.fillMaxWidth(),
                        label = { Text("API Base URL") },
                        placeholder = { Text("https://api.example.com/v1") },
                        singleLine = true, textStyle = AppTypography.BodyMedium,
                        supportingText = {
                            val fullUrl = buildFullApiUrl(apiUrl)
                            if (fullUrl.isNotBlank()) {
                                Text("请求地址: $fullUrl", style = AppTypography.LabelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                            } else {
                                Text("输入 Base URL，自动补全 /chat/completions", style = AppTypography.LabelSmall)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(AppSpacing.Corner.Sm)
                    )
                    StableOutlinedTextField(
                        value = apiKey, onValueChange = onSaveApiKey, modifier = Modifier.fillMaxWidth(),
                        label = { Text("API Key") }, placeholder = { Text("输入 API Key") },
                        visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = { IconButton(onClick = onToggleShowApiKey) { Icon(if (showApiKey) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } },
                        singleLine = true, textStyle = AppTypography.BodyMedium,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(AppSpacing.Corner.Sm)
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        StableOutlinedTextField(
                            value = selectedModel, onValueChange = onSaveModel, modifier = Modifier.fillMaxWidth(),
                            label = { Text("Model") }, placeholder = { Text("如 gpt-4o-mini") },
                            singleLine = true, textStyle = AppTypography.BodyMedium,
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = onFetchModels) {
                                        Icon(Icons.Rounded.Refresh, "刷新模型列表", modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = onToggleModelExpanded) {
                                        Icon(Icons.AutoMirrored.Rounded.List, "模型列表")
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(AppSpacing.Corner.Sm)
                        )
                        DropdownMenu(
                            expanded = isModelExpanded,
                            onDismissRequest = { if (isModelExpanded) onToggleModelExpanded() },
                            modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 300.dp)
                        ) {
                            when (modelListState) {
                                is ModelListState.Loading -> {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(AppSpacing.Sm))
                                                Text("正在拉取模型列表…", style = AppTypography.BodySmall)
                                            }
                                        },
                                        onClick = {}
                                    )
                                }
                                is ModelListState.Error -> {
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    "拉取失败：${modelListState.message}",
                                                    style = AppTypography.BodySmall,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                                Spacer(modifier = Modifier.height(AppSpacing.Xs))
                                                Text(
                                                    "点击重试",
                                                    style = AppTypography.LabelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        onClick = { onFetchModels() }
                                    )
                                }
                                is ModelListState.Empty -> {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "API 返回了空模型列表，点击重新拉取",
                                                style = AppTypography.BodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        onClick = { onFetchModels() }
                                    )
                                }
                                is ModelListState.Success -> {
                                    if (availableModels.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("未拉取到模型", style = AppTypography.BodySmall) },
                                            onClick = { onToggleModelExpanded(); onFetchModels() }
                                        )
                                    } else {
                                        availableModels.forEach { model ->
                                            DropdownMenuItem(
                                                text = { Text(model, style = AppTypography.BodyMedium) },
                                                onClick = {
                                                    onSaveModel(model)
                                                    if (isModelExpanded) onToggleModelExpanded()
                                                }
                                            )
                                        }
                                    }
                                }
                                is ModelListState.Idle -> {
                                    DropdownMenuItem(
                                        text = { Text("点击拉取模型列表", style = AppTypography.BodySmall) },
                                        onClick = { onFetchModels() }
                                    )
                                }
                            }
                        }
                    }
                    BlyyPrimaryButton(
                        text = when (connectionState) { is ConnectionTestState.Testing -> "测试中..."; else -> "测试连接" },
                        onClick = onTestConnection,
                        enabled = connectionState !is ConnectionTestState.Testing && apiKey.isNotBlank() && apiUrl.isNotBlank(),
                        icon = Icons.Rounded.Key, modifier = Modifier.fillMaxWidth()
                    )
                    when (val state = connectionState) {
                        is ConnectionTestState.Success -> {
                            BlyyPanel(accentColor = successColor) {
                                Row(modifier = Modifier.padding(AppSpacing.Md).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm)) {
                                    Icon(Icons.Rounded.Check, null, tint = successColor, modifier = Modifier.size(20.dp))
                                    Text("连接成功", color = successColor, style = AppTypography.BodyMedium)
                                }
                            }
                        }
                        is ConnectionTestState.Error -> {
                            BlyyPanel(accentColor = MaterialTheme.colorScheme.error) {
                                Row(modifier = Modifier.padding(AppSpacing.Md).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm)) {
                                    Icon(Icons.Rounded.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    Text(state.message, color = MaterialTheme.colorScheme.error, style = AppTypography.BodySmall)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
    }

        // ── 已保存 API 配置列表 ──
        BlyyEntrance(index = 2) {
        BlyySectionPanel(title = "已保存 API 配置", icon = Icons.Rounded.Storage, accentColor = MaterialTheme.colorScheme.primary) {
                Column(modifier = Modifier.fillMaxWidth().padding(AppSpacing.Lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.Md)) {
                    Text(
                        "保存多套 API 配置，可在新建对话时与不同舰娘人格自由组合。点击列表项可快速应用。",
                        style = AppTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BlyyPrimaryButton(
                        text = "保存当前 API 配置",
                        onClick = onSaveApiConfig,
                        icon = Icons.Rounded.Bookmark,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = apiUrl.isNotBlank() || apiKey.isNotBlank() || selectedModel.isNotBlank()
                    )
                    if (apiConfigs.isNotEmpty()) {
                        Text(
                            "共 ${apiConfigs.size} 套配置",
                            style = AppTypography.LabelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        apiConfigs.forEach { config ->
                            ApiConfigCard(
                                config = config,
                                isCurrentActive = config.apiUrl == apiUrl && config.apiKey == apiKey && config.model == selectedModel,
                                onApply = { onApplyApiConfig(config) },
                                onEdit = { onEditApiConfig(config) },
                                onDelete = { onDeleteApiConfig(config) }
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(AppSpacing.Lg),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm)) {
                                Icon(Icons.Rounded.Storage, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(40.dp))
                                Text("暂无保存的 API 配置", style = AppTypography.BodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("保存当前配置后，可在此查看和管理", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
    }
        Spacer(modifier = Modifier.height(AppSpacing.Xl))
    }
}

// ════════════════════════════════════════════════════════════
// 分区详情页：舰娘人格
// ════════════════════════════════════════════════════════════

@Composable
internal fun PersonaSection(
    avatarUrl: String,
    jiuxinName: String,
    systemPrompt: String,
    personaConfigs: List<PersonaConfig>,
    memoryText: String,
    memoryUpdatedAt: Long,
    voiceEnabled: Boolean,
    voiceRandomChance: Float,
    voiceKeywords: String,
    voiceShipName: String,
    voiceShipAvatar: String,
    stickersEnabled: Boolean,
    stickerChance: Float,
    onPickAvatar: () -> Unit,
    onSaveJiuxinName: (String) -> Unit,
    onSaveSystemPrompt: (String) -> Unit,
    onEditMemory: () -> Unit,
    onSaveMemory: (String) -> Unit,
    onClearMemory: () -> Unit,
    onSavePersonaConfig: () -> Unit,
    onApplyPersonaConfig: (PersonaConfig) -> Unit,
    onEditPersonaConfig: (PersonaConfig) -> Unit,
    onDeletePersonaConfig: (PersonaConfig) -> Unit,
    onClearPersonaFields: () -> Unit,
    onSaveVoiceEnabled: (Boolean) -> Unit,
    onSaveVoiceRandomChance: (Float) -> Unit,
    onSaveVoiceKeywords: (String) -> Unit,
    onSaveStickersEnabled: (Boolean) -> Unit,
    onSaveStickerChance: (Float) -> Unit,
    onPickVoiceShip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.Screen.Horizontal)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Md)
    ) {
        Spacer(modifier = Modifier.height(AppSpacing.Sm))

        BlyyEntrance(index = 3) {
        BlyySectionPanel(title = "当前舰娘人格", icon = Icons.Rounded.Psychology, accentColor = MaterialTheme.colorScheme.secondary) {
                Column(modifier = Modifier.fillMaxWidth().padding(AppSpacing.Lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.Md)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.Md)) {
                        RobustAvatar(
                            url = avatarUrl,
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                .border(AppSpacing.Border.Thin, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
                                .clickable(onClick = onPickAvatar),
                            fallbackContent = {
                                Icon(Icons.Rounded.Person, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("啾信头像", style = AppTypography.TitleSmall, fontWeight = FontWeight.Medium)
                            Text(text = if (avatarUrl.isNotBlank()) "已选择头像" else "点击选择舰娘头像或上传图片", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    StableOutlinedTextField(value = jiuxinName, onValueChange = onSaveJiuxinName, modifier = Modifier.fillMaxWidth(), label = { Text("啾信名称") }, placeholder = { Text("啾信助手") }, singleLine = true, textStyle = AppTypography.BodyMedium, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(AppSpacing.Corner.Sm))
                    StableOutlinedTextField(value = systemPrompt, onValueChange = onSaveSystemPrompt, modifier = Modifier.fillMaxWidth().height(120.dp), label = { Text("人格提示词") }, placeholder = { Text("描述啾信的人格和行为方式...") }, textStyle = AppTypography.BodyMedium, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(AppSpacing.Corner.Sm))
                    // 一键清空按钮 — 解决配置新人格时需手动逐个清除字段的痛点
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onClearPersonaFields,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.DeleteSweep,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "清空当前配置",
                                style = AppTypography.LabelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
    }

        // ── 长期记忆（跨会话自动摘要，可手动编辑/清空） ──
        BlyyEntrance(index = 4) {
        BlyySectionPanel(title = "长期记忆", icon = Icons.Rounded.AutoStories, accentColor = MaterialTheme.colorScheme.secondary) {
                Column(modifier = Modifier.fillMaxWidth().padding(AppSpacing.Lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.Md)) {
                    Text(
                        "聊天积累后，舰娘会自动把较早的对话提炼为记忆，跨会话记住你们的称呼、话题与约定。清空聊天记录不会删除记忆。",
                        style = AppTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (memoryText.isNotBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AppSpacing.Corner.Md))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                                .padding(AppSpacing.Md),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.Xs)
                        ) {
                            Text(
                                memoryText,
                                style = AppTypography.BodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            )
                            if (memoryUpdatedAt > 0L) {
                                Text(
                                    "更新于 " + java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                                        .format(java.util.Date(memoryUpdatedAt)) + " · 共 ${memoryText.length} 字",
                                    style = AppTypography.LabelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        Text(
                            "暂无记忆。与舰娘多聊几轮（约 40 条消息后）会自动生成；也可以现在亲手写下她的初始记忆。",
                            style = AppTypography.BodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onEditMemory,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (memoryText.isNotBlank()) "编辑记忆" else "写下初始记忆", style = AppTypography.LabelMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                        if (memoryText.isNotBlank()) {
                            TextButton(
                                onClick = onClearMemory,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Rounded.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("清空记忆", style = AppTypography.LabelMedium, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
    }

        // ── 语音设置（合并到舰娘人格分区） ──
        BlyyEntrance(index = 5) {
        BlyySectionPanel(title = "语音设置", icon = Icons.Rounded.VolumeUp, accentColor = MaterialTheme.colorScheme.tertiary) {
                Column(modifier = Modifier.fillMaxWidth().padding(AppSpacing.Lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.Md)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("发送语音", style = AppTypography.TitleSmall, fontWeight = FontWeight.Medium)
                            Text("智能标签匹配或随机发送舰娘语音", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = voiceEnabled, onCheckedChange = onSaveVoiceEnabled)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(AppSpacing.Corner.Md))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                            .clickable(onClick = onPickVoiceShip)
                            .padding(AppSpacing.Md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Md)
                    ) {
                        RobustAvatar(
                            url = voiceShipAvatar,
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)),
                            fallbackContent = {
                                Icon(Icons.Rounded.SmartToy, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.tertiary)
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("舰娘语音", style = AppTypography.TitleSmall, fontWeight = FontWeight.Medium)
                            Text(
                                text = if (voiceShipName.isNotBlank()) voiceShipName else "点击选择舰娘",
                                style = AppTypography.BodySmall,
                                color = if (voiceShipName.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (voiceEnabled) {
                        Text("随机触发概率: ${(voiceRandomChance * 100).toInt()}%", style = AppTypography.BodySmall)
                        Slider(value = voiceRandomChance, onValueChange = onSaveVoiceRandomChance, valueRange = 0f..1f, steps = 19)
                        StableOutlinedTextField(value = voiceKeywords, onValueChange = onSaveVoiceKeywords, modifier = Modifier.fillMaxWidth(), label = { Text("触发关键词") }, placeholder = { Text("你好;早安;晚安") }, supportingText = { Text("用分号 ; 分隔，关键词会自动匹配对应语音场景标签") }, singleLine = true, textStyle = AppTypography.BodyMedium, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(AppSpacing.Corner.Sm))
                        BlyyPanel(accentColor = MaterialTheme.colorScheme.tertiary) {
                            Column(modifier = Modifier.padding(AppSpacing.Md).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.Xs)) {
                                Text("智能标签匹配", style = AppTypography.LabelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                Text("关键词自动映射到舰娘语音场景：\n" +
                                    "「你好/早安/晚安」→ 主界面/问候/登录台词\n" +
                                    "「登录/登录界面」→ 登录台词/登录界面\n" +
                                    "「舰船型号/自我介绍」→ 舰船型号/自我介绍\n" +
                                    "「获取台词/查看详情」→ 获取台词/查看详情\n" +
                                    "「触摸/摸头/特殊触摸」→ 触摸台词/摸头台词\n" +
                                    "「任务/邮件」→ 任务提醒/邮件提醒\n" +
                                    "「回港」→ 回港台词\n" +
                                    "「友好/喜欢/爱」→ 好感度语音\n" +
                                    "「誓约/结婚」→ 誓约台词（优先誓约皮肤）\n" +
                                    "「战斗/胜利/失败」→ 战斗场景\n" +
                                    "未匹配时按概率随机触发", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
    }

        // ── 表情包设置（合并到舰娘人格分区） ──
        BlyyEntrance(index = 6) {
        BlyySectionPanel(title = "表情包设置", icon = Icons.Rounded.Image, accentColor = MaterialTheme.colorScheme.tertiary) {
                Column(modifier = Modifier.fillMaxWidth().padding(AppSpacing.Lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.Md)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("发送表情包", style = AppTypography.TitleSmall, fontWeight = FontWeight.Medium)
                            Text("基于 AI 回复内容自动匹配表情包", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = stickersEnabled, onCheckedChange = onSaveStickersEnabled)
                    }
                    if (stickersEnabled) {
                        Text("表情包发送概率: ${(stickerChance * 100).toInt()}%", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Slider(
                            value = stickerChance,
                            onValueChange = onSaveStickerChance,
                            valueRange = 0f..1f,
                            steps = 19
                        )
                        Text(
                            "控制 AI 每次回复时发送表情包的概率，滑动调节即可实时生效",
                            style = AppTypography.BodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
    }

        BlyyEntrance(index = 7) {
        BlyySectionPanel(title = "已保存舰娘人格", icon = Icons.Rounded.Storage, accentColor = MaterialTheme.colorScheme.secondary) {
                Column(modifier = Modifier.fillMaxWidth().padding(AppSpacing.Lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.Md)) {
                    Text(
                        "保存多套舰娘人格（含头像、名称、提示词、语音、表情包），可在新建对话时与不同 API 配置自由组合。点击列表项可快速应用。",
                        style = AppTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BlyyPrimaryButton(
                        text = "保存当前舰娘人格",
                        onClick = onSavePersonaConfig,
                        icon = Icons.Rounded.Psychology,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = jiuxinName.isNotBlank() || systemPrompt.isNotBlank() || avatarUrl.isNotBlank()
                    )
                    if (personaConfigs.isNotEmpty()) {
                        Text(
                            "共 ${personaConfigs.size} 套人格",
                            style = AppTypography.LabelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        personaConfigs.forEach { config ->
                            PersonaConfigCard(
                                config = config,
                                isCurrentActive = config.jiuxinName == jiuxinName && config.systemPrompt == systemPrompt && config.avatarUrl == avatarUrl,
                                onApply = { onApplyPersonaConfig(config) },
                                onEdit = { onEditPersonaConfig(config) },
                                onDelete = { onDeletePersonaConfig(config) }
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(AppSpacing.Lg),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm)) {
                                Icon(Icons.Rounded.Storage, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(40.dp))
                                Text("暂无保存的舰娘人格", style = AppTypography.BodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("保存当前配置后，可在此查看和管理", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
    }
        Spacer(modifier = Modifier.height(AppSpacing.Xl))
    }
}
