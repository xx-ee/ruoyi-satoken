package com.ruoyi.framework.satoken.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.ruoyi.common.utils.spring.SpringUtils;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by xiedong
 * 2025/7/25
 */
@Data
@Component
public class AllUrlHandler implements InitializingBean {

    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");

    private List<String> urls = new ArrayList<>();

    @Override
    public void afterPropertiesSet() {
        Set<String> set = new HashSet<>();
        RequestMappingHandlerMapping mapping = SpringUtil.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        map.keySet().forEach(info -> {
            if (Objects.nonNull(info) && Objects.nonNull(info.getPathPatternsCondition())
                    && CollUtil.isNotEmpty(info.getPathPatternsCondition().getPatterns())) {

                // 获取注解上边的 path 替代 path variable 为 *
                Objects.requireNonNull(info.getPathPatternsCondition().getPatterns())
                        .forEach(url -> set.add(ReUtil.replaceAll(url.getPatternString(), PATTERN, "*")));
            }
        });
        urls.addAll(set);
    }

}
