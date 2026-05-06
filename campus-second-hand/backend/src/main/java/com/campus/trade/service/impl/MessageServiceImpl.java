package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.dto.PageDTO;
import com.campus.trade.entity.Message;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.MessageMapper;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.service.MessageService;
import com.campus.trade.vo.MessageVO;
import com.campus.trade.vo.Result;
import com.campus.trade.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final UserMapper userMapper;

    @Override
    public Page<Message> getPage(PageDTO dto) {
        Page<Message> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Message::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Message getById(Long id) {
        return this.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(Message entity) {
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return this.save(entity);
    }

    @Override
    public boolean updateById(Message entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return this.updateById(entity);
    }

    @Override
    public boolean deleteById(Long id) {
        return this.removeById(id);
    }

    @Override
    public Result<Page<MessageVO>> getUserMessages(Long userId, Integer type) {
        Page<Message> page = new Page<>(1, 20);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Message::getToUserId, userId).or().eq(Message::getFromUserId, userId));
        
        if (type != null) {
            wrapper.eq(Message::getType, type);
        }
        
        wrapper.orderByDesc(Message::getCreateTime);
        Page<Message> result = this.page(page, wrapper);
        Page<MessageVO> voPage = convertToVOPage(result);
        return Result.success(voPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> sendMessage(Long fromUserId, Long toUserId, String content) {
        User fromUser = userMapper.selectById(fromUserId);
        if (fromUser == null) {
            return Result.error("发送用户不存在");
        }
        
        User toUser = userMapper.selectById(toUserId);
        if (toUser == null) {
            return Result.error("接收用户不存在");
        }

        Message message = new Message();
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setType(0);
        message.setIsRead(false);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());

        return Result.success(this.save(message));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> markAsRead(Long messageId, Long userId) {
        Message message = this.getById(messageId);
        if (message == null) {
            return Result.error("消息不存在");
        }
        if (!message.getToUserId().equals(userId)) {
            return Result.error("无权限操作此消息");
        }

        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Message::getId, messageId)
                .set(Message::getIsRead, true)
                .set(Message::getUpdateTime, LocalDateTime.now());

        return Result.success(this.update(wrapper));
    }

    @Override
    public Result<Integer> getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getToUserId, userId)
                .eq(Message::getIsRead, false);
        long count = this.count(wrapper);
        return Result.success((int) count);
    }

    private MessageVO convertToVO(Message message) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setFromUserId(message.getFromUserId());
        vo.setToUserId(message.getToUserId());
        vo.setContent(message.getContent());
        vo.setType(message.getType());
        vo.setIsRead(message.getIsRead());
        vo.setCreateTime(message.getCreateTime());
        vo.setUpdateTime(message.getUpdateTime());

        User fromUser = userMapper.selectById(message.getFromUserId());
        if (fromUser != null) {
            UserVO fromUserVO = new UserVO();
            fromUserVO.setId(fromUser.getId());
            fromUserVO.setUsername(fromUser.getUsername());
            fromUserVO.setNickname(fromUser.getNickname());
            fromUserVO.setAvatar(fromUser.getAvatar());
            vo.setFromUser(fromUserVO);
        }

        User toUser = userMapper.selectById(message.getToUserId());
        if (toUser != null) {
            UserVO toUserVO = new UserVO();
            toUserVO.setId(toUser.getId());
            toUserVO.setUsername(toUser.getUsername());
            toUserVO.setNickname(toUser.getNickname());
            toUserVO.setAvatar(toUser.getAvatar());
            vo.setToUser(toUserVO);
        }

        return vo;
    }

    private Page<MessageVO> convertToVOPage(Page<Message> page) {
        Page<MessageVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<MessageVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }
}
