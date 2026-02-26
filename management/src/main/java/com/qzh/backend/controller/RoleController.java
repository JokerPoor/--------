package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.annotation.AuthCheck;
import com.qzh.backend.annotation.LogInfoRecord;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.role.*;
import com.qzh.backend.model.vo.RoleVO;
import com.qzh.backend.service.RoleService;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.qzh.backend.constants.Interface.RoleInterfaceConstant.*;
import static com.qzh.backend.constants.ModuleConstant.ROLE_MODULE;

@RestController
@RequestMapping("role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("list")
    @AuthCheck(interfaceName = ROLE_LIST_GET)
    public BaseResponse<Page<RoleVO>> getRoleList(RoleQueryDTO dto) {
        Page<RoleVO> rolePage = roleService.getRolePage(dto);
        return ResultUtils.success(rolePage);
    }

    @PostMapping
    @AuthCheck(interfaceName = ROLE_CREATE_POST)
    @LogInfoRecord(SystemModule = ROLE_MODULE + ":" + ROLE_CREATE_POST)
    public BaseResponse<Long> createRole(@RequestBody @Valid RoleCreateDTO dto, HttpServletRequest request) {
        Long roleId = roleService.createRole(dto,request);
        return ResultUtils.success(roleId);
    }

    @GetMapping("{id}")
    @AuthCheck(interfaceName = ROLE_DETAIL_GET)
    public BaseResponse<RoleVO> getRoleById(@PathVariable Long id) {
        RoleVO roleVO = roleService.getRoleDetailById(id);
        return ResultUtils.success(roleVO);
    }

    @PutMapping("{id}")
    @AuthCheck(interfaceName = ROLE_UPDATE_PUT)
    @LogInfoRecord(SystemModule = ROLE_MODULE + ":" + ROLE_UPDATE_PUT)
    public BaseResponse<Void> updateRole(@PathVariable Long id, @RequestBody @Valid RoleUpdateDTO dto) {
        Boolean b = roleService.updateRole(id, dto);
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR,"更新角色出错");
        return ResultUtils.success(null);
    }

    @DeleteMapping("{id}")
    @AuthCheck(interfaceName = ROLE_DELETE_DELETE)
    @LogInfoRecord(SystemModule = ROLE_MODULE + ":" + ROLE_DELETE_DELETE)
    public BaseResponse<Void> deleteRole(@PathVariable Long id) {
        Boolean b = roleService.deleteRole(id);
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR,"删除角色出错");
        return ResultUtils.success(null);
    }

    @PutMapping("/{roleId}/permission")
    @AuthCheck(interfaceName = ROLE_ASSIGN_PERMISSION_PUT)
    @LogInfoRecord(SystemModule = ROLE_MODULE + ":" + ROLE_ASSIGN_PERMISSION_PUT)
    public BaseResponse<Void> assignRolePermissions(@PathVariable Long roleId, @RequestBody RoleAddPermissionDTO permissionDTO,HttpServletRequest request) {
        boolean success = roleService.assignRolePermissions(roleId, permissionDTO,request);
        ThrowUtils.throwIf(!success, ErrorCode.SYSTEM_ERROR, "角色权限修改失败");
        return ResultUtils.success(null);
    }

    @PostMapping("/{roleId}/assign-pages")
    @AuthCheck(interfaceName = ROLE_ASSIGN_PAGE_POST)
    @LogInfoRecord(SystemModule = ROLE_MODULE + ":" + ROLE_ASSIGN_PAGE_POST)
    public BaseResponse<Void> assignRolePages(@PathVariable Long roleId, @RequestBody RolePageAssignDTO assignDTO,HttpServletRequest request) {
        boolean success = roleService.assignRolePages(roleId, assignDTO,request);
        ThrowUtils.throwIf(!success, ErrorCode.SYSTEM_ERROR, "页面权限分配失败");
        return ResultUtils.success(null);
    }

}
