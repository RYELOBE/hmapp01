package com.campus.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartVO {

    private Long id;

    private Long userId;

    private Long productId;

    private String productTitle;

    private String productImage;

    private BigDecimal productPrice;

    private String productCondition;

    private Integer quantity;

    private LocalDateTime createdAt;
}
