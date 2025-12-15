package com.atlasculinary.services.impl;

import com.atlasculinary.dtos.ReportRequest;
import com.atlasculinary.dtos.ReportResponse;
import com.atlasculinary.dtos.ReportStatisticsResponse;
import com.atlasculinary.entities.*;
import com.atlasculinary.enums.ReportStatus;
import com.atlasculinary.enums.ReportType;
import com.atlasculinary.repositories.ReportRepository;
import com.atlasculinary.repositories.AccountRepository;
import com.atlasculinary.repositories.RestaurantRepository;
import com.atlasculinary.repositories.DishRepository;
import com.atlasculinary.repositories.ReviewRepository;
import com.atlasculinary.services.NotificationService;
import com.atlasculinary.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    private static final Logger LOGGER = Logger.getLogger(ReportServiceImpl.class.getName());
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private DishRepository dishRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public ReportResponse createReport(ReportRequest request, String reporterUsername) {
        Account reporter = accountRepository.findByEmail(reporterUsername).orElseThrow();
        Report report = new Report();
        report.setReporterAccount(reporter);
        if (request.getRestaurantId() != null) {
            Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId()).orElse(null);
            report.setReportType(ReportType.RESTAURANT_REPORT);
            report.setRestaurant(restaurant);
        }
        if (request.getReviewId() != null) {
            Review review = reviewRepository.findById(request.getReviewId()).orElse(null);
            report.setReportType(ReportType.REVIEW_REPORT);
            report.setReview(review);
        }
        report.setReason(request.getReason());
        report.setStatus(ReportStatus.PENDING);
        // Lưu xuống DB
        report = reportRepository.save(report);

        try {
            notificationService.notifyAdminNewReport(report.getReportId());
        } catch (Exception e) {
            LOGGER.warning("Không thể gửi thông báo report tới Admin: " + e.getMessage());
        }
        return toResponse(report);
    }

    @Override
    public List<ReportResponse> getReportsByReporter(String reporterUsername) {
        Account reporter = accountRepository.findByEmail(reporterUsername).orElseThrow();
        return reportRepository.findByReporterAccount(reporter)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ReportResponse> getAllReports() {
        return reportRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReportResponse updateReportStatus(UUID reportId, ReportStatus status, String adminUsername) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new RuntimeException("Report not found"));
        Account admin = accountRepository.findByEmail(adminUsername).orElseThrow(() -> new RuntimeException("Admin not found"));
        report.setStatus(status);
        report.setProcessedByAccount(admin);
        report.setProcessedAt(LocalDateTime.now());
        report = reportRepository.save(report);
        return toResponse(report);
    }

    @Override
    public ReportStatisticsResponse getReportStatistics() {
        List<Report> allReports = reportRepository.findAll();
        long total = allReports.size();
        long pending = allReports.stream().filter(r -> r.getStatus() == ReportStatus.PENDING).count();
        long resolved = allReports.stream().filter(r -> r.getStatus() == ReportStatus.RESOLVED).count();
        long rejected = allReports.stream().filter(r -> r.getStatus() == ReportStatus.REJECTED).count();
        return new ReportStatisticsResponse(total, pending, resolved, rejected);
    }

    private ReportResponse toResponse(Report report) {
        ReportResponse res = new ReportResponse();
        res.setReportId(report.getReportId());
        res.setReason(report.getReason());
        res.setStatus(report.getStatus());
        res.setCreatedAt(report.getCreatedAt());
        res.setProcessedAt(report.getProcessedAt());
        return res;
    }
}
