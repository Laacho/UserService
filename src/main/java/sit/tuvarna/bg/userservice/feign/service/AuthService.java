package sit.tuvarna.bg.userservice.feign.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.tuvarna.bg.userservice.aop.Loggable;
import sit.tuvarna.bg.userservice.feign.client.AuthClient;
import sit.tuvarna.bg.userservice.user.model.User;
import sit.tuvarna.bg.userservice.web.dto.request.IssueRequest;
import sit.tuvarna.bg.userservice.web.dto.response.TokenPairResponse;

import java.util.Set;

@Service
public class AuthService {

    private final AuthClient authClient;

    @Autowired
    public AuthService(AuthClient authClient) {
        this.authClient = authClient;
    }

    @Loggable
    public TokenPairResponse issueTokens(User user) {

        IssueRequest request = IssueRequest.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .roles(Set.of(user.getRole().name()))
                .build();

        return authClient.issue(request);
    }
}
