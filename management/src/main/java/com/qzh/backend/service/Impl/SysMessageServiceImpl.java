package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.mapper.RoleMapper;
import com.qzh.backend.mapper.SysMessageMapper;
import com.qzh.backend.mapper.UserRelatedRoleMapper;
import com.qzh.backend.model.entity.Role;
import com.qzh.backend.model.entity.SysMessage;
import com.qzh.backend.model.entity.UserRelatedRole;
import com.qzh.backend.service.SysMessageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {

    // 新增依赖注入
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private UserRelatedRoleMapper userRelatedRoleMapper;

    private static final String SUPER_ADMIN_ROLE_NAME = "超级管理员";
    private static final String STORE_ADMIN_ROLE_NAME = "门店管理员";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(String messageType, String content, Long relatedId, Long productId, Long recipientId) {
        // 步骤1：查询目标角色ID
        List<Long> targetRoleIds = getTargetRoleIds();
        if (CollectionUtils.isEmpty(targetRoleIds)) {
            log.warn("未查询到超级管理员/门店管理员角色，消息发送失败");
            return null;
        }

        // 步骤2：查询管理员用户ID
        List<Long> adminUserIds = getAdminUserIdsByRoleIds(targetRoleIds);
        if (CollectionUtils.isEmpty(adminUserIds)) {
            log.warn("未查询到超级管理员/门店管理员用户，消息发送失败");
            return null;
        }

        // 步骤3：批量构建消息对象
        List<SysMessage> messageList = adminUserIds.stream().map(userId -> {
            SysMessage sysMessage = new SysMessage();
            sysMessage.setMessageType(messageType);
            sysMessage.setContent(content);
            sysMessage.setRelatedId(relatedId);
            sysMessage.setProductId(productId);
            sysMessage.setRecipientId(userId);
            sysMessage.setReadStatus(0);
            sysMessage.setCreateTime(new Date());
            sysMessage.setReadTime(null);
            return sysMessage;
        }).collect(Collectors.toList());
        // 批量保存（MyBatis-Plus批量插入，效率更高）
        boolean saveBatch = this.saveBatch(messageList);
        return saveBatch && !messageList.isEmpty() ? messageList.get(0).getId() : null;
    }

    /**
     * 私有方法：查询超级管理员、门店管理员的角色ID
     */
    private List<Long> getTargetRoleIds() {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        // 按角色名称查询（注意：角色名称需与数据库中一致）
        queryWrapper.in(Role::getRoleName, SUPER_ADMIN_ROLE_NAME, STORE_ADMIN_ROLE_NAME);
        List<Role> roles = roleMapper.selectList(queryWrapper);

        // 提取角色ID列表
        return roles.stream()
                .map(Role::getId)
                .collect(Collectors.toList());
    }

    /**
     * 私有方法：根据角色ID查询对应的用户ID
     */
    private List<Long> getAdminUserIdsByRoleIds(List<Long> roleIds) {
        LambdaQueryWrapper<UserRelatedRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UserRelatedRole::getRoleId, roleIds);
        List<UserRelatedRole> userRelatedRoles = userRelatedRoleMapper.selectList(queryWrapper);

        // 提取用户ID列表（去重，避免一个用户有多个角色时重复发送）
        return userRelatedRoles.stream()
                .map(UserRelatedRole::getUserId)
                .distinct() // 去重
                .collect(Collectors.toList());
    }

    /**
     * 标记消息为已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long messageId) {
        SysMessage sysMessage = new SysMessage();
        sysMessage.setId(messageId);
        sysMessage.setReadStatus(1); // 标记为已读
        sysMessage.setReadTime(new Date()); // 设置已读时间

        // 更新消息状态
        return this.updateById(sysMessage);
    }

    /**
     * 批量标记消息为已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchMarkAsRead(List<Long> messageIds) {
        if (CollectionUtils.isEmpty(messageIds)) {
            return 0;
        }
        // 构建更新条件
        LambdaQueryWrapper<SysMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysMessage::getId, messageIds);
        // 批量更新
        SysMessage sysMessage = new SysMessage();
        sysMessage.setReadStatus(1);
        sysMessage.setReadTime(new Date()); // 设置已读时间
        return this.update(sysMessage, queryWrapper) ? messageIds.size() : 0;
    }

    /**
     * 分页查询用户的消息列表
     */
    @Override
    public IPage<SysMessage> queryMessagePage(Page<SysMessage> page, Long recipientId, String messageType, Integer readStatus) {
        // 构建查询条件
        LambdaQueryWrapper<SysMessage> queryWrapper = new LambdaQueryWrapper<>();
        // 必选条件：接收人ID
        queryWrapper.eq(SysMessage::getRecipientId, recipientId);
        // 可选条件：消息类型
        if (messageType != null && !messageType.isEmpty()) {
            queryWrapper.eq(SysMessage::getMessageType, messageType);
        }
        // 可选条件：已读状态
        if (readStatus != null) {
            queryWrapper.eq(SysMessage::getReadStatus, readStatus);
        }
        // 按创建时间倒序（最新消息在前）
        queryWrapper.orderByDesc(SysMessage::getCreateTime);

        // 分页查询
        return this.page(page, queryWrapper);
    }

    /**
     * 查询用户的未读消息数量
     */
    @Override
    public long countUnreadMessage(Long recipientId) {
        LambdaQueryWrapper<SysMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysMessage::getRecipientId, recipientId)
                    .eq(SysMessage::getReadStatus, 0); // 未读状态

        return this.count(queryWrapper);
    }
}