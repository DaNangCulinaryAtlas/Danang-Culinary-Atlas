package com.atlasculinary.services.impl;

import com.atlasculinary.dtos.AddNotificationRequest;
import com.atlasculinary.entities.BusinessLicense;
import com.atlasculinary.entities.Restaurant;
import com.atlasculinary.enums.ApprovalStatus;
import com.atlasculinary.enums.LicenseType;
import com.atlasculinary.enums.NotificationType;
import com.atlasculinary.repositories.BusinessLicenseRepository;
import com.atlasculinary.repositories.RestaurantRepository;
import com.atlasculinary.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class BusinessLicenseExpirationCheckService {
    
    private static final Logger LOGGER = Logger.getLogger(BusinessLicenseExpirationCheckService.class.getName());
    
    private final BusinessLicenseRepository businessLicenseRepository;
    private final RestaurantRepository restaurantRepository;
    private final NotificationService notificationService;
    
   
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void checkExpiredLicenses() {
        try {
            LOGGER.info("Bắt đầu kiểm tra giấy chứng nhận an toàn thực phẩm đã hết hạn...");
            
            LocalDate today = LocalDate.now();
            
            // Tìm tất cả giấy chứng nhận an toàn thực phẩm đã được duyệt
            List<BusinessLicense> approvedFoodSafetyCerts = businessLicenseRepository
                    .findByLicenseTypeAndApprovalStatus(LicenseType.FOOD_SAFETY_CERT, ApprovalStatus.APPROVED);
            
            int expiredCount = 0;
            
            for (BusinessLicense license : approvedFoodSafetyCerts) {
                LocalDate expireDate = license.getExpireDate();
                
                // Kiểm tra nếu giấy phép đã hết hạn
                if (expireDate != null && expireDate.isBefore(today)) {
                    Restaurant restaurant = license.getRestaurant();
                    System.out.println("expireDate neeeee: " + expireDate);
                    
                    // Chỉ xử lý nếu giấy phép hiện đang ở trạng thái APPROVED
                    if (license.getApprovalStatus() == ApprovalStatus.APPROVED) {
                        // Chuyển giấy phép về trạng thái REJECTED
                        license.setApprovalStatus(ApprovalStatus.REJECTED);
                        businessLicenseRepository.save(license);
                        
                        // Chuyển nhà hàng về trạng thái chờ duyệt
                        restaurant.setApprovalStatus(ApprovalStatus.PENDING);
                        restaurant.setRejectionReason("Giấy chứng nhận an toàn thực phẩm đã hết hạn vào ngày " + expireDate);
                        restaurantRepository.save(restaurant);
                        
                        // Gửi thông báo cho vendor
                        UUID vendorId = restaurant.getOwnerAccount().getAccountId();
                        sendLicenseExpiredNotification(vendorId, license, restaurant);
                        
                        expiredCount++;
                        LOGGER.info(String.format(
                            "Đã cập nhật nhà hàng '%s' (ID: %s) về trạng thái chờ duyệt do giấy phép hết hạn",
                            restaurant.getName(),
                            restaurant.getRestaurantId()
                        ));
                    }
                }
            }
            
            LOGGER.info(String.format(
                "Hoàn thành kiểm tra giấy phép. Tổng số: %d, Đã hết hạn: %d",
                approvedFoodSafetyCerts.size(),
                expiredCount
            ));
            
        } catch (Exception e) {
            LOGGER.severe("Lỗi khi kiểm tra giấy phép hết hạn: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gửi thông báo cho vendor khi giấy chứng nhận an toàn thực phẩm hết hạn
     */
    private void sendLicenseExpiredNotification(UUID vendorId, BusinessLicense license, Restaurant restaurant) {
        try {
            String title = "Giấy chứng nhận an toàn thực phẩm hết hạn";
            String message = String.format(
                "Giấy chứng nhận an toàn thực phẩm (số %s) của nhà hàng '%s' đã hết hạn vào ngày %s. " +
                "Nhà hàng của bạn đã được chuyển về trạng thái chờ duyệt. " +
                "Vui lòng cập nhật giấy chứng nhận mới để tiếp tục hoạt động.",
                license.getLicenseNumber(),
                restaurant.getName(),
                license.getExpireDate()
            );
            
            AddNotificationRequest notification = new AddNotificationRequest(
                vendorId,
                title,
                message,
                NotificationType.SYSTEM_ALERT,
                null
            );
            
            notificationService.createInAppNotification(notification);
        } catch (Exception e) {
            LOGGER.warning("Không thể gửi thông báo cho vendor " + vendorId + ": " + e.getMessage());
        }
    }
}
