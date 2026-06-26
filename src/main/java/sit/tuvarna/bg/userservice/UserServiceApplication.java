package sit.tuvarna.bg.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import sit.tuvarna.bg.userservice.config.KafkaTopicsProperties;

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties(KafkaTopicsProperties.class)
public class UserServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
}