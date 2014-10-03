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

import jenkins.model.Jenkins;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;


public abstract class RetryConditionConfig implements Describable<RetryConditionConfig>, ExtensionPoint {

    public abstract RetryCondition getRetryCondition();
    
    public Descriptor<RetryConditionConfig> getDescriptor() {
        return Jenkins.getInstance().getDescriptorOrDie(getClass());
    }
    
    public static DescriptorExtensionList<RetryConditionConfig, Descriptor<RetryConditionConfig>> all() {
        return Jenkins.getInstance().<RetryConditionConfig, Descriptor<RetryConditionConfig>>getDescriptorList(RetryConditionConfig.class);
    }

}
