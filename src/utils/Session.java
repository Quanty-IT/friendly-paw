package utils;

import modules.User.models.User;

public class Session {
    private static User currentUser;

    public static void set(User user) { currentUser = user; }
    public static User get() { return currentUser; }
    public static boolean isLoggedIn() { return currentUser != null; }
    public static void clear() { currentUser = null; }
}
