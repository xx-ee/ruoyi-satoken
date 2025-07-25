package com.ruoyi.framework.satoken.core;

import cn.dev33.satoken.dao.auto.SaTokenDaoBySessionFollowObject;
import cn.dev33.satoken.util.SaFoxUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.redis.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sa-Token持久层接口(使用框架自带RedisUtils实现 协议统一)
 * <p>
 * 采用 caffeine + redis 多级缓存 优化并发查询效率
 * <p>
 * SaTokenDaoBySessionFollowObject 是 SaTokenDao 子集简化了session方法处理
 *
 * @author Lion Li
 */
@Service
@Slf4j
public class PlusSaTokenDao implements SaTokenDaoBySessionFollowObject {

//    private static final Cache<String, Object> CAFFEINE = Caffeine.newBuilder()
//            // 设置最后一次写入或访问后经过固定时间过期
//            .expireAfterWrite(5, TimeUnit.SECONDS)
//            // 初始的缓存空间大小
//            .initialCapacity(100)
//            // 缓存的最大条数
//            .maximumSize(1000)
//            .build();

    @Resource
    private RedisCache redisCache;

    /**
     * 获取Value，如无返空
     */
    @Override
    public String get(String key) {
//        Object o = CAFFEINE.get(key, k -> redisCache.getCacheObject(key));
//        return (String) o;
        return redisCache.getCacheObject(key);
    }

    /**
     * 写入Value，并设定存活时间 (单位: 秒)
     */
    @Override
    public void set(String key, String value, long timeout) {
        if (timeout == 0 || timeout <= NOT_VALUE_EXPIRE) {
            return;
        }
        // 判断是否为永不过期
        if (timeout == NEVER_EXPIRE) {
            redisCache.setCacheObject(key, value);
        } else {
            if (redisCache.hasKey(key)) {
                redisCache.setCacheObject(key, value);
            } else {
                redisCache.setCacheObject(key, value, Long.valueOf(timeout).intValue(), TimeUnit.SECONDS);
            }
        }
//        CAFFEINE.invalidate(key);
    }

    /**
     * 修修改指定key-value键值对 (过期时间不变)
     */
    @Override
    public void update(String key, String value) {
        if (redisCache.hasKey(key)) {
            redisCache.setCacheObject(key, value);
//            CAFFEINE.invalidate(key);
        }
    }

    /**
     * 删除Value
     */
    @Override
    public void delete(String key) {
        redisCache.deleteObject(key);
    }

    /**
     * 获取Value的剩余存活时间 (单位: 秒)
     */
    @Override
    public long getTimeout(String key) {
        long timeout = redisCache.getTimeToLive(key);
        // 加1的目的 解决sa-token使用秒 redis是毫秒导致1秒的精度问题 手动补偿
//        return timeout < 0 ? timeout : timeout / 1000 + 1;
        return timeout;
    }

    /**
     * 修改Value的剩余存活时间 (单位: 秒)
     */
    @Override
    public void updateTimeout(String key, long timeout) {
        redisCache.expire(key, timeout);
    }


    /**
     * 获取Object，如无返空
     */
    @Override
    public Object getObject(String key) {
//        Object o = CAFFEINE.get(key, k -> redisCache.getCacheObject(key));
//        return o;
        return redisCache.getCacheObject(key);
    }

    /**
     * 获取 Object (指定反序列化类型)，如无返空
     *
     * @param key 键名称
     * @return object
     */
    @SuppressWarnings("unchecked cast")
    @Override
    public <T> T getObject(String key, Class<T> classType) {
//        Object o = CAFFEINE.get(key, k -> redisCache.getCacheObject(key));
        Object o = redisCache.getCacheObject(key);
        /*
        o = {JSONObject@16977}  size = 9
 "@type" -> "cn.dev33.satoken.session.SaSession"
 "createTime" -> {Long@16993} 1749458380275
 "dataMap" -> {JSONObject@16995}  size = 1
 "historyTerminalCount" -> {Integer@16997} 1
 "id" -> "Authorization:login:session:1"
 "loginId" -> {Long@17001} 1
 "loginType" -> "login"
 "terminalList" -> {JSONArray@17005}  size = 1
 "type" -> "Account-Session"
         */
        if(o instanceof JSONObject){
            T t = JSON.parseObject(JSON.toJSONString(o), classType);
            return t;
        }
        return (T) o;
    }

    /**
     * 写入Object，并设定存活时间 (单位: 秒)
     */
    @Override
    public void setObject(String key, Object object, long timeout) {
        if (timeout == 0 || timeout <= NOT_VALUE_EXPIRE) {
            return;
        }
        // 判断是否为永不过期
        if (timeout == NEVER_EXPIRE) {
            redisCache.setCacheObject(key, object);
        } else {
            if (redisCache.hasKey(key)) {
                redisCache.setCacheObject(key, object);
            } else {
                redisCache.setCacheObject(key, object, Long.valueOf(timeout).intValue(), TimeUnit.SECONDS);
            }
        }
//        CAFFEINE.invalidate(key);
    }

    /**
     * 更新Object (过期时间不变)
     */
    @Override
    public void updateObject(String key, Object object) {
        if (redisCache.hasKey(key)) {
            redisCache.setCacheObject(key, object);
//            CAFFEINE.invalidate(key);
        }
    }

    /**
     * 删除Object
     */
    @Override
    public void deleteObject(String key) {
        redisCache.deleteObject(key);
    }

    /**
     * 获取Object的剩余存活时间 (单位: 秒)
     */
    @Override
    public long getObjectTimeout(String key) {
        long timeout = redisCache.getTimeToLive(key);
        // 加1的目的 解决sa-token使用秒 redis是毫秒导致1秒的精度问题 手动补偿
        return timeout < 0 ? timeout : timeout / 1000 + 1;
    }

    /**
     * 修改Object的剩余存活时间 (单位: 秒)
     */
    @Override
    public void updateObjectTimeout(String key, long timeout) {
        redisCache.expire(key, timeout);
    }

    /**
     * 搜索数据
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        String keyStr = prefix + "*" + keyword + "*";
//        return (List<String>) CAFFEINE.get(keyStr, k -> {
//            Collection<String> keys = redisCache.keys(keyStr);
//            List<String> list = new ArrayList<>(keys);
//            return SaFoxUtil.searchList(list, start, size, sortType);
//        });


        Collection<String> keys = redisCache.keys(keyStr);
        List<String> list = new ArrayList<>(keys);
        return SaFoxUtil.searchList(list, start, size, sortType);
    }
}
