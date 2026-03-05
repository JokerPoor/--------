CREATE TABLE `sys_message` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `messageType` varchar(20) NOT NULL COMMENT '消息类型(0-自动调拨,1-自动购买)',
                               `content` text NOT NULL COMMENT '消息内容',
                               `relatedId` bigint DEFAULT NULL COMMENT '关联业务ID(调拨单ID/采购单ID)',
                               `productId` bigint DEFAULT NULL COMMENT '关联商品ID',
                               `recipientId` bigint NOT NULL COMMENT '消息接收人ID',
                               `readStatus` tinyint NOT NULL DEFAULT 0 COMMENT '已读状态(0-未读,1-已读)',
                               `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `readTime` datetime DEFAULT NULL COMMENT '已读时间',
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统定时任务消息通知表';