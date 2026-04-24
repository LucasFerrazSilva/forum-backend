package com.ferraz.forumbackend.infra.aspect;

import com.ferraz.forumbackend.infra.annotation.RequiresFeature;
import com.ferraz.forumbackend.infra.exception.ForbiddenException;
import com.ferraz.forumbackend.infra.service.UserContext;
import com.ferraz.forumbackend.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class RequiresFeatureAspect {

    @Before("@annotation(requiresFeature)")
    public void checkFeature(RequiresFeature requiresFeature) {
        String requiredFeature = requiresFeature.value();
        UserEntity user = UserContext.getUser();

        if (!Arrays.asList(user.getFeatures()).contains(requiredFeature)) {
            throw new ForbiddenException();
        }
    }
}
