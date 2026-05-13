package co.edu.sena.ga_ms_restaurante.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserContext {
    private final UUID userId;
    private final String userRole;
}
