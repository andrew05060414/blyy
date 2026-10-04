# BLYY UI 视觉与体验升级方案 — 设计语言 V2「深海舰队」

> 版本：v2.0（2026-10-04）
> 定位：本文件是**视觉与体验层的完整设计方案**，回答「改成什么样」；结构与缺陷问题见 [UI_DEEP_AUDIT_AND_REFACTOR_PLAN.md](UI_DEEP_AUDIT_AND_REFACTOR_PLAN.md)（回答「哪里有债、按什么顺序改」）。两份文档共用同一套阶段划分：视觉升级搭载在结构重构的阶段 1/3 上执行，避免「同一处代码改两遍」。
> 前提约束：本方案基于现有令牌体系（`ui/theme/` 11 个文件）渐进演化，**不推翻、不换品牌色**——青色 × 金色的品牌识别保持延续，升级的是「质感密度、光的一致性、克制程度」。

---

## 0. 设计诊断：现在离「精致」差在哪

当前 UI 的令牌层纪律良好（屏幕层 `Color(0x...)` 为 0），但站在用户视角看，「高级感」被四类问题稀释：

| 问题 | 表现 | 根因 |
|---|---|---|
| **光没有秩序** | 有的面板顶面高光、有的纯平；阴影一处用 `AppElevation`、一处用自拟稀有度色（ShipCard.kt:99-104）；`AppColors.Depth` 有色阴影令牌（Color.kt:106-115）定义了却几乎没人用 | 缺少统一的「深度层级 → 光影规格」映射 |
| **贵重感被通胀** | 金色描边、金色渐变、金色文字在普通面板边框（`Gradient.PanelBorder` 青→金→青）、普通 Chip 上大量出现 | 缺少「金色只属于珍贵时刻」的使用红线 |
| **细节密度不均** | 空态是「图标+两行字」的毛坯（BlyyEmptyState），而隔壁翻牌屏有 800ms 呼吸动画；同一屏里 28dp 与 48dp 的按钮并存 | 组件规格没有最小触控/尺寸下限约束 |
| **动效无预算** | ShipCard 高稀有+誓约卡 7 个常驻无限动画（审计 §3.1），播放键暂停时辉光仍在跑；而历史列表删除又毫无反馈 | 缺少「无限动画白名单」制度 |

V2 的目标一句话：**让光有方向、让金有分量、让每一处细节密度一致、让动画只为状态而动。**

---

## 1. 设计语言 V2「深海舰队 / Deep-Sea Fleet」

### 1.1 世界观与四大支柱

设计叙事：这是一座**漂浮在深海上的舰队指挥舱**——界面是精密的航海仪器：玻璃罩下是冷光的仪表盘，黄铜（金）只出现在誓约与荣耀的铭牌上，一切浮起物都有海水的重量。

| 支柱 | 含义 | 落到界面上 |
|---|---|---|
| **① 光的秩序** | 全 app 单一光源：左上 45°。所有浮起表面受光一致：顶部亮、底部沉、阴影向右下 | 面板顶部 1px 高光 + 底部渐变沉降；阴影统一用 `Depth` 双色（ambient 环境光 + spot 直射光） |
| **② 克制的贵金属** | 金色 = 稀缺资源。金色只出现在：誓约、海上传奇/决战方案稀有度、结算奖杯、成就类徽章 | 普通面板描边、普通 Chip、导航元素**禁用金色**；金色元素必须搭配 Metallic 渐变与辉光，做出「抛光黄铜」而不是「土黄」 |
| **③ 精密仪器感** | HUD 细节（切角、刻度线、等宽数字、扫描线）是**缝线不是面料**：只用在数据密集与交互边缘处，大面积区域保持低细节的呼吸感 | 切角只用于 L1/L2 交互容器；分数/进度/计数一律等宽字（tnum）；四角 L 装饰只在 L2 浮起面板出现 |
| **④ 物理动效** | 每个元素有质量：按压会沉、松开会回弹带一点过冲；入场有先后（stagger）；无限动画只表达持续状态 | 统一按压三联动（scale + shadow 收缩 + 边框变亮）；无限动画白名单制（§8.4） |

### 1.2 双风格策略

- **指挥中心风格（主推）**：V2 全部规格针对它设计——切角面板、玻璃、HUD 细节。
- **经典风格（Material 亲肤）**：继承 V2 的**色彩、光影、间距、动效、触控规格**，但形状语言换回圆角（`BlyyShapes.DialogClassic` 一系），不引入切角与扫描线。即：**V2 是材质与光效系统，不是单一皮肤**。
- **手表模式**：继承色彩与形状，动效降级（shimmer → 静态色块、stagger → 同时入场）、`WatchSpacing` 继续 65% 缩放。

### 1.3 品牌记忆点（做加法的锚）

升级后用户应该记住的三个签名瞬间：
1. **首页誓约墙**：誓约卡的金色细边 + 按下时一次金色流光扫过（一次性，非常驻）。
2. **底部播放器折叠球**：播放中呼吸辉光 + 图标随节拍轻摆，暂停即静——「设备活着」的感觉只属于播放态。
3. **识舰娘结算**：奖杯金色入场 + 统计逐项浮现（stagger ≤8 项），是全 app 仪式感浓度最高的 2 秒。

---

## 2. 色彩体系

### 2.1 色彩架构：五族分工

```
海床色（中性）── 承载一切：背景、表面、容器阶梯
功能色（青）  ── 一切可交互：按钮、链接、选中态、进度
辅助色（蓝紫）── 次级信息与多选场景
贵金属（金）  ── 只属于珍贵时刻：誓约、传奇、奖杯、成就
稀有度谱（7 档）── 只出现在舰娘卡片语境
```

**比例纪律**：一个正常屏幕上，海床色 ≥75%、功能青 ≤15%、语义/稀有度 ≤8%、金 ≤2%。金色超标是「廉价感」的第一来源。

### 2.2 海床色阶（微调，保持家族连续）

现状的问题：亮色背景 `#D4E8F5` 饱和度偏高，整个 app 泡在蓝色里，白色表面反而显灰。V2 把背景提亮降饱和、把表面推近纯白，**让「表面浮起于海床」的层次真正可感**：

| 令牌 | 现值 | V2 值 | 意图 |
|---|---|---|---|
| BackgroundGradientStartLight | #D6EBF7 | **#E9F3FA** | 提亮 +2%，降饱和 |
| BackgroundGradientMidLight | #E8F4FC | **#F3F8FC** | 顶部更透气 |
| BackgroundGradientEndLight | #C5DFF0 | **#DFEDF7** | 底部沉降更轻 |
| SurfaceLight | #F5FAFF | **#FAFCFE** | 去蓝味，接近瓷白 |
| SurfaceContainerLight（Base/High/Highest） | #D0E4F2/#B8D4E8/#9BB8CC | **#EAF2F8 / #DEEAF4 / #CFE0ED** | 阶梯坡度放缓，层间差 ≈3% 而非 5% |
| Dark 全系 | #0A1628 家族 | **保持不变** | 暗色海床已优秀，不动（品牌识别） |

> 暗色模式是本 app 的主场（深海叙事），亮色是「瓷白海雾」变体。上面亮色微调建议做成 A/B 截图对比后一次定稿，避免反复。

### 2.3 功能青：补齐「深浅两档 + 文字安全规则」

| 令牌 | 值 | 用途 |
|---|---|---|
| Primary（品牌青，不变） | 亮 #0096C7 / 暗 #48CAE4 | 交互主体：按钮填充、选中态、进度 |
| **PrimaryDeep（新增）** | 亮 #0077B6 / 暗 #0096C7 | ① 小字号文字直落于青底时改用此填充（#0096C7 上白字对比约 3.4:1，仅够大字；#0077B6 约 4.6:1 达 AA）② 按压态渐变终点 |
| PrimarySoft（现有 Cyan 收编） | #48CAE4 | 渐变浅端、辉光、水波纹 |
| PrimaryVeil（新增） | 亮 #0096C7 @12% / 暗 #48CAE4 @16% | 选中行底色、聚焦光晕——替代各处手写的 `primary.copy(alpha=0.1f)` |

**文字安全规则**（写进组件注释）：青色填充上的文字必须 `≥14sp SemiBold` 或填充改用 PrimaryDeep；金色 #FFD166 上禁放任何文字（对比 1.7:1），金底一律用深色字 #1A1200。

### 2.4 贵金属金：三条红线 + 金属规格

红线（lint 级约定，code review 检查）：
1. 金色**不得**用于：普通面板描边、普通按钮、导航高亮、加载动画。现有 `Gradient.PanelBorder`（青→金→青，Color.kt:239-245）降金——改为纯青双档渐变 `#48CAE4@45% → #0096C7@18%`。
2. 金色**必须**用于：誓约边框/徽章、Legendary/Decisive 稀有度、结算奖杯、成就徽章、关于页版本号强调。
3. 金色出现处必须同时具备**渐变 + 光**：填充用 `Gradient.GoldAccent`（#FFE599→#E8A838→#FFD166），文字用 `Gradient.MetallicText`，边缘配 `GoldGlow`（新增，#FFD166@28%）——「抛光黄铜」而非「土黄」。

新增文字安全档：`GoldTextLight = #8C5E00`（亮色下可读的金系文字，对比 ≈5:1）/ `GoldTextDark = #FFD97A`（暗色下 ≥7:1）。

### 2.5 稀有度谱：补全 7 档渐变对

现状只有 3 档有渐变（Color.kt:176-189）。V2 为全部 7 档定义 `色 + 渐变 + 辉光` 三件套，并统一「饱和度从上到下递减」的视觉排序，使卡片阵列稀有度一眼可辨：

| 档位 | 主色 | 渐变对 | 辉光 alpha |
|---|---|---|---|
| 海上传奇 | #FFD700 | → #FFA500（保持） | 0.40 |
| 决战方案 | #FF6B6B | → #FF8E53（保持） | 0.35 |
| 超稀有 | #48CAE4 | → #0096C7（保持） | 0.35 |
| 最高方案 | #60A5FA | → **#A78BFA** | 0.30（新增） |
| 精锐 | #34D399 | → **#0EA5E9** | 0.25（新增） |
| 稀有 | #94A3B8 | → **#64748B** | 0.20（新增） |
| 普通 | #64748B | → **#475569** | —（无辉光） |

### 2.6 遮罩与玻璃：从「黑白硬编码」到语义化

审计发现约 40 处 `Color.White/Black.copy(alpha)` 遮罩（语音页渐晕、播放器高光、相机页、裁剪页）。V2 定义两个语义族：

```kotlin
/** 遮罩族 — 语音/相机/媒体查看器黑底语境 */
object Scrim {
    val Base = Color(0xFF05101E)        // 深海黑（非纯黑，与品牌同族）
    val OnScrim = Color.White
    val Subtle = 0.35f                   // 列表底部渐晕、图片上的次级文字底
    val Medium = 0.55f                   // 播放器控件底、气泡渐晕
    val Heavy = 0.85f                    // 全屏查看器背景、相机取景
}

/** 玻璃三档 — 按「浮起高度」分档，替代 5 处各写各的 glassSurface.copy(0.9f) */
Glass.Bar    // 顶栏/播放条/悬浮圆钮：surface alpha 0.78(暗 0.72)，border 0.30，顶部高光 0.12
Glass.Card   // 图片上的信息卡：alpha 0.88，border 0.18
Glass.Sheet  // 底部弹窗：alpha 0.94，border 0.22 + 顶部 1px 高光线
```

相机/裁剪这类「黑底功能页」允许脱离主题色，但必须引用 `Scrim` 族（统一 #05101E 而非纯黑，与全 app 阴影同色族，黑得「深」而不「脏」）。

### 2.7 对比度验收矩阵

| 组合 | 要求 | 验证方式 |
|---|---|---|
| onSurface / surface（明暗两套） | ≥7:1 | 脚本抽查（§11.3） |
| onSurfaceVariant / surface | ≥4.5:1 | 同上 |
| 白字 / Primary 填充 | 仅限 ≥14sp SemiBold；小字场景换 PrimaryDeep | 组件注释 + review |
| 金色文字 | 只允许 GoldTextLight/Dark 两档 | grep `FFD166` 出现在 TextStyle 的场景 |
| Scrim 上的文字 | ≥4.5:1（alpha ≥Medium 档才可放字） | 设计约定 |

---

## 3. 光影与材质系统（V2 的灵魂）

### 3.1 四层深度模型

把全 app 的表面归入四层，**每层绑定一套不可拆分的光影规格**（写成一个 `Modifier.blyyDepth(layer)` 扩展，见 §10）：

| 层 | 语义 | tonal | 阴影（ambient + spot，用 Depth 双色） | 边缘 |
|---|---|---|---|---|
| **L0 海床** | 屏幕背景、AGSL 流体层 | 0 | 无 | 无 |
| **L1 甲板** | 内容面板（BlyyPanel/BlyySectionPanel）、卡片 | Level2(3dp) | ambient `Depth.Ambient` 4dp | 1px CardBorder 渐变 + 顶部 12% 高光 |
| **L2 仪表** | 浮起控件：主按钮、播放器、FAB 级圆钮、题面卡 | Level3(6dp) | ambient 6dp + spot `Depth.Spot` 12dp | 四角 L 装饰**只在此层**出现 |
| **L3 瞭望** | 对话框、BottomSheet、全屏查看器 | Level4/5(12/16dp) | ambient 12dp + spot 16dp | 玻璃 Sheet 档 + 顶部高光线 |

规则：**禁止跨层混用**——一个组件属于哪层，它的全部光影就由该层规格决定，不允许「L1 的面板配 L3 的阴影」。（这条直接治好 ShipCard 自拟阴影、GuessImage `shadow(16.dp)` 绕过令牌等审计问题。）

### 3.2 有色阴影规范

- 亮色：ambient `#27435C@12%` + spot `#274A63@20%`（现有 Depth 令牌即此值，开始真正使用）。
- 暗色：ambient `#051214@20%` + spot `#05101E@30%`——**深海蓝黑，永不纯黑**（纯黑阴影在蓝底上发脏）。
- 稀有度语境例外：ShipCard 的 spot 允许染稀有度色（`rarityColor@25%`），但 ambient 仍用标准 Depth——彩色只染「直射光」，环境光保持统一，阵列才不花。
- 按压响应：按下时 spot 距离收缩 40%、ambient 不变（「按进海面」），松开 spring 回弹。

### 3.3 受光面板与蚀刻描边

- **受光面**（已有 `Panel.FillDark/Light` 渐变，推广为面板默认材质）：顶部 12% 白高光（`Gradient.HighlightTop`）+ 底部 8% 沉降（`Gradient.ShadowBottom`），中间 90% 区域保持纯净——细节只在边缘。
- **蚀刻描边**（SecondaryButton/未选中 Chip 升级）：外圈 1px 主题描边 @30% + **内侧 1px 白高光 @8%**，形成「刻进去」的凹槽感，替代现在的单线描边。
- **微纹理防条带**：大面积渐变背景（Home 流体层、结算弹窗底）叠一层 2.5% alpha 的噪点贴图（`res/drawable/noise_tile.xml` 程序化生成一次），消除渐变条带——这是「高端感」的隐形功臣。

### 3.4 玻璃三档规格（承接 §2.6）

Compose 无原生实时模糊（API 31 前），维持现有「高 alpha 半透明 + 渐变描边 + 顶部高光」的**伪玻璃**，但三档参数收敛为常量：滚动后顶栏从 Bar 档渐变到 Bar@0.92（视觉上「玻璃变实」），底栏胶囊同理。API 33+ 可选启用 `RenderEffect.createBlurEffect` 真模糊，作为渐进增强，不作为依赖。

---

## 4. 形状与栅格

### 4.1 切角语言规则（指挥中心风格）

1. 切角（Chamfer）**只允许**出现在 L1/L2 交互容器上；L0 背景无形状；纯文字元素（标签、时间戳）用圆角或直角。
2. 切角尺寸阶梯维持 `BlyyShapes` 现有 8/12/16 三档；**同屏内切角尺寸差不得超过一档**（防止大小切角混排的凌乱）。
3. `diagonalChamferedShape`（对角切）专属导航胶囊与「强调性按钮」，不得用于信息容器。

### 4.2 圆角与卡片比例

- 圆角阶梯以 `AppSpacing.Corner` 为准（不变），新增约定：**列表项圆角 = 面板圆角 − 4dp**，形成嵌套 hierarchy。
- **统一卡片纵横比 `AppSpacing.Card.AspectRatio = 0.75`**：修复 ShipCard 的 `aspectRatio(0.8f)`（ShipCard.kt:97）与 shimmer 0.75f 不一致——骨架与真卡形状必须逐像素吻合，否则加载完成瞬间会「跳一下」。

### 4.3 栅格与触控

- 8dp 基网格不变；横向内容边距 `Screen.Horizontal=16dp` 不变（手表 10dp）。
- **触控下限 48dp**（含 `minimumInteractiveComponentSize`）：直接消灭审计发现的 28dp/32dp IconButton（VoicePlayLaterSheet:344,452、GuessHistoryScreen:432,469）；视觉尺寸可以小（icon 28dp），热区必须足。
- **拇指区法则**：主操作贴底（≤ 距底 88dp 内），破坏性操作放屏上部或长按二级菜单；底部弹窗的主按钮永远在右下（右手拇指）。

### 4.4 内容宽度与响应式

- 正文列最大 600dp（大屏居中），网格类用 `GridCells.Adaptive` 自适应（现状已是）。
- ≥840dp 宽（平板/横屏）：详情页允许「列表 | 内容」双栏（如 Gallery→Voice 的承继关系），作为后续增强项（需引入 WindowSizeClass），V2 阶段只做**单栏下的 max-width 居中**，成本极低收益明显。

---

## 5. 排版系统

### 5.1 现状与问题

字阶（Type.kt）本身健康，问题是三点：① HUD 数字（分数/进度/计数）与中文混排时数字宽度跳动，读数不稳；② 全大写/等宽标签（CommandCenter 的 Mono 标签族）与正文混排缺乏「仪表感」的专门样式；③ 系统字体在数字上的字形与品牌青不搭调。

### 5.2 V2 增补：等宽数字与仪表字族

```kotlin
// Type.kt 增补 —— 数字一律等宽（tnum），读数不跳动
val NumericHud    = LabelLarge.copy(fontFeatureSettings = "tnum", fontFamily = BlyyFontFamily.Mono)
val NumericScore  = HeadlineSmallBold.copy(fontFeatureSettings = "tnum")
val NumericTimer  = TitleMediumBold.copy(fontFeatureSettings = "tnum")
```

- 应用点：GuessScoreChip/ScoreBanner（分数）、VoicePlayerBar 进度时间、排行榜名次、历史统计、会话未读数。
- 数字 + 单位混排规则：数值 `NumericHud` + 单位 `LabelSmall`（小一档、低一档对比度），如 `128 / 150`。

### 5.3 中文排版细节

- 行高维持现有；**段宽**：正文列 ≤28 个汉字/行（600dp @BodyLarge 自然满足）。
- 省略规范：标题 1 行 ellipsis、副标题 2 行 ellipsis、聊天记录摘要 1 行——现有实现基本符合，写入组件默认值。
- 字重纪律：同屏 Bold 只允许一类元素（数据或标题，二选一）；字重变体族（TitleMediumBold 等）已有，禁止再 `.copy(fontWeight=...)` 散写（审计遗留 125+ 处，随阶段 1 收编）。

---

## 6. 组件视觉规格 V2（逐组件：现状 → 规格）

> 落地方式：以下规格写进对应组件的 KDoc + 参数默认值；收敛并存实现（审计 §2.3 并存表）时**只升级收敛后的那一个**。

### 6.1 BlyyPrimaryButton（主按钮 — L2 仪表）

现状：渐变填充 + 按压流光 + 主色辉光（BlyyComponents.kt:470-608），基础已好。
V2 规格：
- 填充：`Gradient.Primary` 起点提用 PrimaryDeep（#0077B6→#0096C7 亮 / #0096C7→#48CAE4 暗），文字白 + `ButtonText`。
- 光影：L2 全套（tonal 6 + ambient 6 + spot 12 主色@20%）；**按下**：scale 0.94（Heavy 档）+ spot 收缩 40% + 流光扫过一次（200ms，一次性）。
- 辉光：默认态辉光删除（现在的常驻辉光造成视觉噪音）；辉光只出现在「唯一主操作」场景（每屏 ≤1 个带辉光按钮）。
- 禁用：填充降为 38% alpha、去渐变去光影、文字 onSurface@60%。
- 尺寸：高 52dp（Game 场景 52、表单 48），最小宽 88dp，圆角 `BlyyShapes.Button`。

### 6.2 BlyySecondaryButton（次按钮 — 蚀刻）

现状：单线描边。V2：蚀刻描边（§3.3）+ 背景 `surfaceContainer@40%`；文字 Primary；按下 scale 0.96 + 描边 alpha 升至 60%。**禁用金色渐变**。

### 6.3 BlyyPanel / BlyySectionPanel（面板 — L1 甲板）

现状：切角 + 四角 L 装饰 + 顶面高光（:357-459）。
V2：
- 默认材质换 `Panel.Fill` 受光渐变 + L1 光影全套；四角 L 装饰从 Panel 上**移除**，只保留在 L2 组件（否则满屏都是角饰，细节通胀）。
- 新增 `variant: Flat（无光影，用于嵌套小面板）/ Layered（默认）/ Floating（=L2，用于「从面板中浮起」的强调卡）`——替代各处手写的 tonalElevation 微调。
- `BlyySectionPanel` 的图标圆徽章统一 40dp radialGradient 规格（收编 SecretaryShipModeScreen 重复 5 次的样板，审计 §3.5）。

### 6.4 BlyyTopBar（顶栏）

现状：HUD 切角面板 + 渐变描边 + 2dp 光带（:160-266）。
V2：
- 静止态：Glass.Bar 档 + 底部 1px 渐变分隔线（青@25%，中间亮两端灭）。
- **滚动态**：滚动 >8dp 后顶栏玻璃「变实」（alpha 0.78→0.92）+ 分隔线升至 45% + 标题字距 +0.2sp（微妙的「锁定」感）；用 `nestedScroll` 派生状态驱动，替代 GalleryScreen 手写 snapshotFlow 双动画（GalleryScreen.kt:138-176）。
- 2dp 光带保留但**只在指挥中心风格 + 滚动态**出现（静止时隐藏，减少常态噪音）。

### 6.5 ShipCard（英雄组件 — 重点重设计）

现状问题：7 个常驻无限动画/卡、纯黑渐变遮罩、BrokenImage 占位、稀有度辉光常驻。
V2 规格：

| 部位 | 规格 |
|---|---|
| 图片底部融合 | 渐变从 `Scrim.Base@92%`（深海蓝黑）取代纯黑——黑得有品牌味；高度 70dp（`Figure.Banner`） |
| Nameplate | 名称 `CardTitle` 白 + 阵营/舰种合并为单行 `CardLabel` @65% 白，**置于一枚半透明胶囊**（Glass.Card）内，贴卡片底缘 8dp——「舰船铭牌」而非「图片上的字」 |
| 稀有度 | 双通道：左侧 3dp 竖向光条（`Rarity.Gradient` 渐变填充，全档启用 §2.5 新表）+ nameplate 底缘 1px rarity@40% 线。**不做常驻辉光** |
| 誓约 | 金 1.5dp 蚀刻边（外金@70% + 内白@10%）+ 右上角 20dp 金戒徽章（静态）+ **按下时金色流光扫过一次**（签名瞬间①） |
| 进入视口脉冲 | 高稀有卡首次进入视口：rarity 辉光 800ms 一次性脉冲后熄灭（替代常驻 Glow 的「被看见」时刻） |
| 按压 | 现有三联动保留（scale 0.97 + elevation 回落 + 边框变亮），阴影换 §3.2 稀有度染色规则 |
| 占位 | 中性舰船剪影 icon@24% + `Panel.Fill` 渐变（修复 BrokenImage 语义错误） |
| 加载完成 | 0.95→1 scale + 150ms（现有）保留 |
| **动画预算** | 常驻无限动画 = **0**；一次性动画 ≤2（入场 + 视口脉冲） |

### 6.6 对话框 / BottomSheet（L3 瞭望）

- 统一走 `BlyyDialog`/`BlyyBottomSheet` 体系（消灭 Material AlertDialog × 自绘玻璃 Dialog 双轨，审计 §2.3）：Sheet 用 Glass.Sheet + `BlyyDragHandle` + 圆角 `BlyyShapes.BottomSheet`；进入 spring `Component.bottomSheet()`， scrim 用 `Scrim.Base@Medium`。
- 危险操作（删除/清空）：确认对话框的错误图标底换 `Semantic.Error@12%` 圆形徽章 + 主按钮自动切换为 error 填充——「危险有颜色」。
- Sheet 高度规则：内容超限时 `heightIn(max = 屏高 − 顶栏 132dp)`，主操作条固定底部不被键盘遮挡（`imePadding`）。

### 6.7 输入框（StableOutlinedTextField 系）

- 聚焦态升级：描边 Primary 2dp + 外圈 4dp `PrimaryVeil` 光晕 + 光标 PrimaryDeep；未聚焦蚀刻描边（§3.3）。
- 错误态：描边 error + 下缘 2px error 渐变线 + 尾部 error 图标（替代纯文字报错）。
- 密度：高 56dp（`Height.Input`）不变；多行输入（聊天框）维持 40dp 单行防抖设计的**行为**，视觉上允许 3 行内增高（行为不变量见审计红线 4）。

### 6.8 Chip / 分段控件

- Chip：高 28dp 视觉 / 48dp 热区；选中态 = PrimaryVeil 底 + Primary 渐变描边 + 文字 Primary（现状良好，规格化即可）。
- `GuessDifficultySelector` 分段控件已是标杆（三段受光渐变 + Depth 阴影 + Press 缩放）——**作为 V2 分段控件的规范实现**，抽通用 `BlyySegmentedControl` 供筛选/语言切换复用。

### 6.9 列表行（BlyyListItem 激活版 — L1）

规格：40dp radialGradient 图标徽章（强调色可注入）+ 标题 `TitleSmall` + 副标题 `BodySmall`@60% + 尾部 chevron@40%；整行按压 scale 0.98 + 背景 PrimaryVeil；分组场景行距 `Gap.ListItem=8dp`。**收编 6 个私有同构实现**（SettingsNavigationRow/SettingsEntryCard/ModeOptionCard/SubOptionCard/UpdateChannelCard/UpdateChannelOption，审计 §2.3 表首行）。

### 6.10 空态 / 错误态 / 骨架屏（细节密度补齐）

- **BlyyEmptyState V2**：图标置于 96dp「仪表圆环」上（圆环 = 1px 描边 @20% + 四个刻度短线 @40%，指挥中心风格限定）+ 圆后 `CardGlow` 光晕；标题 `EmptyTitle` + 描述 `EmptyDescription`；**可选主 CTA 按钮**（空态必须给出口——首页空态的「去船坞」即签名场景）。经典风格圆环换浅色圆形底。
- **BlyyErrorState V2**：同构但圆环描边换 error@40%；**重试是 PrimaryButton**（全 app 统一「重试=真重试」，修复 ShipGallery 重试=返回的语义错误 B10）。
- 骨架屏：统一 shimmer 波形（`Repeating.shimmer(2800)` + 15% 白扫光带宽 40%）；**形状必须与内容逐像素同构**（网格骨架用真卡 0.75 比例、列表骨架用列表行高）；watch 掉级为静态 @8% 色块。

### 6.11 触觉（已有制度，补场景）

四档语义保留；补：卡片长按 LongPress、下拉刷新触发 Tick、翻牌揭示 Heavy、发送成功 Tick、删除 Heavy。每屏触觉 ≤3 种，过多即麻木。

---

## 7. 分界面布局与体验升级

### 7.0 全局体验基线（所有屏适用）

1. **四态完备**：loading（骨架）/ error（含真重试）/ empty（含 CTA）/ data（含缓存时顶部细错误条）——以 GalleryScreen 现有四态为基线，`GalleryScaffold` 组件化后全 app 复用（对应审计 T1.3）。
2. **滚动方向单一**：一屏只允许一个主滚动轴；嵌套滚动场景（图鉴筛选 Sheet 内）内层独立滚动并限高。
3. **返回即保存直觉**：所有编辑场景（配置页、编辑器）返回时若有未保存变更，弹 BlyyConfirmDialog 三选（保存/放弃/继续编辑）——啾信配置页优先落地。
4. **转场连续性**：头像 sharedElement 转场保留并扩展到「卡片 → 详情头卡」；列表项删除用 `animateItemPlacement` 滑出而非瞬间消失。

### 7.1 首页（我的后宅 — 誓约墙）

- 网格 `Adaptive(120dp)` 保持；**誓约舰卡升格 hero**：每行第一张卡允许 `span` 双倍宽（横版誓约卡，图片更完整）——「后宅是相册墙不是货架」。
- 空态：`PremiumFloatingArtwork` 悬浮装置保留（已是签名级），CTA 按钮「前往船坞」换 PrimaryDeep 渐变 + 按下金流光（首次引导允许金色，因属「誓约叙事」）。
- 错误态：下拉刷新失败 → 顶部滑入 `GalleryErrorBanner` 同款细条（修复 error 从未消费的 B10）。
- AGSL 流体背景：无誓约舰时降为静态渐变（性能 + 空态安静感）。

### 7.2 船坞/成员图鉴（Gallery）

- 顶栏常驻：搜索 + 筛选（计数徽章）+ 档案切换（CompactArchiveSwitcher）——三件归一进 `AdaptiveGalleryTopBar`，滚动隐藏保留但加「搜索激活时不隐藏」规则（用户正在输入时顶栏消失是灾难）。
- **筛选心智统一为「草稿 + 应用」**：学生 14 维 Sheet 迁移到与舰娘一致的草稿模式（底部操作条常驻，显示已选计数）；Sheet 内分组 sticky 头。
- 结果反馈：筛选后结果数以 `NumericHud` 徽章短暂浮现 800ms（改完即知道命中多少，不用数格子）。
- 卡片阵列：进入视口 stagger 脉冲只对**首屏**生效（首屏 ≤12 张），滚动加载的卡直接静态出现——滚动中批量脉冲是掉帧源。

### 7.3 语音档案（Voice）

- 结构维持：立绘 + 分组列表 + 底部播放器。
- **播放器 V2**（签名瞬间②）：折叠球 64dp；展开 90dp；**拖拽跟手**（translationX/Y 实时联动，松手 snap）修复「松手才瞬移」；播放中 = 辉光呼吸 + 图标 ±5° 摇摆；暂停 = 全部静止（白名单制）。
- 进度条：`NumericTimer` 等宽时间 + 渐变填充 + 缓冲轨 @20% + 拖动时拇指放大至 12dp 光环；进度更新从 500ms 轮询改 `Player.Listener` 回调 + 拖动态 100ms 插值（滑杆跟手）。
- 列表：key 修复（`scene+url`，B6）；收藏置顶时 item 用 `animateItemPlacement` 平移到新位（现在是整表 key 失效重建）；正在播放行 = 左侧 3dp Primary 光条 + `NumericHud` 时长高亮。
- 立绘拖拽气泡保留；台词文本用 `Scrim.Medium` 底胶囊提升可读性。

### 7.4 识舰娘（Guess — 游戏聚焦设计）

- **三区布局锁定**：顶部 HUD 状态区（分数/难度，`NumericHud`）固定；中部题面区（L2 浮起 + 全套光影）占屏 45%；底部操作区固定（输入 + 提交/下一题主次互换）。**反馈卡（答对/揭示/答错）不得遮挡题面**——改为题面卡底缘滑入的横幅式，或压缩题面后在其下方展开（`AnimatedContent` 尺寸动画）。
- 答错反馈：输入框 100ms 抖动 ×2 + 边框 error 300ms + 输入框保留错误答案（用户对照），提交按钮短暂禁用 400ms 防连点。
- 看图/听音合屏：`GuessGameScaffold` slot 模板（对应审计 T2.6），听音题面 = 52dp 耳机光晕卡 + 播放按钮（呼吸光晕仅播放中）；「回放」按钮文案与行为对齐（真重播，修 B9）。
- 结算（签名瞬间③）：奖杯 `Gradient.GoldAccent` 填充 + 800ms spring 入场 + 光晕一次性扩散；统计四项 stagger 60ms 逐项浮现；「再来一局」PrimaryDeep 主按钮。

### 7.5 啾信（Jiuxin — 阅读优先）

- 气泡 V2：我方 = Primary 渐变（PrimaryDeep→Primary）白字；对方 = `surfaceContainerLow` 底 + 1px 蚀刻边；**时间戳合并**（同发送者 5 分钟内仅末条显示）；`BubbleText` 行高保持。
- **失败态**（修 B2）：气泡右下角 error 圆点 + 叹号；点击气泡 = 重试；发送中 = 时间戳位置换 12dp 转圈。
- 群聊发送者名：按 name hash 从 5 色稳定取色（Primary/金禁用、Tertiary/Success/Info + 两档中性蓝），一行小字 @80%。
- 输入栏：聚焦时 `PrimaryVeil` 光晕 + 上边线升亮（现固定分隔线）；+ 号面板图标网格化（现有 Sheet 规格化）。
- 会话列表：拖拽排序跟手化（行高假设 76dp 换实测 `onGloballyPositioned`，修 B11 的漂移根因）；编辑模式顶栏切换「完成」按钮 + 全屏 `Scrim.Subtle` 提示态。

### 7.6 秘书舰（Secretary）

- Mode 屏拆分后（审计 T2.1），选择方式卡用 `BlyyListItem`（收编 ModeOptionCard）；**SD 小人预览卡**：实时 Spine/SD 预览置于 L1 面板 + 底部渐变台座（小人「站」在光上而非悬浮）。
- 设置屏：`BlyySettingsRow` 全量替换；语音间隔 Slider 换 `NumericHud` 刻度读数（0.5h 步进可感）。
- 翻牌屏：翻转改真 3D 翻面（`graphicsLayer rotationY` 0→90 换面 →90→0，两段各 400ms `Specs.normal()`）替代无限旋转；揭示卡 spring 保持。

### 7.7 设置 / 关于 / 排行榜 / 水印

- 设置：分组导航全部 `BlyyListItem`；每分组 ≤6 行（现 6 组结构保持）。
- 关于：版本号 `NumericHud` + 更新渠道卡与 AppChrome 合并为单实现（审计 T1.2 的一部分）；「检查更新」加载态脉冲省略号保留。
- 排行榜：前三名奖牌渐变（MedalColors）+ 名次 `NumericHud`；我的排名行 = PrimaryVeil 底 + 左 3dp Primary 光条（在长列表中一眼找到自己）。
- 水印相机：黑底语境全走 `Scrim` 族；快门外环按压 0.85 + 白色内圆曝光渐亮动画（模拟快门）。

---

## 8. 动效设计系统

### 8.1 分层时长（全部走 AppAnimation，禁止散写）

| 层级 | 时长/曲线 | 场景 |
|---|---|---|
| 微交互 | 100–200ms / `Springs.Stiff`、`Specs.fast()` | 按压、勾选、图标切换、toast |
| 元素进出 | 200–350ms / `Specs.normal()`、`Press.*` | 卡片入场、面板展开、反馈条滑入 |
| 屏幕转场 | 350–450ms / `Easings.Emphasized*` | NavHost 转场（现状已规范，保持） |
| 仪式时刻 | 600–900ms / `Springs.Snappy` + stagger | 翻牌揭示、结算奖杯、首次空态引导 |

### 8.2 入场编排

- Stagger 步长 40ms（`Duration.StaggerDelay`）、**上限 8 项**——第 9 项起同时入场；列表滚动加载项不 stagger。
- 编排顺序 = 视觉权重：标题 → 面板 → 内容 → 操作（自上而下「铺开」）。

### 8.3 按压语言（全 app 统一，已有雏形）

- 轻件（Chip/小按钮）0.96；标准（卡片/行）0.97；重件（主按钮/FAB）0.94——即 `Press.Light/Standard/HeavyScale`，配对 `Press.light/standard/heavy()` spec。
- 按压必配阴影响应（§3.2 收缩规则）——**只缩放不沉光影 = 贴纸感**。

### 8.4 无限动画白名单（制度）

允许常驻 `rememberInfiniteTransition` 的场景**只有五种**：
1. 正在播放（播放器球呼吸、语音行光条呼吸）；
2. 正在加载（shimmer、转圈、脉冲省略号）；
3. 正在录音/生成（Typing 三点）；
4. 拖拽中的跟随反馈；
5. 首页流体背景（AGSL，30fps 节流 + STARTED 绑定，已有）。

其余一律改一次性动画（入场脉冲、按下流光）。验收用脚本：`grep -rn "rememberInfiniteTransition\|infiniteRepeatable" app/src/main/java/com/azurlane/blyy/ui --include=*.kt | grep -v theme` 逐条对照白名单（§11.3）。

### 8.5 转场连续性

- 头像/头卡 sharedElement 保持；新增「列表项 → 详情」容器变换用于图鉴→语音页的卡片过渡（`AnimatedContent` scope 已具备）。
- 底栏隐藏/显示维持现状（滚动驱动 + FadeThrough tab 切换）。

---

## 9. 深色模式与可访问性

### 9.1 深色 = 主场

- 暗色为设计基准（深海叙事），亮色为其「瓷白海雾」映射——**所有新组件先出暗色稿再映射亮色**，避免「暗色是亮色的补丁」。
- 暗色下：辉光/渐变可稍浓（自发光仪表感），但表面对比（面板 vs 背景）压到 ≤8% 亮度差，防「灰蒙蒙」。
- 修 `ClassicDarkScheme` 缺失的 `surfaceContainerLowest/Low`（Theme.kt:80-101）——补 #040B14/#0A1524，两风格阶梯对齐。

### 9.2 可访问性清单（V2 验收项）

1. 触控目标 ≥48dp（§4.3，含热区补偿）。
2. 可交互元素全部有语义：折叠播放球（「播放/暂停，当前 XX」）、语言切换、手势区（「双指缩放」）、编辑模式（`stateDescription`）。
3. 对比度矩阵过检（§2.7）。
4. 动效不喧宾：尊重系统「移除动画」偏好（`Settings.Global.ANIMATOR_DURATION_SCALE == 0f` 时 stagger 退化为同时入场、脉冲/流光跳过）——一个 `rememberReducedMotion()` 工具函数统一判断。
5. 动态字体：标题类 `maxLines` + ellipsis 显式声明，防大字号爆破布局。

---

## 10. Token 落地代码（可直接并入 theme/）

```kotlin
// ───────── Color.kt 增量 ─────────
object Scrim {
    val Base = Color(0xFF05101E)
    val OnScrim = Color.White
    const val Subtle = 0.35f
    const val Medium = 0.55f
    const val Heavy = 0.85f
}

// Primary 补档
val PrimaryDeepLight = Color(0xFF0077B6)
val PrimaryVeilLight = Color(0x1F0096C7)   // 12%
val PrimaryVeilDark = Color(0x2948CAE4)    // 16%

// 金色文字安全档
object Gold {
    val TextLight = Color(0xFF8C5E00)
    val TextDark = Color(0xFFFFD97A)
    val Glow = Color(0x47FFD166)           // 28%
}

// Rarity 补全（其余档同构，见 §2.5 表）
val PriorityGlow = Color(0x4D60A5FA)
val EliteGlow = Color(0x4034D399)

// Glass 三档常量（供 adaptiveGlassSurface 内部按档取用）
object GlassSpec {
    const val BAR_SURFACE_DARK = 0xB8.toInt()   // 0.72
    const val BAR_BORDER = 0x4D                  // 30%
    const val CARD_SURFACE_DARK = 0xE0           // 0.88
    const val SHEET_SURFACE_DARK = 0xF0          // 0.94
}

// ───────── Elevation.kt 旁新增：四层深度 Modifier ─────────
enum class DepthLayer { Sea, Deck, Instrument, Lookout }

@Composable
fun Modifier.blyyDepth(
    layer: DepthLayer,
    shape: Shape,
    spotTint: Color? = null,   // 稀有度染色例外入口
): Modifier {
    val isDark = LocalIsDark.current
    val ambient = if (isDark) AppColors.Depth.AmbientDark else AppColors.Depth.AmbientLight
    val spotBase = if (isDark) AppColors.Depth.SpotDark else AppColors.Depth.SpotLight
    val spot = spotTint?.copy(alpha = 0.25f) ?: spotBase
    return when (layer) {
        DepthLayer.Sea -> this
        DepthLayer.Deck -> shadow(4.dp, shape, ambientColor = ambient, spotColor = spot, clip = false)
        DepthLayer.Instrument -> shadow(12.dp, shape, ambientColor = ambient, spotColor = spot)
        DepthLayer.Lookout -> shadow(16.dp, shape, ambientColor = ambient, spotColor = spot)
    }
}

// ───────── Type.kt 增量 ─────────
val NumericHud = LabelLarge.copy(fontFeatureSettings = "tnum", fontFamily = BlyyFontFamily.Mono)
val NumericScore = HeadlineSmallBold.copy(fontFeatureSettings = "tnum")
val NumericTimer = TitleMediumBold.copy(fontFeatureSettings = "tnum")

// ───────── Animation.kt 增量 ─────────
object Reduced {
    /** 系统关闭动画时退化为瞬时；一次性装饰动效直接跳过 */
    @Composable fun rememberEnabled(): Boolean = remember {
        Settings.Global.getFloat(
            LocalContext.current.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE, 1f
        ) != 0f
    }
}
```

配套资源：`res/drawable/noise_tile.xml`（程序化 2×2/4×4 抖动点阵，`TileMode.Repeat`，alpha 2.5%），叠于大面积渐变之上。

---

## 11. 实施计划与验收

### 11.1 与结构重构的搭载关系（不新增阶段，改造原四阶段）

| 原阶段（审计文档 §6） | 搭载的 V2 视觉任务 |
|---|---|
| 阶段 0 止血 | 无视觉任务（纯 bug 修复，保持像素不变便于回归对比） |
| 阶段 1 组件收敛 | **V2 主战场**：每次「N 套并 1 套」时，收敛产物直接按 §6 新规格实现——BlyyScreenScaffold/BlyyListItem 激活即按新规格、GalleryScaffold 四态按 §6.10、SegmentedControl 从 Guess 抽出、按钮/面板/顶栏规格刷新、令牌增量（§10）先行合入 |
| 阶段 2 结构拆解 | ShipCard 重设计（§6.5）、播放器 V2（§7.3）、Guess 合屏 + 三区布局（§7.4）、气泡 V2 + 失败态（§7.5）、翻牌真翻面（§7.6） |
| 阶段 3 打磨 | 无限动画白名单治理（§8.4）、a11y 清单（§9.2）、reduced-motion、微纹理、暗色阶梯补全、对比度过检 |

### 11.2 里程碑与交付物

| 里程碑 | 内容 | 交付物 |
|---|---|---|
| M1 令牌先行（0.5 周） | §10 全部令牌 + Theme 暗色阶梯补全 + noise 资源；旧组件不改但可开始引用 | theme/ 一次提交，app 外观基本不变（安全合入） |
| M2 骨架组件（1 周，随阶段 1） | Scaffold/ListItem/四态渲染器/分段控件/按钮面板顶栏新规格 | 首批试点屏：**设置 + 排行榜 + 识舰娘历史**（改动面小、四态齐全，做视觉回归样板） |
| M3 英雄组件（1.5 周，随阶段 2） | ShipCard、播放器、Guess 合屏、聊天气泡 | 四大核心场景截图对比集 |
| M4 全量打磨（1 周，随阶段 3） | 白名单治理、a11y、reduced-motion、纹理 | 验收报告（§11.3 全绿） |

### 11.3 验收清单（自动化 + 人工）

```bash
# 1. 屏幕层颜色硬编码保持 0
grep -rn "Color(0x" app/src/main/java/com/azurlane/blyy/ui/screens app/src/main/java/com/azurlane/blyy/ui/components | grep -v "//" | wc -l   # 期望 0

# 2. 裸 dp 治理进度（目标 <60，现状 ~250）
grep -rEn "[0-9]+\.dp" app/src/main/java/com/azurlane/blyy/ui/screens --include=*.kt | grep -vE "AppSpacing|Corner|Watch" | wc -l

# 3. 无限动画白名单（逐条人工对照 §8.4 五类）
grep -rn "rememberInfiniteTransition\|infiniteRepeatable" app/src/main/java/com/azurlane/blyy/ui --include=*.kt | grep -v theme

# 4. 阴影旁路（期望 0 —— 全走 blyyDepth/AppElevation）
grep -rn "\.shadow(" app/src/main/java/com/azurlane/blyy/ui/screens --include=*.kt

# 5. 触控目标（28/32dp IconButton 期望 0）
grep -rn "size(28.dp)\|size(32.dp)" app/src/main/java/com/azurlane/blyy/ui/screens --include=*.kt
```

人工项：模拟器（API 35）明暗 × 双风格截图矩阵过一遍 M2 试点屏与 M3 四场景；TalkBack 抽查播放球/语言切换/编辑模式；对比度抽查表（§2.7）逐项打勾；性能——ShipCard 滚动帧率（预算：常驻无限动画/卡 = 0）。

### 11.4 风险与回滚

| 风险 | 缓解 |
|---|---|
| 亮色海床微调引发「不像原来」 | M1 合入时背景令牌**不改**，M2 试点屏 A/B 截图后再定稿一次切换 |
| ShipCard 重设计改动面大 | 先在「识舰娘历史/排行榜」等非核心路径验证四态渲染器，ShipCard 最后动；新旧实现短期以 `flag` 并存一个里程碑 |
| 玻璃变实/真模糊 API 差异 | 伪玻璃为基线，`RenderEffect` 仅 API 33+ 渐进增强，失败自动回落 |
| 金色降配引发「变素了」观感 | 与签名瞬间（誓约流光/播放呼吸/结算奖杯）同里程碑交付——密度下降的同时峰值上升 |

---

## 12. 一页纸总览

```
设计主张   光的秩序 · 克制的贵金属 · 精密仪器感 · 物理动效
色彩       海床五档阶梯(提亮降饱和) + 功能青补 Deep/Veil 档 + 金三条红线
           + 稀有度 7 档渐变对 + Scrim/Glass 语义化 + 对比度矩阵
材质       四层深度(Sea/Deck/Instrument/Lookout) × 有色双色阴影 × 玻璃三档
           × 受光面板 + 蚀刻描边 + 2.5% 噪点防条带
形状栅格   切角仅限 L1/L2 · 列表圆角=面板−4dp · 卡比例统一 0.75 · 触控 ≥48dp
排版       字阶维持 + tnum 等宽数字族(NumericHud/Score/Timer) + 字重纪律
组件       主按钮(去常驻辉光/按压流光) 蚀刻次按钮 面板三层 variant
           顶栏滚动变实 ShipCard 重设计(铭牌/光条/零常驻动画) 四态渲染器
布局       三区锁定(Guess) 誓约 hero 卡(Home) 播放器跟手(Voice)
           阅读优先+失败重试(Jiuxin) 真翻牌(Secretary)
动效       四层时长 · stagger≤8 · 按压必配光影 · 无限动画五类白名单
落地       搭载原四阶段 · M1 令牌先行 · M2 试点三屏 · M3 英雄四场景 · M4 验收全绿
```
