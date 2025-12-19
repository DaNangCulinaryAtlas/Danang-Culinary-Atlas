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

    // 1. Lấy danh sách giấy phép của một Nhà hàng cụ thể
    List<BusinessLicense> findAllByRestaurant_RestaurantId(UUID restaurantId);

    // 2. Lấy danh sách TẤT CẢ giấy phép của một Account (thông qua các nhà hàng họ sở hữu)
    List<BusinessLicense> findAllByRestaurant_OwnerAccount_AccountId(UUID ownerAccountId);

    // 3. Kiểm tra trùng số giấy phép (toàn hệ thống) - Giữ nguyên
    boolean existsByLicenseNumber(String licenseNumber);

    // 4. Kiểm tra xem NHÀ HÀNG đó đã có loại giấy này chưa
    boolean existsByRestaurant_RestaurantIdAndLicenseType(UUID restaurantId, LicenseType licenseType);


    @Query("SELECT b FROM BusinessLicense b WHERE " +
            "(:licenseType IS NULL OR b.licenseType = :licenseType) AND " +
            "(:approvalStatus IS NULL OR b.approvalStatus = :approvalStatus)")
    Page<BusinessLicense> findAllByFilters(
            @Param("licenseType") LicenseType licenseType,
            @Param("approvalStatus") ApprovalStatus approvalStatus,
            Pageable pageable);
}