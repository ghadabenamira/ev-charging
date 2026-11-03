package com.team.evcharging;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Proves that Maven and JUnit 5 are set up correctly.
 * Delete this file once real tests exist.
 */
class SmokeTest {

    @Test
    void mainClassIsLoadable() {
        assertNotNull(new Main());
    }
}
