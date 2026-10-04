package com.azurlane.blyy.ui.components

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

// ── 头像 URL 复合格式编解码 ──
// 格式: primary||fallback  例如: file:///data/.../ship_xxx.jpg||https://webstatic.cn/xxx.png
// 无 fallback 时为普通 URL，兼容旧数据
private const val AVATAR_DELIMITER = "||"

private fun encodeAvatarUrl(primary: String, fallback: String?): String {
    if (fallback.isNullOrBlank() || fallback == primary) return primary
    return "$primary$AVATAR_DELIMITER$fallback"
}

private fun decodeAvatarUrl(url: String): Pair<String, String?> {
    if (url.isBlank()) return "" to null
    val idx = url.indexOf(AVATAR_DELIMITER)
    return if (idx >= 0) {
        url.substring(0, idx) to url.substring(idx + AVATAR_DELIMITER.length).takeIf { it.isNotBlank() }
    } else {
        url to null
    }
}
/**
 * 健壮头像加载组件：优先加载本地文件头像，加载失败时自动回退到网络 URL。
 *
 * - 支持 file://（本地文件）、file:/（asset）、content://（相册）、https://（网络）等所有 URI
 * - 支持 [encodeAvatarUrl] 编码的复合 URL，自动解析 primary/fallback
 * - 两个 URL 均失败时显示 [fallbackContent]
 *
 * @param url 头像 URL（可为复合格式 primary||fallback）
 * @param modifier 尺寸与裁剪修饰符
 * @param contentDescription 无障碍描述（头像具备语义时传入，如舰娘名）
 * @param fallbackContent 两个 URL 均失败时显示的内容（通常为 Icon）
 */
@Composable
fun RobustAvatar(
    url: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
    fallbackContent: @Composable () -> Unit
) {
    val (primary, fallback) = remember(url) { decodeAvatarUrl(url) }
    // 当前正在尝试加载的 URL，先尝试 primary，失败后切换到 fallback
    var currentTarget by remember(url) { mutableStateOf(primary) }
    // 两个 URL 均失败时为 true
    var allFailed by remember(url) { mutableStateOf(false) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (currentTarget.isNotBlank() && !allFailed) {
            AsyncImage(
                model = currentTarget,
                contentDescription = contentDescription,
                // B8 修复：不再复用外层 modifier（同一 modifier 在 Box 与 AsyncImage 上
                // 双重应用），改为铺满 Box，由 Box 承接调用方的尺寸/裁剪约束
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                onState = { state ->
                    if (state is AsyncImagePainter.State.Error) {
                        if (fallback != null && currentTarget != fallback) {
                            // primary 失败，切换到 fallback（网络 URL）
                            currentTarget = fallback
                        } else {
                            // 无 fallback 或 fallback 也失败
                            allFailed = true
                        }
                    }
                }
            )
        }
        if (currentTarget.isBlank() || allFailed) {
            fallbackContent()
        }
    }
}
