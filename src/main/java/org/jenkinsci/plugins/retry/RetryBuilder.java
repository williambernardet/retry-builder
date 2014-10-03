/*
 * The MIT License
 *
 * Copyright (c) 2014, William Bernardet
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.retry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.retry.conditions.Always;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Saveable;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.DescribableList;

/**
 * This builder allows user to configure sub-builder steps, and retry them in case of 
 * builder marks the build to stop, or in case of exception.
 *  
 * @author William Bernardet
 *
 */
public class RetryBuilder extends Builder {

    private DescribableList<Builder, Descriptor<Builder>> builders;
    private int numberOfRetry;
    private int timeout;
    private RetryConditionConfig retryConditionConfig;
    
    @DataBoundConstructor
    public RetryBuilder(int numberOfRetry, int timeout, RetryConditionConfig retryCondition) {
        this.numberOfRetry = numberOfRetry;
        this.timeout = timeout;
        setRetryCondition(retryCondition);
    }
    
    public List<Builder> getBuilders() {
        return getBuilderList().toList();
    }
    
    public synchronized DescribableList<Builder, Descriptor<Builder>> getBuilderList() {
        if (builders == null) {
            builders = new DescribableList<Builder, Descriptor<Builder>>(Saveable.NOOP);
        }
        return builders;
    }
    
    public int getNumberOfRetry() {
        return numberOfRetry;
    }

    public void setRetryCondition(RetryConditionConfig retryConditionConfig) {
        this.retryConditionConfig = retryConditionConfig;
    }
    
    public RetryConditionConfig getRetryConditionConfig() {
        if (this.retryConditionConfig == null) {
            this.retryConditionConfig = new Always();
        }
        return this.retryConditionConfig;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        for (Builder builder : this.getBuilderList()) {
            if (!builder.prebuild(build, listener)) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<? extends Action> getProjectActions(
            AbstractProject<?, ?> project) {
        List<Action> actions = new ArrayList<Action>();
        for (Builder builder : this.getBuilderList()) {
            actions.addAll(builder.getProjectActions(project));
        }
        return actions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
            BuildListener listener) throws InterruptedException, IOException {
        RetryCondition condition = getRetryConditionConfig().getRetryCondition();
        condition.configure(build, launcher, listener);
        boolean result = true;
        int retries = this.getNumberOfRetry() + 1; // will always be executed once...
        for (int i = 0 ; i < retries ; i++) {
            result = true;
            try {
                for (Builder builder : this.getBuilderList()) {
                    result = builder.perform(build, launcher, listener);
                    if (!result) {
                        boolean retry = condition.shallRetry(true, null);
                        listener.getLogger().println("Builder failed, retrying? " + retry);
                        if (!retry) {
                            return false;
                        }
                        break;
                    }
                }
                return true;
            } catch (IOException e) {
                boolean retry = condition.shallRetry(false, e);
                listener.getLogger().println("Builder thrown IOException, retrying? " + retry);
                if (!retry) {
                    throw e;
                }
            }
            
            // Waiting timeout between 2 trials
            listener.getLogger().println("Waiting " + this.getTimeout() + "ms");
            Thread.sleep(getTimeout());
        }
        return result;
    }


    @Extension    
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return "Retry";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder newInstance(StaplerRequest req, JSONObject formData)
                throws FormException {
            RetryBuilder builder = (RetryBuilder)super.newInstance(req, formData);
            try {
                builder.getBuilderList().rebuildHetero(req, formData, Builder.all(), "builders");
            } catch (IOException e) {
                throw new FormException("Error rebuilding builders list: " + e.getMessage(),
                        e, "builders");
            }
            return builder;
        }

        /**
         * Get the list of Builder descriptors.
         * @return
         */
        public DescriptorExtensionList<Builder, Descriptor<Builder>> getBuilderDescriptors() {
            return Builder.all();
        }

        /**
         * Get the list of RetryCondition descriptors. 
         * @return
         */
        public DescriptorExtensionList<RetryConditionConfig, Descriptor<RetryConditionConfig>> getRetryConditionDescriptors() {
            return Jenkins.getInstance().<RetryConditionConfig,Descriptor<RetryConditionConfig>>getDescriptorList(RetryConditionConfig.class);
        }
        
    }
    
}
