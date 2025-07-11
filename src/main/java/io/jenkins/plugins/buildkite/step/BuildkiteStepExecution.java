package io.jenkins.plugins.buildkite.step;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.security.ACL;
import io.jenkins.plugins.buildkite.apiclient.BuildkiteApiClient;
import io.jenkins.plugins.buildkite.apiclient.BuildkiteBuild;
import io.jenkins.plugins.buildkite.apiclient.CreateBuildRequest;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

import java.io.PrintStream;

public class BuildkiteStepExecution extends SynchronousNonBlockingStepExecution<Void> {
    private transient final BuildkiteStep step;

    public BuildkiteStepExecution(@NonNull BuildkiteStep step, @NonNull StepContext context) {
        super(context);
        this.step = step;
    }

    @Override
    protected Void run() throws Exception {
        TaskListener listener = getContext().get(TaskListener.class);
        PrintStream console = listener.getLogger();

        printHello(console);

        StringCredentials credentials = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentialsInItem(
                        StringCredentials.class,
                        null,
                        ACL.SYSTEM2
                ),
                CredentialsMatchers.withId(this.step.getCredentialsId())
        );

        if (credentials == null) {
            var errorMessage = String.format("Could not find Credentials with id: %s", this.step.getCredentialsId());
            console.println(errorMessage);

            this.getContext().onFailure(new FlowInterruptedException(Result.FAILURE));

            return null;
        }

        var client = new BuildkiteApiClient(credentials.getSecret());
        var request = new CreateBuildRequest();

        // TODO: parameterise this
        request.setCommit("HEAD");
        request.setBranch("main");

        BuildkiteBuild build = client.createBuild(
                this.step.getOrganization(),
                this.step.getPipeline(),
                request
        );

        printBuildTriggered(build, console);

        Thread.sleep(2000);

        BuildkiteBuild buildCheck = null;
        while (buildCheck == null || !buildCheck.buildFinished()) {
            buildCheck = client.getBuild(
                    this.step.getOrganization(),
                    this.step.getPipeline(),
                    build.getNumber()
            );
            console.println(String.format("State: %s", buildCheck.getState()));

            Thread.sleep(5000);
        }

        if (!buildCheck.buildPassed()) {
            printBuildFailed(buildCheck, console);
            this.getContext().onFailure(new FlowInterruptedException(Result.FAILURE));
        } else {
            printBuildPassed(buildCheck, console);
            this.getContext().onSuccess(build);
        }

        return null;
    }

    private void printHello(PrintStream console) {
        console.println("Hello Buildkite!");
        console.println(String.format("Organization: %s", this.step.getOrganization()));
        console.println(String.format("Pipeline: %s", this.step.getPipeline()));
        console.println(String.format("Credentials Id: %s", this.step.getCredentialsId()));
    }

    private void printBuildTriggered(BuildkiteBuild build, PrintStream console) {
        console.println("");
        console.println("Build triggered");
        console.println(String.format("Id: %s", build.getId()));
        console.println(String.format("Number: %s", build.getNumber()));
        console.println(String.format("State: %s", build.getState()));
        console.println(String.format("Branch: %s", build.getBranch()));
        console.println(String.format("Commit: %s", build.getCommit()));
        console.println(String.format("Web Url: %s", build.getWebUrl()));
    }

    private void printBuildFailed(BuildkiteBuild build, PrintStream console) {
        var message = String.format(
                "Buildkite build %s/%s#%s failed with state: %s",
                this.step.getOrganization(),
                this.step.getPipeline(),
                build.getNumber(),
                build.getState()
        );

        console.println("");
        console.println(message);
    }

    private void printBuildPassed(BuildkiteBuild build, PrintStream console) {
        var message = String.format(
                "Buildkite build %s/%s#%s passed",
                this.step.getOrganization(),
                this.step.getPipeline(),
                build.getNumber()
        );

        console.println("");
        console.println(message);
    }
}
