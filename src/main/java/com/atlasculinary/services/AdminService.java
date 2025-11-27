package com.atlasculinary.services;

import com.atlasculinary.dtos.AdminDto;
import com.atlasculinary.dtos.AdminOverviewDto;
import com.atlasculinary.dtos.RestaurantCountByTagDto;
import com.atlasculinary.dtos.RestaurantLocationDto;
import com.atlasculinary.dtos.ReviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {
    List<AdminDto> getAllAdmins();
    AdminOverviewDto getAdminOverview();
    List<RestaurantCountByTagDto> getRestaurantCountByTag();
    List<RestaurantLocationDto> searchRestaurantsByLocation(Integer wardId, Integer districtId, Integer provinceId);
    Page<ReviewDto> getAllReviews(Pageable pageable);
}
