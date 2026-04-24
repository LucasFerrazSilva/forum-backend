package com.ferraz.forumbackend.infra.service;

import com.ferraz.forumbackend.user.UserEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AuthorizationService {

    public boolean loggedUserCan(String requiredFeature) {
        return can(UserContext.getUser(), requiredFeature);
    }

    public boolean can(UserEntity user, String requiredFeature) {
        return user != null && Arrays.asList(user.getFeatures()).contains(requiredFeature);
    }

}
