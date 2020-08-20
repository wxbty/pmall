package ink.zfei.user;

import ink.zfei.user.bean.MallUser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class App {


    public static void main(String[] args) {
        SpringApplication.run(App.class);

    }

}
