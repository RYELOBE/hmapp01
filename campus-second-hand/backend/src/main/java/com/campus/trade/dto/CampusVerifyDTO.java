package com.campus.trade.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampusVerifyDTO {

    @NotBlank(message = "学校名称不能为空")
    private String school;

    @NotBlank(message = "学号不能为空")
    private String studentId;
}
