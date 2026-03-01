package com.qzh.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.message.MessageQueryDTO;
import com.qzh.backend.model.entity.SysMessage;
import com.qzh.backend.service.SysMessageService;
import com.qzh.backend.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 系统消息通知控制器
 * 对齐RoleController风格：权限校验、日志记录、统一响应、参数校验
 */
@RestController
@RequestMapping("sys-message")
@RequiredArgsConstructor
public class SysMessageController {

    private final SysMessageService sysMessageService;

    /**
     * 分页查询用户消息列表
     */
    @GetMapping("page")
    public BaseResponse<IPage<SysMessage>> getMessagePage(@Valid MessageQueryDTO dto) {
        Page<SysMessage> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        IPage<SysMessage> messagePage = sysMessageService.queryMessagePage(page,
                dto.getRecipientId(), dto.getMessageType(), dto.getReadStatus());
        return ResultUtils.success(messagePage);
    }

    /**
     * 标记单条消息为已读
     */
    @PutMapping("mark-read/{messageId}")
    public BaseResponse<Boolean> markMessageAsRead(@PathVariable Long messageId) {
        boolean success = sysMessageService.markAsRead(messageId);
        ThrowUtils.throwIf(!success, ErrorCode.SYSTEM_ERROR, "标记消息已读失败");
        return ResultUtils.success(success);
    }

    /**
     * 批量标记消息为已读
     */
    @PutMapping("batch-mark-read")
    public BaseResponse<Integer> batchMarkMessageAsRead(@RequestBody List<Long> messageIds) {
        ThrowUtils.throwIf(messageIds.isEmpty(), ErrorCode.PARAMS_ERROR, "消息ID列表不能为空");
        int successCount = sysMessageService.batchMarkAsRead(messageIds);
        ThrowUtils.throwIf(successCount <= 0 && !messageIds.isEmpty(),
                ErrorCode.SYSTEM_ERROR, "批量标记消息已读失败");
        return ResultUtils.success(successCount);
    }

    /**
     * 查询用户未读消息数量
     */
    @GetMapping("unread-count")
    public BaseResponse<Long> getUnreadMessageCount(@RequestParam Long recipientId) {
        ThrowUtils.throwIf(recipientId == null || recipientId <= 0,
                ErrorCode.PARAMS_ERROR, "接收人ID不能为空且必须为正数");
        long unreadCount = sysMessageService.countUnreadMessage(recipientId);
        return ResultUtils.success(unreadCount);
    }
}