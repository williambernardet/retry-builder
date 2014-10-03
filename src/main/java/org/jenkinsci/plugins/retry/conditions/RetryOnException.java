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
package org.jenkinsci.plugins.retry.conditions;

import hudson.Extension;
import hudson.model.Descriptor;

import org.jenkinsci.plugins.retry.RetryCondition;
import org.jenkinsci.plugins.retry.RetryConditionConfig;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Allow builders to be re-run if an exception of a certain 
 * type is raised.
 * @author William Bernardet
 *
 */
public class RetryOnException extends RetryConditionConfig {
    
    private String exception;
    @DataBoundConstructor
    public RetryOnException(String exception) {
        this.exception = exception;
    }
    
    
    public String getException() {
        if (exception == null) {
            exception = Exception.class.getCanonicalName();
        }
        return exception;
    }
    
    @Override
    public RetryCondition getRetryCondition() {
        
        return new RetryCondition() {
            @Override
            public boolean shallRetry(boolean hasError, Exception exc) {
                if (exc != null) {
                    try {
                        Class<?> clazz = Class.forName(RetryOnException.this.getException());
                        return clazz.isInstance(exc);
                    } catch (ClassNotFoundException e) {
                        return false;
                    }
                }
                return false;
            }
        };
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<RetryConditionConfig> {

        @Override
        public String getDisplayName() {
            return "Retry on Exception";
        }
        
    }

}
