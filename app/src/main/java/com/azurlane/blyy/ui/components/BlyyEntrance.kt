package com.azurlane.blyy.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.azurlane.blyy.ui.theme.AppAnimation
import com.azurlane.blyy.ui.theme.AppSpacing

/**
 * 区块进场动效 — 渐显 + 轻微上移，按 [index] 错峰。
 *
 * 用于配置/设置/工具类静态界面的内容分区，消除生硬的整页直切。
 * 规格走 [AppAnimation] 令牌（staggered 时长 + Emphasized 缓动），
 * 位移为 [AppSpacing.Lg]，保持低打扰。
 */
@Composable
fun BlyyEntrance(
    index: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val startOffsetPx = with(LocalDensity.current) { AppSpacing.Lg.toPx() }
    val alpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = AppAnimation.Specs.staggered(index),
        label = "entranceAlpha"
    )
    val translationY by animateFloatAsState(
        targetValue = if (appeared) 0f else startOffsetPx,
        animationSpec = AppAnimation.Specs.staggered(index),
        label = "entranceTranslationY"
    )
    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = translationY
        }
    ) {
        content()
    }
}
