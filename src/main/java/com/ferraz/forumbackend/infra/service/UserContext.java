package com.ferraz.forumbackend.infra.service;

import com.ferraz.forumbackend.session.SessionEntity;
import com.ferraz.forumbackend.user.UserEntity;

public class UserContext {

    private static final String[] ANONYMOUS_USER_FEATURES = new String[]{"create:user"};

    private static final ThreadLocal<Boolean> isAnonymousSession = new ThreadLocal<>();
    private static final ThreadLocal<SessionEntity> currentSession = new ThreadLocal<>();

    public static void clear() {
        isAnonymousSession.remove();
        currentSession.remove();
    }

    public static void setSession(SessionEntity sessionEntity) {
        isAnonymousSession.set(sessionEntity == null);

        if (isAnonymousSession.get()) {
            UserEntity user = new UserEntity();
            user.setFeatures(ANONYMOUS_USER_FEATURES);
            sessionEntity = new SessionEntity();
            sessionEntity.setUser(user);
        }

        currentSession.set(sessionEntity);
    }

    public static UserEntity getUser() {
        return currentSession.get().getUser();
    }

    public static SessionEntity getSession() {
        return currentSession.get();
    }

    public static boolean isAnonymousSession() {
        return isAnonymousSession.get() != null ? isAnonymousSession.get() : false;
    }

}
