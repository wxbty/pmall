package ink.zfei.user.controller;


import cn.hutool.crypto.SecureUtil;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySources;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import ink.zfei.domain.Result;
import ink.zfei.user.Auth;
import ink.zfei.user.bean.MallUser;
import ink.zfei.user.mapper.MallUserMapper;
import ink.zfei.user.service.MallUserService;
import ink.zfei.util.GsonUtil;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@NacosPropertySource(dataId = "ink.zfei.user", autoRefreshed = true)
@RequestMapping("/user-api/users")
@Controller
public class UserController {

    @NacosValue(value = "${code.max:5}", autoRefreshed = true)
    private int max;

    @Resource
    private HttpSession session;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private MallUserMapper mallUserMapper;
    @Resource
    private MallUserService mallUserService;

    private static final String PREX_CODE_CHECK = "PREX_CODE_CHECK_";
    private static final String PREX_CODE_SAVE = "PREX_CODE_SAVE_";
    private static final String PREX_CODE_SUM = "PREX_CODE_SUM_";

    @Auth
    @ResponseBody
    @RequestMapping(value = "/index", produces = "text/plain;charset=UTF-8")
    public String index(String mobile) {
        //用户信息
        MallUser mallUser = new MallUser();
        mallUser.setMobile(mobile);
        Object result = mallUserMapper.select(mallUser);

        return GsonUtil.Obj2JsonStr(result);
    }

    @ResponseBody
    @RequestMapping(value = "/info", produces = "text/plain;charset=UTF-8")
    public String info(String mobile) {
        if ("123".equals(mobile)) {
            throw new RuntimeException("手机号码不对");
        }
        return GsonUtil.Obj2JsonStr(Result.success());
    }

    @ResponseBody
    @RequestMapping(value = "/passport/mobile/send_register_code", produces = "text/plain;charset=UTF-8")
    public String sendRegisterCode(String mobile) {

        //1、验证上次操作是否超过一分钟，查找key=mobile记录，如存在，返回错误码操作太频繁
        RBucket rBucket_check = redissonClient.getBucket(PREX_CODE_CHECK + mobile);
        if (rBucket_check.isExists()) {
            Result result = new Result();
            result.setStatus(-100);
            result.setMessage("操作太频繁");
            return GsonUtil.Obj2JsonStr(result);
        }

        //System.out.println(max);
        //有没有超过5次
        RBucket rBucket_sum = redissonClient.getBucket(PREX_CODE_SUM + mobile);
        int sum = 0;
        if (rBucket_sum.isExists()) {
            sum = (Integer) rBucket_sum.get();
            if (sum >= max) {
                Result result = new Result();
                result.setStatus(-101);
                result.setMessage("今日次数已达上限");
                return GsonUtil.Obj2JsonStr(result);
            }
        }
        //2、生成4位随机验证码
        String code = UUID.randomUUID().toString().substring(0, 4);
        //3、存入redis，有效期30分钟
        RBucket rBucket_save = redissonClient.getBucket(PREX_CODE_SAVE + mobile);
        rBucket_save.set(code, 30L, TimeUnit.MINUTES);
        //4、redis还需要存入code，有效期1分钟
        rBucket_check.set(code, 1L, TimeUnit.MINUTES);

//        rBucket_sum.set(sum + 1, 1L, TimeUnit.DAYS);
        rBucket_sum.set(sum + 1, getRemainSecondsOneDay(new Date()), TimeUnit.MINUTES);
        return GsonUtil.Obj2JsonStr(Result.success());
    }

    @ResponseBody
    @RequestMapping(value = "/passport/mobile/register", produces = "text/plain;charset=UTF-8")
    public String regist(String mobile, String code) {
        if (code == null || code.length() != 4) {
            Result result = new Result();
            result.setStatus(-102);
            result.setMessage("验证码格式错误");
            return GsonUtil.Obj2JsonStr(result);
        }
        RBucket rBucket_save = redissonClient.getBucket(PREX_CODE_SAVE + mobile);
        if (!rBucket_save.isExists()) {
            Result result = new Result();
            result.setStatus(-103);
            result.setMessage("请重新发送验证码");
            return GsonUtil.Obj2JsonStr(result);
        }
        String password = (String) rBucket_save.get();
        if (!password.equals(code)) {
            Result result = new Result();
            result.setStatus(-104);
            result.setMessage("验证码错误");
            return GsonUtil.Obj2JsonStr(result);
        }
        MallUser mallUser = new MallUser();
        mallUser.setMobile(mobile);
        //判断手机号是否已注册
        if (mallUserMapper.select(mallUser) == null) {
            //注册表里插入
            mallUser.setCreateTime(System.currentTimeMillis());
            mallUser.setUpdateTime(System.currentTimeMillis());
            mallUserMapper.insert(mallUser);
        }
//        else {
//            //更新update_time
//            mallUser.setUpdateTime(System.currentTimeMillis());
//            mallUserMapper.update(mallUser);
//        }
        else {
            Result result = new Result();
            result.setStatus(-105);
            result.setMessage("手机号已注册");
            return GsonUtil.Obj2JsonStr(result);
        }
        //插入操作表


        return GsonUtil.Obj2JsonStr(mallUser);
    }

    @ResponseBody
    @RequestMapping(value = "/passport/mobile/login", produces = "text/plain;charset=UTF-8")
    public String login(String mobile, String code) {
        session.setAttribute("mobile", mobile);

        if (code == null || code.length() != 4) {
            Result result = new Result();
            result.setStatus(-102);
            result.setMessage("验证码格式错误");
            return GsonUtil.Obj2JsonStr(result);
        }
        RBucket rBucket_save = redissonClient.getBucket(PREX_CODE_SAVE + mobile);
        if (!rBucket_save.isExists()) {
            Result result = new Result();
            result.setStatus(-103);
            result.setMessage("请重新发送验证码");
            return GsonUtil.Obj2JsonStr(result);
        }
        String password = (String) rBucket_save.get();
        if (!password.equals(code)) {
            Result result = new Result();
            result.setStatus(-104);
            result.setMessage("验证码错误");
            return GsonUtil.Obj2JsonStr(result);
        }
        MallUser mallUser = new MallUser();
        mallUser.setMobile(mobile);
        //判断手机号是否已注册
        if (mallUserMapper.select(mallUser) == null) {
            Result result = new Result();
            result.setStatus(-105);
            result.setMessage("手机号未注册");
            return GsonUtil.Obj2JsonStr(result);
        }
        //操作表插入


        return GsonUtil.Obj2JsonStr(mallUser);
    }


    public static long getRemainSecondsOneDay(Date currentDate) {
        LocalDateTime midnight = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault()).plusDays(1).withHour(0).withMinute(0)
                .withSecond(0).withNano(0);
        LocalDateTime currentDateTime = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault());
        long seconds = ChronoUnit.MINUTES.between(currentDateTime, midnight);
        return seconds;
    }

    @Auth(sign = true)
    @ResponseBody
    @RequestMapping(value = "/out/open", produces = "text/plain;charset=UTF-8")
    public String outopen(String mobile, String code) {


        return " GsonUtil.Obj2JsonStr(mallUser)";
    }


    public static void main(String[] args) {
        Map<String, String> params = Maps.newHashMap();
        params.put("appId", "lc91fa6e24ff4b4e99");
        params.put("uid", "10023");
        params.put("mobile", "1235654444");
        String appSecret = "12459ac547434b3ea83db5e6d56789";
        String sign = SecureUtil.signParamsMd5(params,appSecret);
        System.out.println(sign);
//        params.put("sign",sign);
    }
}
