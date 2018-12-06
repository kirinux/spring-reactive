package infrastructure;

import org.craftedsw.katas.reactive.domain.User;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 *
 *
 */
@Configuration
@ComponentScan(basePackages = "org.craftedsw.katas.reactive")
public class AppTestConfig {

    public static final String DEFAULT_USER_NAME_1 = "default1";
    public static final String DEFAULT_USER_NAME_2 = "default2";
    public static final User DEFAULT_USER_1 = new User(DEFAULT_USER_NAME_1);
    public static final User DEFAULT_USER_2 = new User(DEFAULT_USER_NAME_2);
}
