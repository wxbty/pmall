package ink.zfei.user.controller;


import ink.zfei.user.mapper.MallUserMapper;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@RequestMapping("/index")
@Controller
public class IndexController {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private MallUserMapper mallUserMapper;


    @ResponseBody
    @RequestMapping(value = "/test", produces = "text/plain;charset=UTF-8")
    public String index() {

        return "hello pmall";
    }

}
