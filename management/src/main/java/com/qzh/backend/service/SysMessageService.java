package com.qzh.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qzh.backend.model.entity.SysMessage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统消息通知 Service 接口
 *
 * @author 你的名字
 * @date 2026-03-01
 */
public interface SysMessageService extends IService<SysMessage> {

    /**
     * 发送消息（新增消息记录）
     *
     * @param messageType  消息类型(0-自动调拨,1-自动购买)
     * @param content      消息内容
     * @param relatedId    关联业务ID(调拨单ID/采购单ID)
     * @param productId    关联商品ID
     * @param recipientId  消息接收人ID
     * @return 新增的消息ID
     */
    Long sendMessage(String messageType, String content, Long relatedId, Long productId, Long recipientId);

    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     * @return 是否标记成功
     */
    boolean markAsRead(Long messageId);

    /**
     * 批量标记消息为已读
     *
     * @param messageIds 消息ID列表
     * @return 成功标记的数量
     */
    int batchMarkAsRead(List<Long> messageIds);

    /**
     * 分页查询用户的消息列表
     *
     * @param page         分页对象
     * @param recipientId  接收人ID
     * @param messageType  消息类型（可选，传null则查询所有类型）
     * @param readStatus   已读状态（0-未读,1-已读，可选，传null则查询所有状态）
     * @return 分页消息列表
     */
    IPage<SysMessage> queryMessagePage(Page<SysMessage> page, Long recipientId, String messageType, Integer readStatus);

    /**
     * 查询用户的未读消息数量
     *
     * @param recipientId 接收人ID
     * @return 未读消息数量
     */
    long countUnreadMessage(Long recipientId);
}