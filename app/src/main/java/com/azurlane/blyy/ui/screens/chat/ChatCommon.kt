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

internal val EmptyCallback: () -> Unit = {}

// ── JUUSTAGRAM 设计规范色彩系统（映射到 JuusPalette） ──
// 来源：UI_work/juustagram-messaging-ui/docs/design-spec.md
// 主色 #5BA4E6，气泡 incoming=#FFFFFF / outgoing=#5BA4E6
internal object JuusColors {
    val Primary = JuusPalette.Primary              // #5BA4E6
    val PrimaryLight = JuusPalette.PrimaryLight    // #D6EBFF
    val PrimaryBg = JuusPalette.Bg                 // #F0F4F8
    val UserBubble = JuusPalette.BubbleOutgoing    // #5BA4E6
    val AiBubble = JuusPalette.BubbleIncoming      // #FFFFFF
    val AiBubbleBorder = JuusPalette.Border        // #E4E8EC
    val AiName = JuusPalette.Primary               // #5BA4E6
    val VoiceBubble = JuusPalette.VoiceBubble      // 语音气泡粉底
    val VoiceBorder = JuusPalette.VoiceBorder      // 语音边框粉
    val VoiceAccent = JuusPalette.VoiceAccent      // 语音强调粉
    val FooterBg = JuusPalette.Glass85             // 毛玻璃 85% 白
    val FooterBorder = JuusPalette.Divider         // #EDEFF2
    val InputBg = JuusPalette.Bg                   // #F0F4F8
    val InputBorder = JuusPalette.BorderLight      // #EFF2F5
    val InputFocusBorder = JuusPalette.Primary     // #5BA4E6
    val SendActive = JuusPalette.Primary           // #5BA4E6
    val SendInactive = JuusPalette.BorderLight     // #EFF2F5
    val SendIconInactive = JuusPalette.TextSecondary // #555566
    val TextPrimary = JuusPalette.TextPrimary      // #1A1A2E
    val TextSecondary = JuusPalette.TextSecondary  // #555566
    val TextOnPrimary = JuusPalette.TextOnPrimary  // #FFFFFF
    val TextTime = JuusPalette.TextTertiary        // #8899AA
    val SystemText = JuusPalette.TextTertiary      // #8899AA
    val AvatarBorder = JuusPalette.BorderLight     // #EFF2F5
    val TypingDot = JuusPalette.Primary            // #5BA4E6
    val ErrorBg = JuusPalette.ErrorBg              // 错误背景
    val ErrorText = JuusPalette.ErrorText          // 错误文字

    // 深色模式
    object Dark {
        val PrimaryBg = JuusPalette.Dark.Bg
        val UserBubble = JuusPalette.Dark.BubbleOutgoing
        val AiBubble = JuusPalette.Dark.BubbleIncoming
        val AiBubbleBorder = JuusPalette.Dark.Border
        val AiName = JuusPalette.Dark.Primary
        val VoiceBubble = JuusPalette.Dark.VoiceBubble
        val VoiceBorder = JuusPalette.Dark.VoiceBorder
        val VoiceAccent = JuusPalette.Dark.VoiceAccent
        val FooterBg = JuusPalette.Dark.Glass85
        val FooterBorder = JuusPalette.Dark.Divider
        val InputBg = JuusPalette.Dark.Bg
        val InputBorder = JuusPalette.Dark.Border
        val InputFocusBorder = JuusPalette.Dark.Primary
        val SendActive = JuusPalette.Dark.Primary
        val SendInactive = JuusPalette.Dark.Border
        val SendIconActive = JuusPalette.Dark.Bg
        val TextPrimary = JuusPalette.Dark.TextPrimary
        val TextSecondary = JuusPalette.Dark.TextSecondary
        val TextOnPrimary = Color.White
        val TextTime = JuusPalette.Dark.TextTertiary
        val SystemText = JuusPalette.Dark.TextTertiary
        val AvatarBorder = JuusPalette.Dark.Border
        val TypingDot = JuusPalette.Dark.Primary
        val ErrorBg = JuusPalette.Dark.ErrorBg
        val ErrorText = JuusPalette.Dark.ErrorText
    }
}
// SimpleDateFormat 实例缓存 — 仅在主线程使用，避免每次调用都新建实例（内部含 Calendar、TimeZone 等数百字节）
// 注意：SimpleDateFormat 非线程安全，这里仅供 Compose 主线程调用，未来如需后台调用请改用 ThreadLocal
private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
private val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

internal fun formatTime(timestamp: Long): String = timeFormatter.format(Date(timestamp))

internal fun formatSessionTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "刚刚"
        diff < 86_400_000 -> timeFormatter.format(Date(timestamp))
        diff < 604_800_000 -> "${diff / 86_400_000}天前"
        else -> dateFormatter.format(Date(timestamp))
    }
}
