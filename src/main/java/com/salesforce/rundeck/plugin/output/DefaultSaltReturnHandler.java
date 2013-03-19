package com.salesforce.rundeck.plugin.output;

/**
 * An implementation of SaltReturnHandler that returns an always successful exit code and
 * the raw response in standard out.
 */
public class DefaultSaltReturnHandler implements SaltReturnHandler {
    
    protected Integer exitCode;
    
    public DefaultSaltReturnHandler() {
        this(0);
    }
    
    public DefaultSaltReturnHandler(Integer exitCode) {
        setExitCode(exitCode);
    }
    
    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }
    
    @Override
    public SaltReturnResponse extractResponse(String rawResponse) throws SaltReturnResponseParseException {
        SaltReturnResponse response = new SaltReturnResponse();
        response.setExitCode(exitCode);
        if (rawResponse != null) {
            response.addOutput(rawResponse);
        }
        return response;
    }
}
