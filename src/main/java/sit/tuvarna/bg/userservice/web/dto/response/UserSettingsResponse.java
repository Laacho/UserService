package sit.tuvarna.bg.userservice.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sit.tuvarna.bg.userservice.userSettings.model.TwoFactorMethod;

@Getter
@Setter
@Builder
public class UserSettingsResponse {
    private boolean emailNotificationsEnabled;
    private boolean internalNotificationsEnabled;
    private boolean twoFactorEnabled;
    private TwoFactorMethod twoFactorMethod;
}

