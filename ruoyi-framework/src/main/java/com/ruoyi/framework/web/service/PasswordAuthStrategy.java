package com.ruoyi.framework.web.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginBody;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.enums.UserStatus;
import com.ruoyi.common.exception.CustomException;
import com.ruoyi.common.exception.base.BaseException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.LoginHelper;
import com.ruoyi.framework.web.domain.LoginVo;
import com.ruoyi.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by xiedong
 * 2025/6/9
 */
@Slf4j
@Service("password" + IAuthStrategy.BASE_NAME)
public class PasswordAuthStrategy implements IAuthStrategy {
    @Resource
    private SysLoginService sysLoginService;

    @Resource
    private ISysUserService userService;

    @Resource
    private SysPermissionService permissionService;

    @Override
    public LoginVo login(String body) {
        LoginBody loginBody = JSON.parseObject(body, LoginBody.class);
//        ValidatorUtils.validate(loginBody);

        String username = loginBody.getUsername();
        String password = loginBody.getPassword();
        String code = loginBody.getCode();
        String uuid = loginBody.getUuid();

        // 验证码校验
        sysLoginService.validateCaptcha(username, code, uuid);
        // 登录前置校验
        sysLoginService.loginPreCheck(username, password);

//        loginService.checkLogin(LoginType.PASSWORD,);

        String encryptedPassword = SecurityUtils.encryptPassword(password);
        SysUser user = userService.selectUserByUserName(username);
        if (StringUtils.isNull(user)) {
            log.info("登录用户：{} 不存在.", username);
            throw new CustomException("登录用户：" + username + " 不存在");
        } else if (!StringUtils.equals(encryptedPassword, user.getPassword())) {
            throw new CustomException("密码错误！");
        } else if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            log.info("登录用户：{} 已被删除.", username);
            throw new BaseException("对不起，您的账号：" + username + " 已被删除");
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new BaseException("对不起，您的账号：" + username + " 已停用");
        }
        LoginUser loginUser = createLoginUser(user);

        SaLoginParameter model = new SaLoginParameter();
//        model.setDeviceType(client.getDeviceType());
        // 自定义分配 不同用户体系 不同 token 授权时间 不设置默认走全局 yml 配置
        // 例如: 后台用户30分钟过期 app用户1天过期
//        model.setTimeout(client.getTimeout());
//        model.setActiveTimeout(client.getActiveTimeout());
//        model.setExtra(LoginHelper.CLIENT_KEY, client.getClientId());

        // 生成token
        LoginHelper.login(loginUser, model);

        LoginVo loginVo = new LoginVo();
        loginVo.setAccessToken(StpUtil.getTokenValue());
        loginVo.setExpireIn(StpUtil.getTokenTimeout());
//        loginVo.setClientId(client.getClientId());
        return loginVo;
    }

    private LoginUser createLoginUser(SysUser user) {
        return new LoginUser(user, permissionService.getRolePermission(user), permissionService.getMenuPermission(user));
    }
}
