package ink.zfei.user;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import ink.zfei.user.bean.MallOperationLog;
import ink.zfei.user.mapper.MallOperationLogMapper;
import ink.zfei.util.GsonUtil;
import jodd.util.StringUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.*;

@Aspect
@Component
public class Advisor {
    @Resource
    private MallOperationLogMapper logMapper;

    @Resource
    private HttpSession session;

    @Resource
    private HttpServletRequest request;

    @Pointcut("execution(* ink.zfei.user.controller.*.*(..))")
    public void pointcut() {
        System.out.println("Pointcut");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        String paramStr = null;
        String resultStr = null;
        String ex = null;
        try {
            Object param = joinPoint.getArgs();
            paramStr = GsonUtil.Obj2JsonStr(param);
            System.out.println("around before" + paramStr);

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method mehthod = signature.getMethod();

            Auth auth = mehthod.getDeclaredAnnotation(Auth.class);
            if (auth != null) {

                if (auth.sign()) {

                    Map<String, String> params = getAllRequestParam(request);
//                    params.put("appId","lc91fa6e24ff4b4e99");
//                    params.put("uid","10023");
//                    params.put("mobile","1235654444");
                    String paramSign = params.remove("sign");
                    String time = params.get("time");
                    if (System.currentTimeMillis() - Long.parseLong(time) > 5 * 60 * 1000) {
                        throw new RuntimeException("请求过期");
                    }


                    String appSecret = getFromDbByAppId(params.get("appId"));
                    String sign = SecureUtil.signParamsMd5(params, appSecret);
                    if (!sign.equals(paramSign)) {
                        throw new RuntimeException("鉴权失败！sign错误");
                    }

                } else {
                    //校验session是否存在
                    String mobile = (String) session.getAttribute("mobile").toString();

                    if (mobile == null) {
                        throw new RuntimeException("未登录就访问");
                    }
                }

            }
            Object result = joinPoint.proceed();

            resultStr = GsonUtil.Obj2JsonStr(result);
            System.out.println("around after" + resultStr);

            return result;
        } catch (Throwable e) {
            ex = GsonUtil.Obj2JsonStr(e);
            System.out.println("around exception" + ex);
            //e.printStackTrace();
            throw e;
        } finally {
            System.out.println("around");
            MallOperationLog log = new MallOperationLog();
            log.setCreateTime(System.currentTimeMillis());
            log.setMethodName(joinPoint.getSignature().getName());
            log.setParam(paramStr);

            if (ex != null) {
                log.setResult(ex);
            } else {
                log.setResult(resultStr);
            }
            String mobile = App.mobileHolder.get();
            if (!StringUtil.isEmpty(mobile)) {
                log.setUid(Long.parseLong(mobile));
            }

            logMapper.insertSelective(log);


//            ex = null;
        }
    }

    private String getFromDbByAppId(String appId) {
        return "12459ac547434b3ea83db5e6d56789";
    }


    private Map<String, String> getAllRequestParam(final HttpServletRequest request) {
        Map<String, String> res = new HashMap<String, String>();
        Enumeration<?> temp = request.getParameterNames();
        if (null != temp) {
            while (temp.hasMoreElements()) {
                String en = (String) temp.nextElement();
                String value = request.getParameter(en);
                res.put(en, value);
                //如果字段的值为空，判断若值为空，则删除这个字段>
                if (null == res.get(en) || "".equals(res.get(en))) {
                    res.remove(en);
                }
            }
        }
        return res;
    }

}
