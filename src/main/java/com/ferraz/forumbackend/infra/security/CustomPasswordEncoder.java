package com.ferraz.forumbackend.infra.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomPasswordEncoder implements PasswordEncoder {

    private final PasswordEncoder passwordEncoder;
    private final String pepper;

    public CustomPasswordEncoder(@Value("${security.pepper}") String pepper) {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.pepper = pepper;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return passwordEncoder.encode(getPasswordWithPepper(rawPassword));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return passwordEncoder.matches(getPasswordWithPepper(rawPassword), encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return passwordEncoder.upgradeEncoding(encodedPassword);
    }

    private String getPasswordWithPepper(CharSequence rawPassword) {
        return rawPassword + pepper;
    }

}
