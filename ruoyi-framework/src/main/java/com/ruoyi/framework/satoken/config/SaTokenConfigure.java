package com.ruoyi.framework.satoken.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.framework.satoken.handler.AllUrlHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by xiedong
 * 2025/7/25
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
@RequiredArgsConstructor
public class SaTokenConfigure implements WebMvcConfigurer {

 private final SecurityProperties securityProperties;


 // 注册sa-token的拦截器
 @Override
 public void addInterceptors(InterceptorRegistry registry) {


  // 注册 Sa-Token 拦截器，定义详细认证规则
//        registry.addInterceptor(new SaInterceptor(handler -> {
//            // 指定一条 match 规则
//            SaRouter
//                    .match("/**")    // 拦截的 path 列表，可以写多个 */
//                    .notMatch("/system/dict/data/type/system_config","/captchaImage")        // 排除掉的 path 列表，可以写多个
//                    .check(r -> StpUtil.checkLogin());        // 要执行的校验动作，可以写完整的 lambda 表达式
//
//            // 根据路由划分模块，不同模块不同鉴权
////            SaRouter.match("/user/**", r -> StpUtil.checkPermission("user"));
////            SaRouter.match("/orders/**", r -> StpUtil.checkPermission("orders"));
////            SaRouter.match("/notice/**", r -> StpUtil.checkPermission("notice"));
////            SaRouter.match("/comment/**", r -> StpUtil.checkPermission("comment"));
//        })).addPathPatterns("/**");


  // 注册路由拦截器，自定义验证规则
  registry.addInterceptor(new SaInterceptor(handler -> {
           AllUrlHandler allUrlHandler = SpringUtils.getBean(AllUrlHandler.class);
           // 登录验证 -- 排除多个路径
           SaRouter
                   // 获取所有的
                   .match(allUrlHandler.getUrls())
                   // 对未排除的路径进行检查
                   .check(() -> {
                    HttpServletRequest request = ServletUtils.getRequest();
//                                tokenService.setLoginUser();
                    // 检查是否登录 是否有token
                    StpUtil.checkLogin();

                    long tokenTimeout = StpUtil.getTokenTimeout();
                    long sessionTimeout = StpUtil.getSessionTimeout();
                    long tokenSessionTimeout = StpUtil.getTokenSessionTimeout();
                    log.info("tokenSessionTimeout: {}, tokenTimeout: {}, sessionTimeout: {}", tokenSessionTimeout, tokenTimeout, sessionTimeout);
                    StpUtil.renewTimeout(StpUtil.getStpLogic().getConfigOrGlobal().getTimeout());
                    // 有效率影响 用于临时测试
                    // if (log.isDebugEnabled()) {
                    //     log.info("剩余有效时间: {}", StpUtil.getTokenTimeout());
                    //     log.info("临时有效时间: {}", StpUtil.getTokenActivityTimeout());
                    // }

                   });
          })).addPathPatterns("/**")
          // 排除不需要拦截的路径
          .excludePathPatterns(securityProperties.getExcludes());
 }
}
