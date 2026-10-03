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

// ── JUUSTAGRAM 打字动画指示器（28dp 头像） ──
@Composable
internal fun TypingIndicator(
    jiuxinName: String,
    avatarUrl: String,
    isDark: Boolean
) {
    val dotColor = if (isDark) JuusColors.Dark.TypingDot else JuusColors.TypingDot
    val bubbleBg = if (isDark) JuusColors.Dark.AiBubble else JuusColors.AiBubble
    val bubbleBorder = if (isDark) JuusColors.Dark.AiBubbleBorder else JuusColors.AiBubbleBorder
    val nameColor = if (isDark) JuusColors.Dark.AiName else JuusColors.AiName
    val avatarBorder = if (isDark) JuusColors.Dark.AvatarBorder else JuusColors.AvatarBorder

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.Md, vertical = AppSpacing.Xs),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // AI头像（40dp — 增大舰娘头像尺寸）
        RobustAvatar(
            url = avatarUrl,
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(bubbleBg)
                .border(1.dp, avatarBorder, CircleShape),
            fallbackContent = {
                Icon(Icons.Rounded.SmartToy, contentDescription = null, modifier = Modifier.size(22.dp), tint = nameColor)
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = jiuxinName,
                style = AppTypography.LabelSmallMedium.copy(color = nameColor),
                modifier = Modifier.padding(start = AppSpacing.Xxs, bottom = 3.dp)
            )
            // 打字气泡 — 与 AI 消息气泡共用同一分组形状（独立消息）
            Box(
                modifier = Modifier
                    .clip(incomingBubbleShape(GroupPosition.SINGLE))
                    .background(bubbleBg)
                    .border(1.dp, bubbleBorder, incomingBubbleShape(GroupPosition.SINGLE))
                    .padding(horizontal = AppSpacing.Md, vertical = AppSpacing.Sm)
            ) {
                TypingDots(dotColor = dotColor)
            }
        }
    }
}

// ── 三个跳动圆点动画 ──
@Composable
private fun TypingDots(dotColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { index ->
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 400, delayMillis = index * 150, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )
            Box(
                modifier = Modifier
                    .offset(y = offsetY.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}
private fun incomingBubbleShape(groupPos: GroupPosition): RoundedCornerShape = when (groupPos) {
    GroupPosition.SINGLE -> RoundedCornerShape(AppSpacing.Corner.Md, AppSpacing.Corner.Md, AppSpacing.Corner.Md, AppSpacing.Corner.Xs) // 12 12 12 4
    GroupPosition.FIRST -> RoundedCornerShape(AppSpacing.Corner.Md, AppSpacing.Corner.Md, AppSpacing.Corner.Xs, AppSpacing.Corner.Xs)   // 12 12 4 4
    GroupPosition.MIDDLE -> RoundedCornerShape(AppSpacing.Corner.Xs, AppSpacing.Corner.Md, AppSpacing.Corner.Xs, AppSpacing.Corner.Xs)   // 4 12 4 4
    GroupPosition.LAST -> RoundedCornerShape(AppSpacing.Corner.Xs, AppSpacing.Corner.Md, AppSpacing.Corner.Md, AppSpacing.Corner.Xs)    // 4 12 12 4
}

private fun outgoingBubbleShape(groupPos: GroupPosition): RoundedCornerShape = when (groupPos) {
    GroupPosition.SINGLE -> RoundedCornerShape(AppSpacing.Corner.Md, AppSpacing.Corner.Md, AppSpacing.Corner.Xs, AppSpacing.Corner.Md) // 12 12 4 12
    GroupPosition.FIRST -> RoundedCornerShape(AppSpacing.Corner.Md, AppSpacing.Corner.Md, AppSpacing.Corner.Xs, AppSpacing.Corner.Xs)   // 12 12 4 4
    GroupPosition.MIDDLE -> RoundedCornerShape(AppSpacing.Corner.Xs, AppSpacing.Corner.Xs, AppSpacing.Corner.Xs, AppSpacing.Corner.Md)   // 4 4 4 12
    GroupPosition.LAST -> RoundedCornerShape(AppSpacing.Corner.Xs, AppSpacing.Corner.Md, AppSpacing.Corner.Xs, AppSpacing.Corner.Md)    // 4 12 4 12
}

@Composable
internal fun MessageBubble(
    message: ChatMessage,
    jiuxinName: String,
    jiuxinAvatarUrl: String,
    userName: String,
    userAvatarUrl: String,
    isDark: Boolean,
    isPlaying: Boolean,
    maxWidth: Dp,
    groupPosition: GroupPosition,
    topPadding: Dp,
    isGroup: Boolean = false,
    onVoiceClick: () -> Unit,
    onStickerClick: () -> Unit,
    onMessageLongClick: () -> Unit = {}
) {
    val context = LocalContext.current
    // 是否显示头像/名称（仅组首和独立消息）
    val showAvatar = groupPosition == GroupPosition.SINGLE || groupPosition == GroupPosition.FIRST
    // 是否显示时间戳（仅独立消息和组末条）
    val showTimestamp = groupPosition == GroupPosition.SINGLE || groupPosition == GroupPosition.LAST

    when (message.type) {
        ChatMessageType.USER.name -> {
            // ── 用户消息：JUUSTAGRAM 蓝色气泡 #5BA4E6，右对齐，无头像（设计规范） ──
            val bubbleColor = if (isDark) JuusColors.Dark.UserBubble else JuusColors.UserBubble
            val bubbleShape = outgoingBubbleShape(groupPosition)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = topPadding, start = AppSpacing.Md, end = AppSpacing.Md),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.widthIn(max = maxWidth), horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(bubbleShape)
                            .background(bubbleColor)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = onMessageLongClick
                            )
                            .padding(horizontal = AppSpacing.Md, vertical = AppSpacing.Sm)
                    ) {
                        Text(
                            text = message.content,
                            style = AppTypography.BubbleText,
                            color = JuusColors.TextOnPrimary
                        )
                    }
                    if (showTimestamp) {
                        Text(
                            text = formatTime(message.timestamp),
                            style = AppTypography.LabelMedium,
                            color = if (isDark) JuusColors.Dark.TextTime else JuusColors.TextTime,
                            modifier = Modifier.padding(end = AppSpacing.Xs, top = AppSpacing.Xxs)
                        )
                    }
                }
            }
        }

        ChatMessageType.AI.name -> {
            // ── AI消息：JUUSTAGRAM 白色气泡，左对齐，40dp 头像仅组首显示 ──
            // 群聊扩展：群聊会话使用消息自带的 avatarUrl（各舰娘自己的头像），私聊用会话级头像
            val bubbleBg = if (isDark) JuusColors.Dark.AiBubble else JuusColors.AiBubble
            val bubbleBorder = if (isDark) JuusColors.Dark.AiBubbleBorder else JuusColors.AiBubbleBorder
            val textColor = if (isDark) JuusColors.Dark.TextPrimary else JuusColors.TextPrimary
            val avatarBorder = if (isDark) JuusColors.Dark.AvatarBorder else JuusColors.AvatarBorder
            val nameColor = if (isDark) JuusColors.Dark.AiName else JuusColors.AiName
            val bubbleShape = incomingBubbleShape(groupPosition)
            // 群聊：优先使用消息自带的发送者头像/名称；私聊：回退到会话级头像
            val displayAvatar = if (isGroup && message.avatarUrl.isNotBlank()) message.avatarUrl else jiuxinAvatarUrl
            val displaySenderName = if (isGroup && message.shipName.isNotBlank()) message.shipName else ""

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = topPadding, start = AppSpacing.Md, end = AppSpacing.Md),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                // 头像或占位（40dp — 增大舰娘头像尺寸）
                if (showAvatar) {
                    RobustAvatar(
                        url = displayAvatar,
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                            .background(bubbleBg)
                            .border(1.dp, avatarBorder, CircleShape),
                        fallbackContent = {
                            Icon(Icons.Rounded.SmartToy, contentDescription = null, modifier = Modifier.size(22.dp), tint = JuusColors.AiName)
                        }
                    )
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.widthIn(max = maxWidth)) {
                    // 群聊：组首消息上方显示发送者名称
                    if (isGroup && showAvatar && displaySenderName.isNotBlank()) {
                        Text(
                            text = displaySenderName,
                            style = AppTypography.LabelMediumSemiBold,
                            color = nameColor,
                            modifier = Modifier.padding(start = AppSpacing.Xxs, bottom = AppSpacing.Xxs)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(bubbleShape)
                            .background(bubbleBg)
                            .border(1.dp, bubbleBorder, bubbleShape)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = onMessageLongClick
                            )
                            .padding(horizontal = AppSpacing.Md, vertical = AppSpacing.Sm)
                    ) {
                        Text(
                            text = message.content,
                            style = AppTypography.BubbleText,
                                color = textColor
                        )
                    }
                    if (showTimestamp) {
                        Text(
                            text = formatTime(message.timestamp),
                            style = AppTypography.LabelMedium,
                            color = if (isDark) JuusColors.Dark.TextTime else JuusColors.TextTime,
                            modifier = Modifier.padding(start = AppSpacing.Xxs, top = AppSpacing.Xxs)
                        )
                    }
                }
            }
        }

        ChatMessageType.VOICE.name -> {
            // ── 语音消息：粉色气泡，40dp 头像仅组首显示 ──
            val bubbleBg = if (isDark) JuusColors.Dark.VoiceBubble else JuusColors.VoiceBubble
            val bubbleBorder = if (isDark) JuusColors.Dark.VoiceBorder else JuusColors.VoiceBorder
            val voiceAccent = if (isDark) JuusColors.Dark.VoiceAccent else JuusColors.VoiceAccent
            val textColor = if (isDark) JuusColors.Dark.TextPrimary else JuusColors.TextPrimary
            val avatarBorder = if (isDark) JuusColors.Dark.AvatarBorder else JuusColors.AvatarBorder
            val nameColor = if (isDark) JuusColors.Dark.AiName else JuusColors.AiName
            val bubbleShape = incomingBubbleShape(groupPosition)
            val voiceSenderName = if (isGroup && message.shipName.isNotBlank()) message.shipName else ""

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = topPadding, start = AppSpacing.Md, end = AppSpacing.Md),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                if (showAvatar) {
                    RobustAvatar(
                        url = message.avatarUrl,
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                            .background(bubbleBg)
                            .border(1.dp, bubbleBorder, CircleShape),
                        fallbackContent = {
                            Icon(Icons.Rounded.SmartToy, contentDescription = null, modifier = Modifier.size(22.dp), tint = voiceAccent)
                        }
                    )
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.widthIn(max = maxWidth)) {
                    // 群聊：组首消息上方显示发送者名称
                    if (isGroup && showAvatar && voiceSenderName.isNotBlank()) {
                        Text(
                            text = voiceSenderName,
                            style = AppTypography.LabelMediumSemiBold,
                            color = nameColor,
                            modifier = Modifier.padding(start = AppSpacing.Xxs, bottom = AppSpacing.Xxs)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(bubbleShape)
                            .background(bubbleBg)
                            .border(1.dp, bubbleBorder, bubbleShape)
                            .combinedClickable(
                                onClick = onVoiceClick,
                                onLongClick = onMessageLongClick
                            )
                            .padding(horizontal = AppSpacing.Md, vertical = AppSpacing.Sm)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = voiceAccent
                            )
                            Text(
                                text = message.dialogue.ifBlank { "语音消息" },
                                style = AppTypography.BubbleText,
                                color = textColor
                            )
                        }
                    }
                    if (showTimestamp) {
                        Text(
                            text = formatTime(message.timestamp),
                            style = AppTypography.LabelMedium,
                            color = if (isDark) JuusColors.Dark.TextTime else JuusColors.TextTime,
                            modifier = Modifier.padding(start = AppSpacing.Xxs, top = AppSpacing.Xxs)
                        )
                    }
                }
            }
        }

        ChatMessageType.STICKER.name -> {
            // ── 表情包消息：40dp 头像仅组首显示 ──
            val bubbleBg = if (isDark) JuusColors.Dark.AiBubble else JuusColors.AiBubble
            val nameColor = if (isDark) JuusColors.Dark.AiName else JuusColors.AiName
            val avatarBorder = if (isDark) JuusColors.Dark.AvatarBorder else JuusColors.AvatarBorder
            val stickerSenderName = if (isGroup && message.shipName.isNotBlank()) message.shipName else ""

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = topPadding, start = AppSpacing.Md, end = AppSpacing.Md),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                if (showAvatar) {
                    RobustAvatar(
                        url = message.avatarUrl,
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                            .background(bubbleBg)
                            .border(1.dp, avatarBorder, CircleShape),
                        fallbackContent = {
                            Icon(Icons.Rounded.SmartToy, contentDescription = null, modifier = Modifier.size(22.dp), tint = nameColor)
                        }
                    )
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.widthIn(max = maxWidth)) {
                    // 群聊：组首消息上方显示发送者名称
                    if (isGroup && showAvatar && stickerSenderName.isNotBlank()) {
                        Text(
                            text = stickerSenderName,
                            style = AppTypography.LabelMediumSemiBold,
                            color = nameColor,
                            modifier = Modifier.padding(start = AppSpacing.Xxs, bottom = AppSpacing.Xxs)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .widthIn(max = 160.dp)
                            .heightIn(min = 60.dp, max = 180.dp)
                            .clip(RoundedCornerShape(AppSpacing.Corner.Sm))
                            .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.03f))
                            .combinedClickable(
                                onClick = onStickerClick,
                                onLongClick = onMessageLongClick
                            )
                    ) {
                        var isLoading by remember(message.id) { mutableStateOf(true) }
                        var isError by remember(message.id) { mutableStateOf(false) }

                        AsyncImage(
                            model = remember(message.stickerUrl) {
                                ImageRequest.Builder(context)
                                    .data(message.stickerUrl)
                                    .crossfade(true)
                                    .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                                    .addHeader("Referer", "https://wiki.biligame.com/")
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .build()
                            },
                            contentDescription = "表情包",
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (isLoading) Modifier.shimmer() else Modifier),
                            contentScale = ContentScale.FillWidth,
                            onState = { state ->
                                isLoading = state is AsyncImagePainter.State.Loading
                                isError = state is AsyncImagePainter.State.Error
                            }
                        )

                        if (isError) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.Lg, horizontal = AppSpacing.Md),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = message.content.ifBlank { "[表情包]" },
                                    style = AppTypography.BodySmall.copy(
                                        color = nameColor.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }
                    if (showTimestamp) {
                        Text(
                            text = formatTime(message.timestamp),
                            style = AppTypography.LabelMedium,
                            color = if (isDark) JuusColors.Dark.TextTime else JuusColors.TextTime,
                            modifier = Modifier.padding(start = AppSpacing.Xxs, top = AppSpacing.Xxs)
                        )
                    }
                }
            }
        }

        ChatMessageType.SYSTEM.name -> {
            // ── 系统消息：居中灰色文字 ──
            Box(modifier = Modifier.fillMaxWidth().padding(top = topPadding, bottom = 6.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = message.content,
                    style = AppTypography.LabelMedium,
                    color = if (isDark) JuusColors.Dark.SystemText else JuusColors.SystemText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
