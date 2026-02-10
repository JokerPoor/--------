CREATE DATABASE IF NOT EXISTS `managerment` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `managerment`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `sys_operation_log`;
DROP TABLE IF EXISTS `sys_inventory_detail`;
DROP TABLE IF EXISTS `sys_inventory`;
DROP TABLE IF EXISTS `sys_amount_order`;
DROP TABLE IF EXISTS `sys_purchase_return`;
DROP TABLE IF EXISTS `sys_purchase_order`;
DROP TABLE IF EXISTS `sys_product`;
DROP TABLE IF EXISTS `sys_store`;
DROP TABLE IF EXISTS `sys_page_permission`;
DROP TABLE IF EXISTS `sys_role_page`;
DROP TABLE IF EXISTS `sys_page`;
DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `sys_role_permission`;
DROP TABLE IF EXISTS `sys_permission`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_user`;
DROP TABLE IF EXISTS `sys_warehouse`;
DROP TABLE IF EXISTS `sys_transfer_log`;
DROP TABLE IF EXISTS `sys_sale_order`;
DROP TABLE IF EXISTS `sys_sale_return`;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                            `userAccount` varchar(50) NOT NULL COMMENT '登录账号',
                            `userPassword` varchar(100) NOT NULL COMMENT '登录密码',
                            `userName` varchar(50) NOT NULL COMMENT '真实姓名',
                            `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
                            `email` varchar(100) NOT NULL COMMENT '用户邮箱',
                            `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0-禁用,1-正常)',
                            `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `userAccount` (`userAccount`)
) ENGINE=InnoDB COMMENT='用户表';

CREATE TABLE `sys_role` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
                            `roleName` varchar(50) NOT NULL COMMENT '角色名称',
                            `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
                            `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `roleName` (`roleName`)
) ENGINE=InnoDB COMMENT='角色表';

CREATE TABLE `sys_permission` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
                                  `name` varchar(100) NOT NULL COMMENT '权限名称',
                                  `description` varchar(200) DEFAULT NULL COMMENT '权限描述',
                                  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `roleName` (`name`)
) ENGINE=InnoDB COMMENT='权限资源表';

CREATE TABLE `sys_role_permission` (
                                       `roleId` bigint NOT NULL COMMENT '角色ID',
                                       `permissionId` bigint NOT NULL COMMENT '权限ID',
                                       `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                                       UNIQUE KEY `uk_roleid_permissionid` (`roleId`,`permissionId`)
) ENGINE=InnoDB COMMENT='角色权限关联表';

CREATE TABLE `sys_user_role` (
                                 `userId` bigint NOT NULL COMMENT '用户ID',
                                 `roleId` bigint NOT NULL COMMENT '角色ID',
                                 `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（分配角色时间）',
                                 `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                                 UNIQUE KEY `uk_userid_roleid` (`userId`, `roleId`)
) ENGINE=InnoDB COMMENT='用户角色关联表';

CREATE TABLE `sys_page` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '页面ID',
                            `parentId` BIGINT DEFAULT NULL COMMENT '父页面ID，NULL 或 0 表示顶级',
                            `name` VARCHAR(100) NOT NULL COMMENT '页面/菜单显示名称',
                            `path` VARCHAR(200) DEFAULT NULL COMMENT '前端路由路径，如 /users',
                            `component` VARCHAR(255) DEFAULT NULL COMMENT '前端组件路径/名称（前端用于动态路由）',
                            `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
                            `orderNum` INT NOT NULL DEFAULT 0 COMMENT '排序值，越大越靠前',
                            `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见（菜单展示）:1=显示,0=隐藏',
                            `meta` JSON DEFAULT NULL COMMENT '扩展字段，存放额外信息（如权限提示、layout 等）',
                            `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            `createBy` BIGINT DEFAULT NULL COMMENT '创建人ID',
                            PRIMARY KEY (`id`),
                            KEY `idx_parent` (`parentId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='页面/菜单表';

CREATE TABLE `sys_role_page` (
                                 `roleId` BIGINT NOT NULL COMMENT '角色ID',
                                 `pageId` BIGINT NOT NULL COMMENT '页面ID',
                                 `grantType` TINYINT NOT NULL DEFAULT 1 COMMENT '授权类型：1=访问/显示权限,2=完全控制/管理（可自定义）',
                                 `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 `createBy` BIGINT DEFAULT NULL COMMENT '创建人ID',
                                 UNIQUE KEY `uk_roleid_pageid` (`roleId`,`pageId`),
                                 KEY `idx_role` (`roleId`),
                                 KEY `idx_page` (`pageId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-页面关联表';

CREATE TABLE `sys_page_permission` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT,
                                       `pageId` BIGINT NOT NULL COMMENT '所属页面ID',
                                       `permissionId` BIGINT NOT NULL COMMENT 'sys_permission.id',
                                       `action` VARCHAR(100) DEFAULT NULL COMMENT '动作标识，如 create/update/delete/export',
                                       `description` VARCHAR(200) DEFAULT NULL,
                                       `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       `createBy` BIGINT DEFAULT NULL COMMENT '创建人ID',
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uk_page_perm` (`pageId`,`permissionId`),
                                       KEY `idx_page` (`pageId`),
                                       KEY `idx_permission` (`permissionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='页面按钮/动作与权限表';

CREATE TABLE `sys_store` (
                             `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '门店ID（主键）',
                             `storeName` VARCHAR(64) NOT NULL COMMENT '门店名称（如"XX市XX区旗舰店"）',
                             `managerId` BIGINT NOT NULL COMMENT '店长ID（关联sys_user.id，对应"门店店长"角色）',
                             `contactName` VARCHAR(32) NOT NULL COMMENT '联系人（门店负责人）',
                             `contactPhone` VARCHAR(20) NOT NULL COMMENT '联系电话',
                             `contactEmail` VARCHAR(64) DEFAULT '' COMMENT '联系邮箱',
                             `address` VARCHAR(255) NOT NULL COMMENT '详细地址',
                             `longitude` DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
                             `latitude` DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
                             `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统门店表';

CREATE TABLE `sys_product` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
                           `name` VARCHAR(128) NOT NULL COMMENT '商品名称',
                           `description` varchar(200) DEFAULT NULL COMMENT '商品描述',
                           `url` varchar(512) DEFAULT NULL COMMENT '商品图片',
                           `supplierId` BIGINT NOT NULL COMMENT '供应商账号ID',
                           `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
                           `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0-下架`，1-上架）',
                           `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE `sys_purchase_order` (
                                      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '采购订单ID',
                                      `storeId` BIGINT NOT NULL COMMENT '门店ID',
                                      `supplierId` BIGINT NOT NULL COMMENT '供应商ID',
                                      `productId` BIGINT NOT NULL COMMENT '商品ID',
                                      `productName` VARCHAR(128) NOT NULL COMMENT '商品名称',
                                      `productUrl` varchar(512) DEFAULT NULL COMMENT '商品图片',
                                      `productDescription` varchar(200) DEFAULT NULL COMMENT '商品描述',
                                      `productPrice` DECIMAL(10,2) NOT NULL COMMENT '采购单价',
                                      `productQuantity` INT NOT NULL COMMENT '采购数量',
                                      `totalAmount` DECIMAL(12,2) NOT NULL COMMENT '明细总金额',
                                      `status` TINYINT NOT NULL COMMENT '状态（0-待发货，1-已发货，2-已入库）',
                                      `type` TINYINT NOT NULL DEFAULT 0 COMMENT '类型（0-手动发起，1-阈值触发）',
                                      `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

CREATE TABLE `sys_purchase_return` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '退货单ID',
                                       `productId` BIGINT NOT NULL COMMENT '商品ID',
                                       `productName` VARCHAR(128) NOT NULL COMMENT '商品名称',
                                       `productUrl` varchar(512) DEFAULT NULL COMMENT '商品图片',
                                       `productDescription` varchar(200) DEFAULT NULL COMMENT '商品描述',
                                       `productPrice` DECIMAL(10,2) NOT NULL COMMENT '退货单价',
                                       `productQuantity` INT NOT NULL COMMENT '退货数量',
                                       `storeId` BIGINT NOT NULL COMMENT '门店ID',
                                       `warehouseId` BIGINT NOT NULL COMMENT '仓库ID',
                                       `supplierId` BIGINT NOT NULL COMMENT '供应商ID',
                                       `totalAmount` DECIMAL(12,2) NOT NULL COMMENT '退货总金额',
                                       `status` TINYINT NOT NULL COMMENT '状态（0-未完成，1-已完成）',
                                       `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购退货表';

CREATE TABLE `sys_amount_order` (
                                    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '金额订单ID',
                                    `orderId` VARCHAR(64) NOT NULL COMMENT '订单编号',
                                    `type` TINYINT NOT NULL COMMENT '类型（0-采购，1-采退，2-销售，3-销退）',
                                    `storeId` BIGINT NOT NULL COMMENT '门店ID',
                                    `payerId` BIGINT NOT NULL COMMENT '付款人ID（门店ID）',
                                    `payeeId` BIGINT NOT NULL COMMENT '收款人ID（供应商ID）',
                                    `amount` DECIMAL(12,2) NOT NULL COMMENT '金额',
                                    `status` TINYINT NOT NULL COMMENT '状态（0-待支付，1-已支付，2-已取消）',
                                    `payType` VARCHAR(32) DEFAULT '' COMMENT '支付方式（alipay-支付宝）',
                                    `tradeNo` VARCHAR(64) DEFAULT '' COMMENT '第三方支付流水号',
                                    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='金额订单表';

CREATE TABLE `sys_inventory` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存ID',
                                 `productId` BIGINT NOT NULL COMMENT '商品ID',
                                 `productName` VARCHAR(128) NOT NULL COMMENT '商品名称',
                                 `productDescription` varchar(200) DEFAULT NULL COMMENT '商品描述',
                                 `productUrl` varchar(512) DEFAULT NULL COMMENT '商品图片',
                                 `productPrice` DECIMAL(10,2) NOT NULL COMMENT '出售单价',
                                 `storeId` BIGINT NOT NULL COMMENT '门店ID',
                                 `warningThreshold` INT NOT NULL DEFAULT 10 COMMENT '预警阈值',
                                 `warehouseId` BIGINT NOT NULL COMMENT '仓库ID',
                                 `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                                 `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

CREATE TABLE `sys_inventory_detail` (
                                        `productId` BIGINT NOT NULL COMMENT '商品ID',
                                        `orderId` BIGINT NOT NULL COMMENT '订单Id',
                                        `type` TINYINT NOT NULL DEFAULT 0 COMMENT '类型（0-出库，1-入库）',
                                        `orderType` TINYINT NOT NULL DEFAULT 0 COMMENT '类型（0-采购，1-采退，2-销售，3-销退）',
                                        `warehouseId` BIGINT NOT NULL COMMENT '仓库ID',
                                        `productQuantity` INT NOT NULL COMMENT '入库或出库数量',
                                        `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                                        `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                        KEY `idx_orderId_productId` (`productId`,`orderId`),
                                        KEY `idx_product_warehouse` (`productId`,`warehouseId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存变化记录表';

CREATE TABLE `sys_operation_log` (
                                     `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志唯一标识',
                                     `operationTime` DATETIME NOT NULL COMMENT '操作执行时间',
                                     `operatorId` BIGINT NOT NULL COMMENT '操作人ID',
                                     `operatorIp` VARCHAR(64) NOT NULL COMMENT '操作人IP地址（支持IPv4/IPv6）',
                                     `operatorDevice` VARCHAR(255) DEFAULT '' COMMENT '操作设备/浏览器信息',
                                     `systemModule` VARCHAR(64) NOT NULL COMMENT '操作所属系统模块（如用户管理、订单审核）',
                                     `operationContent` TEXT COMMENT '操作详细内容（JSON格式，记录修改前后数据）',
                                     `operationResult` VARCHAR(16) NOT NULL COMMENT '操作结果（SUCCESS-成功/FAIL-失败/PARTIAL-部分成功）',
                                     `errorMsg` TEXT COMMENT '错误信息（操作失败时记录）',
                                     PRIMARY KEY (`id`),
                                     KEY `idx_operator_id` (`operatorId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

CREATE TABLE `sys_warehouse` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '仓库唯一ID',
                                 `name` VARCHAR(100) NOT NULL COMMENT '仓库名称',
                                 `address` VARCHAR(255) NOT NULL COMMENT '详细地址',
                                 `description` TEXT NULL COMMENT '仓库描述',
                                 `createBy` BIGINT NOT NULL COMMENT '创建人ID',
                                 `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库表';

CREATE TABLE sys_transfer_log(
    id                  BIGINT AUTO_INCREMENT COMMENT '日志ID' PRIMARY KEY,
    transferOrderId     BIGINT COMMENT '调拨订单ID（关联调拨订单表）',
    sourceWarehouseId   BIGINT NOT NULL COMMENT '发货仓库ID',
    targetWarehouseId   BIGINT NOT NULL COMMENT '收货仓库ID',
    productId           BIGINT NOT NULL COMMENT '商品ID',
    transferQuantity    INT NOT NULL COMMENT '调拨数量',
    remark              VARCHAR(512) COMMENT '调拨备注（如自动补货调拨、库存不足调拨等）',
    createTime          DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime          DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='自动调拨日志记录表';

CREATE TABLE sys_sale_order(
    id                 BIGINT AUTO_INCREMENT COMMENT '销售订单ID' PRIMARY KEY,
    storeId            BIGINT                             NOT NULL COMMENT '门店ID',
    userId             BIGINT                             NOT NULL COMMENT '购买用户ID',
    productId          BIGINT                             NOT NULL COMMENT '商品ID',
    productName        VARCHAR(128)                       NOT NULL COMMENT '商品名称',
    productUrl         VARCHAR(512)                       NULL COMMENT '商品图片',
    productDescription VARCHAR(200)                       NULL COMMENT '商品描述',
    productPrice       DECIMAL(10, 2)                     NOT NULL COMMENT '销售单价',
    productQuantity    INT                                NOT NULL COMMENT '销售数量',
    totalAmount        DECIMAL(12, 2)                     NOT NULL COMMENT '订单总金额',
    warehouseId        BIGINT                             NOT NULL COMMENT '发货仓库ID',
    status             TINYINT                            NOT NULL COMMENT '状态（0-待发货，1-已发货，2-已完成）',
    createTime         DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime         DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    createBy           BIGINT                             NULL COMMENT '创建人ID（店员ID）'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='销售订单表';

CREATE TABLE sys_sale_return(
    id                 BIGINT AUTO_INCREMENT COMMENT '销退订单ID'
        PRIMARY KEY,
    saleOrderId        BIGINT                             NOT NULL COMMENT '关联销售订单ID',
    storeId            BIGINT                             NOT NULL COMMENT '门店ID',
    userId             BIGINT                             NOT NULL COMMENT '退货用户ID',
    productId          BIGINT                             NOT NULL COMMENT '商品ID',
    productName        VARCHAR(128)                       NOT NULL COMMENT '商品名称',
    productUrl         VARCHAR(512)                       NULL COMMENT '商品图片',
    productDescription VARCHAR(200)                       NULL COMMENT '商品描述',
    productPrice       DECIMAL(10, 2)                     NOT NULL COMMENT '退货单价（原销售单价）',
    productQuantity    INT                                NOT NULL COMMENT '退货数量',
    totalAmount        DECIMAL(12, 2)                     NOT NULL COMMENT '退货总金额',
    status             TINYINT                            NOT NULL COMMENT '状态（0-未完成，1-已完成）',
    warehouseId        BIGINT                             NOT NULL COMMENT '入库仓库ID（退货商品入库仓库）',
    reason             VARCHAR(512)                       NULL COMMENT '退货原因',
    createTime         DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime         DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    createBy           BIGINT                             NULL COMMENT '创建人ID'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='销售退货表';