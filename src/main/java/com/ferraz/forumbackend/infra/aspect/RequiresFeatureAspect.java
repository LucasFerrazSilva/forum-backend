package com.ferraz.forumbackend.infra.aspect;

import com.ferraz.forumbackend.infra.annotation.RequiresFeature;
import com.ferraz.forumbackend.infra.exception.ForbiddenException;
import com.ferraz.forumbackend.infra.service.AuthorizationService;
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
        if (!authorizationService.loggedUserCan(requiresFeature.value())) {
            throw new ForbiddenException();
        }
    }
}
