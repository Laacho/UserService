package sit.tuvarna.bg.userservice.addresses.model;

import java.util.Arrays;

public enum AddressType {
    PERMANENT("permanent"),CORRESPONDENCE("correspondence"),OTHER("other"),UNKNOWN("");
    private final String code;
    AddressType(String code){
        this.code = code;
    }

    public static AddressType getByCode(String code){
        return Arrays.stream(AddressType.values())
                .filter(e->e.code.equals(code))
                .findFirst()
                .orElse(UNKNOWN);
    }

}
