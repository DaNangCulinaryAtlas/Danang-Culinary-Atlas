package com.atlasculinary.services.impl;

import com.atlasculinary.dtos.AdminDto;
import com.atlasculinary.dtos.AdminOverviewDto;
import com.atlasculinary.dtos.RestaurantCountByTagDto;
import com.atlasculinary.enums.ApprovalStatus;
import com.atlasculinary.mappers.AdminMapper;
import com.atlasculinary.repositories.AccountRepository;
import com.atlasculinary.repositories.AdminRepository;
import com.atlasculinary.repositories.RestaurantRepository;
import com.atlasculinary.services.AdminService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;
    private final AccountRepository accountRepository;
    private final RestaurantRepository restaurantRepository;
    
    public AdminServiceImpl(AdminRepository adminRepository,
                            AdminMapper adminMapper,
                            AccountRepository accountRepository,
                            RestaurantRepository restaurantRepository) {
        this.adminRepository = adminRepository;
        this.adminMapper = adminMapper;
        this.accountRepository = accountRepository;
        this.restaurantRepository = restaurantRepository;
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
}
