package com.qzh.backend.model.dto.role;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.common.PageRequest;
import com.qzh.backend.constants.RoleNameConstant;
import com.qzh.backend.model.entity.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoleQueryDTO extends PageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String roleName;

    private String description;

    private Date startTime;

    private Date endTime;

    private Boolean excludeAdmin;

    public static QueryWrapper<Role> getQueryWrapper(RoleQueryDTO roleQueryDTO) {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        if (roleQueryDTO == null){
            return queryWrapper;
        }
        String roleName = roleQueryDTO.getRoleName();
        String description = roleQueryDTO.getDescription();
        Date startTime = roleQueryDTO.getStartTime();
        String sortField = roleQueryDTO.getSortField();
        String sortOrder = roleQueryDTO.getSortOrder();
        Date endTime = roleQueryDTO.getEndTime();
        Boolean excludeAdmin = roleQueryDTO.getExcludeAdmin();
        queryWrapper.like(ObjUtil.isNotNull(roleName),"roleName",roleName);
        queryWrapper.like(ObjUtil.isNotNull(description),"description",description);
        if (Boolean.TRUE.equals(excludeAdmin)) {
            queryWrapper.ne("roleName", RoleNameConstant.ADMIN);
            queryWrapper.ne("roleName", RoleNameConstant.STORE_ADMIN);
        }
        queryWrapper.ge(ObjUtil.isNotEmpty(startTime), "createTime", startTime);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        queryWrapper.lt(ObjUtil.isNotEmpty(endTime), "createTime", endTime);
        return queryWrapper;
    }
}
