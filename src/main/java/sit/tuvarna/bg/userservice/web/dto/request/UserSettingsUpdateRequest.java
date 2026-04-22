package sit.tuvarna.bg.userservice.web.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import sit.tuvarna.bg.userservice.userSettings.model.TwoFactorMethod;

@Getter
@Setter
public class UserSettingsUpdateRequest {
    @NotNull(message = "Email notifications enabled flag is required")
    private Boolean emailNotificationsEnabled;
    @NotNull(message = "Internal notifications enabled flag is required")
    private Boolean internalNotificationsEnabled;
    @NotNull(message = "Two factor enabled flag is required")
    private Boolean twoFactorEnabled;
    private TwoFactorMethod twoFactorMethod;
}
