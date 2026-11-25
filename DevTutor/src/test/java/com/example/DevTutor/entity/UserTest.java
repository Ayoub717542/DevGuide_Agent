package com.example.DevTutor.entity;

import com.example.DevTutor.enums.Roles;
import com.example.DevTutor.enums.SubscriptionStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    private User userWith(SubscriptionStatus status) {
        User user = new User();
        user.setSubscriptionStatus(status);
        return user;
    }

    @Test
    void newUserIsFree() {
        assertFalse(new User().hasProAccess());
    }

    @Test
    void activeTrialingAndPastDueArePro() {
        assertTrue(userWith(SubscriptionStatus.ACTIVE).hasProAccess());
        assertTrue(userWith(SubscriptionStatus.TRIALING).hasProAccess());
        assertTrue(userWith(SubscriptionStatus.PAST_DUE).hasProAccess());
    }

    @Test
    void pausedAndCanceledAreFree() {
        assertFalse(userWith(SubscriptionStatus.PAUSED).hasProAccess());
        assertFalse(userWith(SubscriptionStatus.CANCELED).hasProAccess());
    }

    @Test
    void activeWithFutureCancelDateIsStillPro() {
        User user = userWith(SubscriptionStatus.ACTIVE);
        user.setCancelEffectiveAt(Instant.now().plus(Duration.ofDays(5)));
        assertTrue(user.hasProAccess());
    }

    @Test
    void activeWithPassedCancelDateIsFree() {
        User user = userWith(SubscriptionStatus.ACTIVE);
        user.setCancelEffectiveAt(Instant.now().minus(Duration.ofMinutes(1)));
        assertFalse(user.hasProAccess());
    }

    @Test
    void adminIsAlwaysPro() {
        User admin = new User();
        admin.setRole(Roles.ADMIN);
        assertTrue(admin.hasProAccess());
    }
}
