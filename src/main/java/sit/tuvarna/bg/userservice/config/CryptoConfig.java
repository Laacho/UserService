package sit.tuvarna.bg.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class CryptoConfig {

    @Value("${encryption.secret-key-base64}")
    private String secretKeyBase64;

    @Bean
    public SecretKey aesSecretKey() {
        byte[] keyBytes  = Base64.getDecoder().decode(secretKeyBase64);
        if(keyBytes.length != 32) {
            throw new IllegalArgumentException("Secret key must be 256 bits (32 bytes)");
        }
        return new SecretKeySpec(keyBytes, "AES");
    }
}
