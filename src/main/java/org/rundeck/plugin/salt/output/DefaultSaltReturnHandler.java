package org.rundeck.plugin.salt.output;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * An implementation of SaltReturnHandler that returns an always successful exit code and
 * the raw response in standard out.
 */
@Component
public class DefaultSaltReturnHandler implements SaltReturnHandler {
    
    protected Integer exitCode;
    
    public DefaultSaltReturnHandler() {
        this(0);
    }
    
    @Autowired
    public DefaultSaltReturnHandler(@Value("${defaultSaltReturner.exitCode}") Integer exitCode) {
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
