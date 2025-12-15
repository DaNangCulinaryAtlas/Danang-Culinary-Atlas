package com.atlasculinary.services.impl;

import com.atlasculinary.controllers.WebSocketNotificationController;
import com.atlasculinary.dtos.*;
import com.atlasculinary.entities.*;
import com.atlasculinary.exceptions.ResourceNotFoundException;
import com.atlasculinary.mappers.NotificationMapper;
import com.atlasculinary.repositories.ReportRepository;
import com.atlasculinary.repositories.ReviewRepository;
import com.atlasculinary.services.*;
import com.atlasculinary.utils.NameUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import com.atlasculinary.enums.ApprovalStatus;
import com.atlasculinary.enums.NotificationType;
import com.atlasculinary.repositories.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger LOGGER = Logger.getLogger(NotificationServiceImpl.class.getName());
    private final NotificationRepository notificationRepository;
    private final AccountService accountService;
    private final AdminService adminService;
    private final VendorService vendorService;
    private final JavaMailSender mailSender;
    private final NotificationMapper notificationMapper;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final WebSocketNotificationController webSocketController;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            NotificationMapper notificationMapper,
            AccountService accountService,
            AdminService adminService,
            VendorService vendorService,
            JavaMailSender mailSender,
            ReviewRepository reviewRepository,
            ReportRepository reportRepository,
            WebSocketNotificationController webSocketController
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.accountService = accountService;
        this.adminService = adminService;
        this.vendorService = vendorService;
        this.mailSender = mailSender;
        this.reviewRepository = reviewRepository;
        this.reportRepository = reportRepository;
        this.webSocketController = webSocketController;
    }
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    @Value("${app.backend.url}")
    private String backendUrl;
    
    @Value("${app.deeplink.scheme}")
    private String deeplinkScheme;

    @Override
    public void sendRealtimeNotification(UUID accountId, NotificationDto notification) {
        try {
            // Get user email to use as WebSocket principal
            Account account = accountService.getAccountById(accountId);
            webSocketController.sendNotificationToUser(account.getEmail(), notification);
            LOGGER.info("Sent real-time notification to user: " + accountId + " (" + account.getEmail() + ")");
        } catch (Exception e) {
            LOGGER.warning("Failed to send real-time notification: " + e.getMessage());
        }
    }

    @Override
    public void sendWelcomeNotification(UUID accountId) {
        try {
            var account = accountService.getAccountById(accountId);
            String recipientEmail = account.getEmail();
            String subject = "Chào mừng đến với Atlas Culinary!";
            String content = buildWelcomeEmailContent(recipientEmail);
            sendEmail(recipientEmail, subject, content);
        } catch (Exception e) {
            LOGGER.severe("Lỗi gửi email thông báo welcome: " + e.getMessage());
        }

    }

    @Override
    public void sendPasswordResetRequest(PasswordResetRequest passwordResetRequest) {
        try{
            var accountDto = accountService.getAccountById(passwordResetRequest.getAccountId());
            String recipientEmail = accountDto.getEmail();
            String platform = passwordResetRequest.getPlatform() != null ? passwordResetRequest.getPlatform() : "web";

            String subject = "Yêu cầu Đặt lại Mật khẩu";
            String content;

            if ("mobile".equalsIgnoreCase(platform)) {
                content = buildPasswordResetContentForMobile(passwordResetRequest.getResetToken());
            } else {
                content = buildPasswordResetContentForWeb(passwordResetRequest.getResetToken());
            }

            sendEmail(recipientEmail, subject, content);
            LOGGER.info("Đã gửi email reset password cho " + recipientEmail + " (platform: " + platform + ")");
        } catch (Exception e) {
            LOGGER.severe("Lỗi gửi email thông báo reset password: " + e.getMessage());
        }
    }

    @Override
    public void notifyAdminNewRestaurantSubmission(UUID restaurantId) {
        try {
            // [NÊN LÀM] Lấy tên quán ăn để hiển thị cho đẹp, thay vì chỉ hiện UUID
            // Ví dụ: Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(...);
            // String restaurantName = restaurant.getName();

            String subject = "[CẦN XÉT DUYỆT] Nhà hàng mới đang chờ duyệt"; // Hoặc chèn tên quán vào đây
            String content = buildAdminSubmissionContent(restaurantId);

            // 1. Lấy danh sách Admin
            List<AdminDto> adminDtoList = adminService.getAllAdmins();

            // 2. Gửi Email 1 lần cho cả nhóm (Sử dụng BCC)
            // Gom danh sách email
            String[] adminEmails = adminDtoList.stream()
                    .map(AdminDto::getEmail)
                    .toArray(String[]::new);

            if (adminEmails.length > 0) {
                sendEmailToGroup(adminEmails, subject, content);
            }

            for (var adminDto : adminDtoList) {
                try {
                    AddNotificationRequest addNotificationRequest = new AddNotificationRequest(
                            adminDto.getAccountId(),
                            "Nhà hàng mới cần duyệt",
                            "Một nhà hàng mới đã được gửi lên. ID: " + restaurantId,
                            NotificationType.RESTAURANT_SUBMISSION,
                            "/admin/review/" + restaurantId
                    );

                    createInAppNotification(addNotificationRequest);
                } catch (Exception innerEx) {
                    // Log lỗi nhẹ để không ảnh hưởng luồng chính
                    LOGGER.warning("Lỗi tạo noti cho Admin " + adminDto.getEmail());
                }
            }

        } catch (Exception e) {
            LOGGER.severe("Lỗi luồng notifyAdminNewRestaurantSubmission: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void notifyVendorRestaurantStatusUpdate(RestaurantStatusUpdateRequest request) {
        var vendorDto = vendorService.getVendorById(request.getVendorId());
        String vendorEmail = vendorDto.getEmail();
        UUID vendorAccountId = vendorDto.getAccountId();

        try {
            String subject = "Cập nhật Trạng thái Nhà hàng: " + request.getRestaurantName();
            String content = buildVendorStatusUpdateContent(request.getRestaurantName(), request.getNewStatus(), request.getRejectionReason());
            sendEmail(vendorEmail, subject, content);
            LOGGER.severe("Gui Thanh Cong");
            String title = request.getNewStatus() == ApprovalStatus.APPROVED ? "Nhà hàng được phê duyệt" : "Nhà hàng bị từ chối";
            String message = request.getNewStatus() == ApprovalStatus.APPROVED ?
                    request.getRestaurantName() + " của bạn đã được Admin phê duyệt." :
                    request.getRestaurantName() + " của bạn đã bị từ chối. Lý do: " + request.getRejectionReason();
            NotificationType type = request.getNewStatus() == ApprovalStatus.APPROVED ? NotificationType.RESTAURANT_APPROVED: NotificationType.RESTAURANT_REJECTED;

            AddNotificationRequest addNotificationRequest = new AddNotificationRequest(
                    vendorAccountId,
                    title,
                    message,
                    type,
                    "/vendor/restaurant/" + request.getRestaurantName()
            );
            createInAppNotification(addNotificationRequest);

        } catch (Exception e) {
            LOGGER.severe("Lỗi gửi email cập nhật trạng thái tới Vendor " + vendorEmail + ": " + e.getMessage());
        }
    }

    @Override
    public void notifySystemError(SystemErrorRequest request) {
        try {
            String errorTitle = request.getErrorTitle();
            String errorMessage = request.getErrorMessage();

            List<AdminDto> adminDtoList = adminService.getAllAdmins();

            if (adminDtoList.isEmpty()) return;

            String subject = "[KHẨN CẤP] Lỗi Hệ Thống: " + errorTitle;
            String content = buildSystemErrorContent(errorTitle, errorMessage);

            String[] adminEmails = adminDtoList.stream()
                    .map(AdminDto::getEmail)
                    .toArray(String[]::new);

            if (adminEmails.length > 0) {
                sendEmailToGroup(adminEmails, subject, content);
            }

            for (var admin : adminDtoList) {
                try {
                    AddNotificationRequest addNotificationRequest = new AddNotificationRequest(
                            admin.getAccountId(),
                            "Cảnh báo Lỗi Hệ thống",
                            errorTitle + ". Chi tiết: " + errorMessage,
                            NotificationType.SYSTEM_ALERT,
                            "/admin/system-logs"
                    );
                    createInAppNotification(addNotificationRequest);
                } catch (Exception innerEx) {
                    LOGGER.warning("Không thể tạo In-App Noti cho admin " + admin.getEmail());
                }
            }

        } catch (Exception e) {
            LOGGER.severe("CRITICAL: Lỗi gửi cảnh báo hệ thống: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void createInAppNotification(AddNotificationRequest request) {

        Account recipientAccount = accountService.getAccountById(request.getAccountId());

        Notification notification = notificationMapper.toEntity(request);
        notification.setAccount(recipientAccount);
        notification.setIsRead(false);

        Notification savedNotification = notificationRepository.save(notification);
        
        // Send real-time notification via WebSocket
        NotificationDto notificationDto = notificationMapper.toDto(savedNotification);
        sendRealtimeNotification(request.getAccountId(), notificationDto);
    }

    @Override
    public Page<NotificationDto> getNotificationsByRecipientId(UUID accountId, int page, int size, String sortBy, String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC :
                Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Notification> notificationPage = notificationRepository.findByAccount_AccountId(accountId, pageable);


        return notificationPage.map(notificationMapper::toDto);
    }

    @Override
    public long getUnreadCount(UUID accountId) {
        return notificationRepository.countByAccount_AccountIdAndIsReadFalse(accountId);
    }

    @Override
    public List<NotificationDto> getTop10Unread(UUID accountId) {

        List<Notification> notifications = notificationRepository
                .findTop10ByAccount_AccountIdAndIsReadFalseOrderByCreatedAtDesc(accountId);

        return notificationMapper.toDtoList(notifications);
    }

    @Override
    @Transactional
    public void markAsRead(UUID accessAccountId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        // ********** KIỂM TRA QUYỀN SỞ HỮU **********
        if (!accountService.isAdmin(accessAccountId) && !notification.getAccount().getAccountId().equals(accessAccountId)) {
            throw new AccessDeniedException("You do not have permission to modify this notification.");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            Notification updatedNotification = notificationRepository.save(notification);
            
            // Send real-time update via WebSocket
            NotificationDto notificationDto = notificationMapper.toDto(updatedNotification);
            sendRealtimeNotification(accessAccountId, notificationDto);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID accountId) {
        notificationRepository.markAllAsReadByAccountId(accountId);

    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {

        var notification = notificationRepository.findById(notificationId)
                        .orElseThrow(()-> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public void notifyVendorNewUserReview(UUID reviewId) {
        try {
            // 1. Lấy thông tin cần thiết: Review, Restaurant, Vendor
            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

            Restaurant restaurant = review.getRestaurant();
            Account owerRestaurantAccount = restaurant.getOwnerAccount();
            UUID vendorAccountId = owerRestaurantAccount.getAccountId();
            String restaurantName = restaurant.getName();
            String emailReviewer = review.getReviewerAccount().getEmail();
            String reviewerName = review.getReviewerAccount().getFullName();
            if (reviewerName == null || reviewerName.isEmpty()) {
                reviewerName = NameUtil.getNameFromEmail(emailReviewer);
            }
            String reviewTitle = review.getComment();

            String vendorEmail = owerRestaurantAccount.getEmail();
            String emailSubject = "Bạn có Đánh giá mới cho nhà hàng " + restaurantName;
            // Sử dụng hàm build đã được đơn giản hóa
            String emailContent = buildVendorNewReviewContent(restaurantName, reviewerName);

            sendEmail(vendorEmail, emailSubject, emailContent);

            String title = "Đánh giá mới cho " + restaurantName;
            String message = reviewerName + " đã gửi đánh giá: \"" + reviewTitle + "\"";

            // --- ĐIỀU CHỈNH TARGET URL CHO VENDOR ---
            String restaurantIdString = restaurant.getRestaurantId().toString();
            String reviewIdString = reviewId.toString();

            // FE Route dẫn đến trang chi tiết review trong khu vực quản lý của Vendor
            String targetUrl = "/vendor/restaurants/" + restaurantIdString + "/reviews/" + reviewIdString;
            // ---------------------------------------

            AddNotificationRequest addNotificationRequest = new AddNotificationRequest(
                    vendorAccountId,
                    title,
                    message,
                    NotificationType.NEW_REVIEW,
                    targetUrl
            );
            createInAppNotification(addNotificationRequest);

        } catch (ResourceNotFoundException e) {
            LOGGER.warning("Không tìm thấy Review hoặc thông tin liên quan với ID: " + reviewId);
        } catch (Exception e) {
            LOGGER.severe("Lỗi gửi email thông báo Review mới: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void notifyAdminNewReport(UUID reportId) {
        try {
            // Lấy thông tin Report
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + reportId));

            // Lấy thông tin hiển thị (Sử dụng Helper Method trong Entity Report)
            String targetName = report.getRestaurant().getName();
            String reportTypeStr = report.getReportType().toString();
            String reason = report.getReason();
            String reporterEmail = report.getReporterAccount().getEmail();

            // Chuẩn bị nội dung thông báo
            String notiTitle = "Báo cáo vi phạm mới: " + reportTypeStr;
            // Message ngắn gọn cho Notification
            String notiMessage = "Đối tượng \"" + targetName + "\" bị báo cáo. Lý do: " + reason;

            String targetUrl = "/admin/reports/" + reportId;

            String emailSubject = "[ADMIN] Cần xử lý báo cáo: " + targetName;
            String emailContent = buildAdminReportContent(reportTypeStr, targetName, reason, reporterEmail, reportId);

            List<AdminDto> adminDtoList = adminService.getAllAdmins();

            String[] adminEmails = adminDtoList.stream()
                    .map(AdminDto::getEmail)
                    .toArray(String[]::new);

            if (adminEmails.length > 0) {
                sendEmailToGroup(adminEmails, emailSubject, emailContent);
            }
            for (AdminDto admin : adminDtoList) {
                try {
                    AddNotificationRequest addNotificationRequest = new AddNotificationRequest(
                            admin.getAccountId(),
                            notiTitle,
                            notiMessage,
                            NotificationType.NEW_REPORT,
                            targetUrl
                    );
                    createInAppNotification(addNotificationRequest);
                } catch (Exception innerEx) {
                    LOGGER.warning("Lỗi tạo noti cho Admin " + admin.getAccountId());
                }
            }

        } catch (ResourceNotFoundException e) {
            LOGGER.warning("Không tìm thấy Report với ID: " + reportId);
        } catch (Exception e) {
            LOGGER.severe("Lỗi hệ thống: " + e.getMessage());
        }
    }

    private String buildAdminReportContent(String type, String targetName, String reason, String reporter, UUID reportId) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='background-color: #f8d7da; padding: 15px; border-radius: 5px; color: #721c24; margin-bottom: 20px;'>" +
                "<h2 style='margin: 0;'>⚠️ Yêu cầu xử lý vi phạm</h2>" +
                "</div>" +
                "<p>Hệ thống vừa nhận được một báo cáo mới từ người dùng <b>" + reporter + "</b>.</p>" +
                "<table style='width: 100%; border-collapse: collapse; margin-bottom: 20px;'>" +
                "  <tr>" +
                "    <td style='padding: 8px; border-bottom: 1px solid #ddd;'><b>Loại đối tượng:</b></td>" +
                "    <td style='padding: 8px; border-bottom: 1px solid #ddd;'>" + type + "</td>" +
                "  </tr>" +
                "  <tr>" +
                "    <td style='padding: 8px; border-bottom: 1px solid #ddd;'><b>Tên đối tượng:</b></td>" +
                "    <td style='padding: 8px; border-bottom: 1px solid #ddd;'><b>" + targetName + "</b></td>" +
                "  </tr>" +
                "  <tr>" +
                "    <td style='padding: 8px; border-bottom: 1px solid #ddd;'><b>Lý do báo cáo:</b></td>" +
                "    <td style='padding: 8px; border-bottom: 1px solid #ddd; color: #d9534f;'>" + reason + "</td>" +
                "  </tr>" +
                "  <tr>" +
                "    <td style='padding: 8px; border-bottom: 1px solid #ddd;'><b>ID Báo cáo:</b></td>" +
                "    <td style='padding: 8px; border-bottom: 1px solid #ddd; font-family: monospace;'>" + reportId + "</td>" +
                "  </tr>" +
                "</table>" +
                "<p>Vui lòng đăng nhập vào trang quản trị để xem chi tiết và đưa ra quyết định (Xóa/Bỏ qua).</p>" +
                "<div style='text-align: center; margin-top: 30px;'>" +
                "  <a href='" + frontendUrl + "/admin/reports/" + reportId + "' " +
                "     style='background-color: #dc3545; color: white; padding: 12px 25px; text-decoration: none; border-radius: 4px; font-weight: bold;'>" +
                "     Xử lý ngay" +
                "  </a>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    @Async
    private void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
            System.out.println("Đã gửi mail thành công đến: " + to);

        } catch (MessagingException | MailException e) {
            System.err.println("Gửi mail thất bại đến " + to + ": " + e.getMessage());
        }
    }


    @Async
    public void sendEmailToGroup(String[] recipients, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);

            helper.setBcc(recipients);
            // Khi dùng BCC, nên set field "To" là chính email hệ thống hoặc để trống (tùy mail server)
            helper.setTo(fromEmail);

            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
            LOGGER.info("Đã gửi email nhóm thành công tới " + recipients.length + " người.");

        } catch (MessagingException | MailException e) {
            LOGGER.severe("Gửi email nhóm thất bại: " + e.getMessage());
        }
    }

    private String buildWelcomeEmailContent(String username) {
        return "<html><body style='font-family: Arial, sans-serif;'><h2>Chào mừng, " + username + "!</h2><p>Tài khoản của bạn đã được tạo thành công.</p><a href='http://app.link/login'>Đăng nhập ngay</a></body></html>";
    }

    private String buildPasswordResetContentForWeb(String resetToken) {
        String webLink = frontendUrl + "/reset-password?token=" + resetToken;
        
        return "<html>" +
                "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                
                "<!-- Header -->" +
                "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; border-radius: 10px; text-align: center; color: white;'>" +
                "<h1 style='margin: 0; font-size: 28px;'>🔐 Đặt Lại Mật Khẩu</h1>" +
                "</div>" +
                
                "<!-- Body -->" +
                "<div style='background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px;'>" +
                
                "<h2 style='color: #333; margin-top: 0;'>Yêu cầu đặt lại mật khẩu</h2>" +
                
                "<p style='color: #666; line-height: 1.6; font-size: 16px;'>" +
                "Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản Atlas Culinary của bạn." +
                "</p>" +
                
                "<p style='color: #666; line-height: 1.6; font-size: 16px;'>" +
                "Nhấp vào nút bên dưới để đặt lại mật khẩu. Link này sẽ hết hạn sau <strong>5 phút</strong>." +
                "</p>" +
                
                "<!-- Primary Button -->" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='" + webLink + "' " +
                "style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                "color: white; padding: 15px 40px; text-decoration: none; border-radius: 25px; " +
                "font-weight: bold; font-size: 16px; display: inline-block; " +
                "box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);'>" +
                "🔑 Đặt Lại Mật Khẩu" +
                "</a>" +
                "</div>" +
                
                "<!-- Security Warning -->" +
                "<div style='background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 5px; padding: 15px; margin: 20px 0;'>" +
                "<p style='margin: 0; color: #856404; font-size: 14px; line-height: 1.6;'>" +
                "<strong>⚠️ Lưu ý bảo mật:</strong><br>" +
                "• Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này<br>" +
                "• Không chia sẻ link này với bất kỳ ai<br>" +
                "• Link sẽ tự động hết hạn sau 5 phút" +
                "</p>" +
                "</div>" +
                
                "<!-- Footer -->" +
                "<p style='color: #999; font-size: 12px; text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd;'>" +
                "Email này được gửi từ <strong>Atlas Culinary System</strong><br>" +
                "Nếu có thắc mắc, vui lòng liên hệ support@atlasculinary.com" +
                "</p>" +
                
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    private String buildPasswordResetContentForMobile(String resetToken) {
        // Sử dụng Universal Link thay vì custom scheme để email client hỗ trợ tốt hơn
        String universalLink = backendUrl + "/api/v1/auth/deeplink/reset-password?token=" + resetToken;
        
        return "<html>" +
                "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                
                "<!-- Header -->" +
                "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; border-radius: 10px; text-align: center; color: white;'>" +
                "<h1 style='margin: 0; font-size: 28px;'>📱 Đặt Lại Mật Khẩu</h1>" +
                "</div>" +
                
                "<!-- Body -->" +
                "<div style='background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px;'>" +
                
                "<h2 style='color: #333; margin-top: 0;'>Yêu cầu đặt lại mật khẩu</h2>" +
                
                "<p style='color: #666; line-height: 1.6; font-size: 16px;'>" +
                "Bạn đã yêu cầu đặt lại mật khẩu từ ứng dụng Atlas Culinary" +
                "</p>" +
                
                "<!-- Deep Link Button -->" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='" + universalLink + "' " +
                "style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                "color: white; padding: 15px 40px; text-decoration: none; border-radius: 25px; " +
                "font-weight: bold; font-size: 16px; display: inline-block; " +
                "box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);'>" +
                "🔓Đặt Lại Mật Khẩu" +
                "</a>" +
                "</div>" +
                
                "<!-- Note -->" +
                "<div style='background: #fff9e6; border: 1px solid #ffe082; border-radius: 5px; padding: 15px; margin: 20px 0;'>" +
                "<p style='margin: 0; color: #f57f17; font-size: 13px; line-height: 1.6;'>" +
                "<strong>💡 Lưu ý:</strong><br>" +
                "Nút trên chỉ hoạt động khi bạn <strong>mở email này trên điện thoại</strong> và đã cài đặt app Atlas Culinary." +
                "</p>" +
                "</div>" +
                
                "<!-- Security Warning -->" +
                "<div style='background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 5px; padding: 15px; margin: 20px 0;'>" +
                "<p style='margin: 0; color: #856404; font-size: 14px; line-height: 1.6;'>" +
                "<strong>⚠️ Bảo mật:</strong><br>" +
                "• Link này chỉ có hiệu lực trong <strong>5 phút</strong><br>" +
                "• Chỉ sử dụng 1 lần duy nhất<br>" +
                "• Không chia sẻ với bất kỳ ai<br>" +
                "• Nếu không phải bạn yêu cầu, vui lòng bỏ qua email này" +
                "</p>" +
                "</div>" +
                
                "<!-- Footer -->" +
                "<p style='color: #999; font-size: 12px; text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd;'>" +
                "Email này được gửi từ <strong>Atlas Culinary Mobile App</strong><br>" +
                "Nếu có thắc mắc, vui lòng liên hệ support@atlasculinary.com" +
                "</p>" +
                
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String buildAdminSubmissionContent(UUID restaurantId) {
        return "<html><body style='font-family: Arial, sans-serif;'><h2>Nhà hàng mới cần xét duyệt</h2><p>Một Vendor đã gửi một nhà hàng mới có ID: " + restaurantId + ". Vui lòng kiểm tra trang quản trị.</p></body></html>";
    }

    private String buildVendorStatusUpdateContent(String restaurantName, ApprovalStatus newStatus, String rejectionReason) {
        String statusText = newStatus == ApprovalStatus.APPROVED ?
                "đã được phê duyệt!" :
                "đã bị từ chối. Lý do: " + rejectionReason;

        return "<html><body style='font-family: Arial, sans-serif;'><h2>Trạng thái Nhà hàng được Cập nhật</h2><p>Nhà hàng <b>" + restaurantName + "</b> của bạn " + statusText + "</p></body></html>";
    }

    private String buildSystemErrorContent(String errorTitle, String errorMessage) {
        return "<html><body style='font-family: Arial, sans-serif; color: red;'><h2>CẢNH BÁO LỖI HỆ THỐNG</h2><h3>" + errorTitle + "</h3><p>Chi tiết:</p><pre>" + errorMessage + "</pre></body></html>";
    }

    private String buildVendorNewReviewContent(String restaurantName, String reviewerName) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>🔔 Đánh Giá Mới Cho Nhà Hàng</h2>" +
                "<p>Nhà hàng <b>" + restaurantName + "</b> của bạn vừa nhận được một đánh giá mới.</p>" +
                "<p>Từ: <b>" + reviewerName + "</b></p>" +
                "<p>Vui lòng đăng nhập vào trang quản lý để xem chi tiết và phản hồi khách hàng.</p>" +
                "</body></html>";
    }

}
