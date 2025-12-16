package com.atlasculinary.services.impl;

import com.atlasculinary.dtos.*;
import com.atlasculinary.entities.Account;
import com.atlasculinary.entities.BusinessLicense;
import com.atlasculinary.enums.ApprovalStatus;
import com.atlasculinary.enums.LicenseType;
import com.atlasculinary.mappers.BusinessLicenseMapper;
import com.atlasculinary.repositories.AccountRepository;
import com.atlasculinary.repositories.BusinessLicenseRepository;
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
    private final AccountRepository accountRepository;
    private final BusinessLicenseMapper businessLicenseMapper;
    private final AccountService accountService;

    @Override
    @Transactional
    public BusinessLicenseDto createLicense(UUID ownerId, AddBusinessLicenseRequest request) {
        // 1. Kiểm tra User tồn tại
        Account owner = accountRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner account not found"));

        // 2. Kiểm tra trùng số giấy phép (Unique Constraint)
        if (businessLicenseRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new RuntimeException("License number already exists in the system.");
        }

        // 3. Kiểm tra xem User đã có loại giấy tờ này chưa
        // (Một user chỉ được có 1 bản ghi cho BUSINESS_REGISTRATION, 1 bản cho FOOD_SAFETY_CERT...)
        if (businessLicenseRepository.existsByOwnerAccount_AccountIdAndLicenseType(ownerId, request.getLicenseType())) {
            throw new RuntimeException("You already have a license of type: " + request.getLicenseType());
        }

        // 4. Validate Logic nghiệp vụ riêng cho từng loại giấy
        if (request.getLicenseType() == LicenseType.FOOD_SAFETY_CERT && request.getExpireDate() == null) {
            throw new IllegalArgumentException("Food Safety Certificate must have an expiration date.");
        }
        // Đối với BUSINESS_REGISTRATION, expireDate có thể null (vô thời hạn), không cần check.

        // 5. Mapping và Lưu
        BusinessLicense license = businessLicenseMapper.toEntity(request);
        license.setOwnerAccount(owner);
        license.setApprovalStatus(ApprovalStatus.PENDING);

        BusinessLicense savedLicense = businessLicenseRepository.save(license);
        return businessLicenseMapper.toDto(savedLicense);
    }

    @Override
    @Transactional
    public BusinessLicenseDto updateLicense(UUID licenseId, UpdateBusinessLicenseRequest request, UUID ownerId) {
        BusinessLicense license = businessLicenseRepository.findById(licenseId)
                .orElseThrow(() -> new RuntimeException("License not found"));

        // Check quyền sở hữu
        if (!license.getOwnerAccount().getAccountId().equals(ownerId)) {
            throw new RuntimeException("You do not have permission to update this license.");
        }

        // Chỉ được sửa khi chưa duyệt hoặc bị từ chối
        if (license.getApprovalStatus() == ApprovalStatus.APPROVED) {
            throw new RuntimeException("Cannot update an approved license.");
        }

        // Mapping update (ignore null values)
        businessLicenseMapper.updateFromRequest(request, license);

        // Validate lại sau khi update (đề phòng trường hợp user set expireDate = null cho giấy ATTP)
        if (license.getLicenseType() == LicenseType.FOOD_SAFETY_CERT && license.getExpireDate() == null) {
            throw new IllegalArgumentException("Food Safety Certificate must have an expiration date.");
        }

        // Reset trạng thái về PENDING để Admin duyệt lại
        license.setApprovalStatus(ApprovalStatus.PENDING);
        license.setRejectionReason(null);
        license.setApprovedByAccount(null);
        license.setApprovedAt(null);

        return businessLicenseMapper.toDto(businessLicenseRepository.save(license));
    }

    @Override
    public List<BusinessLicenseDto> getMyLicenses(UUID ownerId) {
        // Trả về danh sách tất cả giấy phép của user
        List<BusinessLicense> licenses = businessLicenseRepository.findAllByOwnerAccount_AccountId(ownerId);
        return businessLicenseMapper.toDtoList(licenses);
    }

    @Override
    public BusinessLicenseDto getLicenseById(UUID licenseId) {
        BusinessLicense license = businessLicenseRepository.findById(licenseId)
                .orElseThrow(() -> new RuntimeException("License not found"));
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

        // Logic check quyền: Admin được xóa mọi cái, Owner chỉ được xóa của mình
        // Giả sử logic check quyền đã được xử lý ở Controller hoặc Security Context,
        // hoặc thêm logic đơn giản ở đây:

        boolean isAdmin = accountService.isAdmin(requesterId);
        if (!isAdmin && !license.getOwnerAccount().getAccountId().equals(requesterId)) {
             throw new RuntimeException("Permission denied");
        }


        businessLicenseRepository.delete(license);
    }
}