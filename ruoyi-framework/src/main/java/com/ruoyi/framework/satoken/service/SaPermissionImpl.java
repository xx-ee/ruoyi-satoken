package com.ruoyi.framework.satoken.service;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.LoginHelper;
import com.ruoyi.framework.web.service.SysPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by xiedong
 * 2025/7/25
 */
@Service
@Slf4j
public class SaPermissionImpl implements StpInterface {

    @Resource
    private SysPermissionService sysPermissionService;

    /**
     * 获取菜单权限列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (Objects.isNull(loginUser) || Objects.isNull(loginUser.getUser())) {
            log.info("getPermissionList-exit loginId:{},loginType:{},loginUser is null", loginId, loginType);
            return new ArrayList<>();
        }
        SysUser user = loginUser.getUser();
        long tokenTimeout = StpUtil.getTokenTimeout();
        log.info("getPermissionList-start loginId:{},loginType:{},loginUser:{},tokenTimeout:{}", loginId, loginType, user, tokenTimeout);
        Set<String> menuPermission = loginUser.getRolePermissions();
//        Set<String> menuPermission = sysPermissionService.getMenuPermission(user);
//        if (ObjectUtil.isNull(loginUser) || !loginUser.getLoginId().equals(loginId)) {
//            PermissionService permissionService = getPermissionService();
//            if (ObjectUtil.isNotNull(permissionService)) {
//                List<String> list = StringUtils.splitList(loginId.toString(), ":");
//                return new ArrayList<>(permissionService.getMenuPermission(Long.parseLong(list.get(1))));
//            } else {
//                throw new ServiceException("PermissionService 实现类不存在");
//            }
//        }
//        UserType userType = UserType.getUserType(loginUser.getUserType());
//        if (userType == UserType.APP_USER) {
//            // 其他端 自行根据业务编写
//        }
//        // SYS_USER 默认返回权限
//        return new ArrayList<>(loginUser.getMenuPermission());

        return new ArrayList<>(menuPermission);
    }

    /**
     * 获取角色权限列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (Objects.isNull(loginUser) || Objects.isNull(loginUser.getUser())) {
            log.info("getRoleList-exit loginId:{},loginType:{},loginUser is null", loginId, loginType);
            return new ArrayList<>();
        }
        SysUser user = loginUser.getUser();
        long tokenTimeout = StpUtil.getTokenTimeout();
        log.info("getRoleList-start loginId:{},loginType:{},loginUser:{},tokenTimeout:{}", loginId, loginType, user, tokenTimeout);
        Set<String> rolePermission = loginUser.getPermissions();

//        Set<String> rolePermission = sysPermissionService.getRolePermission(user);
//        if (ObjectUtil.isNull(loginUser) || !loginUser.getLoginId().equals(loginId)) {
//            PermissionService permissionService = getPermissionService();
//            if (ObjectUtil.isNotNull(permissionService)) {
//                List<String> list = StringUtils.splitList(loginId.toString(), ":");
//                return new ArrayList<>(permissionService.getRolePermission(Long.parseLong(list.get(1))));
//            } else {
//                throw new ServiceException("PermissionService 实现类不存在");
//            }
//        }
//        UserType userType = UserType.getUserType(loginUser.getUserType());
//        if (userType == UserType.APP_USER) {
//            // 其他端 自行根据业务编写
//        }
//        // SYS_USER 默认返回权限
//        return new ArrayList<>(loginUser.getRolePermission());
        return new ArrayList<>(rolePermission);
    }

//    private PermissionService getPermissionService() {
//        try {
//            return SpringUtils.getBean(PermissionService.class);
//        } catch (Exception e) {
//            return null;
//        }
//    }

}