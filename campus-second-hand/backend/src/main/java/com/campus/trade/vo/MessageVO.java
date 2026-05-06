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
public class MessageVO {

    private Long id;

    private Long fromUserId;

    private String fromUserName;

    private String fromUserAvatar;

    private Long toUserId;

    private String toUserName;

    private String toUserAvatar;

    private String content;

    private Integer type;

    private String typeName;

    private Integer isRead;

    private LocalDateTime createdAt;
}
