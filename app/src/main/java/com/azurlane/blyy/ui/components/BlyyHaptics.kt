package com.azurlane.blyy.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * 统一触觉反馈语义强度 — 全应用唯一的触觉入口。
 *
 * 用语义化强度表达意图，替代散落的 [androidx.compose.ui.hapticfeedback.HapticFeedbackType]：
 * - [Tick]：轻点确认（选项切换、筛选、chip 选中）
 * - [Confirm]：操作确认（发送、应用、完成）
 * - [LongPress]：长按触发（气泡菜单、拖拽开始）
 * - [Heavy]：强反馈（删除确认、结算成败）
 */
enum class BlyyHaptic { Tick, Confirm, LongPress, Heavy }

/**
 * 获取统一触觉反馈执行器。
 *
 * 基于 View 层 [HapticFeedbackConstants] 而非 Compose 的双常量枚举，
 * 可表达轻/中/重三档强度；API < 30 时回退到等效常量。
 */
@Composable
fun rememberBlyyHaptics(): (BlyyHaptic) -> Unit {
    val view = LocalView.current
    return remember(view) {
        { level -> view.performHaptic(level) }
    }
}

private fun View.performHaptic(level: BlyyHaptic) {
    val constant = when (level) {
        BlyyHaptic.Tick -> HapticFeedbackConstants.CLOCK_TICK
        BlyyHaptic.Confirm ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
            else HapticFeedbackConstants.VIRTUAL_KEY
        BlyyHaptic.LongPress -> HapticFeedbackConstants.LONG_PRESS
        BlyyHaptic.Heavy ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT
            else HapticFeedbackConstants.LONG_PRESS
    }
    performHapticFeedback(constant)
}
