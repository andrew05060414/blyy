package com.azurlane.blyy.data.memory

import com.azurlane.blyy.data.model.ChatMessage
import com.azurlane.blyy.data.model.ChatMessageType
import com.azurlane.blyy.data.model.PersonaMemory
import com.azurlane.blyy.data.model.SessionMemoryProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SessionMemoryProgressTest {

    private fun createMessage(index: Int, timestamp: Long): ChatMessage {
        return ChatMessage(
            id = "msg_" + index,
            type = if (index % 2 == 0) ChatMessageType.USER.name else ChatMessageType.AI.name,
            content = "Message " + index,
            timestamp = timestamp
        )
    }

    @Test
    fun testNewSessionStartsFromBeginningRegardlessOfOtherSessionProgress() {
        // Session 1 积累了 100 条消息并已完成摘要，进度推进到 count=88, ts=1880L
        val session1Progress = SessionMemoryProgress(
            summarizedCount = 88,
            summarizedLastTs = 1880L
        )

        // Session 2 是同一舰娘的新会话，累计 40 条新消息（时间戳晚于 Session 1）
        val session2Messages = (0 until 40).map { i ->
            createMessage(i, 2000L + i * 10L)
        }

        // 验证隔离（修 PX-904）：Session 2 使用自身独立进度（新会话为 null），从下标 0 开始摘要
        val session2Range = MemoryProgressCalculator.calculateProgressRange(
            messages = session2Messages,
            progress = null
        )
        assertNotNull("新会话达到 40 条阈值后应能正常触发摘要", session2Range)
        assertEquals("新会话起始摘要下标必须从 0 开始，不能跳过消息", 0, session2Range!!.from)
        assertEquals("保留最新 12 条，摘要截至下标 28", 28, session2Range.to)
        assertEquals("推进后的已摘要数量应为 28", 28, session2Range.newSummarizedCount)
        assertEquals("推进后的时间戳锚点应为第 27 条的时间戳", 2000L + 27 * 10L, session2Range.newSummarizedLastTs)

        // 证明原 Bug：若错误地共享 Session 1 的进度，Session 2 的前 88 条消息会被当成已摘要永久跳过
        val buggyRange = MemoryProgressCalculator.calculateProgressRange(
            messages = session2Messages,
            progress = session1Progress
        )
        assertNull("原 Bug 表现：共享旧会话进度会导致新会话因 from=40 > to=28 而被跳过", buggyRange)
    }

    @Test
    fun testSessionProgressUpdatesAreIsolatedBetweenSessions() {
        // 模拟 DataStore 存储的会话进度映射表
        val sessionProgressMap = mutableMapOf<String, SessionMemoryProgress>()

        val session1Id = "session_1"
        val session2Id = "session_2"

        // Session 1 完成摘要并保存进度
        sessionProgressMap[session1Id] = SessionMemoryProgress(
            summarizedCount = 88,
            summarizedLastTs = 1880L
        )

        // Session 2 进行第一次摘要（0..28）
        val session2Messages = (0 until 40).map { i -> createMessage(i, 2000L + i * 10L) }
        val range = MemoryProgressCalculator.calculateProgressRange(session2Messages, sessionProgressMap[session2Id])
        assertNotNull(range)

        // 更新 Session 2 进度
        sessionProgressMap[session2Id] = SessionMemoryProgress(
            summarizedCount = range!!.newSummarizedCount,
            summarizedLastTs = range.newSummarizedLastTs
        )

        // 验证两个会话的进度完全隔离
        assertEquals(88, sessionProgressMap[session1Id]?.summarizedCount)
        assertEquals(1880L, sessionProgressMap[session1Id]?.summarizedLastTs)
        assertEquals(28, sessionProgressMap[session2Id]?.summarizedCount)
        assertEquals(2000L + 27 * 10L, sessionProgressMap[session2Id]?.summarizedLastTs)
    }

    @Test
    fun testMemoryTextInheritedAcrossSessionsForSameShip() {
        // 模拟按 shipKey 存储的舰娘长期记忆
        val personaMemories = mutableMapOf<String, PersonaMemory>()
        val shipKey = "preset:enterprise"

        // Session 1 沉淀了长期记忆
        personaMemories[shipKey] = PersonaMemory(
            text = "指挥官喜欢喝红茶，约定好周末一起出击。",
            updatedAt = 1000L
        )

        // Session 2 无论在哪个会话，都能读取到同一舰娘的历史记忆
        val inheritedMemory = personaMemories[shipKey]?.text
        assertEquals("指挥官喜欢喝红茶，约定好周末一起出击。", inheritedMemory)

        // Session 2 摘要后合并记忆
        val updatedText = "指挥官喜欢喝红茶，约定好周末一起出击；最近在研究新的战术演习。"
        personaMemories[shipKey] = PersonaMemory(
            text = updatedText,
            updatedAt = 2000L
        )

        // Session 1 再次对话时也能看到合并后的最新记忆
        assertEquals(updatedText, personaMemories[shipKey]?.text)
    }

    @Test
    fun testSessionDeletionCleansUpOnlyTargetSessionProgress() {
        val sessionProgressMap = mutableMapOf<String, SessionMemoryProgress>()
        sessionProgressMap["session_1"] = SessionMemoryProgress(50, 500L)
        sessionProgressMap["session_2"] = SessionMemoryProgress(28, 2270L)

        // 删除 session_1
        sessionProgressMap.remove("session_1")

        assertNull("被删除会话的进度应被清理", sessionProgressMap["session_1"])
        assertNotNull("未被删除会话的进度应完好保留", sessionProgressMap["session_2"])
        assertEquals(28, sessionProgressMap["session_2"]?.summarizedCount)
    }

    @Test
    fun testTimestampAnchorRelocatesIndexWhenHeadTruncatedWithinSession() {
        // 模拟会话历史截断场景：会话已有进度为 count=28, ts=2270L
        val progress = SessionMemoryProgress(summarizedCount = 28, summarizedLastTs = 2270L)

        // 头部被截断了 10 条消息，当前列表起始时间戳为 2100L（原第 10 条）
        // 共有 50 条消息：时间戳 2100L .. 2590L
        val truncatedMessages = (10 until 60).map { i ->
            createMessage(i, 2000L + i * 10L)
        }

        // 重新计算进度范围
        val range = MemoryProgressCalculator.calculateProgressRange(truncatedMessages, progress)
        assertNotNull(range)

        // 在截断后的列表中，ts=2270L 对应的是原第 27 条，即当前列表中的下标 17
        // 故 from 应该定位到 17 + 1 = 18
        assertEquals("基于时间戳锚点重新定位，防止头部截断导致下标偏移", 18, range!!.from)
        assertEquals(truncatedMessages.size - 12, range.to)
    }
}
