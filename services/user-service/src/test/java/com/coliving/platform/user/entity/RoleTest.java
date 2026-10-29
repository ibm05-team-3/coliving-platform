package com.coliving.platform.user.entity;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleTest {

    @Test
    void accountRolesAreMemberCandidateResidentAndAdmin() {
        assertEquals(Role.USER, Role.defaultAccountRole());
        assertFalse(Role.GUEST.isAccountRole());
        assertEquals(
                Arrays.asList(Role.USER, Role.CANDIDATE, Role.RESIDENT, Role.ADMIN),
                Arrays.stream(Role.values()).filter(Role::isAccountRole).toList()
        );
        assertTrue(Role.USER.isAccountRole());
        assertTrue(Role.CANDIDATE.isAccountRole());
        assertTrue(Role.RESIDENT.isAccountRole());
        assertTrue(Role.ADMIN.isAccountRole());
    }
}
