package ink.zfei.user.controller;


import cn.hutool.crypto.SecureUtil;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.google.common.collect.Maps;
import ink.zfei.user.Auth;
import ink.zfei.user.bean.MallUser;
import ink.zfei.user.mapper.MallUserMapper;
import ink.zfei.user.service.MallUserService;
import ink.zfei.user.util.LoginContext;
import ink.zfei.domain.CommonResult;
import ink.zfei.user.vo.OAuth2AccessTokenBO;
import ink.zfei.user.vo.UserAuthenticationBO;
import ink.zfei.user.vo.UsersUserVO;
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

import static ink.zfei.domain.CommonResult.success;

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
    public static final String USERINFO = "userInfo";

    @Auth
    @ResponseBody
    @RequestMapping(value = "/index", produces = "text/plain;charset=UTF-8")
    public String index(String mobile) {
        //用户信息
        MallUser mallUser = new MallUser();
        mallUser.setMobile(mobile);
        Object result = mallUserMapper.select(mobile);

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
        System.out.println("code=" + code);
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
    @RequestMapping(value = "/passport/mobile/register", produces = "application/json;charset=UTF-8")
    public CommonResult<UserAuthenticationBO> registerAndLogin(String mobile, String code) {
        if (code == null || code.length() != 4) {
            return CommonResult.error(-102, "验证码格式错误");
        }
        RBucket rBucket_save = redissonClient.getBucket(PREX_CODE_SAVE + mobile);
        if (!rBucket_save.isExists()) {
            return CommonResult.error(-103, "请重新发送验证码");
        }
        String password = (String) rBucket_save.get();
        if (!password.equals(code)) {
            return CommonResult.error(-104, "验证码错误");
        }

        //判断手机号是否已注册
        MallUser mallUser = mallUserMapper.select(mobile);
        if (mallUser == null) {
            //注册表里插入
            mallUser = new MallUser();
            mallUser.setMobile(mobile);
            mallUser.setName(mobile);
            mallUser.setCreateTime(System.currentTimeMillis());
            mallUser.setUpdateTime(System.currentTimeMillis());
            String ram = UUID.randomUUID().toString();
            mallUser.setAccessToken(ram.substring(0,4)+ram.substring(5,8));
            mallUserMapper.insert(mallUser);
        }
        //插入操作表

        OAuth2AccessTokenBO tokenBo = new OAuth2AccessTokenBO();
        tokenBo.setAccessToken(mallUser.getAccessToken());
        tokenBo.setRefreshToken("refresh_token1");
        tokenBo.setExpiresIn(100000);

        UserAuthenticationBO authenticationBO = new UserAuthenticationBO();
        authenticationBO.setToken(tokenBo);
        authenticationBO.setId(mallUser.getId());
        authenticationBO.setNickname(mallUser.getMobile());

        LoginContext.put(authenticationBO);

        return success(authenticationBO);
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
        if (mallUserMapper.select(mobile) == null) {
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
        String sign = SecureUtil.signParamsMd5(params, appSecret);
        System.out.println(sign);
//        params.put("sign",sign);
    }

    @Auth
    @ResponseBody
    @RequestMapping(value = "/user/info", produces = "application/json;charset=UTF-8")
    public CommonResult<UsersUserVO> info() {
        MallUser mallUser = mallUserMapper.select(LoginContext.mobile());
        UsersUserVO userResult = new UsersUserVO();
        userResult.setId(mallUser.getId());
        userResult.setMobile(mallUser.getMobile());
        userResult.setAvatar("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1599301985909&di=cbe2d3944388227d6d3b351d8f8cb20d&imgtype=0&src=http%3A%2F%2Fimg.article.pchome.net%2F00%2F25%2F65%2F20%2Fpic_lib%2Fs960x639%2FTiger_3002s960x639.jpg");
        userResult.setNickname(mallUser.getName());
        return success(userResult);
    }
}
