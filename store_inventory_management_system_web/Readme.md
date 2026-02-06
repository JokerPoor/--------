后端接口文档
响应码
JSON
SUCCESS(40200, "success"),
PARAMS_ERROR(40000, "请求参数错误"),
NOT_LOGIN_ERROR(40100, "未登录"),
NO_AUTH_ERROR(40101, "无权限"),
NOT_FOUND_ERROR(40400, "请求数据不存在"),
FORBIDDEN_ERROR(40300, "禁止访问"),
SYSTEM_ERROR(50000, "系统内部异常"),
OPERATION_ERROR(50001, "操作失败");
人员管理模块接口文档
1. 接口概述
1.1 模块说明
人员管理模块核心围绕用户、角色、部门、权限的全生命周期管理，支持用户增删改查、角色分配、权限关联等核心操作，适配多角色权限控制场景，接口设计遵循 RESTful 规范，确保高可用性和扩展性。
1.2 通用返回格式
所有接口统一返回 JSON 格式数据，包含状态码、提示信息和业务数据：
json
JSON
{"code": 200,"message": "操作成功","data": {}}
•状态码说明：200 成功，400 参数错误，401 未授权，403 无权限，500 服务器错误
•data 字段为可选，仅在需要返回业务数据时存在
2. 用户管理接口
2.1 获取用户列表
•接口地址: /api/system/user/list
•请求方法: GET
•权限要求: 拥有 "用户查询" 权限
•请求参数:

点击图片可查看完整电子表格
•返回数据:
json
JSON
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 50,
    "pages": 5,
    "current": 1,
    "size": 10,
    "records": [
      {
        "id": 1,
        "userAccount": "admin",
        "userName": "超级管理员",
        "deptId": 1,
        "deptName": "总部",
        "phone": "13800138000",
        "status": 1,
        "roleIds": [
          1
        ],
        "roleNames": [
          "超级管理员"
        ],
        "createTime": "2025-01-01 00:00:00",
        "updateTime": "2025-01-01 00:00:00"
      }
    ]
  }
}
2.2 创建用户
•接口地址: /api/system/user
•请求方法: POST
•权限要求: 拥有 "用户创建" 权限
•请求体:
json
JSON
{
  "userAccount": "test001",
  "userPassword": "123456",
  "userName": "测试用户",
  "deptId": 2,
  "phone": "13900139000",
  "status": 1,
  "roleIds": [
    2,
    3
  ]
}
•返回数据:
json
JSON
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 2
  }
}
2.3 获取用户详情
•接口地址: /api/system/user/{id}
•请求方法: GET
•权限要求: 拥有 "用户查询" 权限
•路径参数:

点击图片可查看完整电子表格
•返回数据:
json
JSON
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "userAccount": "admin",
    "userName": "超级管理员",
    "deptId": 1,
    "deptName": "总部",
    "phone": "13800138000",
    "status": 1,
    "roleIds": [
      1
    ],
    "roleNames": [
      "超级管理员"
    ],
    "createTime": "2025-01-01 00:00:00",
    "updateTime": "2025-01-01 00:00:00"
  }
}
2.4 更新用户信息
•接口地址: /api/system/user/{id}
•请求方法: PUT
•权限要求: 拥有 "用户编辑" 权限
•路径参数:

点击图片可查看完整电子表格
•请求体:
json
JSON
{
  "userName": "超级管理员（更新）",
  "deptId": 1,
  "phone": "13800138001",
  "status": 1,
  "roleIds": [
    1,
    2
  ]
}
•返回数据:
json
JSON
{"code": 200,"message": "更新成功"}
2.5 重置用户密码
•接口地址: /api/system/user/{id}/reset-password
•请求方法: POST
•权限要求: 拥有 "用户密码重置" 权限
•路径参数:

点击图片可查看完整电子表格
•请求体:
json
JSON
{"newPassword": "654321"}
•返回数据:
json
JSON
{"code": 200,"message": "密码重置成功"}
2.6 批量启用 / 禁用用户
•接口地址: /api/system/user/batch-status
•请求方法: POST
•权限要求: 拥有 "用户状态管理" 权限
•请求体:
json
JSON
{"ids": [1, 2, 3],"status": 0}
•返回数据:
json
JSON
{"code": 200,"message": "操作成功"}
2.7 删除用户
•接口地址: /api/system/user/{id}
•请求方法: DELETE
•权限要求: 拥有 "用户删除" 权限
•路径参数:

点击图片可查看完整电子表格
•返回数据:
json
JSON
{"code": 200,"message": "删除成功"}
3. 角色管理接口
3.1 获取角色列表
•接口地址: /api/system/role/list
•请求方法: GET
•权限要求: 拥有 "角色查询" 权限
•请求参数:

点击图片可查看完整电子表格
•返回数据:
json
JSON
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 10,
    "pages": 1,
    "current": 1,
    "size": 10,
    "records": [
      {
        "id": 1,
        "roleName": "超级管理员",
        "description": "系统最高权限角色",
        "createTime": "2025-01-01 00:00:00",
        "updateTime": "2025-01-01 00:00:00"
      }
    ]
  }
}
3.2 创建角色
•接口地址: /api/system/role
•请求方法: POST
•权限要求: 拥有 "角色创建" 权限
•请求体:
json
JSON
{"roleName": "采购专员","description": "负责采购订单管理相关操作"}
•返回数据:
json
JSON
{"code": 200,"message": "创建成功","data": {"id": 2}}
3.3 获取角色详情
•接口地址: /api/system/role/{id}
•请求方法: GET
•权限要求: 拥有 "角色查询" 权限
•路径参数:

点击图片可查看完整电子表格
•返回数据:
json
JSON
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "roleName": "超级管理员",
    "description": "系统最高权限角色",
    "createTime": "2025-01-01 00:00:00",
    "updateTime": "2025-01-01 00:00:00",
    "permissionIds": [
      1,
      2,
      3
    ],
    "permissionNames": [
      "用户查询",
      "角色管理",
      "权限分配"
    ]
  }
}
3.4 更新角色
•接口地址: /api/system/role/{id}
•请求方法: PUT
•权限要求: 拥有 "角色编辑" 权限
•路径参数:

点击图片可查看完整电子表格
•请求体:
json
JSON
{"roleName": "超级管理员（更新）","description": "系统最高权限角色，负责全量操作"}
•返回数据:
json
JSON
{"code": 200,"message": "更新成功"}
3.5 删除角色
•接口地址: /api/system/role/{id}
•请求方法: DELETE
•权限要求: 拥有 "角色删除" 权限
•路径参数:

点击图片可查看完整电子表格
•返回数据:
json
JSON
{"code": 200,"message": "删除成功"}
4. 权限管理接口
4.1 获取权限列表
•接口地址: /api/system/permission/list
•请求方法: GET
•权限要求: 拥有 "权限查询" 权限
•请求参数: 无
•返回数据:
json
JSON
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "name": "用户查询",
      "description": "查询系统用户信息"
    },
    {
      "id": 2,
      "name": "角色管理",
      "description": "创建、编辑、删除角色"
    }
  ]
}
4.2 给角色分配权限
•接口地址: /api/system/role/{roleId}/assign-permission
•请求方法: POST
•权限要求: 拥有 "权限分配" 权限
•路径参数:

点击图片可查看完整电子表格
•请求体:
json
JSON
{"permissionIds": [1, 2, 3, 4]}
•返回数据:
json
JSON
{"code": 200,"message": "权限分配成功"}
数据库SQL
用户模块
SQL
CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                            `userAccount` varchar(50) NOT NULL COMMENT '登录账号',
                            `userPassword` varchar(100) NOT NULL COMMENT '登录密码',
                            `userName` varchar(50) NOT NULL COMMENT '真实姓名',
                            `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
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
