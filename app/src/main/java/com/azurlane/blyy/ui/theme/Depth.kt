package com.azurlane.blyy.ui.theme

import androidx.compose.ui.graphics.Shape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 四层深度模型（设计语言 V2「深海舰队」）。
 *
 * 全 app 表面归入四层，每层绑定不可拆分的光影规格：
 * 禁止跨层混用（例如 L1 面板配 L3 阴影）——组件属于哪层，
 * 全部光影即由 [blyyDepth] 该层规格决定。
 */
enum class DepthLayer(val elevation: Dp) {
    /** L0 海床 — 屏幕背景、AGSL 流体层：无阴影 */
    Sea(0.dp),

    /** L1 甲板 — 内容面板（BlyyPanel / BlyySectionPanel）、卡片 */
    Deck(4.dp),

    /** L2 仪表 — 浮起控件：主按钮、播放器、题面卡、FAB 级圆钮 */
    Instrument(8.dp),

    /** L3 瞭望 — 对话框、BottomSheet、全屏查看器 */
    Lookout(12.dp)
}

/**
 * 统一深度阴影修饰符 — 双色有色阴影（ambient 环境光 + spot 直射光），
 * 颜色取自 [AppColors.Depth]（深海蓝黑，永不纯黑），亮暗模式自动切换。
 *
 * @param spotTint 稀有度等语境染色例外：仅染直射光（spot），环境光保持统一，
 * 保证卡片阵列不花（如 ShipCard 的稀有度染色阴影）。
 */
@Composable
fun Modifier.blyyDepth(
    layer: DepthLayer,
    shape: Shape,
    spotTint: Color? = null
): Modifier {
    if (layer == DepthLayer.Sea) return this
    val isDark = LocalIsDark.current
    val ambient = if (isDark) AppColors.Depth.AmbientDark else AppColors.Depth.AmbientLight
    val spot = spotTint?.copy(alpha = 0.25f)
        ?: if (isDark) AppColors.Depth.SpotDark else AppColors.Depth.SpotLight
    return this.shadow(
        elevation = layer.elevation,
        shape = shape,
        ambientColor = ambient,
        spotColor = spot
    )
}
