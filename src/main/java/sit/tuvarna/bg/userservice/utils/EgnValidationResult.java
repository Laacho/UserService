package sit.tuvarna.bg.userservice.utils;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class EgnValidationResult {
    private final boolean   valid;
    private final String    errorMessage;
    private final LocalDate birthDate;

    private EgnValidationResult(boolean valid, String errorMessage, LocalDate birthDate) {
        this.valid        = valid;
        this.errorMessage = errorMessage;
        this.birthDate    = birthDate;
    }
    static EgnValidationResult valid(LocalDate birthDate) {
        return new EgnValidationResult(true, null, birthDate);
    }
    static EgnValidationResult invalid(String errorMessage) {
        return new EgnValidationResult(false, errorMessage, null);
    }
}
