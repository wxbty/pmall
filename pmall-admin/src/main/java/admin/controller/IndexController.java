package admin.controller;


import admin.bean.Admin;
import admin.bo.AdminAuthenticationBO;
import admin.bo.OAuth2AccessTokenBO;
import admin.controller.vo.UsersBannerVO;
import admin.mapper.AdminMapper;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import ink.zfei.domain.CommonResult;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ink.zfei.domain.CommonResult.success;

@NacosPropertySource(dataId = "ink.zfei.user", autoRefreshed = true)
@RequestMapping("/admin-api/admins")
@Controller
public class IndexController {

    @NacosValue(value = "${code.max:5}", autoRefreshed = true)
    private int max;

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private RedissonClient redissonClient;


    @ResponseBody
    @RequestMapping(value = "/passport/login", produces = "application/json;charset=UTF-8")
    public CommonResult<AdminAuthenticationBO> login(String username, String password, String type) {

        AdminAuthenticationBO bo = new AdminAuthenticationBO();

        Admin admin = adminMapper.selectByUserName(username);
        if (admin == null) {
            return CommonResult.error(-100, "账号不存在！");
        }
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(admin.getPassword())) {
            return CommonResult.error(-101, "密码错误！");
        }
        BeanUtils.copyProperties(admin, bo);
        OAuth2AccessTokenBO tokenBO = new OAuth2AccessTokenBO();
        tokenBO.setAccessToken("xxxxxxx");
        tokenBO.setRefreshToken("xxxxxxx11");
        tokenBO.setExpiresIn(11000);
        bo.setToken(tokenBO);
        return success(bo);
    }

    @GetMapping("/url_resource_list")
//    @ApiOperation(value = "获得当前登陆的管理员拥有的 URL 权限列表")
    public CommonResult<Set<String>> urlResourceList() {
//        List<ResourceBO> resources = resourceService.getResourcesByTypeAndRoleIds(ResourceConstants.TYPE_BUTTON, AdminSecurityContextHolder.getContext().getRoleIds());
        Set<String> set = new HashSet<>();
        return success(set);
    }

    public static void main(String[] args) {
        String password = "123456";
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        System.out.println(password);
    }

}
