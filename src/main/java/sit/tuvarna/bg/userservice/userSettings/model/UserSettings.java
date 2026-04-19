package sit.tuvarna.bg.userservice.userSettings.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sit.tuvarna.bg.userservice.user.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private boolean emailNotificationEnabled;
    private boolean internalNotificationEnabled;

    private boolean twoFactorEnabled;

    @Enumerated(EnumType.STRING)
    private TwoFactorMethod twoFactorMethod;

    @Column(name = "two_factor_secret")
    private String twoFactorSecret; // encrypted

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
