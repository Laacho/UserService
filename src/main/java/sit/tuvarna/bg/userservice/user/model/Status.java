package sit.tuvarna.bg.userservice.user.model;

import java.util.Arrays;

public enum Status {
    ACTIVE("active"),DISABLED("disabled"),PENDING_VERIFICATION("pending_verification"),UNKNOWN("");
    private final String code;
    Status(String code){
        this.code = code;
    }

    public static Status getByCode(String code){
        return Arrays.stream(Status.values())
                .filter(e->e.code.equals(code))
                .findFirst()
                .orElse(UNKNOWN);
    }

}
