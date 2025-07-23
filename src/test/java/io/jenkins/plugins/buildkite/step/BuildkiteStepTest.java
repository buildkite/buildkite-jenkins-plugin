package io.jenkins.plugins.buildkite.step;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuildkiteStepTest {

    private BuildkiteStep step;

    @BeforeEach
    void setUp() {
        step = new BuildkiteStep("my-org", "my-pipeline", "creds-id");
    }

    @Test
    void constructor_setsRequiredFields() {
        assertEquals("my-org", step.getOrganization());
        assertEquals("my-pipeline", step.getPipeline());
        assertEquals("creds-id", step.getCredentialsId());
    }

    @Test
    void constructor_setsDefaults() {
        assertEquals("main", step.getBranch());
        assertEquals("HEAD", step.getCommit());
        assertFalse(step.isAsync());
        assertNull(step.getMessage());
    }

    @Test
    void setBranch_updatesValue() {
        step.setBranch("feature-branch");
        assertEquals("feature-branch", step.getBranch());
    }

    @Test
    void setBranch_ignoresNullValue() {
        step.setBranch(null);
        assertEquals("main", step.getBranch());
    }

    @Test
    void setBranch_ignoresEmptyValue() {
        step.setBranch("");
        assertEquals("main", step.getBranch());
    }

    @Test
    void setBranch_ignoresWhitespaceValue() {
        step.setBranch("   ");
        assertEquals("main", step.getBranch());
    }

    @Test
    void setCommit_updatesValue() {
        step.setCommit("abc123");
        assertEquals("abc123", step.getCommit());
    }

    @Test
    void setCommit_ignoresNullValue() {
        step.setCommit(null);
        assertEquals("HEAD", step.getCommit());
    }

    @Test
    void setCommit_ignoresEmptyValue() {
        step.setCommit("");
        assertEquals("HEAD", step.getCommit());
    }

    @Test
    void setCommit_ignoresWhitespaceValue() {
        step.setCommit("   ");
        assertEquals("HEAD", step.getCommit());
    }

    @Test
    void setMessage_updatesValue() {
        step.setMessage("Custom message");
        assertEquals("Custom message", step.getMessage());
    }

    @Test
    void setMessage_allowsNullValue() {
        step.setMessage("Custom message");
        step.setMessage(null);
        assertEquals("Custom message", step.getMessage());
    }

    @Test
    void setAsync_updatesValue() {
        step.setAsync(true);
        assertTrue(step.isAsync());

        step.setAsync(false);
        assertFalse(step.isAsync());
    }

    @Test
    void start_withCustomMessage_usesCustomMessage() {
        String customMessage = "Custom build message";
        step.setMessage(customMessage);

        assertEquals(customMessage, step.getMessage());
    }
}
