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

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

/**
 * Extends this class to implement the condition implementation.
 * This class can be use to store context between retry, as it is created
 * when the RetryBuilder starts; and discarded afterwards.
 * @author William Bernardet
 *
 */
public abstract class RetryCondition {

    /**
     * This method is called before the initial loop actually starts.
     * It allows the condition to configure itself, and store initial context
     * if it requires it.
     * @param build
     * @param launcher
     * @param listener
     */
    public void configure(AbstractBuild<?, ?> build, Launcher launcher,
            BuildListener listener) {
    }

    /**
     * If the builder is failing on error then hasError is true. If the builder fails on an Exception
     * then e is not null.
     * @param hasError error has occurred.
     * @param e exception has been thrown
     * @return
     */
    public boolean shallRetry(boolean hasError, Exception e) {
        return false;
    }

}
