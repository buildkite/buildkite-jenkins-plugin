package io.jenkins.plugins.buildkite.step;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import hudson.security.ACL;
import io.jenkins.plugins.buildkite.apiclient.BuildkiteApiClient;
import io.jenkins.plugins.buildkite.apiclient.CreateBuildRequest;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

import java.io.PrintStream;

class BuildkiteStepExecution extends SynchronousNonBlockingStepExecution<Void> {
    private transient final BuildkiteStep step;

    protected BuildkiteStepExecution(@NonNull BuildkiteStep step, @NonNull StepContext context) {
        super(context);
        this.step = step;
    }

    @Override
    protected Void run() throws Exception {
        TaskListener listener = getContext().get(TaskListener.class);

        PrintStream console = listener.getLogger();

        console.println("Hello Buildkite!");
        console.println(String.format("Organization: %s", this.step.getOrganization()));
        console.println(String.format("Pipeline: %s", this.step.getPipeline()));
        console.println(String.format("Credentials Id: %s", this.step.getCredentialsId()));

        StringCredentials credentials = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentialsInItem(
                        StringCredentials.class,
                        null,
                        ACL.SYSTEM2
                ),
                CredentialsMatchers.withId(this.step.getCredentialsId())
        );

        var client = new BuildkiteApiClient(credentials.getSecret());

        var request = new CreateBuildRequest();
        request.setCommit("HEAD");
        request.setBranch("main");

        var build = client.createBuild(this.step.getOrganization(), this.step.getPipeline(), request);

        console.println("Build triggered");
        console.println(String.format("Id: %s", build.getId()));
        console.println(String.format("Number: %s", build.getNumber()));
        console.println(String.format("State: %s", build.getState()));
        console.println(String.format("Branch: %s", build.getBranch()));
        console.println(String.format("Commit: %s", build.getCommit()));
        console.println(String.format("Web Url: %s", build.getWebUrl()));

        return null;
    }
}
