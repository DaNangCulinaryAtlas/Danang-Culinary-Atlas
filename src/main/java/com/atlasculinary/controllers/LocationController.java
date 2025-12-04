package com.atlasculinary.controllers;

import com.atlasculinary.dtos.ProvinceDto;
import com.atlasculinary.dtos.DistrictDto;
import com.atlasculinary.dtos.WardDto;
import com.atlasculinary.services.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/locations")
@Tag(name = "Location Management", description = "API for Vietnamese administrative divisions: provinces, districts, and wards")
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "Get all provinces", description = "Retrieve the list of all provinces/cities in Viet Nam")
    @GetMapping("/provinces")
    public ResponseEntity<List<ProvinceDto>> getAllProvinces() {
        List<ProvinceDto> provinces = locationService.getAllProvinces();
        return ResponseEntity.ok(provinces);
    }

    @Operation(summary = "Get a province by ID", description = "Retrieve detailed information for a specific province/city")
    @GetMapping("/provinces/{provinceId}")
    public ResponseEntity<ProvinceDto> getProvinceById(@PathVariable int provinceId) {
        ProvinceDto province = locationService.getProvinceById(provinceId);
        return ResponseEntity.ok(province);
    }

    // --- 2. District (Quận/Huyện) ---

    @Operation(summary = "Get districts by province", description = "Retrieve all districts that belong to a specific province")
    @GetMapping("/provinces/{provinceId}/districts")
    public ResponseEntity<List<DistrictDto>> getDistrictsByProvince(@PathVariable int provinceId) {
        List<DistrictDto> districts = locationService.getDistrictsByProvince(provinceId);
        return ResponseEntity.ok(districts);
    }

    @Operation(summary = "Get a district by ID", description = "Retrieve detailed information for a specific district")
    @GetMapping("/districts/{districtId}")
    public ResponseEntity<DistrictDto> getDistrictById(@PathVariable int districtId) {
        DistrictDto district = locationService.getDistrictById(districtId);
        return ResponseEntity.ok(district);
    }

    // --- 3. Ward (Phường/Xã) ---

    @Operation(summary = "Get wards by district", description = "Retrieve all wards that belong to a specific district")
    @GetMapping("/districts/{districtId}/wards")
    public ResponseEntity<List<WardDto>> getWardsByDistrict(@PathVariable int districtId) {
        List<WardDto> wards = locationService.getWardsByDistrict(districtId);
        return ResponseEntity.ok(wards);
    }

    @Operation(summary = "Get a ward by ID", description = "Retrieve detailed information for a specific ward")
    @GetMapping("/wards/{wardId}")
    public ResponseEntity<WardDto> getWardById(@PathVariable int wardId) {
        WardDto ward = locationService.getWardById(wardId);
        return ResponseEntity.ok(ward);
    }
}