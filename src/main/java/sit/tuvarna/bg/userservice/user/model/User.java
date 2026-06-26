package sit.tuvarna.bg.userservice.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sit.tuvarna.bg.userservice.addresses.model.Address;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "first_name")
    private String firstName;
    @Column(nullable = false,name = "second_name")
    private String secondName;
    @Column(nullable = false,name = "third_name")
    private String thirdName;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    private String egn; //must be hashed

    @Enumerated(EnumType.STRING)
    private Roles role;

    @Column(unique = true)
    private String email; //not sure may remove

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "phone_number")
    private String phoneNumber;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL,orphanRemoval = true)
    private Address address;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_account_ids", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "account_id")
    @Builder.Default
    private List<UUID> accountIds = new ArrayList<>();

    @Column(name = "last_login")
    private LocalDate lastLogin;

    @Column(name = "last_password_change")
    private LocalDate lastPasswordChange;

    // Refresh tokens issued before this instant are rejected on refresh.
    // Bumped on password change to invalidate all other sessions.
    @Column(name = "tokens_valid_from")
    private Instant tokensValidFrom;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
