package com.salesforce.rundeck.plugin.output;

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
        if (StringUtils.isNotEmpty(out)) {
            standardOutput.add(out);
        }
    }

    public void addError(String err) {
        if (StringUtils.isNotEmpty(err)) {
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
