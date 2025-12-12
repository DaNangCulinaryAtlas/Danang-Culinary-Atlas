package com.atlasculinary.services.impl;

import com.atlasculinary.dtos.AdminDto;
import com.atlasculinary.dtos.UserDto;
import com.atlasculinary.dtos.VendorDto;
import com.atlasculinary.dtos.VendorOverviewDto;
import com.atlasculinary.dtos.profile.*;
import com.atlasculinary.entities.Account;
import com.atlasculinary.entities.UserProfile;
import com.atlasculinary.entities.AdminProfile;
import com.atlasculinary.entities.VendorProfile;
import com.atlasculinary.mappers.AdminMapper;
import com.atlasculinary.mappers.UserMapper;
import com.atlasculinary.mappers.VendorMapper;
import com.atlasculinary.repositories.AccountRepository;
import com.atlasculinary.repositories.AdminRepository;
import com.atlasculinary.repositories.UserRepository;
import com.atlasculinary.repositories.VendorRepository;
import com.atlasculinary.repositories.RestaurantRepository;
import com.atlasculinary.repositories.DishRepository;
import com.atlasculinary.repositories.ReviewRepository;
import com.atlasculinary.services.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileServiceImpl implements ProfileService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final VendorRepository vendorRepository;
    private final RestaurantRepository restaurantRepository;
    private final DishRepository dishRepository;
    private final ReviewRepository reviewRepository;
    private final UserMapper userMapper;
    private final VendorMapper vendorMapper;
    private final AdminMapper adminMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserProfile(String email) {
        UserProfile userProfile = userRepository.findByAccountEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));

        return userMapper.toDto(userProfile);
    }

    @Override
    public UserDto updateUserProfile(String email, UserProfileUpdateDto updateDto) {
        UserProfile userProfile = userRepository.findByAccountEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));

        userMapper.updateEntityFromRequest(updateDto, userProfile);
        UserProfile savedProfile = userRepository.save(userProfile);

        return userMapper.toDto(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDto getAdminProfile(String email) {
        AdminProfile adminProfile = adminRepository.findByAccountEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin quản trị viên"));

        return adminMapper.toDto(adminProfile);
    }

    @Override
    public AdminDto updateAdminProfile(String email, AdminProfileUpdateDto updateDto) {
        AdminProfile adminProfile = adminRepository.findByAccountEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin quản trị viên"));

        adminMapper.updateEntityFromRequest(updateDto, adminProfile);
        AdminProfile savedProfile = adminRepository.save(adminProfile);

        return adminMapper.toDto(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public VendorDto getVendorProfile(String email) {
        VendorProfile vendorProfile = vendorRepository.findByAccountEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhà cung cấp"));

        return vendorMapper.toDto(vendorProfile);
    }

    @Override
    public VendorDto updateVendorProfile(String email, VendorProfileUpdateDto updateDto) {
        VendorProfile vendorProfile = vendorRepository.findByAccountEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhà cung cấp"));

        vendorMapper.updateEntityFromRequest(updateDto, vendorProfile);
        VendorProfile savedProfile = vendorRepository.save(vendorProfile);

        return vendorMapper.toDto(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public VendorOverviewDto getVendorOverview(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        
        UUID vendorAccountId = account.getAccountId();
        
        Long totalRestaurants = restaurantRepository.countByOwnerAccount(account);
        Long totalDishes = dishRepository.countByRestaurant_OwnerAccount_AccountId(vendorAccountId);
        Long totalReviews = reviewRepository.countByVendorAccountId(vendorAccountId);
        
        return new VendorOverviewDto(totalRestaurants, totalDishes, totalReviews);
    }
}