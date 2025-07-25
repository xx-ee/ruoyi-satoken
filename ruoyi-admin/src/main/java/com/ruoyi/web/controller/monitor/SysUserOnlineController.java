package com.ruoyi.web.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaTerminalInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.LoginHelper;
import com.ruoyi.system.domain.SysUserOnline;
import com.ruoyi.system.service.ISysUserOnlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 在线用户监控
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/online")
public class SysUserOnlineController extends BaseController
{
    @Autowired
    private ISysUserOnlineService userOnlineService;

    @Autowired
    private RedisCache redisCache;

    @SaCheckPermission("monitor:online:list")
    @GetMapping("/list")
    public TableDataInfo list(String ipaddr, String userName)
    {
        List<SysUserOnline> userOnlineList = new ArrayList<>();



        // 获取所有已登录的会话id
        List<String> sessionIdList = StpUtil.searchSessionId("", 0, -1, true);
        for (String sessionId : sessionIdList) {
            // 根据会话id，查询对应的 SaSession 对象，此处一个 SaSession 对象即代表一个登录的账号
            SaSession session = StpUtil.getSessionBySessionId(sessionId);
            Object loginId = session.getLoginId();
//            String token = session.getToken();

            // 查询这个账号都在哪些设备登录了，依据上面的示例，账号A 的 SaTerminalInfo 数量是 3，账号B 的 SaTerminalInfo 数量是 2
            List<SaTerminalInfo> terminalList = session.terminalListCopy();
            for (SaTerminalInfo saTerminalInfo : terminalList) {
                String tokenValue = saTerminalInfo.getTokenValue();
                LoginUser user = LoginHelper.getLoginUserByToken(tokenValue);
//                Object loginId = LoginHelper.getLoginIdByToken(tokenValue);

                if (Objects.isNull(user)) {
                    continue;
                }
//                user.setToken(tokenValue);
                user.setToken(loginId);


                if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName)) {
                    userOnlineList.add(userOnlineService.selectOnlineByInfo(ipaddr, userName, user));
                } else if (StringUtils.isNotEmpty(ipaddr)) {
                    userOnlineList.add(userOnlineService.selectOnlineByIpaddr(ipaddr, user));
                } else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotNull(user.getUser())) {
                    userOnlineList.add(userOnlineService.selectOnlineByUserName(userName, user));
                } else {
                    userOnlineList.add(userOnlineService.loginUserToUserOnline(user));
                }

//                System.out.println();
            }
//            System.out.println("会话id：" + sessionId + "，共在 " + terminalList.size() + " 设备登录");
        }


    /*    Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        for (String key : keys) {
            LoginUser user = redisCache.getCacheObject(key);
            if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName)) {
                userOnlineList.add(userOnlineService.selectOnlineByInfo(ipaddr, userName, user));
            } else if (StringUtils.isNotEmpty(ipaddr)) {
                userOnlineList.add(userOnlineService.selectOnlineByIpaddr(ipaddr, user));
            } else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotNull(user.getUser())) {
                userOnlineList.add(userOnlineService.selectOnlineByUserName(userName, user));
            } else {
                userOnlineList.add(userOnlineService.loginUserToUserOnline(user));
            }
        }*/
        Collections.reverse(userOnlineList);
        userOnlineList.removeAll(Collections.singleton(null));
        return getDataTable(userOnlineList);
    }

    /**
     * 强退用户
     */
    @SaCheckPermission("monitor:online:forceLogout")
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @DeleteMapping("/{tokenId}")
    public AjaxResult forceLogout(@PathVariable String tokenId)
    {
        redisCache.deleteObject(CacheConstants.LOGIN_TOKEN_KEY + tokenId);
        return success();
    }
}
