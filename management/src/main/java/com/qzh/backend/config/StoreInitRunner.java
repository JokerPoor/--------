package com.qzh.backend.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.config.StoreInitConfig;
import com.qzh.backend.constants.RoleNameConstant;
import com.qzh.backend.model.entity.PageInfo;
import com.qzh.backend.model.entity.Permission;
import com.qzh.backend.model.entity.Role;
import com.qzh.backend.model.entity.RoleRelatedPage;
import com.qzh.backend.model.entity.RoleRelatedPermission;
import com.qzh.backend.model.entity.Store;
import com.qzh.backend.service.RoleService;
import com.qzh.backend.service.PageService;
import com.qzh.backend.service.PermissionService;
import com.qzh.backend.service.RoleRelatedPageService;
import com.qzh.backend.service.RoleRelatedPermissionService;
import com.qzh.backend.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 单门店初始化执行器：确保系统中只有一条门店信息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StoreInitRunner implements ApplicationRunner {

    private final StoreInitConfig storeInitConfig;

    private final StoreService storeService;

    private final RoleService roleService;

    private final PageService pageService;

    private final RoleRelatedPageService roleRelatedPageService;

    private final PermissionService permissionService;

    private final RoleRelatedPermissionService roleRelatedPermissionService;

    private final AppGlobalConfig appGlobalConfig;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("===== 开始执行单门店初始化任务 =====");
        Store existingStore = storeService.getOne(new QueryWrapper<>());
        Store configStore = storeInitConfig.toStore();
        if (existingStore == null) {
            storeService.save(configStore);
        } else {
            configStore.setId(existingStore.getId());
            storeService.updateById(configStore);
        }
        initDefaultRoles(configStore.getManagerId());
        initAdminAccess(configStore.getManagerId());
        appGlobalConfig.setCurrentStoreId(configStore.getId());
        appGlobalConfig.setCurrentStoreName(configStore.getStoreName());
        appGlobalConfig.setManagerId(configStore.getManagerId());
        log.info("===== 单门店初始化任务执行完成 =====");
        log.info("门店ID：{}，门店名：{}", appGlobalConfig.getCurrentStoreId(),appGlobalConfig.getCurrentStoreName());
    }

    private void initDefaultRoles(Long createBy) {
        List<String> roleNames = List.of(RoleNameConstant.ADMIN, RoleNameConstant.SUPPLIER, RoleNameConstant.CUSTOMER);
        for (String roleName : roleNames) {
            boolean exists = roleService.count(new LambdaQueryWrapper<Role>().eq(Role::getRoleName, roleName)) > 0;
            if (exists) {
                continue;
            }
            Role role = new Role();
            role.setRoleName(roleName);
            role.setDescription(roleName);
            role.setCreateBy(createBy);
            roleService.save(role);
        }
    }

    private record PageSeed(String name, String path, String component, int orderNum, int visible) {
    }

    private void initAdminAccess(Long createBy) {
        Role adminRole = roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleName, RoleNameConstant.ADMIN));
        if (adminRole == null) {
            return;
        }

        List<PageSeed> seeds = List.of(
                new PageSeed("用户管理", "/users", "pages/users/UsersPage", 100, 1),
                new PageSeed("角色管理", "/roles", "pages/roles/RolesPage", 90, 1),
                new PageSeed("权限管理", "/permissions", "pages/permissions/PermissionsPage", 80, 1),
                new PageSeed("页面管理", "/pages", "pages/pages/PagesPage", 70, 1)
        );

        Map<String, PageInfo> existingPagesByPath = pageService.list(
                new LambdaQueryWrapper<PageInfo>()
                        .in(PageInfo::getPath, seeds.stream().map(PageSeed::path).toList())
        ).stream().collect(Collectors.toMap(PageInfo::getPath, p -> p, (a, b) -> a));

        for (PageSeed seed : seeds) {
            if (existingPagesByPath.containsKey(seed.path())) {
                continue;
            }
            PageInfo pageInfo = new PageInfo();
            pageInfo.setParentId(0L);
            pageInfo.setName(seed.name());
            pageInfo.setPath(seed.path());
            pageInfo.setComponent(seed.component());
            pageInfo.setOrderNum(seed.orderNum());
            pageInfo.setVisible(seed.visible());
            pageInfo.setCreateBy(createBy);
            pageService.save(pageInfo);
        }

        List<PageInfo> pages = pageService.list(
                new LambdaQueryWrapper<PageInfo>()
                        .in(PageInfo::getPath, seeds.stream().map(PageSeed::path).toList())
        );
        Set<Long> pageIds = pages.stream().map(PageInfo::getId).collect(Collectors.toSet());

        Set<Long> existingRolePageIds = roleRelatedPageService.list(
                new LambdaQueryWrapper<RoleRelatedPage>()
                        .eq(RoleRelatedPage::getRoleId, adminRole.getId())
                        .in(RoleRelatedPage::getPageId, pageIds)
        ).stream().map(RoleRelatedPage::getPageId).collect(Collectors.toSet());

        List<RoleRelatedPage> toSaveRolePages = pageIds.stream()
                .filter(pid -> !existingRolePageIds.contains(pid))
                .map(pid -> {
                    RoleRelatedPage rp = new RoleRelatedPage();
                    rp.setRoleId(adminRole.getId());
                    rp.setPageId(pid);
                    rp.setCreateBy(createBy);
                    return rp;
                })
                .toList();
        if (!toSaveRolePages.isEmpty()) {
            roleRelatedPageService.saveBatch(toSaveRolePages);
        }

        List<String> basePermNames = List.of("角色管理", "权限分配");
        Map<String, Permission> existingPermsByName = permissionService.list(
                new LambdaQueryWrapper<Permission>().in(Permission::getName, basePermNames)
        ).stream().collect(Collectors.toMap(Permission::getName, p -> p, (a, b) -> a));

        for (String permName : basePermNames) {
            if (existingPermsByName.containsKey(permName)) {
                continue;
            }
            Permission permission = new Permission();
            permission.setName(permName);
            permission.setDescription(permName);
            permission.setCreateBy(createBy);
            permissionService.save(permission);
        }

        List<Permission> perms = permissionService.list(
                new LambdaQueryWrapper<Permission>().in(Permission::getName, basePermNames)
        );
        Set<Long> permIds = perms.stream().map(Permission::getId).collect(Collectors.toSet());

        Set<Long> existingRolePermIds = roleRelatedPermissionService.list(
                new LambdaQueryWrapper<RoleRelatedPermission>()
                        .eq(RoleRelatedPermission::getRoleId, adminRole.getId())
                        .in(RoleRelatedPermission::getPermissionId, permIds)
        ).stream().map(RoleRelatedPermission::getPermissionId).collect(Collectors.toSet());

        List<RoleRelatedPermission> toSaveRolePerms = permIds.stream()
                .filter(pid -> !existingRolePermIds.contains(pid))
                .map(pid -> {
                    RoleRelatedPermission rp = new RoleRelatedPermission();
                    rp.setRoleId(adminRole.getId());
                    rp.setPermissionId(pid);
                    rp.setCreateBy(createBy);
                    return rp;
                })
                .toList();
        if (!toSaveRolePerms.isEmpty()) {
            roleRelatedPermissionService.saveBatch(toSaveRolePerms);
        }
    }
}
