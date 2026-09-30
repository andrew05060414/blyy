package com.azurlane.blyy.util

import android.content.Context
import android.net.Uri
import android.util.Log

/**
 * 舰娘头像本地资源解析器
 *
 * 将舰娘名称映射到打包在 APK assets 中的本地高清头像，优先使用本地图片，
 * 匹配不到时由调用方回退到网络 URL。匹配策略核心在 [AvatarMatcher]
 * （纯 Kotlin、无 Context 依赖，可被 JVM 单测覆盖全量名单）。
 *
 * 资源约定：
 * - 头像文件位于 `app/src/main/assets/blhx_avatar/`，这是 Android 标准的 assets
 *   子目录，无需额外 build.gradle.kts 配置即可随 APK 打包。
 * - 支持的图片扩展名：`.png` / `.jpg` / `.jpeg` / `.webp`（不区分大小写）。
 * - 文件名 = 舰娘名称的**无声调全拼** + 可选皮肤后缀 + 扩展名
 *   （例如 `boge.webp` 对应"博格"，`z23_h.webp` 对应"Z23"的誓约婚皮）。
 * - 新增舰娘头像只需将图片放入上述目录，重新构建即可自动匹配。
 *
 * 性能策略：
 * - 首次调用时通过 `AssetManager.list("blhx_avatar")` 构建文件名索引，O(n) 一次性构建。
 * - 后续查询为 O(1) HashMap 查找。
 * - 索引在进程生命周期内缓存（头像随 APK 打包，运行期不变）。
 */
object LocalAvatarResolver {

    private const val TAG = "LocalAvatarResolver"
    private const val ASSET_URI_SCHEME = "file"
    private const val AVATAR_DIR = "blhx_avatar"
    private const val ASSET_PATH_PREFIX = "/android_asset/$AVATAR_DIR/"

    /** 支持的图片扩展名（小写，按匹配优先级排列） */
    private val SUPPORTED_EXTENSIONS = listOf(".png", ".jpg", ".jpeg", ".webp")

    /**
     * 进程级缓存：文件名（不含扩展名，小写） → assets 中的完整文件名（含扩展名，原始大小写）
     *
     * 使用小写键以支持大小写不敏感匹配（如文件 `Z23.webp` 的键为 `z23`，
     * 舰娘 "Z23" 的拼音 `z23` 可直接匹配）。
     */
    @Volatile
    private var avatarFiles: Map<String, String>? = null

    private val lock = Any()

    /**
     * 解析舰娘头像，返回本地 assets URI。匹配策略详见 [AvatarMatcher.match]。
     *
     * @param context 任意 Context（内部使用 ApplicationContext 避免泄漏）
     * @param shipName 舰娘名称（与 biligame wiki 图鉴列表一致）
     * @return 本地头像 URI（如 `file:///android_asset/blhx_avatar/boge.webp`），匹配不到返回 null
     */
    fun resolve(context: Context, shipName: String): String? {
        val matched = AvatarMatcher.match(shipName, ensureIndex(context)) ?: return null
        return buildAssetUri(matched)
    }

    /**
     * 解析舰娘头像（区分档案类型，避免同名不同类型舰娘误匹配）。
     *
     * 匹配策略：
     * 1. 优先按 `shipName@archiveType` 小写精确匹配
     * 2. 仅当 archiveType 为 "DOCK" 时，回退到不区分类型的 [resolve]（兼容现有 assets 命名，
     *    assets 中的文件默认就是 DOCK 类型）
     * 3. 对于其他类型（如 "STUDENT"），不回退，返回 null（学生档案有不同立绘，
     *    不应复用 DOCK 头像，避免同名误匹配）
     *
     * @param archiveType 档案类型（"DOCK" 或 "STUDENT"），null/空字符串表示不区分
     */
    fun resolve(context: Context, shipName: String, archiveType: String?): String? {
        if (shipName.isBlank()) return null
        if (archiveType.isNullOrBlank()) return resolve(context, shipName)

        val index = ensureIndex(context)
        if (index.isEmpty()) return null

        // 1. 优先按 "舰娘名@档案类型" 小写精确匹配（避免同名误匹配）
        val typeKey = "${shipName}@$archiveType".lowercase()
        index[typeKey]?.let { return buildAssetUri(it) }

        // 2. 仅 DOCK 类型回退到不区分类型的匹配（assets 默认就是 DOCK 头像）
        // STUDENT 等其他类型不回退，避免学生档案错误显示同名 DOCK 舰娘的本地头像
        if (archiveType == "DOCK") {
            return resolve(context, shipName)
        }

        return null
    }

    /**
     * 解析舰娘头像（区分档案类型 + 誓约状态）。
     *
     * 誓约状态下优先匹配 `_h` 婚皮变体（[AvatarMatcher.matchOath]）：
     * - 若婚皮不存在，回退到默认头像
     * - 若默认头像也不存在，返回 null（由调用方回退到网络 URL）
     *
     * @param archiveType 档案类型（"DOCK" 或 "STUDENT"），null/空字符串表示不区分
     * @param isOathed 是否已誓约（true 时优先匹配 `_h` 婚皮）
     */
    fun resolve(context: Context, shipName: String, archiveType: String?, isOathed: Boolean): String? {
        if (!isOathed) return resolve(context, shipName, archiveType)

        // 誓约状态：仅 DOCK 类型尝试婚皮（STUDENT 无婚皮概念）
        if (archiveType != null && archiveType != "DOCK") {
            return resolve(context, shipName, archiveType)
        }

        val index = ensureIndex(context)
        val matched = AvatarMatcher.matchOath(shipName, index) ?: return null
        return buildAssetUri(matched)
    }

    /**
     * 返回有效的头像 URI：优先本地，回退网络 URL。
     * 便于调用方一行完成 "本地优先 → 网络兜底" 逻辑。
     */
    fun resolveOrDefault(context: Context, shipName: String, networkUrl: String): String {
        return resolve(context, shipName) ?: networkUrl
    }

    /**
     * 返回有效的头像 URI（区分档案类型）：优先本地，回退网络 URL。
     */
    fun resolveOrDefault(context: Context, shipName: String, archiveType: String?, networkUrl: String): String {
        return resolve(context, shipName, archiveType) ?: networkUrl
    }

    /**
     * 返回有效的头像 URI（区分档案类型 + 誓约状态）：优先本地婚皮，回退默认/网络 URL。
     *
     * @param isOathed 是否已誓约（true 时优先匹配 `_h` 婚皮）
     */
    fun resolveOrDefault(context: Context, shipName: String, archiveType: String?, isOathed: Boolean, networkUrl: String): String {
        return resolve(context, shipName, archiveType, isOathed) ?: networkUrl
    }

    /** 懒加载构建 assets 文件名索引（线程安全双重检查锁） */
    private fun ensureIndex(context: Context): Map<String, String> {
        avatarFiles?.let { return it }
        synchronized(lock) {
            avatarFiles?.let { return it }
            val index = buildIndex(context.applicationContext)
            avatarFiles = index
            Log.i(TAG, "Local avatar index built: ${index.size} files")
            return index
        }
    }

    /**
     * 扫描 assets/blhx_avatar 目录，构建"文件名(不含扩展名，小写) → 完整文件名(含扩展名)"映射。
     *
     * 使用小写键以支持大小写不敏感匹配。同名不同扩展名时按 [SUPPORTED_EXTENSIONS] 优先级保留最优
     * （例如同时存在 `Z23.png` 和 `Z23.jpg` 时优先保留 `.png`）。
     */
    private fun buildIndex(appContext: Context): Map<String, String> {
        return try {
            val files = appContext.assets.list(AVATAR_DIR) ?: emptyArray()
            buildIndexFromFiles(files)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to list avatar assets", e)
            emptyMap()
        }
    }

    /** 从文件名数组构建索引（与 [buildIndex] 共用，便于测试复用同一命名规则） */
    private fun buildIndexFromFiles(files: Array<String>): Map<String, String> {
        val index = HashMap<String, String>(files.size)
        for (file in files) {
            val lower = file.lowercase()
            val ext = SUPPORTED_EXTENSIONS.firstOrNull { lower.endsWith(it) } ?: continue
            val name = file.dropLast(ext.length)
            val key = name.lowercase()
            val extPriority = SUPPORTED_EXTENSIONS.indexOf(ext)
            // 同名文件保留扩展名优先级更高的（数值更小 = 更优先）
            val existing = index[key]
            if (existing == null || extPriority < extensionPriority(existing)) {
                index[key] = file
            }
        }
        return index
    }

    /** 返回文件扩展名在 SUPPORTED_EXTENSIONS 中的优先级（越小越优先），不支持返回 Int.MAX_VALUE */
    private fun extensionPriority(fileName: String): Int {
        val lower = fileName.lowercase()
        return SUPPORTED_EXTENSIONS.indexOfFirst { lower.endsWith(it) }.let { if (it < 0) Int.MAX_VALUE else it }
    }

    /**
     * 构建 assets URI。
     *
     * 使用 Uri.Builder 构造，确保特殊字符（★、·、(、)、μ 等）正确处理。
     * Coil 的 AssetUriFetcher 会从 uri.path 中提取 /android_asset/ 之后的部分
     * 作为 asset 路径传给 AssetManager.open()，因此路径不能被 percent-encode。
     *
     * @param fileName assets 中的完整文件名（含扩展名），如 "boge.webp"
     */
    private fun buildAssetUri(fileName: String): String {
        // 直接拼接字符串而非 Uri.encode，因为：
        // 1. 舰娘文件名不含 / ? # % & + 等需要 encode 的字符
        // 2. AssetManager.open() 需要原始文件名，percent-encoding 会导致找不到文件
        // 3. Android Uri.parse 对 path 中的非 ASCII 字符（★、·、中文字符）保持原样
        val path = "$ASSET_PATH_PREFIX$fileName"
        return Uri.Builder()
            .scheme(ASSET_URI_SCHEME)
            .path(path)
            .build()
            .toString()
    }
}
