package com.atlasculinary.repositories;

import com.atlasculinary.entities.BusinessLicense;
import com.atlasculinary.enums.ApprovalStatus;
import com.atlasculinary.enums.LicenseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BusinessLicenseRepository extends JpaRepository<BusinessLicense, UUID> {

    // Lấy danh sách tất cả giấy phép của một Account (trả về List)
    List<BusinessLicense> findAllByOwnerAccount_AccountId(UUID ownerAccountId);

    // Kiểm tra trùng số giấy phép (toàn hệ thống)
    boolean existsByLicenseNumber(String licenseNumber);

    // Kiểm tra xem Account đã có loại giấy này chưa
    // (Ví dụ: Đã có Giấy ĐKKD rồi thì không cho tạo thêm cái ĐKKD thứ 2, nhưng được tạo ATTP)
    boolean existsByOwnerAccount_AccountIdAndLicenseType(UUID ownerAccountId, LicenseType licenseType);

    @Query("SELECT b FROM BusinessLicense b WHERE " +
            "(:licenseType IS NULL OR b.licenseType = :licenseType) AND " +
            "(:approvalStatus IS NULL OR b.approvalStatus = :approvalStatus)")
    Page<BusinessLicense> findAllByFilters(
            @Param("licenseType") LicenseType licenseType,
            @Param("approvalStatus") ApprovalStatus approvalStatus,
            Pageable pageable);
}