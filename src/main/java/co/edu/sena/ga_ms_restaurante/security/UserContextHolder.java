package co.edu.sena.ga_ms_restaurante.security;

import java.util.UUID;

public class UserContextHolder {

    private static final ThreadLocal<UserContext> holder = new ThreadLocal<>();

    public static void setContext(UserContext context) {
        holder.set(context);
    }

    public static UserContext getContext() {
        return holder.get();
    }

    public static UUID getCurrentUserId() {
        UserContext ctx = holder.get();
        if (ctx == null) return null;
        return ctx.getUserId();
    }

    public static String getCurrentUserRole() {
        UserContext ctx = holder.get();
        if (ctx == null) return null;
        return ctx.getUserRole();
    }

    public static void clear() {
        holder.remove();
    }
}
