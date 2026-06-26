package sit.tuvarna.bg.userservice.user.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sit.tuvarna.bg.userservice.aop.Loggable;
import sit.tuvarna.bg.userservice.exception.*;
import sit.tuvarna.bg.userservice.feign.service.AuthService;
import sit.tuvarna.bg.userservice.kafka.UserEventPublisher;
import sit.tuvarna.bg.userservice.kafka.event.*;
import sit.tuvarna.bg.userservice.user.model.Roles;
import sit.tuvarna.bg.userservice.user.model.Status;
import sit.tuvarna.bg.userservice.user.model.User;
import sit.tuvarna.bg.userservice.user.repository.UserRepository;
import sit.tuvarna.bg.userservice.utils.EgnValidationResult;
import sit.tuvarna.bg.userservice.utils.EgnValidator;
import sit.tuvarna.bg.userservice.utils.JwtValidator;
import sit.tuvarna.bg.userservice.web.dto.request.ChangePasswordRequest;
import sit.tuvarna.bg.userservice.web.dto.request.LoginRequest;
import sit.tuvarna.bg.userservice.web.dto.request.RegisterRequest;
import sit.tuvarna.bg.userservice.web.dto.response.TokenPairResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final JwtValidator jwtValidator;
    private final UserEventPublisher eventPublisher;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthService authService, JwtValidator jwtValidator, UserEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.jwtValidator = jwtValidator;
        this.eventPublisher = eventPublisher;
    }

    @Loggable
    public TokenPairResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username '" + request.getUsername() + "' already exists");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        EgnValidationResult result = EgnValidator.validate(request.getEgn());
        if (!result.isValid()) {
            throw new InvalidOperationException("Invalid EGN provided");
        }
        String hashedEgn = passwordEncoder.encode(request.getEgn());
        LocalDate birthDate = result.getBirthDate();

        User user = User.builder()
                .firstName(request.getFirstName())
                .secondName(request.getSecondName())
                .thirdName(request.getThirdName())
                .username(request.getUsername())
                .password(hashedPassword)
                .egn(hashedEgn)
                .role(Roles.CLIENT)
                .email(request.getEmail())
                .status(Status.PENDING_VERIFICATION)
                .birthDate(birthDate)
                .phoneNumber(request.getPhoneNumber())
                .lastLogin(LocalDate.now())
                .lastPasswordChange(LocalDate.now())
                .tokensValidFrom(java.time.Instant.now())
                .build();

        User saved = userRepository.save(user);

        eventPublisher.publish(
                "user.registered",
                saved.getId().toString(),
                new UserRegisteredPayload(
                        saved.getId(),
                        saved.getUsername(),
                        saved.getEmail(),
                        saved.getRole().name()
                ));

        return authService.issueTokens(saved);
    }

    @Loggable
    public TokenPairResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user == null) {
            eventPublisher.publish("user.login-failed",
                    request.getUsername(),
                    new UserLoginFailedPayload(null, request.getUsername(), "USER_NOT_FOUND", null));
            throw new AuthenticationException("Invalid credentials");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            eventPublisher.publish("user.login-failed",
                    user.getId().toString(),
                    new UserLoginFailedPayload(user.getId(), request.getUsername(), "INVALID_PASSWORD", null));
            throw new AuthenticationException("Invalid credentials");
        }
        user.setLastLogin(LocalDate.now());
        userRepository.save(user);

        eventPublisher.publish("user.logged-in",
                user.getId().toString(),
                new UserLoggedInPayload(user.getId(), java.time.Instant.now().toString()));

        return authService.issueTokens(user);
    }
    /**
     * Changes the user's password after verifying the current one, then bumps
     * {@code tokensValidFrom} so every refresh token issued before now is rejected
     * (invalidates all other sessions). Returns a fresh token pair so the caller's
     * current session keeps working.
     */
    @Loggable
    public TokenPairResponse changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username '" + username + "' not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidOperationException("New password must differ from the old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setLastPasswordChange(LocalDate.now());
        user.setTokensValidFrom(java.time.Instant.now());
        userRepository.save(user);

        eventPublisher.publish("user.password-changed",
                user.getId().toString(),
                new UserLoggedInPayload(user.getId(), java.time.Instant.now().toString()));

        // fresh pair (iat >= tokensValidFrom) so THIS session survives the gate
        return authService.issueTokens(user);
    }

    @Loggable
    public Instant getTokensValidFrom(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id '" + userId + "' not found"));
        return user.getTokensValidFrom();
    }

    @Loggable
    public UUID resolveUserId(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username '" + username + "' not found"));
        return user.getId();
    }
    @Loggable
    public void linkAccount(UUID userId, UUID accountId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id '" + userId + "' not found"));

        List<UUID> accountIds = user.getAccountIds();
        if (!accountIds.contains(accountId)) {
            accountIds.add(accountId);
        }
        userRepository.save(user);
    }

    @Loggable
    public List<UUID> getAllAccounts(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new InvalidOperationException("Invalid or missing Authorization header");
        }
        String token = header.substring(7);
        UUID userId = jwtValidator.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id '" + userId + "' not found"));
        return user.getAccountIds();

    }

    @Loggable
    public boolean validateAccount(UUID userId, UUID accountId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            //log user not found
            log.error("User with id '{}' not found when validating account '{}'", userId, accountId);
            return false;
        }
        User user = optionalUser.get();
        List<UUID> accountIds = user.getAccountIds();
        return accountIds.stream()
                .anyMatch(id -> id.equals(accountId));

    }

    @Loggable
    public void deleteAccount(UUID userId, UUID accountId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id '" + userId + "' not found"));
        List<UUID> accountIds = user.getAccountIds();
        accountIds.removeIf(id -> id.equals(accountId));
        userRepository.save(user);
    }

    @Loggable
    public boolean checkIfUserExists(UUID userId) {
    return userRepository.existsById(userId);
    }

    /**
     * Sets a target user's role (promote or demote). Admin-only at the web layer.
     * An admin cannot change their own role — prevents accidental self-lockout.
     *
     * @param actingUserId  the authenticated admin performing the change
     * @param targetUserId  the user whose role is being changed
     * @param newRole       the role to assign (must not be null/UNKNOWN)
     */
    @Loggable
    public User updateRole(UUID actingUserId, UUID targetUserId, Roles newRole) {
        if (newRole == null || newRole == Roles.UNKNOWN) {
            throw new InvalidOperationException("Invalid role");
        }
        if (actingUserId.equals(targetUserId)) {
            throw new InvalidOperationException("You cannot change your own role");
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException("User with id '" + targetUserId + "' not found"));

        Roles oldRole = user.getRole();
        user.setRole(newRole);
        User saved = userRepository.save(user);

        log.info("Admin {} changed role of user {} from {} to {}", actingUserId, targetUserId, oldRole, newRole);
        return saved;
    }
}