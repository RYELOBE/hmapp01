package com.campus.trade.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPublishDTO {

    @NotBlank(message = "商品标题不能为空")
    private String title;

    @NotBlank(message = "商品描述不能为空")
    private String description;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "原价必须大于0")
    private BigDecimal originalPrice;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotBlank(message = "商品成色不能为空")
    private String condition;

    private List<String> images;

    @NotBlank(message = "配送方式不能为空")
    private String deliveryType;
}
