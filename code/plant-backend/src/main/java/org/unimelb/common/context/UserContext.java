package org.unimelb.common.context;

import org.unimelb.user.entity.User;

public class UserContext {
    private static final ThreadLocal<User> userHolder = new ThreadLocal<>();

    public static void setUser(User user) {
        userHolder.set(user);
    }

    public static User getCurrentUser() {
        return userHolder.get();
    }

    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public static void clear() {
        userHolder.remove();
    }
}