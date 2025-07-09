package io.jenkins.plugins.buildkite;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;

@Extension
public class GlobalConfiguration extends jenkins.model.GlobalConfiguration {
    private String baseUrl;
    private String apiKey;

    public static GlobalConfiguration get() {
        return ExtensionList.lookupSingleton(GlobalConfiguration.class);
    }

    public GlobalConfiguration() {
        load();
    }

    @Override
    public boolean configure(StaplerRequest2 req, JSONObject json) {
        req.bindJSON(this, json);
        save();
        return true;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    @DataBoundSetter
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

//    public FormValidation doCheckLabel(@QueryParameter String value) {
//        if (StringUtils.isEmpty(value)) {
//            return FormValidation.warning("Please specify a label.");
//        }
//        return FormValidation.ok();
//    }
}