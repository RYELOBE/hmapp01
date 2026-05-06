package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.dto.PageDTO;
import com.campus.trade.entity.Message;
import com.campus.trade.vo.MessageVO;
import com.campus.trade.vo.Result;

import java.util.List;

public interface MessageService extends BaseService<Message> {
    Result<IPage<MessageVO>> getUserMessages(Long userId, Integer type);
    Result<Boolean> sendMessage(Long fromUserId, Long toUserId, String content);
    Result<Boolean> markAsRead(Long messageId, Long userId);
    Result<Integer> getUnreadCount(Long userId);
}
