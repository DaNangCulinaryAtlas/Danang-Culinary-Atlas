package com.atlasculinary.services;

import com.atlasculinary.dtos.AddBusinessLicenseRequest;
import com.atlasculinary.dtos.BusinessLicenseDto;
import com.atlasculinary.dtos.UpdateBusinessLicenseRequest;
import com.atlasculinary.dtos.UpdateLicenseStatusRequest;
import com.atlasculinary.enums.ApprovalStatus;
import com.atlasculinary.enums.LicenseType;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface BusinessLicenseService {

    // Vendor: Tạo giấy phép mới
    BusinessLicenseDto createLicense(UUID ownerId, AddBusinessLicenseRequest request);

    // Vendor: Cập nhật thông tin giấy phép
    BusinessLicenseDto updateLicense(UUID licenseId, UpdateBusinessLicenseRequest request, UUID ownerId);

    // Vendor: Xem danh sách giấy phép của chính mình (Trả về List)
    List<BusinessLicenseDto> getMyLicenses(UUID ownerId);

    // Admin: Xem chi tiết giấy phép bất kỳ
    BusinessLicenseDto getLicenseById(UUID licenseId);

    Page<BusinessLicenseDto> getAllLicenses(int page, int size, String sortBy, String sortDirection,
                                            LicenseType licenseType, ApprovalStatus approvalStatus);

    // Admin: Duyệt hoặc Từ chối giấy phép
    BusinessLicenseDto updateApprovalStatus(UUID adminId, UUID licenseId, UpdateLicenseStatusRequest request);

    // Admin/Vendor: Xóa giấy phép
    void deleteLicense(UUID licenseId, UUID requesterId);
}