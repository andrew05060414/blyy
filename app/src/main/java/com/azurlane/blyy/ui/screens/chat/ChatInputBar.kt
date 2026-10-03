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

@Composable
internal fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    isDark: Boolean,
    onInputFocus: () -> Unit = {},
    onPlusClick: () -> Unit = {}
) {
    // JUUSTAGRAM 设计：毛玻璃输入栏 bg-white/85 + backdrop-blur(8px)
    // 布局：[+ 按钮 36dp] [圆角输入框 height 40dp] [发送按钮 36dp]
    val focusRequester = remember { FocusRequester() }
    val footerBg = if (isDark) JuusColors.Dark.FooterBg else JuusColors.FooterBg
    val footerBorder = if (isDark) JuusColors.Dark.FooterBorder else JuusColors.FooterBorder
    val inputBg = if (isDark) JuusColors.Dark.InputBg else JuusColors.InputBg
    val sendActive = if (isDark) JuusColors.Dark.SendActive else JuusColors.SendActive
    val sendInactive = if (isDark) JuusColors.Dark.SendInactive else JuusColors.SendInactive
    val textColor = if (isDark) JuusColors.Dark.TextPrimary else JuusColors.TextPrimary
    val hintColor = if (isDark) JuusColors.Dark.TextSecondary else JuusColors.TextSecondary
    val plusIconColor = if (isDark) JuusColors.Dark.TextSecondary else JuusColors.TextSecondary
    val canSend = inputText.isNotBlank() && enabled
    val haptic = rememberBlyyHaptics()

    // 使用 TextFieldValue 精确控制光标位置，避免 String 状态下重组导致的光标跳变
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(inputText))
    }
    // 同步外部状态变更（如清空、切换会话）到 textFieldValue
    LaunchedEffect(inputText) {
        if (textFieldValue.text != inputText) {
            textFieldValue = TextFieldValue(
                text = inputText,
                selection = androidx.compose.ui.text.TextRange(inputText.length)
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(footerBg)
            .drawBehind {
                // 顶部分隔线 border-t
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = footerBorder,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = strokeWidth
                )
            }
            // imePadding + navigationBarsPadding 作用于输入栏：
            // 键盘弹出时推高 ChatInputBar，LazyColumn 的 weight(1f) 可用高度保持稳定。
            // 关键：Row 的高度由 heightIn 约束的 BasicTextField 决定，不随 IME 动画抖动。
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = AppSpacing.Md, vertical = AppSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm)
    ) {
        // JUUSTAGRAM 设计：加号按钮（圆形 36dp 视觉，48dp 触达热区）
        Box(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(36.dp)
                .clip(CircleShape)
                .background(inputBg)
                .clickable {
                    haptic(BlyyHaptic.Tick)
                    onPlusClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Add,
                contentDescription = "添加",
                tint = plusIconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        // JUUSTAGRAM 设计：圆角输入框（rounded-full, bg-juustagram-bg）
        // 修复光标延迟和移位：使用 TextFieldValue + Box 叠加 placeholder 避免条件渲染布局跳变
        val fieldShape = RoundedCornerShape(AppSpacing.Corner.Xl)
        val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
        androidx.compose.foundation.text.BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                onInputChange(newValue.text)
            },
            modifier = Modifier
                .weight(1f)
                // 关键修复：固定高度 40dp，消除多行输入导致的高度变化抖动
                // 原实现 heightIn(min=40, max=120) 会让 Row 高度随输入行数变化，
                // 挤压 LazyColumn 的 weight(1f) 空间，与 IME 动画叠加产生抽搐。
                // 改为固定 40dp + maxLines=1 + 水平滚动，单行输入体验更稳定。
                // 若需多行体验，可在外层加垂直滚动容器，但当前固定高度消除抖动优先级更高。
                .height(40.dp)
                .focusRequester(focusRequester)
                .onFocusEvent { state ->
                    if (state.isFocused) onInputFocus()
                }
                .clip(fieldShape)
                .background(inputBg)
                .padding(horizontal = 14.dp, vertical = AppSpacing.Sm),
            textStyle = AppTypography.BodyMedium.copy(color = textColor),
            // 单行输入：固定高度，避免多行高度变化导致布局抖动
            maxLines = 1,
            cursorBrush = androidx.compose.ui.graphics.SolidColor(if (isDark) JuusPalette.Dark.Primary else JuusPalette.Primary),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
                imeAction = androidx.compose.ui.text.input.ImeAction.Send
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onSend = {
                    if (canSend) {
                        haptic(BlyyHaptic.Confirm)
                        onSend()
                        keyboardController?.hide()
                    }
                }
            ),
            decorationBox = { innerTextField ->
                // 使用 Box 叠加 placeholder 和 innerTextField，避免条件渲染导致的布局跳变
                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth()) {
                    if (inputText.isEmpty()) {
                        Text(
                            "输入消息...",
                            style = AppTypography.BodyMedium,
                            color = hintColor
                        )
                    }
                    innerTextField()
                }
            }
        )
        // JUUSTAGRAM 设计：发送按钮（圆形 36dp 视觉，48dp 触达热区）
        Box(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(36.dp)
                .clip(CircleShape)
                .background(if (canSend) sendActive else sendInactive)
                .clickable(enabled = canSend) {
                    haptic(BlyyHaptic.Confirm)
                    onSend()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.Send,
                contentDescription = "发送",
                tint = if (canSend) {
                    if (isDark) JuusColors.Dark.SendIconActive else Color.White
                } else {
                    if (isDark) Color.White else JuusColors.SendIconInactive
                },
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
