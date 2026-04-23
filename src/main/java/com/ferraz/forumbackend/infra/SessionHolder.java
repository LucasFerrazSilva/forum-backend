package com.ferraz.forumbackend.infra;

import com.ferraz.forumbackend.session.SessionEntity;
import com.ferraz.forumbackend.user.UserEntity;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@Getter
public class SessionHolder {

    private static final String[] ANONYMOUS_USER_FEATURES =
            new String[]{"read:activation_token", "create:session", "create:user"};

    private boolean isAnonymousSession;
    private SessionEntity session;

    public void setSession(SessionEntity sessionEntity) {
        this.isAnonymousSession = sessionEntity == null;

        if (this.isAnonymousSession) {
            UserEntity user = new UserEntity();
            user.setFeatures(ANONYMOUS_USER_FEATURES);
            sessionEntity = new SessionEntity();
            sessionEntity.setUser(user);
        }

        this.session = sessionEntity;
    }

    public UserEntity getUser() {
        return session.getUser();
    }

}
