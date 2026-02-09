package com.qzh.backend.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.RoleMapper;
import com.qzh.backend.mapper.UserMapper;
import com.qzh.backend.mapper.UserRelatedRoleMapper;
import com.qzh.backend.model.entity.Role;
import com.qzh.backend.model.entity.User;
import com.qzh.backend.model.entity.UserRelatedRole;
import com.qzh.backend.service.RoleService;
import com.qzh.backend.service.UserRelatedRoleService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.qzh.backend.constants.UserConstant.USER_LOGIN_STATE;

@RequiredArgsConstructor
@Component
public class GetLoginUserUtil {

    @Resource
    @Lazy
    private RoleService roleService;

    private final UserRelatedRoleService userRelatedRoleService;

    public User getLoginUser(HttpServletRequest request) {
        // 从 Session 获取登录状态
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        // 未登录校验
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录，请先登录");
        }
        return currentUser;
    }

    public List<Role> getRoleList(Long id) {
        List<UserRelatedRole> userRoleRelations = userRelatedRoleService.list(
                new LambdaQueryWrapper<UserRelatedRole>()
                        .eq(UserRelatedRole::getUserId, id)
        );
        // 填充完整角色列表（替换原角色名称列表）
        if (!CollectionUtils.isEmpty(userRoleRelations)) {
            // 提取角色ID列表（去重）
            List<Long> roleIds = userRoleRelations.stream()
                    .map(UserRelatedRole::getRoleId)
                    .distinct()
                    .collect(Collectors.toList());
            // 批量查询完整角色实体
            List<Role> roleList = roleService.list(
                    new LambdaQueryWrapper<Role>()
                            .in(Role::getId, roleIds)
            );
            // 过滤无效角色（避免关联已删除的角色）
            return roleList.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

}
