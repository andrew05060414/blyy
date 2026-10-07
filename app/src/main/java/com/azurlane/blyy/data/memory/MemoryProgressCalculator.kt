package com.azurlane.blyy.data.memory

import com.azurlane.blyy.data.model.ChatMessage
import com.azurlane.blyy.data.model.SessionMemoryProgress

/**
 * 会话级记忆摘要进度计算器
 *
 * 封装已摘要区间的定位与推进逻辑：
 * 1. 进度按会话（sessionId）隔离，新会话从第 0 条消息开始计算进度，不受其他会话影响（修 PX-904）；
 * 2. 优先使用时间戳锚点（[SessionMemoryProgress.summarizedLastTs]）在当前消息列表中定位，
 *    防止历史截断导致的下标漂移；
 * 3. 严格遵循阈值保护（消息总数 ≥ 40，保留最新 12 条，新增待摘要 ≥ 20 条）。
 */
object MemoryProgressCalculator {

    const val MEMORY_TOTAL_THRESHOLD = 40
    const val MEMORY_KEEP_RECENT = 12
    const val MEMORY_MIN_NEW = 20

    data class ProgressRange(
        val from: Int,
        val to: Int,
        val newSummarizedCount: Int,
        val newSummarizedLastTs: Long
    )

    /**
     * 计算当前会话可进行摘要的消息区间 [from, to)。
     *
     * @param messages 当前会话的消息列表
     * @param progress 当前会话的已有摘要进度（若为新会话则为 null）
     * @return 若满足摘要条件，返回 [ProgressRange]；若不满足条件，返回 null
     */
    fun calculateProgressRange(
        messages: List<ChatMessage>,
        progress: SessionMemoryProgress?,
        totalThreshold: Int = MEMORY_TOTAL_THRESHOLD,
        keepRecent: Int = MEMORY_KEEP_RECENT,
        minNew: Int = MEMORY_MIN_NEW
    ): ProgressRange? {
        if (messages.size < totalThreshold) return null

        val from = progress?.summarizedLastTs?.takeIf { it > 0 }?.let { ts ->
            var lastMatchIdx = -1
            messages.forEachIndexed { i, m -> if (m.timestamp <= ts) lastMatchIdx = i }
            if (lastMatchIdx >= 0) lastMatchIdx + 1
            else progress.summarizedCount.coerceAtMost(messages.size)
        } ?: (progress?.summarizedCount ?: 0).coerceAtMost(messages.size)

        val to = messages.size - keepRecent
        if (to - from < minNew) return null

        // 用户消息时间戳可能相同（同毫秒连发），确保锚至少推进一条，避免原地空转
        if (progress != null && to == from && messages.isNotEmpty() &&
            messages[to - 1].timestamp <= progress.summarizedLastTs
        ) return null

        val lastTs = messages.getOrNull(to - 1)?.timestamp ?: 0L
        return ProgressRange(
            from = from,
            to = to,
            newSummarizedCount = maxOf(progress?.summarizedCount ?: 0, to),
            newSummarizedLastTs = maxOf(progress?.summarizedLastTs ?: 0L, lastTs)
        )
    }
}
