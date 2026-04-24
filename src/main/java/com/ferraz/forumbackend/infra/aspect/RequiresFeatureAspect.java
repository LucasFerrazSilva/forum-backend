package com.ferraz.forumbackend.infra.aspect;

import com.ferraz.forumbackend.infra.annotation.RequiresFeature;
import com.ferraz.forumbackend.infra.exception.ForbiddenException;
import com.ferraz.forumbackend.infra.exception.UnauthorizedException;
import com.ferraz.forumbackend.infra.service.AuthorizationService;
import com.ferraz.forumbackend.infra.service.UserContext;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class RequiresFeatureAspect {

    private final AuthorizationService authorizationService;

    @Before("@annotation(requiresFeature)")
    public void checkFeature(RequiresFeature requiresFeature) {
        if (UserContext.isAnonymousSession()) {
            throw new UnauthorizedException();
        }

        if (!authorizationService.loggedUserCan(requiresFeature.value())) {
            throw new ForbiddenException();
        }
    }
}
