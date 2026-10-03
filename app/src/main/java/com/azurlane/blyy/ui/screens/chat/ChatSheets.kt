package com.azurlane.blyy.ui.screens.chat

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.azurlane.blyy.util.findActivityViewModelStoreOwner
import com.valentinilk.shimmer.shimmer
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.azurlane.blyy.R
import com.azurlane.blyy.data.model.ChatMessage
import com.azurlane.blyy.data.model.ChatMessageType
import com.azurlane.blyy.data.model.ChatSession
import com.azurlane.blyy.data.model.GroupPosition
import com.azurlane.blyy.data.model.PersonaConfig
import com.azurlane.blyy.data.model.Ship
import com.azurlane.blyy.data.model.getGroupPosition
import com.azurlane.blyy.ui.components.AdaptiveScreenBackground
import com.azurlane.blyy.ui.components.BlyyBottomSheet
import com.azurlane.blyy.ui.components.BlyyConfirmDialog
import com.azurlane.blyy.ui.components.BlyyHaptic
import com.azurlane.blyy.ui.components.rememberBlyyHaptics
import com.azurlane.blyy.ui.components.BlyyDragHandle
import com.azurlane.blyy.ui.components.BlyyPanel
import com.azurlane.blyy.ui.components.BlyySectionPanel
import com.azurlane.blyy.ui.components.StableOutlinedTextField
import com.azurlane.blyy.ui.theme.AppSpacing
import com.azurlane.blyy.ui.theme.AppTypography
import com.azurlane.blyy.ui.theme.JuusPalette
import com.azurlane.blyy.ui.theme.LocalIsDark
import com.azurlane.blyy.viewmodel.ConnectionTestState
import com.azurlane.blyy.viewmodel.JiuxinViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.azurlane.blyy.ui.components.RobustAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPickerSheet(viewModel: JiuxinViewModel, currentAvatarUrl: String, onDismiss: () -> Unit, onAvatarSelected: (String) -> Unit) {
    val filteredShips by viewModel.filteredShipList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.shipSearchQuery.collectAsStateWithLifecycle()
    val isDark = LocalIsDark.current
    val scope = rememberCoroutineScope()
    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            scope.launch {
                // 复制到内部存储，避免 content:// URI 重启后丢失权限
                val filePath = viewModel.copyAvatarToInternalStorage(it)
                if (filePath != null) {
                    onAvatarSelected(filePath)
                }
            }
        }
    }
    var avatarTab by remember { mutableStateOf(0) }

    BlyyBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().height(520.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.Lg, vertical = AppSpacing.Md), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "选择头像", style = AppTypography.TitleMediumBold)
                IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, contentDescription = "关闭") }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.Lg), horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm)) {
                com.azurlane.blyy.ui.components.BlyyChip(label = "舰娘头像", selected = avatarTab == 0, onClick = { avatarTab = 0 })
                com.azurlane.blyy.ui.components.BlyyChip(label = "本地上传", selected = avatarTab == 1, onClick = { avatarTab = 1 })
            }
            Spacer(modifier = Modifier.height(AppSpacing.Sm))
            if (avatarTab == 0) {
                StableOutlinedTextField(value = searchQuery, onValueChange = viewModel::setShipSearchQuery, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.Lg), placeholder = { Text("搜索舰娘...") }, singleLine = true, textStyle = AppTypography.BodyMedium, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = if (isDark) JuusColors.Dark.AiName else JuusColors.Primary), shape = RoundedCornerShape(AppSpacing.Corner.Sm))
                Spacer(modifier = Modifier.height(AppSpacing.Sm))
                LazyVerticalGrid(columns = GridCells.Fixed(5), modifier = Modifier.fillMaxSize().padding(horizontal = AppSpacing.Lg), horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm), verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm)) {
                    items(filteredShips, key = { it.name }, contentType = { "ship_avatar" }) { ship ->
                        // 啾信功能仅使用网络加载获取的头像
                        val effectiveAvatar = ship.avatarUrl
                        val isSelected = effectiveAvatar == currentAvatarUrl
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(52.dp).clip(CircleShape).border(if (isSelected) 2.dp else 1.dp, if (isSelected) (if (isDark) JuusColors.Dark.AiName else JuusColors.Primary) else Color.LightGray.copy(alpha = 0.3f), CircleShape).clickable {
                                // 选中舰娘头像时，归一化网络 URL 后回调
                                scope.launch {
                                    val reliablePath = viewModel.resolveAndCopyShipAvatar(ship.name, ship.avatarUrl, ship.archiveType)
                                    onAvatarSelected(reliablePath)
                                }
                            }, contentAlignment = Alignment.Center) { RobustAvatar(url = effectiveAvatar, modifier = Modifier.size(52.dp).clip(CircleShape), fallbackContent = { Icon(Icons.Rounded.Person, null, modifier = Modifier.size(20.dp), tint = (if (isDark) JuusColors.Dark.AiName else JuusColors.Primary).copy(alpha = 0.5f)) }) }
                            Text(text = ship.name, style = AppTypography.CardLabel, maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (isSelected) (if (isDark) JuusColors.Dark.AiName else JuusColors.Primary) else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(52.dp))
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = AppSpacing.Lg), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    if (currentAvatarUrl.isNotBlank()) {
                        RobustAvatar(url = currentAvatarUrl, modifier = Modifier.size(120.dp).clip(CircleShape).border(2.dp, (if (isDark) JuusColors.Dark.AiName else JuusColors.Primary).copy(alpha = 0.3f), CircleShape), fallbackContent = { Icon(Icons.Rounded.Person, null, modifier = Modifier.size(48.dp), tint = (if (isDark) JuusColors.Dark.AiName else JuusColors.Primary).copy(alpha = 0.5f)) })
                        Spacer(modifier = Modifier.height(AppSpacing.Md)); Text("当前头像", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(modifier = Modifier.height(AppSpacing.Lg))
                    }
                    TextButton(onClick = {
                        // 部分 OEM ROM 精简版或无文件管理器的设备会抛 ActivityNotFoundException
                        try {
                            imagePickerLauncher.launch(arrayOf("image/*"))
                        } catch (e: Exception) {
                            Log.e("AvatarPickerSheet", "No image picker available", e)
                            // TODO: 可考虑回退到 PickVisualMedia（Android Photo Picker）
                        }
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm)) {
                            Icon(Icons.Rounded.AddPhotoAlternate, null)
                            Text("从相册选择图片")
                        }
                    }
                    Spacer(modifier = Modifier.height(AppSpacing.Sm))
                    Text("支持 JPG、PNG 格式，建议使用正方形图片", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    if (currentAvatarUrl.isNotBlank()) { Spacer(modifier = Modifier.height(AppSpacing.Lg)); Text("清除头像", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.clickable { onAvatarSelected("") }) }
                }
            }
        }
    }
}

/**
 * 聊天背景选择弹窗
 *
 * 支持三种背景设置方式：
 * 1. 设置当前聊天背景：仅当前会话生效（会话级 backgroundUrl）
 * 2. 设置全部聊天背景：所有会话默认使用（全局 aiChatBackgroundUrl）
 * 3. 恢复默认背景：清除会话级 + 全局背景
 *
 * 使用 PickVisualMedia（Android Photo Picker）代替 OpenDocument（文件管理器），
 * 提供更符合移动端使用习惯的图库选择体验。
 *
 * 设计：底部弹窗 + 当前背景预览 + 操作按钮（区分会话级/全局）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BackgroundPickerSheet(
    viewModel: JiuxinViewModel,
    sessionId: String,
    sessionBackgroundUrl: String,
    globalBackgroundUrl: String,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val accentColor = if (isDark) JuusColors.Dark.AiName else JuusColors.Primary
    val subColor = if (isDark) JuusColors.Dark.TextSecondary else JuusColors.TextSecondary

    // 当前生效的背景（会话级优先，回退到全局）
    val effectiveBackgroundUrl = sessionBackgroundUrl.ifBlank { globalBackgroundUrl }
    // 当前生效的背景来源标签
    val effectiveSourceLabel = when {
        sessionBackgroundUrl.isNotBlank() -> "当前会话背景"
        globalBackgroundUrl.isNotBlank() -> "全局背景"
        else -> "默认背景"
    }

    // PickVisualMedia：Android Photo Picker（真正的图库选择器，无需权限）
    // 优于 OpenDocument（文件管理器）：更流畅、更符合移动端习惯、自动处理权限
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val filePath = viewModel.copyBackgroundToInternalStorage(it)
                if (filePath != null) {
                    // 默认设置为当前会话背景（用户最常见的需求）
                    if (sessionId.isNotBlank()) {
                        viewModel.saveSessionBackgroundUrl(sessionId, filePath)
                        Toast.makeText(context, "已设置当前聊天背景", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.saveChatBackgroundUrl(filePath)
                        Toast.makeText(context, "已设置聊天背景", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "背景图片加载失败，请重试", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    BlyyBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(AppSpacing.Lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm)) {
                    Icon(Icons.Rounded.Image, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    Text(text = "聊天背景", style = AppTypography.TitleMediumBold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "关闭", tint = subColor)
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.Md))

            // 当前背景预览（显示生效中的背景，会话级优先）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(AppSpacing.Corner.Lg))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(AppSpacing.Corner.Lg)),
                contentAlignment = Alignment.Center
            ) {
                if (effectiveBackgroundUrl.isNotBlank()) {
                    AsyncImage(
                        model = effectiveBackgroundUrl,
                        contentDescription = "当前背景预览",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(AppSpacing.Corner.Lg)),
                        contentScale = ContentScale.Crop
                    )
                    // 右下角标识来源
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(AppSpacing.Sm)
                            .clip(RoundedCornerShape(AppSpacing.Corner.Sm))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = AppSpacing.Sm, vertical = AppSpacing.Xxs)
                    ) {
                        Text(text = effectiveSourceLabel, style = AppTypography.LabelSmall, color = Color.White)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(AppSpacing.Xs)) {
                        Icon(
                            Icons.Rounded.Image,
                            contentDescription = null,
                            tint = subColor.copy(alpha = 0.4f),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(text = "默认纯色背景", style = AppTypography.BodySmall, color = subColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.Lg))

            // 主操作按钮：从图库选择（设置为当前聊天背景）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppSpacing.Corner.Md))
                    .background(accentColor)
                    .clickable {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .padding(vertical = AppSpacing.Md),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(AppSpacing.Sm))
                Text(text = "从图库选择背景", style = AppTypography.LabelLarge, color = Color.White)
            }

            Spacer(modifier = Modifier.height(AppSpacing.Sm))

            // 次要操作：设为全部聊天背景（将当前生效的背景应用到全局）
            // 仅当当前背景来自会话级时显示，提供"提升为全局背景"的入口
            if (sessionBackgroundUrl.isNotBlank() && sessionId.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppSpacing.Corner.Md))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                        .clickable {
                            viewModel.saveChatBackgroundUrl(sessionBackgroundUrl)
                            Toast.makeText(context, "已设为全部聊天的默认背景", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = AppSpacing.Md),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(AppSpacing.Sm))
                    Text(text = "设为全部聊天背景", style = AppTypography.LabelLarge, color = accentColor)
                }
                Spacer(modifier = Modifier.height(AppSpacing.Sm))
            }

            // 清除背景按钮（区分清除会话级/全局）
            if (effectiveBackgroundUrl.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppSpacing.Corner.Md))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                        .clickable {
                            if (sessionBackgroundUrl.isNotBlank() && sessionId.isNotBlank()) {
                                // 清除会话级背景（回退到全局或默认）
                                viewModel.saveSessionBackgroundUrl(sessionId, "")
                                Toast.makeText(context, "已清除当前会话背景", Toast.LENGTH_SHORT).show()
                            } else {
                                // 清除全局背景
                                viewModel.saveChatBackgroundUrl("")
                                Toast.makeText(context, "已恢复默认背景", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(vertical = AppSpacing.Md),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(AppSpacing.Sm))
                    Text(
                        text = if (sessionBackgroundUrl.isNotBlank()) "清除当前会话背景" else "恢复默认背景",
                        style = AppTypography.LabelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.Sm))
            Text(
                text = "会话级背景优先于全局背景。建议使用竖屏比例图片，会自适应铺满聊天界面",
                style = AppTypography.BodySmall,
                color = subColor.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = AppSpacing.Xs)
            )
        }
    }
}

// ── 长按消息操作弹窗（精致玻璃质感设计） ──
@Composable
internal fun MessageActionSheet(
    isUser: Boolean,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val primaryColor = if (isDark) JuusColors.Dark.AiName else JuusColors.Primary
    val errorColor = if (isDark) JuusColors.Dark.ErrorText else JuusColors.ErrorText
    val cardBg = if (isDark) JuusPalette.Dark.DialogSurface else JuusPalette.DialogSurface
    val textPrimary = if (isDark) JuusColors.Dark.TextPrimary else JuusColors.TextPrimary
    val textSecondary = if (isDark) JuusColors.Dark.TextSecondary else JuusColors.TextSecondary
    val dividerColor = if (isDark) JuusPalette.Dark.DialogDivider else JuusPalette.DialogDivider

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppSpacing.Corner.Xxl))
                .background(cardBg)
                .padding(vertical = AppSpacing.Sm)
        ) {
            // 顶部统一拖拽手柄
            BlyyDragHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
            // 标题
            Text(
                text = "消息操作",
                style = AppTypography.TitleMediumBold.copy(color = textPrimary),
                modifier = Modifier.padding(start = AppSpacing.Xxl, end = AppSpacing.Xxl, bottom = AppSpacing.Sm)
            )

            // 编辑重发（仅用户消息）
            if (isUser) {
                MessageActionItem(
                    icon = Icons.Rounded.Edit,
                    title = "编辑并重新发送",
                    subtitle = "修改内容后重新生成回复",
                    accentColor = primaryColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    dividerColor = dividerColor,
                    onClick = onEdit
                )
            }

            // 删除消息
            MessageActionItem(
                icon = Icons.Rounded.Delete,
                title = "删除消息",
                subtitle = "仅删除此条消息",
                accentColor = errorColor,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                dividerColor = dividerColor,
                isLast = true,
                onClick = onDelete
            )

            // 取消按钮
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppSpacing.Xs)
                    .clip(RoundedCornerShape(AppSpacing.Corner.None))
                    .background(dividerColor)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.Xs)
                ) {
                    Text(
                        text = "取消",
                        style = AppTypography.LabelLarge.copy(color = textSecondary)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    dividerColor: Color,
    isLast: Boolean = false,
    onClick: () -> Unit
) {
    val haptic = rememberBlyyHaptics()
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    haptic(BlyyHaptic.Tick)
                    onClick()
                }
                .padding(horizontal = AppSpacing.Xxl, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 图标容器 — 圆角方形，带淡色背景
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(AppSpacing.Corner.Md))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            // 文字
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTypography.BodyLarge.copy(fontWeight = FontWeight.SemiBold, color = textPrimary)
                )
                Text(
                    text = subtitle,
                    style = AppTypography.BodySmall.copy(color = textSecondary.copy(alpha = 0.7f)),
                    modifier = Modifier.padding(top = AppSpacing.Xxs)
                )
            }
        }
        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 80.dp)
                    .height(0.5.dp)
                    .background(dividerColor)
            )
        }
    }
}

// ── 编辑重发弹窗（精致玻璃质感设计） ──
@Composable
internal fun MessageEditSheet(
    originalContent: String,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var editContent by remember(originalContent) { mutableStateOf(originalContent) }
    val primaryColor = if (isDark) JuusColors.Dark.AiName else JuusColors.Primary
    val cardBg = if (isDark) JuusPalette.Dark.DialogSurface else JuusPalette.DialogSurface
    val textPrimary = if (isDark) JuusColors.Dark.TextPrimary else JuusColors.TextPrimary
    val textSecondary = if (isDark) JuusColors.Dark.TextSecondary else JuusColors.TextSecondary
    val dividerColor = if (isDark) JuusPalette.Dark.DialogDivider else JuusPalette.DialogDivider
    val warningBg = if (isDark) JuusPalette.Dark.DialogWarningBg else JuusPalette.DialogWarningBg

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppSpacing.Corner.Xxl))
                .background(cardBg)
                .padding(vertical = AppSpacing.Sm)
        ) {
            // 顶部统一拖拽手柄
            BlyyDragHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
            // 标题行：图标 + 标题
            Row(
                modifier = Modifier.padding(start = AppSpacing.Xxl, end = AppSpacing.Xxl, bottom = AppSpacing.Lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(AppSpacing.Corner.Chamfer))
                        .background(primaryColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "编辑并重新发送",
                    style = AppTypography.TitleMediumBold.copy(color = textPrimary)
                )
            }

            // 提示横幅
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = AppSpacing.Xxl, end = AppSpacing.Xxl, bottom = AppSpacing.Lg)
                    .clip(RoundedCornerShape(AppSpacing.Corner.Md))
                    .background(warningBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "发送后将删除此消息及后续回复，重新生成",
                    style = AppTypography.BodySmall.copy(color = textSecondary)
                )
            }

            // 编辑输入框
            StableOutlinedTextField(
                value = editContent,
                onValueChange = { editContent = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = AppSpacing.Xxl, end = AppSpacing.Xxl, bottom = AppSpacing.Xl)
                    .heightIn(min = 80.dp),
                label = { Text("消息内容") },
                textStyle = AppTypography.BodyMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = dividerColor
                ),
                shape = RoundedCornerShape(AppSpacing.Corner.Xs2)
            )

            // 底部按钮区
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = AppSpacing.Xxl, end = AppSpacing.Xxl, bottom = AppSpacing.Sm),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("取消", style = AppTypography.LabelLarge.copy(color = textSecondary))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppSpacing.Corner.Md))
                        .background(primaryColor)
                        .clickable { onSend(editContent) }
                        .padding(horizontal = AppSpacing.Xxl, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "发送",
                            style = AppTypography.LabelLargeBold.copy(color = Color.White)
                        )
                    }
                }
            }
        }
    }
}
