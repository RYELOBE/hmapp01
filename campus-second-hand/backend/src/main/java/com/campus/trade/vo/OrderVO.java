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
public class OrderVO {

    private Long id;

    private String orderNo;

    private Long buyerId;

    private String buyerName;

    private String buyerAvatar;

    private Long sellerId;

    private String sellerName;

    private String sellerAvatar;

    private Long productId;

    private String productTitle;

    private String productImage;

    private BigDecimal totalAmount;

    private Integer status;

    private String statusName;

    private String address;

    private String receiverName;

    private String receiverPhone;

    private LocalDateTime payTime;

    private LocalDateTime deliveryTime;

    private LocalDateTime confirmTime;

    private LocalDateTime createdAt;
}
