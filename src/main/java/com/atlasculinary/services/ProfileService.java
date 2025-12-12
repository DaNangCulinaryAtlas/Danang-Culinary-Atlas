package com.atlasculinary.services;

import com.atlasculinary.dtos.AdminDto;
import com.atlasculinary.dtos.UserDto;
import com.atlasculinary.dtos.VendorDto;
import com.atlasculinary.dtos.VendorOverviewDto;
import com.atlasculinary.dtos.profile.*;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    
  UserDto getUserProfile(String email);
  UserDto updateUserProfile(String email, UserProfileUpdateDto updateDto);
    
  AdminDto getAdminProfile(String email);
  AdminDto updateAdminProfile(String email, AdminProfileUpdateDto updateDto);
  
  VendorDto getVendorProfile(String email);
  VendorDto updateVendorProfile(String email, VendorProfileUpdateDto updateDto);
  
  VendorOverviewDto getVendorOverview(String email);
}