package com.atlasculinary.mappers;

import com.atlasculinary.dtos.AddBusinessLicenseRequest;
import com.atlasculinary.dtos.BusinessLicenseDto;
import com.atlasculinary.dtos.UpdateBusinessLicenseRequest;
import com.atlasculinary.entities.BusinessLicense;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BusinessLicenseMapper {

    @Mapping(source = "restaurant.restaurantId", target = "restaurantId")
    @Mapping(source = "restaurant.name", target = "restaurantName")


    @Mapping(source = "restaurant.ownerAccount.accountId", target = "ownerAccountId")
    @Mapping(source = "restaurant.ownerAccount.email", target = "ownerEmail")


    @Mapping(source = "approvedByAccount.accountId", target = "approvedByAccountId")
    @Mapping(source = "approvedByAccount.email", target = "approvedByEmail")
    BusinessLicenseDto toDto(BusinessLicense entity);



    @Mapping(target = "licenseId", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "approvedByAccount", ignore = true)
    @Mapping(target = "approvalStatus", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    BusinessLicense toEntity(AddBusinessLicenseRequest request);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "licenseId", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "approvedByAccount", ignore = true)
    @Mapping(target = "approvalStatus", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "licenseType", ignore = true)
    void updateFromRequest(UpdateBusinessLicenseRequest request, @MappingTarget BusinessLicense targetEntity);

    List<BusinessLicenseDto> toDtoList(List<BusinessLicense> entityList);
}