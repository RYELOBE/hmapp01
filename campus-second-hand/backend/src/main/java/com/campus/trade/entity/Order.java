package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long buyerId;

    private Long sellerId;

    private Long productId;

    private BigDecimal totalAmount;

    private Integer status;

    private String address;

    private String receiverName;

    private String receiverPhone;

    private LocalDateTime payTime;

    private LocalDateTime deliveryTime;

    private LocalDateTime confirmTime;

    private LocalDateTime createdAt;
}
