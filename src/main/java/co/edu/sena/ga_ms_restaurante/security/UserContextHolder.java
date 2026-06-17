package co.edu.sena.ga_ms_restaurante.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

public class UserContextHolder {

    public static UUID getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr != null) {
                return UUID.fromString(userIdStr);
            }
        }
        // Retorno por defecto si falla la extracción (solo para evitar crasheos si se prueba sin token en rutas abiertas)
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    public static String getCurrentUserRole() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String rol = (String) request.getAttribute("nombreRol");
            if (rol != null) {
                return rol;
            }
        }
        return "MESERO";
    }

    public static void clear() {
        // Método mantenido vacío para no romper la compatibilidad con el resto del código.
        // Spring y la librería de seguridad ahora manejan el ciclo de vida de la petición.
    }
}