package sit.tuvarna.bg.userservice.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import sit.tuvarna.bg.userservice.web.dto.request.IssueRequest;
import sit.tuvarna.bg.userservice.web.dto.response.TokenPairResponse;

@FeignClient(name = "auth-service", url = "${services.auth.url}")
public interface AuthClient {
    @PostMapping("/issue")
    TokenPairResponse issue(@RequestBody IssueRequest request);
}
