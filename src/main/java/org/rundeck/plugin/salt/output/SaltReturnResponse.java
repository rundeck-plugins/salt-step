/**
 * Copyright (c) 2013, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.rundeck.plugin.salt.output;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

/**
 * Represents a salt minion response.
 */
public class SaltReturnResponse {
    protected Integer exitCode;
    protected final List<String> standardOutput = Lists.newLinkedList();
    protected final List<String> standardError = Lists.newLinkedList();

    public void addOutput(String out) {
        if (StringUtils.isNotBlank(out)) {
            standardOutput.add(out);
        }
    }

    public void addError(String err) {
        if (StringUtils.isNotBlank(err)) {
            standardError.add(err);
        }
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public List<String> getStandardOutput() {
        return Collections.unmodifiableList(standardOutput);
    }

    public List<String> getStandardError() {
        return Collections.unmodifiableList(standardError);
    }

    public boolean isSuccessful() {
        return exitCode != null && exitCode == 0;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    @Override
    public String toString() {
        return "SaltReturnResponse [exitCode=" + exitCode + ", standardOutput=" + standardOutput + ", standardError="
                + standardError + "]";
    }
}
