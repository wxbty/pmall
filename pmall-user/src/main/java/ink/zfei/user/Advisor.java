package ink.zfei.user;

import com.google.gson.Gson;
import ink.zfei.user.bean.MallOperationLog;
import ink.zfei.user.mapper.MallOperationLogMapper;
import ink.zfei.util.GsonUtil;
import jodd.util.StringUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Aspect
@Component
public class Advisor {
    @Resource
    private MallOperationLogMapper logMapper;

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

}
