package cr.ac.tec.cvexpert.usuarios.model;

import com.fasterxml.jackson.annotation.JsonValue;

/** Rol del usuario. En JSON viaja en minúscula: {@code user} o {@code admin}. */
public enum Role {
    USER("user"),
    ADMIN("admin");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
