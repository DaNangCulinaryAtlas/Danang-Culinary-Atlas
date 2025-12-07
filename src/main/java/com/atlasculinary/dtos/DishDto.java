package com.atlasculinary.dtos;

import com.atlasculinary.enums.ApprovalStatus;
import com.atlasculinary.enums.DishStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DishDto {

    private UUID dishId;
    private UUID restaurantId;
    private String name;
    private String[] images;
    private String description;
    private BigDecimal price;
    private DishStatus status = DishStatus.AVAILABLE;
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
    private LocalDateTime approvedAt;
    private String rejectionReason;
}