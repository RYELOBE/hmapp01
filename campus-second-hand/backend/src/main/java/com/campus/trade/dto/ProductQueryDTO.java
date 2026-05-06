package com.campus.trade.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductQueryDTO {

    private Long categoryId;

    private String keyword;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private String condition;

    private String sortBy;

    private String sortOrder;

    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量必须大于0")
    private Integer size = 10;
}
