package com.example.backend.content.notification.service

import com.example.backend.content.notification.converter.NotificationConverter
import com.example.backend.content.notification.dto.NotificationPageResponse
import com.example.backend.content.notification.exception.NotificationErrorCode
import com.example.backend.content.notification.exception.NotificationException
import com.example.backend.content.notification.sse.SseConnectionPool
import com.example.backend.content.notification.type.NotificationType
import com.example.backend.entity.NotificationEntity
import com.example.backend.entity.NotificationRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * @author kwak
 * 2025. 3. 11.
 */
@Service
open class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val converter: NotificationConverter,
    private val sseConnectionPool: SseConnectionPool
) {

    companion object {
        private const val THIRTY_DAYS = 30L
        private const val PAGE_SIZE = 10
    }

    /**
     * 각 targetId 는
     * Like -> postId, Follow -> senderId, Comment -> commentId
     * @author kwak
     * @since 2025-02-11
     */
    @Transactional
    open fun createNotification(memberId: Long, targetId: Long, type: NotificationType, message: String): NotificationEntity {
        // 알림 엔티티 생성 및 저장
        val notificationEntity = NotificationEntity.create(message, memberId, type, targetId)
        return notificationRepository.save(notificationEntity)

        // sse 로 실시간 알림 전송
        // sseConnectionPool.sendNotification(memberId, converter.toResponse(notification, targetId))
    }

    @Async
    open fun sendNotification(memberId: Long, notification: NotificationEntity) {
        sseConnectionPool.sendNotification(memberId, converter.toResponse(notification, notification.targetId))
    }

    @Transactional
    open fun markRead(notificationId: Long, memberId: Long) {
        val notification = notificationRepository
            .findByIdAndMemberId(notificationId, memberId)
            .orElseThrow { NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND) }

        if (notification.isRead()) {
            return
        }
        notification.markRead()
    }

    fun getNotificationPage(page: Int, memberId: Long): NotificationPageResponse {
        val pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createDate"))
        // 알림 목록 조회 (최근 30일)
        val thirtyDaysAgo = LocalDateTime.now().minusDays(THIRTY_DAYS)

        val notifications =
            notificationRepository.findByMemberId(memberId, thirtyDaysAgo, pageRequest)

        val likeResponse = notifications.map { notification ->
            converter.toResponse(notification, notification.targetId)
        }

        return converter.toPageResponse(likeResponse)
    }
}
