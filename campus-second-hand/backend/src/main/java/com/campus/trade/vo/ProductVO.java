package com.campus.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVO {

    private Long id;

    private String title;

    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Long categoryId;

    private String categoryName;

    private Long sellerId;

    private String sellerName;

    private String sellerAvatar;

    private String sellerSchool;

    private Integer sellerCreditScore;

    private List<String> images;

    private String condition;

    private Integer status;

    private Integer viewCount;

    private Integer favoriteCount;

    private String deliveryType;

    private Boolean isFavorite;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
