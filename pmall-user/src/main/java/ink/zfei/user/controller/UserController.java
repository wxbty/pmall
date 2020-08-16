package ink.zfei.user.controller;


import com.google.gson.Gson;
import ink.zfei.domain.Result;
import ink.zfei.user.mapper.MallUserMapper;
import ink.zfei.user.service.MallUserService;
import ink.zfei.util.GsonUtil;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequestMapping("/user-api/users")
@Controller
public class UserController {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private MallUserService mallUserService;

    private static final String PREX_CODE = "MALL_CODE_";

    @ResponseBody
    @RequestMapping(value = "/passport/mobile/send_register_code", produces = "text/plain;charset=UTF-8")
    public String sendRegisterCode(String mobile) {

        //1、验证上次操作是否超过一分钟，查找key=mobile记录，如存在，返回错误码操作太频繁
        RBucket rBucket = redissonClient.getBucket(PREX_CODE + mobile);
        if (rBucket.isExists()) {
            Result result = new Result();
            result.setCode(-100);
            result.setMessage("操作太频繁");
            return GsonUtil.Obj2JsonStr(result);
        } else {
            rBucket.getAndSet(1, 1L, TimeUnit.MINUTES);
        }


        //2、生成4位随机验证码

        //3、存入redis，有效期30分钟

        //4、redis还需要存入code，有效期30分钟

        return GsonUtil.Obj2JsonStr(Result.sucess());
    }

}
