package com.campus.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    private Long id;

    private String username;

    private String phone;

    private String email;

    private String avatar;

    private String role;

    private Integer status;

    private String school;

    private String studentId;

    private Integer isVerified;

    private Integer creditScore;

    private LocalDateTime createdAt;
}
