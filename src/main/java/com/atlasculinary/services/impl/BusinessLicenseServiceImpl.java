package com.atlasculinary.services.impl;

import com.atlasculinary.dtos.*;
import com.atlasculinary.entities.Account;
import com.atlasculinary.entities.BusinessLicense;
import com.atlasculinary.entities.Restaurant;
import com.atlasculinary.enums.ApprovalStatus;
import com.atlasculinary.enums.LicenseType;
import com.atlasculinary.mappers.BusinessLicenseMapper;
import com.atlasculinary.repositories.AccountRepository;
import com.atlasculinary.repositories.BusinessLicenseRepository;
import com.atlasculinary.repositories.RestaurantRepository;
import com.atlasculinary.services.AccountService;
import com.atlasculinary.services.BusinessLicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessLicenseServiceImpl implements BusinessLicenseService {

    private final BusinessLicenseRepository businessLicenseRepository;
    private final RestaurantRepository restaurantRepository; // <-- Cần thêm cái này
    private final AccountRepository accountRepository;
    private final BusinessLicenseMapper businessLicenseMapper;
    private final AccountService accountService;

    @Override
    @Transactional
    public BusinessLicenseDto createLicense(UUID requesterId, AddBusinessLicenseRequest request) {
        // 1. Tìm Restaurant dựa trên ID trong request
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        // 2. Kiểm tra Quyền: Người tạo (requesterId) phải là chủ sở hữu của Nhà hàng này
        if (!restaurant.getOwnerAccount().getAccountId().equals(requesterId)) {
            throw new RuntimeException("You do not have permission to add license for this restaurant.");
        }

        // 3. Kiểm tra trùng số giấy phép (Unique Constraint - Global)
        if (businessLicenseRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new RuntimeException("License number already exists in the system.");
        }

        // 4. Kiểm tra xem NHÀ HÀNG này đã có loại giấy tờ này chưa
        if (businessLicenseRepository.existsByRestaurant_RestaurantIdAndLicenseType(
                request.getRestaurantId(), request.getLicenseType())) {
            throw new RuntimeException("This restaurant already has a license of type: " + request.getLicenseType());
        }

        // 5. Validate Logic nghiệp vụ riêng
        if (request.getLicenseType() == LicenseType.FOOD_SAFETY_CERT && request.getExpireDate() == null) {
            throw new IllegalArgumentException("Food Safety Certificate must have an expiration date.");
        }

        // 6. Mapping và Set quan hệ
        BusinessLicense license = businessLicenseMapper.toEntity(request);

        // QUAN TRỌNG: Set Restaurant thủ công (vì Mapper ignore)
        license.setRestaurant(restaurant);

        license.setApprovalStatus(ApprovalStatus.PENDING);

        BusinessLicense savedLicense = businessLicenseRepository.save(license);
        return businessLicenseMapper.toDto(savedLicense);
    }

    @Override
    @Transactional
    public BusinessLicenseDto updateLicense(UUID licenseId, UpdateBusinessLicenseRequest request, UUID requesterId) {
        BusinessLicense license = businessLicenseRepository.findById(licenseId)
                .orElseThrow(() -> new RuntimeException("License not found"));

        // 1. Check quyền: Requester phải là chủ sở hữu của nhà hàng gắn với giấy phép này
        if (!license.getRestaurant().getOwnerAccount().getAccountId().equals(requesterId)) {
            throw new RuntimeException("You do not have permission to update this license.");
        }

        // 2. Chỉ được sửa khi chưa duyệt hoặc bị từ chối (hoặc Pending)
        // Nếu đã Approved thì thường không cho sửa, bắt phải tạo mới hoặc quy trình gia hạn
        if (license.getApprovalStatus() == ApprovalStatus.APPROVED) {
            throw new RuntimeException("Cannot update an approved license.");
        }

        // 3. Mapping update
        businessLicenseMapper.updateFromRequest(request, license);

        // 4. Validate lại expireDate
        if (license.getLicenseType() == LicenseType.FOOD_SAFETY_CERT && license.getExpireDate() == null) {
            throw new IllegalArgumentException("Food Safety Certificate must have an expiration date.");
        }

        // 5. Reset trạng thái
        license.setApprovalStatus(ApprovalStatus.PENDING);
        license.setRejectionReason(null);
        license.setApprovedByAccount(null);
        license.setApprovedAt(null);

        return businessLicenseMapper.toDto(businessLicenseRepository.save(license));
    }

    @Override
    public List<BusinessLicenseDto> getMyLicenses(UUID ownerId) {
        // Lấy tất cả giấy phép thuộc về các nhà hàng của Owner này
        List<BusinessLicense> licenses = businessLicenseRepository.findAllByRestaurant_OwnerAccount_AccountId(ownerId);
        return businessLicenseMapper.toDtoList(licenses);
    }

    @Override
    public List<BusinessLicenseDto> getLicensesByRestaurant(UUID restaurantId, UUID requesterId) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        boolean isOwner = restaurant.getOwnerAccount().getAccountId().equals(requesterId);
        boolean isAdmin = accountService.isAdmin(requesterId); // Giả sử bạn có hàm check admin

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Access denied. You do not own this restaurant.");
        }

        List<BusinessLicense> licenses = businessLicenseRepository.findAllByRestaurant_RestaurantId(restaurantId);
        return businessLicenseMapper.toDtoList(licenses);
    }

    @Override
    public BusinessLicenseDto getLicenseById(UUID licenseId, UUID requesterId) {
        BusinessLicense license = businessLicenseRepository.findById(licenseId)
                .orElseThrow(() -> new RuntimeException("License not found"));

        boolean isAdmin = accountService.isAdmin(requesterId);
        boolean isOwner = license.getRestaurant().getOwnerAccount().getAccountId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("Access denied. You do not own this license.");
        }


        return businessLicenseMapper.toDto(license);
    }

    @Override
    public Page<BusinessLicenseDto> getAllLicenses(int page, int size, String sortBy, String sortDirection,
                                                   LicenseType licenseType, ApprovalStatus approvalStatus) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BusinessLicense> licensePage = businessLicenseRepository.findAllByFilters(licenseType, approvalStatus, pageable);

        return licensePage.map(businessLicenseMapper::toDto);
    }

    @Override
    @Transactional
    public BusinessLicenseDto updateApprovalStatus(UUID adminId, UUID licenseId, UpdateLicenseStatusRequest request) {
        BusinessLicense license = businessLicenseRepository.findById(licenseId)
                .orElseThrow(() -> new RuntimeException("License not found"));

        Account admin = accountRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin account not found"));

        license.setApprovalStatus(request.getStatus());

        if (request.getStatus() == ApprovalStatus.APPROVED) {
            license.setApprovedByAccount(admin);
            license.setApprovedAt(LocalDateTime.now());
            license.setRejectionReason(null);
        } else if (request.getStatus() == ApprovalStatus.REJECTED) {
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
                throw new RuntimeException("Rejection reason is required when rejecting a license.");
            }
            license.setRejectionReason(request.getRejectionReason());
            license.setApprovedByAccount(admin);
            license.setApprovedAt(LocalDateTime.now());
        }

        return businessLicenseMapper.toDto(businessLicenseRepository.save(license));
    }

    @Override
    @Transactional
    public void deleteLicense(UUID licenseId, UUID requesterId) {
        BusinessLicense license = businessLicenseRepository.findById(licenseId)
                .orElseThrow(() -> new RuntimeException("License not found"));

        // Check quyền:
        boolean isAdmin = accountService.isAdmin(requesterId); // Giả sử service này có method check role
        boolean isOwner = license.getRestaurant().getOwnerAccount().getAccountId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("Permission denied. Only Admin or Restaurant Owner can delete.");
        }

        businessLicenseRepository.delete(license);
    }
}