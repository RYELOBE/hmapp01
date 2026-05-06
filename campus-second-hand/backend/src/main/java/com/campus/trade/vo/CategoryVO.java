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
public class CategoryVO {

    private Long id;

    private String name;

    private String icon;

    private Integer sortOrder;

    private Integer productCount;

    private Integer status;

    private LocalDateTime createdAt;
}
