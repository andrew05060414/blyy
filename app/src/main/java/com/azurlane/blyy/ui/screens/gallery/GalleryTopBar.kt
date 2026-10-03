package com.azurlane.blyy.ui.screens.gallery

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.azurlane.blyy.data.model.Ship
import com.azurlane.blyy.data.model.StudentFilterData
import com.azurlane.blyy.ui.components.AdaptiveScreenBackground
import com.azurlane.blyy.ui.components.BlyyBottomSheet
import com.azurlane.blyy.ui.components.BlyyEmptyState
import com.azurlane.blyy.ui.components.BlyyTopBar
import com.azurlane.blyy.ui.components.ShipCard
import com.azurlane.blyy.ui.components.ShipCardShimmer
import com.azurlane.blyy.ui.theme.*
import com.azurlane.blyy.viewmodel.GalleryIntent
import com.azurlane.blyy.viewmodel.GalleryViewState
import com.azurlane.blyy.ui.components.rememberBlyyHaptics
import com.azurlane.blyy.ui.components.BlyyHaptic
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * 自适应船坞顶部栏 — 根据UI风格自动切换布局
 * Command Center：BlyyTopBar + 玻璃搜索栏（HUD风格）
 * Classic：集成式 Material Design 顶栏 + 搜索框
 */
@Composable
internal fun AdaptiveGalleryTopBar(
    title: String,
    totalCount: Int,
    filteredCount: Int,
    searchInput: String,
    onSearchInputChange: (String) -> Unit,
    isSearchFocused: Boolean,
    onSearchFocusChange: (Boolean) -> Unit,
    searchFocusRequester: FocusRequester,
    searchHistory: List<String>,
    onHistoryItemClick: (String) -> Unit,
    onClearHistory: () -> Unit,
    onRemoveHistoryItem: (String) -> Unit,
    onSubmitSearch: (String) -> Unit,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    onFilterClick: () -> Unit,
    hasActiveFilters: Boolean,
    activeFilterCount: Int,
    archiveType: com.azurlane.blyy.viewmodel.ArchiveType = com.azurlane.blyy.viewmodel.ArchiveType.DOCK,
    onSwitchArchive: (com.azurlane.blyy.viewmodel.ArchiveType) -> Unit = {},
    isRefreshing: Boolean = false,
    isCacheHit: Boolean = false,
    cacheTimestamp: Long = 0L
) {
    val uiStyle = LocalUiStyle.current
    val isCommandCenter = uiStyle.isCommandCenter()
    val entityLabel = when (archiveType) {
        com.azurlane.blyy.viewmodel.ArchiveType.DOCK -> "舰娘"
        com.azurlane.blyy.viewmodel.ArchiveType.STUDENT -> "学生"
    }

    Column {
        if (isCommandCenter) {
            // Command Center 风格：档案切换器整合到顶部栏 actions 中
            BlyyTopBar(
                title = title,
                subtitle = "共 $totalCount 位$entityLabel"
            ) {
                // 顶部栏右侧：紧凑型档案切换器
                CompactArchiveSwitcher(
                    archiveType = archiveType,
                    onSwitchArchive = onSwitchArchive
                )
            }
            // 后台刷新进度条 — 细线动画
            AnimatedVisibility(
                visible = isRefreshing,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.Screen.Horizontal),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            }
            GallerySearchBar(
                searchInput = searchInput,
                onSearchInputChange = onSearchInputChange,
                isSearchFocused = isSearchFocused,
                onSearchFocusChange = onSearchFocusChange,
                searchFocusRequester = searchFocusRequester,
                onFilterClick = onFilterClick,
                hasActiveFilters = hasActiveFilters,
                activeFilterCount = activeFilterCount,
                onSubmitSearch = onSubmitSearch,
                entityLabel = entityLabel
            )
        } else {
            // Classic 风格：档案切换器整合到标题卡片中
            ClassicGallerySearchBar(
                title = title,
                totalCount = totalCount,
                filteredCount = filteredCount,
                searchInput = searchInput,
                onSearchInputChange = onSearchInputChange,
                isSearchFocused = isSearchFocused,
                onSearchFocusChange = onSearchFocusChange,
                searchFocusRequester = searchFocusRequester,
                onFilterClick = onFilterClick,
                hasActiveFilters = hasActiveFilters,
                activeFilterCount = activeFilterCount,
                onSubmitSearch = onSubmitSearch,
                archiveType = archiveType,
                onSwitchArchive = onSwitchArchive,
                isRefreshing = isRefreshing,
                entityLabel = entityLabel
            )
        }

        // 搜索建议/历史下拉面板
        SearchDropdownPanel(
            isSearchFocused = isSearchFocused,
            searchInput = searchInput,
            searchHistory = searchHistory,
            suggestions = suggestions,
            onHistoryItemClick = onHistoryItemClick,
            onSuggestionClick = onSuggestionClick,
            onClearHistory = onClearHistory,
            onRemoveHistoryItem = onRemoveHistoryItem
        )

        // 结果计数徽章
        if (filteredCount != totalCount) {
            Spacer(modifier = Modifier.height(AppSpacing.Xs))
            ResultCountBadge(filteredCount = filteredCount, totalCount = totalCount)
        }
        Spacer(modifier = Modifier.height(AppSpacing.Md))
    }
}

/**
 * Command Center 风格搜索栏 — HUD 玻璃面板 + 聚焦高亮 + 动画清除按钮
 */
@Composable
private fun GallerySearchBar(
    searchInput: String,
    onSearchInputChange: (String) -> Unit,
    isSearchFocused: Boolean,
    onSearchFocusChange: (Boolean) -> Unit,
    searchFocusRequester: FocusRequester,
    onFilterClick: () -> Unit,
    hasActiveFilters: Boolean,
    activeFilterCount: Int,
    onSubmitSearch: (String) -> Unit,
    entityLabel: String = "舰娘"
) {
    val isDark = LocalIsDark.current
    val isWatch = isWatchScreen()

    val glassSurface = if (isDark) AppColors.GlassSurfaceDark else AppColors.GlassSurfaceLight
    val glassBorder = if (isDark) AppColors.GlassBorderDark else AppColors.GlassBorderLight

    val searchIconScale by animateFloatAsState(
        targetValue = if (searchInput.isNotEmpty()) 1.1f else 1f,
        animationSpec = AppAnimation.Specs.scale(),
        label = "SearchIconScale"
    )

    // 聚焦时边框高亮 + 阴影增强
    val borderAlpha by animateFloatAsState(
        targetValue = if (isSearchFocused) 1f else 0.5f,
        animationSpec = AppAnimation.Specs.fast(),
        label = "BorderAlpha"
    )
    val iconBgAlpha by animateFloatAsState(
        targetValue = if (isSearchFocused) 0.45f else 0.3f,
        animationSpec = AppAnimation.Specs.fast(),
        label = "IconBgAlpha"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.Screen.Horizontal)
            .height(if (isWatch) AppSpacing.Height.Input - 8.dp else AppSpacing.Height.Input),
        shape = BlyyShapes.PanelMedium,
        color = glassSurface.copy(alpha = if (isSearchFocused) 0.98f else 0.92f),
        shadowElevation = if (isSearchFocused) AppSpacing.Elevation.Lg else AppSpacing.Elevation.Md
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = AppSpacing.Border.Thin,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            glassBorder.copy(alpha = borderAlpha),
                            glassBorder.copy(alpha = borderAlpha * 0.2f)
                        )
                    ),
                    shape = BlyyShapes.PanelMedium
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppSpacing.Padding.InputHorizontal),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 搜索图标 — 带圆形背景
                Box(
                    modifier = Modifier
                        .size(if (isWatch) 28.dp else 36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = iconBgAlpha),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(if (isWatch) AppSpacing.Icon.Sm else AppSpacing.Icon.Md)
                            .scale(searchIconScale)
                    )
                }

                Spacer(modifier = Modifier.width(AppSpacing.Md))

                // 输入区域
                Box(modifier = Modifier.weight(1f)) {
                    if (searchInput.isEmpty()) {
                        Text(
                            text = "搜索$entityLabel...",
                            style = AppTypography.BodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    BasicTextField(
                        value = searchInput,
                        onValueChange = onSearchInputChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocusRequester)
                            .onFocusChanged { onSearchFocusChange(it.isFocused) },
                        textStyle = AppTypography.BodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { onSubmitSearch(searchInput) }
                        )
                    )
                }

                // 清除按钮 — 带动画进出
                AnimatedVisibility(
                    visible = searchInput.isNotEmpty(),
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    IconButton(
                        onClick = {
                            onSearchInputChange("")
                            searchFocusRequester.requestFocus()
                        },
                        modifier = Modifier.size(if (isWatch) 28.dp else 32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "清除",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(AppSpacing.Icon.Sm)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(AppSpacing.Xs))

                // 筛选按钮 — 带激活计数徽章
                ModernFilterButton(
                    hasActiveFilters = hasActiveFilters,
                    activeFilterCount = activeFilterCount,
                    onClick = onFilterClick,
                    isWatch = isWatch
                )
            }
        }
    }
}

/**
 * 经典风格船坞搜索栏 — 双层卡片结构
 * 标题卡片（带舰娘总数 + 档案切换器） + 独立搜索卡片
 *
 * 设计优化：
 * - 标题卡片使用 surfaceContainerHigh 背景 + outlineVariant 边框，与 ClassicTopBar 配色协调
 * - 标题左侧添加主色装饰条，增强视觉锚点
 * - 搜索框添加搜索图标圆形背景装饰，与 Command Center 风格视觉一致
 * - 未聚焦时保留淡边框，避免突兀；聚焦时主色边框高亮
 * - 暗色下提高对比度，确保文字清晰可读
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassicGallerySearchBar(
    title: String,
    totalCount: Int,
    filteredCount: Int,
    searchInput: String,
    onSearchInputChange: (String) -> Unit,
    isSearchFocused: Boolean,
    onSearchFocusChange: (Boolean) -> Unit,
    searchFocusRequester: FocusRequester,
    onFilterClick: () -> Unit,
    hasActiveFilters: Boolean,
    activeFilterCount: Int,
    onSubmitSearch: (String) -> Unit,
    archiveType: com.azurlane.blyy.viewmodel.ArchiveType = com.azurlane.blyy.viewmodel.ArchiveType.DOCK,
    onSwitchArchive: (com.azurlane.blyy.viewmodel.ArchiveType) -> Unit = {},
    isRefreshing: Boolean = false,
    entityLabel: String = "舰娘"
) {
    val isDark = LocalIsDark.current
    val isWatch = isWatchScreen()

    // 聚焦时搜索框边框高亮（未聚焦时保留淡边框，避免突兀）
    val searchBorderAlpha by animateFloatAsState(
        targetValue = if (isSearchFocused) 1f else 0.4f,
        animationSpec = AppAnimation.Specs.fast(),
        label = "SearchBorderAlpha"
    )
    // 搜索图标圆形背景透明度动画
    val searchIconBgAlpha by animateFloatAsState(
        targetValue = if (isSearchFocused) 0.4f else 0.2f,
        animationSpec = AppAnimation.Specs.fast(),
        label = "SearchIconBgAlpha"
    )

    Column {
        // 标题卡片 — surfaceContainerHigh 背景 + 主色装饰条 + 档案切换器
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.Screen.Horizontal),
            shape = RoundedCornerShape(AppSpacing.Corner.Lg),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = if (isDark) 0.9f else 0.7f),
            border = androidx.compose.foundation.BorderStroke(
                width = AppSpacing.Border.Thin,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.5f else 0.4f)
            ),
            shadowElevation = AppSpacing.Elevation.Sm
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.Lg, vertical = AppSpacing.Md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧主色装饰条 — 增强视觉锚点
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(if (isWatch) 28.dp else 32.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.width(AppSpacing.Md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = AppTypography.TitleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.Xxs))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "共 $totalCount 位$entityLabel",
                            style = AppTypography.LabelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.9f else 0.85f)
                        )
                        if (filteredCount != totalCount) {
                            Spacer(modifier = Modifier.width(AppSpacing.Sm))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.25f else 0.15f)
                            ) {
                                Text(
                                    text = "$filteredCount/$totalCount",
                                    style = AppTypography.LabelSmallBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = AppSpacing.Sm, vertical = AppSpacing.Xxs)
                                )
                            }
                        }
                    }
                }

                // 档案切换器 — 紧凑型，整合到标题卡片右侧
                CompactArchiveSwitcher(
                    archiveType = archiveType,
                    onSwitchArchive = onSwitchArchive
                )
            }
        }

        // 后台刷新进度条 — 细线动画
        AnimatedVisibility(
            visible = isRefreshing,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.Screen.Horizontal, vertical = AppSpacing.Xxs),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        }

        // 标题与搜索框之间的呼吸间距
        Spacer(modifier = Modifier.height(AppSpacing.Md))

        // 搜索框卡片 — 聚焦时边框高亮 + 阴影增强 + 搜索图标圆形背景
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.Screen.Horizontal)
                .height(if (isWatch) 40.dp else 48.dp),
            shape = RoundedCornerShape(AppSpacing.Corner.Lg),
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = if (isSearchFocused) 0.98f else 0.9f),
            shadowElevation = if (isSearchFocused) AppSpacing.Elevation.Md else AppSpacing.Elevation.Sm
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = AppSpacing.Border.Thin,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = searchBorderAlpha * 0.6f),
                        shape = RoundedCornerShape(AppSpacing.Corner.Lg)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = AppSpacing.Padding.InputHorizontal),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 搜索图标 — 带圆形背景装饰，与 Command Center 视觉一致
                    Box(
                        modifier = Modifier
                            .size(if (isWatch) 28.dp else 32.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = searchIconBgAlpha),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(if (isWatch) AppSpacing.Icon.Sm else AppSpacing.Icon.Md)
                        )
                    }

                    Spacer(modifier = Modifier.width(AppSpacing.Md))

                    Box(modifier = Modifier.weight(1f)) {
                        if (searchInput.isEmpty()) {
                            Text(
                                text = "搜索$entityLabel...",
                                style = AppTypography.BodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.7f else 0.6f)
                            )
                        }
                        BasicTextField(
                            value = searchInput,
                            onValueChange = onSearchInputChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(searchFocusRequester)
                                .onFocusChanged { onSearchFocusChange(it.isFocused) },
                            textStyle = AppTypography.BodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { onSubmitSearch(searchInput) }
                            )
                        )
                    }

                    // 清除按钮 — 带动画进出
                    AnimatedVisibility(
                        visible = searchInput.isNotEmpty(),
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally()
                    ) {
                        IconButton(
                            onClick = {
                                onSearchInputChange("")
                                searchFocusRequester.requestFocus()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "清除",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(AppSpacing.Icon.Sm)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(AppSpacing.Xs))

                    // 筛选按钮 — 带激活计数徽章（与 Command Center 搜索栏位置一致）
                    ModernFilterButton(
                        hasActiveFilters = hasActiveFilters,
                        activeFilterCount = activeFilterCount,
                        onClick = onFilterClick,
                        isWatch = isWatch
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernFilterButton(
    hasActiveFilters: Boolean,
    activeFilterCount: Int,
    onClick: () -> Unit,
    isWatch: Boolean = false
) {
    val isDark = LocalIsDark.current
    val buttonScale by animateFloatAsState(
        targetValue = if (hasActiveFilters) 1.05f else 1f,
        animationSpec = AppAnimation.Specs.scale(),
        label = "FilterButtonScale"
    )

    // 未激活时背景色：暗色下使用 surfaceContainerHigh 提高对比度，避免过于暗淡
    val inactiveBgColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    Box {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = if (hasActiveFilters) {
                MaterialTheme.colorScheme.primary
            } else {
                inactiveBgColor
            },
            modifier = Modifier
                .size(if (isWatch) 36.dp else 40.dp)
                .scale(buttonScale)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "筛选",
                    tint = if (hasActiveFilters) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(if (isWatch) AppSpacing.Icon.Sm else AppSpacing.Icon.Md)
                )
            }
        }
        // 激活筛选数量徽章
        if (activeFilterCount > 0) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.TopEnd)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$activeFilterCount",
                        style = AppTypography.LabelSmallBold,
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }
}

@Composable
fun ResultCountBadge(
    filteredCount: Int,
    totalCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.Screen.Horizontal),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(AppSpacing.Corner.Lg),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = AppSpacing.Md, vertical = AppSpacing.Xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$filteredCount",
                    style = AppTypography.LabelLargeBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = " / $totalCount",
                    style = AppTypography.LabelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}
/**
 * 搜索下拉面板 — 历史记录（输入为空时）或搜索建议（输入非空时）
 */
@Composable
private fun SearchDropdownPanel(
    isSearchFocused: Boolean,
    searchInput: String,
    searchHistory: List<String>,
    suggestions: List<String>,
    onHistoryItemClick: (String) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onClearHistory: () -> Unit,
    onRemoveHistoryItem: (String) -> Unit
) {
    val isDark = LocalIsDark.current
    val glassSurface = if (isDark) AppColors.GlassSurfaceDark else AppColors.GlassSurfaceLight

    val showHistory = isSearchFocused && searchInput.isEmpty() && searchHistory.isNotEmpty()
    val showSuggestions = isSearchFocused && searchInput.isNotEmpty() && suggestions.isNotEmpty()
    val isVisible = showHistory || showSuggestions

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(AppAnimation.Duration.Normal)) +
            expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)),
        exit = fadeOut(animationSpec = tween(AppAnimation.Duration.Fast)) +
            shrinkVertically(animationSpec = tween(AppAnimation.Duration.Fast))
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.Screen.Horizontal),
            shape = BlyyShapes.PanelMedium,
            color = glassSurface.copy(alpha = 0.98f),
            shadowElevation = AppSpacing.Elevation.Lg
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 280.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = AppSpacing.Sm)
            ) {
                if (showHistory) {
                    // 标题行
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.Md, vertical = AppSpacing.Xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "搜索历史",
                            style = AppTypography.LabelLargeBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.clickable { onClearHistory() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = "清除历史",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(AppSpacing.Icon.Sm)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.Xs))
                            Text(
                                text = "清除",
                                style = AppTypography.LabelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    searchHistory.forEach { item ->
                        HistoryItem(
                            text = item,
                            onClick = { onHistoryItemClick(item) },
                            onRemove = { onRemoveHistoryItem(item) }
                        )
                    }
                }

                if (showSuggestions) {
                    // 标题行
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.Md, vertical = AppSpacing.Xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "建议",
                            style = AppTypography.LabelLargeBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${suggestions.size} 个匹配",
                            style = AppTypography.LabelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }

                    suggestions.forEach { suggestion ->
                        SuggestionItem(
                            text = suggestion,
                            searchInput = searchInput,
                            onClick = { onSuggestionClick(suggestion) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(
    text: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.Md, vertical = AppSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(AppSpacing.Icon.Sm)
        )
        Spacer(modifier = Modifier.width(AppSpacing.Md))
        Text(
            text = text,
            style = AppTypography.BodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "删除",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(AppSpacing.Icon.Xs)
            )
        }
    }
}

@Composable
private fun SuggestionItem(
    text: String,
    searchInput: String,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val annotatedText = buildAnnotatedString {
        if (searchInput.isEmpty()) {
            append(text)
            return@buildAnnotatedString
        }
        val highlightStart = text.indexOf(searchInput, ignoreCase = true)
        if (highlightStart >= 0) {
            append(text.substring(0, highlightStart))
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor)) {
                append(text.substring(highlightStart, highlightStart + searchInput.length))
            }
            append(text.substring(highlightStart + searchInput.length))
        } else {
            append(text)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.Md, vertical = AppSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = primaryColor.copy(alpha = 0.7f),
            modifier = Modifier.size(AppSpacing.Icon.Sm)
        )
        Spacer(modifier = Modifier.width(AppSpacing.Md))
        Text(
            text = annotatedText,
            style = AppTypography.BodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(AppSpacing.Icon.Sm)
        )
    }
}

// ── 搜索历史持久化 ──

private const val SEARCH_HISTORY_PREFS = "gallery_search"
private const val SEARCH_HISTORY_KEY = "history"
private const val SEARCH_HISTORY_MAX = 10
private const val SEARCH_HISTORY_DELIMITER = "\n"

internal data class SearchHistoryState(
    val history: List<String>,
    val add: (String) -> Unit,
    val remove: (String) -> Unit,
    val clear: () -> Unit
)

/**
 * 搜索历史管理 — 基于 SharedPreferences 持久化
 * 使用换行符分隔保持顺序，最多保留 10 条
 */
@Composable
internal fun rememberSearchHistory(
    maxItems: Int = SEARCH_HISTORY_MAX
): SearchHistoryState {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(SEARCH_HISTORY_PREFS, 0) }
    val historyState = remember {
        mutableStateOf(
            prefs.getString(SEARCH_HISTORY_KEY, "")
                ?.split(SEARCH_HISTORY_DELIMITER)
                ?.filter { it.isNotBlank() }
                ?: emptyList()
        )
    }

    return remember(historyState.value) {
        SearchHistoryState(
            history = historyState.value,
            add = { query ->
                if (query.isBlank()) return@SearchHistoryState
                val updated = (listOf(query) + historyState.value.filter { it != query }).take(maxItems)
                historyState.value = updated
                prefs.edit().putString(SEARCH_HISTORY_KEY, updated.joinToString(SEARCH_HISTORY_DELIMITER)).apply()
            },
            remove = { query ->
                val updated = historyState.value.filter { it != query }
                historyState.value = updated
                prefs.edit().putString(SEARCH_HISTORY_KEY, updated.joinToString(SEARCH_HISTORY_DELIMITER)).apply()
            },
            clear = {
                historyState.value = emptyList()
                prefs.edit().remove(SEARCH_HISTORY_KEY).apply()
            }
        )
    }
}
/**
 * 紧凑型档案切换器 — 整合到顶部栏 actions 中
 *
 * 设计要点：
 * - 使用图标 + 文字的紧凑布局，节省垂直空间
 * - 选中项带主色背景 + 白色文字，未选中项透明背景
 * - 切换时带缩放动画，提升交互反馈
 * - 适配手表等小屏幕设备
 */
@Composable
private fun CompactArchiveSwitcher(
    archiveType: com.azurlane.blyy.viewmodel.ArchiveType,
    onSwitchArchive: (com.azurlane.blyy.viewmodel.ArchiveType) -> Unit
) {
    val isDark = LocalIsDark.current
    val isWatch = isWatchScreen()
    val isCommandCenter = LocalUiStyle.current.isCommandCenter()
    val accentColor = MaterialTheme.colorScheme.primary

    val tabs = listOf(
        com.azurlane.blyy.viewmodel.ArchiveType.DOCK to "舰娘",
        com.azurlane.blyy.viewmodel.ArchiveType.STUDENT to "学生"
    )

    // 根据 UI 风格切换容器配色：
    // - Command Center：HUD 玻璃面板色（AppColors.Panel）
    // - Classic：Material Design surfaceContainerHigh，与 ClassicTopBar 协调
    val containerColor = if (isCommandCenter) {
        if (isDark) AppColors.Panel.Dark.copy(alpha = 0.6f) else AppColors.Panel.Light.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = if (isDark) 0.85f else 0.7f)
    }
    val containerBorderColor = if (isCommandCenter) {
        accentColor.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    }
    // 选中态：Command Center 用 primary + White 文字；Classic 用 primaryContainer + onPrimaryContainer
    val selectedBgColor = if (isCommandCenter) accentColor else MaterialTheme.colorScheme.primaryContainer
    val selectedTextColor = if (isCommandCenter) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
    val unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(AppSpacing.Corner.Full),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(
            width = AppSpacing.Border.Thin,
            color = containerBorderColor
        )
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.Xxs)
        ) {
            tabs.forEach { (type, label) ->
                val isSelected = archiveType == type
                val targetColor = if (isSelected) selectedBgColor else Color.Transparent
                val textColor = if (isSelected) selectedTextColor else unselectedTextColor
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.95f,
                    animationSpec = AppAnimation.Specs.scale(),
                    label = "ArchiveTabScale"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppSpacing.Corner.Full))
                        .background(targetColor)
                        .clickable { onSwitchArchive(type) }
                        .padding(
                            horizontal = if (isWatch) AppSpacing.Xs else AppSpacing.Sm,
                            vertical = AppSpacing.Xxs
                        )
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = AppTypography.LabelMedium,
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
