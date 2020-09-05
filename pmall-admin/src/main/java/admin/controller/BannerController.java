package admin.controller;


import admin.controller.vo.UsersBannerVO;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import ink.zfei.domain.CommonResult;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

import static ink.zfei.domain.CommonResult.success;

@NacosPropertySource(dataId = "ink.zfei.user", autoRefreshed = true)
@RequestMapping("/promotion-api/users")
@Controller
public class BannerController {

    @NacosValue(value = "${code.max:5}", autoRefreshed = true)
    private int max;

    @Resource
    private HttpSession session;

    @Resource
    private RedissonClient redissonClient;


    @ResponseBody
    @RequestMapping(value = "/banner/list", produces = "application/json;charset=UTF-8")
    public CommonResult<List<UsersBannerVO>> registerAndLogin(String mobile, String code) {

        List<UsersBannerVO> voList = new ArrayList<>();
        return success(voList);
    }


}
