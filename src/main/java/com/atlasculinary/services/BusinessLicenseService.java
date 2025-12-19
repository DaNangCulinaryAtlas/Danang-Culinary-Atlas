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

    // Vendor: Tạo giấy phép mới cho một nhà hàng cụ thể
    BusinessLicenseDto createLicense(UUID requesterId, AddBusinessLicenseRequest request);

    // Vendor: Cập nhật thông tin giấy phép
    BusinessLicenseDto updateLicense(UUID licenseId, UpdateBusinessLicenseRequest request, UUID requesterId);

    // Vendor: Xem danh sách giấy phép của tất cả nhà hàng mình sở hữu
    List<BusinessLicenseDto> getMyLicenses(UUID ownerId);

    // Vendor/Public: Xem giấy phép của 1 nhà hàng cụ thể (Optional - bổ sung thêm cho tiện)
    List<BusinessLicenseDto> getLicensesByRestaurant(UUID restaurantId, UUID requesterId);

    BusinessLicenseDto getLicenseById(UUID licenseId, UUID requesterId);

    // Admin: Lọc danh sách
    Page<BusinessLicenseDto> getAllLicenses(int page, int size, String sortBy, String sortDirection,
                                            LicenseType licenseType, ApprovalStatus approvalStatus);

    // Admin: Duyệt hoặc Từ chối giấy phép
    BusinessLicenseDto updateApprovalStatus(UUID adminId, UUID licenseId, UpdateLicenseStatusRequest request);

    // Admin/Vendor: Xóa giấy phép
    void deleteLicense(UUID licenseId, UUID requesterId);
}