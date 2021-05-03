package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TokenCache {
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);   //声明日志

    public static final String TOKEN_PREFIX = "token_";

    /**
     * initialCapacity初始化容量为1000
     * maximumSize缓存的最大容量10000 当超过此容量本地缓存guava采用LRU算法（最小使用算法来移除缓存项）
     * expireAfterAccess()TimeUnit.HOURS小时制
     *
     */
    private static LoadingCache<String, String> localCache = CacheBuilder.newBuilder().initialCapacity(1000).
            maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS).build (new CacheLoader<String, String>() {
        //默认的加载实现,当调用get取值时,如果key没有对应的值，就调用这个方法进行加载
        @Override
        public String load(String s) throws Exception {
            return "null";
        }
    });

    //加入到本地缓存
    public static void setKey(String key,String value){
        localCache.put(key,value);
    }

    //从本地缓存中获取
    public static String getKey(String key){
         String value = null;
         try {
             value = localCache.get(key);
            if("null".equals(value)){
                return null;
            }
            return value;
         }catch (Exception ex){
             logger.error("localCache get error",ex.getMessage());
         }
         return null;
    }
}
