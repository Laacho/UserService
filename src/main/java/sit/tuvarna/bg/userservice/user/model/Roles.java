package sit.tuvarna.bg.userservice.user.model;

import java.util.Arrays;

public enum Roles {
    CLIENT("client"),STAFF("staff"),ADMIN("admin"),UNKNOWN("");
    private final String code;
    Roles(String code){
        this.code = code;
    }

  public static Roles getByCode(String code){
      return   Arrays.stream(Roles.values())
              .filter(e->e.code.equals(code))
              .findFirst()
              .orElse(UNKNOWN);
  }

}
