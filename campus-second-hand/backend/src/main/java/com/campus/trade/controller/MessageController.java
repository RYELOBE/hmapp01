package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.entity.Message;
import com.campus.trade.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
@Tag(name = "消息管理", description = "消息列表、未读数、已读等接口")
public class MessageController {

    @GetMapping("/list")
    @Operation(summary = "消息列表", description = "获取当前用户的消息列表")
    public Result<PageResult<Message>> getMessageList(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "未读消息数", description = "获取当前用户的未读消息数量")
    public Result<Integer> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        return Result.success(0);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "将指定消息标记为已读")
    public Result<Void> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return Result.success();
    }
}
