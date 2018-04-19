package za.co.absa.subatomic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
@EnableCaching
public class SubatomicConfigServerApplication {

    public static void main(final String[] args) {
        SpringApplication.run(SubatomicConfigServerApplication.class, args);
    }
}
