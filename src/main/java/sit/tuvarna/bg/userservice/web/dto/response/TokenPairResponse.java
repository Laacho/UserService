package sit.tuvarna.bg.userservice.web.dto.response;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenPairResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long accessExpiresIn;   // seconds
    private long refreshExpiresIn;  // seconds
}
