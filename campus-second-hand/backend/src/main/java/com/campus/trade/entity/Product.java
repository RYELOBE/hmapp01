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
@TableName("tb_product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Long categoryId;

    private Long sellerId;

    private String images;

    private String condition;

    private Integer status;

    private Integer viewCount;

    private Integer favoriteCount;

    private String rejectReason;

    private String deliveryType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
