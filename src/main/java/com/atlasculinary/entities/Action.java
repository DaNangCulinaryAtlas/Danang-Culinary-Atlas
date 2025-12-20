package com.atlasculinary.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.UuidGenerator; // Cần import cho UUID tự sinh

import java.util.UUID; // Cần import UUID

@Entity
@Table(name = "action")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    private Long actionId;

    @Column(name = "action_name", nullable = false, length = 100)
    private String actionName;

    @Column(name = "action_code", nullable = false, unique = true, length = 50)
    private String actionCode;


    // Cờ đánh dấu: Hành động này có yêu cầu user phải Active (licensed=true) không?
    // true: Bắt buộc phải licensed mới được dùng (Mặc định)
    // false: Cho phép dùng kể cả khi chưa licensed (như nộp hồ sơ, xem profile)
    @Column(name = "requires_license", nullable = false)
    private Boolean requiresLicense = true;
}