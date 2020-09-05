package ink.zfei.user.util;

import ink.zfei.user.vo.UserAuthenticationBO;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.HashMap;
import java.util.Map;

public class LoginContext {

    public static ThreadLocal<Integer> localUid = new ThreadLocal<>();
    public static ThreadLocal<String> localMobile = new ThreadLocal<>();
    public final static String SESSION_INFO = "pmall_session_";

    public static Map<String, UserAuthenticationBO> userInfos = new HashMap<>();

    public static Integer uid() {
        return localUid.get();
    }

    public static String mobile() {
        return localMobile.get();
    }

    public static void put(UserAuthenticationBO user) {
//        userInfos.put(user.getToken().getAccessToken(), user);
        RedissonClient redissonClient = (RedissonClient) SpringBeanUtil.getBean(RedissonClient.class);
        RMap<String, UserAuthenticationBO> map = redissonClient.getMap(SESSION_INFO);
        map.put(user.getToken().getAccessToken(), user);
        //放到redis
    }

    public static UserAuthenticationBO get(String token) {
        //redis中取
        RedissonClient redissonClient = (RedissonClient) SpringBeanUtil.getBean(RedissonClient.class);
        RMap<String, UserAuthenticationBO> map = redissonClient.getMap(SESSION_INFO);
        return map.get(token);
    }

}
