package com.atlasculinary.services;

import com.atlasculinary.dtos.*;
import com.atlasculinary.enums.ApprovalStatus;
import com.atlasculinary.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;
import java.util.List;

/**
 * Dịch vụ chịu trách nhiệm xử lý mọi hình thức thông báo
 * bao gồm Gửi Email và Quản lý Thông báo trong ứng dụng (In-App Notifications).
 */
public interface NotificationService {

    void sendRealtimeNotification(UUID accountId, NotificationDto notification);

    void sendWelcomeNotification(UUID accountId);

    void sendPasswordResetRequest(PasswordResetRequest passwordResetRequest);

    void notifyAdminNewRestaurantSubmission(UUID restaurantId);

    void notifyVendorRestaurantStatusUpdate(RestaurantStatusUpdateRequest request);

    void notifySystemError(SystemErrorRequest request);

    void createInAppNotification(AddNotificationRequest request);

    Page<NotificationDto> getNotificationsByRecipientId(UUID accountId, int page, int size, String sortBy, String sortDirection);

    long getUnreadCount(UUID accountId);

    List<NotificationDto> getTop10Unread(UUID accountId);

    void markAsRead(UUID accessAccountId, Long notificationId);

    void markAllAsRead(UUID accountId);

    void deleteNotification(Long notificationId);

    void notifyVendorNewUserReview(UUID reviewId);

//    void notifyUserVendorReply(UUID replyId);
}
