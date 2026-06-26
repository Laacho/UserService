package sit.tuvarna.bg.userservice.web.dto.request;

import lombok.Getter;
import lombok.Setter;
import sit.tuvarna.bg.userservice.userSettings.model.TwoFactorMethod;

@Getter
@Setter
public class UserSettingsUpdateRequest {
    // Nullable by design: a null field means "leave unchanged" (partial update).
    private Boolean emailNotificationsEnabled;
    private Boolean internalNotificationsEnabled;
    private Boolean twoFactorEnabled;
    private TwoFactorMethod twoFactorMethod;
}
