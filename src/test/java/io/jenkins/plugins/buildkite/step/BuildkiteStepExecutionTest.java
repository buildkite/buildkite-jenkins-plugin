package io.jenkins.plugins.buildkite.step;

import hudson.model.TaskListener;
import io.jenkins.plugins.buildkite.api_client.BuildkiteBuild;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.PrintStream;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BuildkiteStepExecutionTest {

    private BuildkiteStep step;
    @Mock
    private StepContext mockContext;
    @Mock
    private TaskListener mockListener;
    @Mock
    private PrintStream mockConsole;

    private BuildkiteStepExecution stepExecution;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(mockContext.get(TaskListener.class)).thenReturn(mockListener);
        when(mockListener.getLogger()).thenReturn(mockConsole);

        step = new BuildkiteStep("test-org", "test-pipeline", "test-creds");
        step.setBranch("main");
        step.setCommit("HEAD");
        step.setMessage("Test message");
        step.setAsync(false);

        stepExecution = new BuildkiteStepExecution(step, mockContext);
    }

    @Test
    void printCreatingBuild_outputsCorrectMessage() throws Exception {
        Method method = BuildkiteStepExecution.class.getDeclaredMethod("printCreatingBuild", PrintStream.class);
        method.setAccessible(true);

        method.invoke(stepExecution, mockConsole);

        verify(mockConsole).println("Creating build for test-org/test-pipeline on main (HEAD)");
    }

    @Test
    void printBuildCreated_outputsCorrectMessage() throws Exception {
        Method method = BuildkiteStepExecution.class.getDeclaredMethod(
                "printBuildCreated",
                BuildkiteBuild.class,
                PrintStream.class
        );
        method.setAccessible(true);

        BuildkiteBuild build = new BuildkiteBuild()
                .setNumber(123)
                .setWebUrl("https://buildkite.com/test-org/test-pipeline/builds/123");

        method.invoke(stepExecution, build, mockConsole);

        verify(mockConsole).println("test-org/test-pipeline#123 created: https://buildkite.com/test-org/test-pipeline/builds/123");
    }

    @Test
    void printBuildFinished_outputsCorrectMessage() throws Exception {
        Method method = BuildkiteStepExecution.class.getDeclaredMethod("printBuildFinished",
                BuildkiteBuild.class, PrintStream.class);
        method.setAccessible(true);

        BuildkiteBuild build =
                new BuildkiteBuild()
                        .setNumber(456)
                        .setState("passed");

        method.invoke(stepExecution, build, mockConsole);

        verify(mockConsole).println("test-org/test-pipeline#456 finished with state: passed");
    }

    @Test
    void constructor_createsInstanceWithStepAndContext() {
        assertNotNull(stepExecution);
        // Constructor functionality is verified by successful creation
    }
}
