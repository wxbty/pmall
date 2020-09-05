package ink.zfei.user;

import ink.zfei.domain.servlet.CorsFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SpringBootApplication
@EnableWebMvc
public class App implements WebMvcConfigurer {

    public static ThreadLocal<String> mobileHolder = new ThreadLocal<>();

    public static void main(String[] args) {
        SpringApplication.run(App.class);

    }




    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册TestInterceptor拦截器
        InterceptorRegistration registration = registry.addInterceptor(new AdminInterceptor());
        registration.addPathPatterns("/**");                      //所有路径都被拦截

    }

    class AdminInterceptor implements HandlerInterceptor {

        /**
         * 在请求处理之前进行调用（Controller方法调用之前）
         */
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//        System.out.println("执行了TestInterceptor的preHandle方法");

            //统一拦截（查询当前session是否存在user）(这里user会在每次登陆成功后，写入session)
            String mobile = (String) request.getSession().getAttribute("mobile");
            mobileHolder.set(mobile);
            return true;//如果设置为false时，被请求时，拦截器执行到此处将不会继续操作
            //如果设置为true时，请求将会继续执行后面的操作



        }
    }
}


