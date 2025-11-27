package com.atlasculinary.services.impl;

import com.atlasculinary.dtos.AdminDto;
import com.atlasculinary.dtos.AdminOverviewDto;
import com.atlasculinary.dtos.RestaurantCountByTagDto;
import com.atlasculinary.dtos.RestaurantLocationDto;
import com.atlasculinary.dtos.ReviewDto;
import com.atlasculinary.enums.ApprovalStatus;
import com.atlasculinary.mappers.AdminMapper;
import com.atlasculinary.mappers.ReviewMapper;
import com.atlasculinary.repositories.AccountRepository;
import com.atlasculinary.repositories.AdminRepository;
import com.atlasculinary.repositories.RestaurantRepository;
import com.atlasculinary.repositories.ReviewRepository;
import com.atlasculinary.services.AdminService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;
    private final AccountRepository accountRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    
    public AdminServiceImpl(AdminRepository adminRepository,
                            AdminMapper adminMapper,
                            AccountRepository accountRepository,
                            RestaurantRepository restaurantRepository,
                            ReviewRepository reviewRepository,
                            ReviewMapper reviewMapper) {
        this.adminRepository = adminRepository;
        this.adminMapper = adminMapper;
        this.accountRepository = accountRepository;
        this.restaurantRepository = restaurantRepository;
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
    }
    
    @Override
    @Transactional
    public List<AdminDto> getAllAdmins() {
        var adminList = adminRepository.findAll();
        return adminMapper.toDtoList(adminList);
    }
    
    @Override
    @Transactional
    public AdminOverviewDto getAdminOverview() {
        Long totalUserAccounts = accountRepository.countByRoleName("USER");
        Long totalVendorAccounts = accountRepository.countByRoleName("VENDOR");
        Long totalApprovedRestaurants = restaurantRepository.countByApprovalStatus(ApprovalStatus.APPROVED);
        Long totalPendingRestaurants = restaurantRepository.countByApprovalStatus(ApprovalStatus.PENDING);
        
        return new AdminOverviewDto(
            totalUserAccounts,
            totalVendorAccounts,
            totalApprovedRestaurants,
            totalPendingRestaurants
        );
    }
    
    @Override
    @Transactional
    public List<RestaurantCountByTagDto> getRestaurantCountByTag() {
        return restaurantRepository.countRestaurantsByTag(ApprovalStatus.APPROVED);
    }
    
    @Override
    @Transactional
    public List<RestaurantLocationDto> searchRestaurantsByLocation(Integer wardId, Integer districtId, Integer provinceId) {
        List<Object[]> results;
        
        if (wardId != null) {
            results = restaurantRepository.findRestaurantsByWardId(wardId);
        } else if (districtId != null) {
            results = restaurantRepository.findRestaurantsByDistrictId(districtId);
        } else if (provinceId != null) {
            results = restaurantRepository.findRestaurantsByProvinceId(provinceId);
        } else {
            return List.of();
        }
        
        return results.stream()
                .map(row -> new RestaurantLocationDto(
                    (UUID) row[0],    // restaurantId
                    (String) row[1],   // name
                    (String) row[2]    // address
                ))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public Page<ReviewDto> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable).map(reviewMapper::toDto);
    }
    
}
