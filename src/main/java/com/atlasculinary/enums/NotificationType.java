package com.atlasculinary.enums;

public enum NotificationType {
    // Hệ thống
    SYSTEM_ALERT,           // Cảnh báo chung từ hệ thống

    // Liên quan đến Tài khoản
    WELCOME,                // Chào mừng tài khoản mới

    // Liên quan đến Nhà hàng (Vendor/Admin)
    RESTAURANT_SUBMISSION,  // Vendor gửi nhà hàng mới, gửi đến Admin
    RESTAURANT_APPROVED,    // Admin phê duyệt, gửi đến Vendor
    RESTAURANT_REJECTED,    // Admin từ chối, gửi đến Vendor

    NEW_REVIEW,             // Có đánh giá mới về nhà hàng
    NEW_COMMENT,             // Bình luận mới

    NEW_REPORT
}