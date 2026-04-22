package sit.tuvarna.bg.userservice.config;

import org.springframework.stereotype.Service;
import sit.tuvarna.bg.userservice.aop.Loggable;
import sit.tuvarna.bg.userservice.exception.InvalidOperationException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();


    public EncryptionService(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    @Loggable
    public String encrypt(String plainText) {
        try{
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] ciphertext = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // prepend IV to ciphertext+tag
            byte[] ivAndCiphertext = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, ivAndCiphertext, 0, iv.length);
            System.arraycopy(ciphertext, 0, ivAndCiphertext, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(ivAndCiphertext);
        } catch (Exception e) {
            throw new InvalidOperationException("Encryption failed", e);
        }
    }
    @Loggable
    public String decrypt(String base64IvAndCiphertext) {
        try {
            byte[] ivAndCiphertext = Base64.getDecoder().decode(base64IvAndCiphertext);

            if (ivAndCiphertext.length < IV_LENGTH_BYTES + 16) {
                throw new InvalidOperationException("Invalid encrypted payload");
            }

            byte[] iv = new byte[IV_LENGTH_BYTES];
            byte[] ciphertext = new byte[ivAndCiphertext.length - IV_LENGTH_BYTES];

            System.arraycopy(ivAndCiphertext, 0, iv, 0, IV_LENGTH_BYTES);
            System.arraycopy(ivAndCiphertext, IV_LENGTH_BYTES, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] plaintextBytes = cipher.doFinal(ciphertext);
            return new String(plaintextBytes, StandardCharsets.UTF_8);
        } catch (InvalidOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidOperationException("Decryption failed", e);
        }
    }
}
