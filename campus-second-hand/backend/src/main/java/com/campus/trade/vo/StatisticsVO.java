package com.campus.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsVO {

    private Long totalUsers;

    private Long totalProducts;

    private Long totalOrders;

    private Long todayOrders;

    private Long pendingProducts;

    private Long verifiedUsers;

    private BigDecimal totalGMV;

    private BigDecimal todayGMV;
}
