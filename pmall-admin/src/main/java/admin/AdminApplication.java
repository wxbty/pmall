package admin;

import ink.zfei.domain.servlet.CorsFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class AdminApplication {

    public static ThreadLocal<String> mobileHolder = new ThreadLocal<>();

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class);

    }

}


