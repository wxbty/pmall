package admin.controller;


import admin.bean.Admin;
import admin.bean.AdminRole;
import admin.bean.RoleResource;
import admin.bo.AdminAuthenticationBO;
import admin.bo.OAuth2AccessTokenBO;
import admin.controller.vo.AdminInfo;
import admin.controller.vo.AdminMenuTreeNodeVO;
import admin.controller.vo.UsersBannerVO;
import admin.mapper.AdminMapper;
import admin.mapper.AdminRoleMapper;
import admin.mapper.ResourceMapper;
import admin.mapper.RoleResourceMapper;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
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
import java.util.*;
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
    private AdminRoleMapper adminRoleMapper;

    @Resource
    private RoleResourceMapper roleResourceMapper;

    @Resource
    private ResourceMapper resourceMapper;

    @Resource
    private HttpSession session;

    public static final String USER_INFO = "userinfo";

    public IndexController() {
    }


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

        AdminRole adminRole = adminRoleMapper.selectByUid(admin.getId());
        AdminInfo adminInfo = new AdminInfo();
        adminInfo.setAdmin(admin);
        adminInfo.setAdminRole(adminRole);

        session.setAttribute(USER_INFO, adminInfo);

        BeanUtils.copyProperties(admin, bo);
        OAuth2AccessTokenBO tokenBO = new OAuth2AccessTokenBO();
        tokenBO.setAccessToken("xxxxxxx");
        tokenBO.setRefreshToken("xxxxxxx11");
        tokenBO.setExpiresIn(11000);
        bo.setToken(tokenBO);
        return success(bo);
    }

    @ResponseBody
    @RequestMapping(value = "/admin/url_resource_list", produces = "application/json;charset=UTF-8")
//    @ApiOperation(value = "获得当前登陆的管理员拥有的 URL 权限列表")
    public CommonResult<Set<String>> urlResourceList() {
//        List<ResourceBO> resources = resourceService.getResourcesByTypeAndRoleIds(ResourceConstants.TYPE_BUTTON, AdminSecurityContextHolder.getContext().getRoleIds());
        Set<String> set = new HashSet<>();
        //查询当前用户，url类别的resource 列表
        AdminInfo adminInfo = (AdminInfo) session.getAttribute(USER_INFO);
        if (adminInfo.getAdminRole() != null) {
            int roleId = adminInfo.getAdminRole().getRoleId();
            List<RoleResource> list = roleResourceMapper.selectByRoleId(roleId);
            List<Integer> ids = list.stream().map(RoleResource::getResourceId).collect(Collectors.toList());
            set = ids.stream().map(id -> resourceMapper.selectByPrimaryKey(id).getHandler()).collect(Collectors.toSet());
        }

        return success(set);
    }

    @ResponseBody
    @RequestMapping(value = "/admin/menu_resource_tree", produces = "application/json;charset=UTF-8")
//  @ApiOperation(value = "获得当前登陆的管理员拥有的菜单权限", notes = "以树结构返回")
    public CommonResult<List<AdminMenuTreeNodeVO>> menuResourceTree() {
        AdminInfo adminInfo = (AdminInfo) session.getAttribute(USER_INFO);
        List<AdminMenuTreeNodeVO> adminMenuTreeNodeVOList = new ArrayList<>();
        List<AdminMenuTreeNodeVO> resultList = new ArrayList<>();
        if (adminInfo.getAdminRole() != null) {
            int roleId = adminInfo.getAdminRole().getRoleId();
            List<RoleResource> list = roleResourceMapper.selectByRoleId(roleId);
            //拥有的所有resourceID
            List<Integer> ids = list.stream().map(RoleResource::getResourceId).collect(Collectors.toList());
            List<admin.bean.Resource> resourceList = ids.stream().map(id -> resourceMapper.selectByPrimaryKey(id)).collect(Collectors.toList());

            for (admin.bean.Resource resource : resourceList) {
                AdminMenuTreeNodeVO adminMenuTreeNodeVO = new AdminMenuTreeNodeVO();
                adminMenuTreeNodeVO.setId(resource.getId());
                adminMenuTreeNodeVO.setDisplayName(resource.getDisplayName());
                adminMenuTreeNodeVO.setHandler(resource.getHandler());
                adminMenuTreeNodeVO.setPid(resource.getPid());
                adminMenuTreeNodeVO.setSort(resource.getSort());
                adminMenuTreeNodeVO.setChildren(new ArrayList<>());

                adminMenuTreeNodeVOList.add(adminMenuTreeNodeVO);
            }
            //
            adminMenuTreeNodeVOList.sort(Comparator.comparingInt(AdminMenuTreeNodeVO::getSort));

            for (AdminMenuTreeNodeVO adminMenuTreeNodeVO : adminMenuTreeNodeVOList) {
                for (AdminMenuTreeNodeVO adminMenuTreeNodeVO2 : adminMenuTreeNodeVOList) {
                    if (adminMenuTreeNodeVO.getId().equals(adminMenuTreeNodeVO2.getId())) {
                        continue;
                    }
                    if (adminMenuTreeNodeVO.getPid().equals(adminMenuTreeNodeVO2.getId())) {
                        adminMenuTreeNodeVO2.getChildren().add(adminMenuTreeNodeVO);
                    }
                }
            }

            for (AdminMenuTreeNodeVO adminMenuTreeNodeVO : adminMenuTreeNodeVOList) {
                if (adminMenuTreeNodeVO.getPid().equals(0)) {
                    resultList.add(adminMenuTreeNodeVO);
                }
            }
        }

        return success(resultList);
    }


//    @ResponseBody
//    @RequestMapping(value = "/data_dict/tree", produces = "application/json;charset=UTF-8")
////    @ApiOperation(value = "数据字典树结构", notes = "该接口返回的信息更为精简。一般用于前端缓存数据字典到本地。")
//    public CommonResult<List<DataDictEnumVO>> tree() {
//        // 查询数据字典全列表
//        List<DataDictBO> dataDicts = dataDictService.selectDataDictList();
//        // 构建基于 enumValue 聚合的 Multimap
//        ImmutableListMultimap<String, DataDictBO> dataDictMap = Multimaps.index(dataDicts, DataDictBO::getEnumValue); // KEY 是 enumValue ，VALUE 是 DataDictBO 数组
//        // 构建返回结果
//        List<DataDictEnumVO> dataDictEnumVOs = new ArrayList<>(dataDictMap.size());
//        dataDictMap.keys().forEach(enumValue -> {
//            DataDictEnumVO dataDictEnumVO = new DataDictEnumVO().setEnumValue(enumValue)
//                    .setValues(DataDictConvert.INSTANCE.convert2(dataDictMap.get(enumValue)));
//            dataDictEnumVOs.add(dataDictEnumVO);
//        });
//        return success(dataDictEnumVOs);
//    }

    public static void main(String[] args) {
        String password = "123456";
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        System.out.println(password);
    }

}
