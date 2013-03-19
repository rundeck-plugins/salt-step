#Rundeck Salt-Step Plugin
=========================

##Description

A Rundeck NodeStepPlugin that allows Rundeck to delegate tasks to a Salt master, either by issuing a salt command 
on the same host as Rundeck is running or by executing the request over salt-api.

##Build / Deploy

- To build the project from source issue: `./gradlew clean build`
- The resulting jar files can be found under build/libs. 
- Copy the rundeck-salt-plugin-<version>.jar file to your $RDECK_BASE/libext folder
- Restart Rundeck
- You should now have two additional "salt local" and "salt remote" options when configuring jobs

##Configuration

- Rundeck node resource IDs MUST match salt minion IDs

##Usage

The following two job-level params should be configured to provide authentication input fields:
- SALT_USER - standard input field, required.
- SALT_PASSWORD - secure input field, required. *NOTE this should *not* be secure remote authentication*

In addition:
- Workflow configuration must be set to dispatch to nodes.

###Remote execution over salt-api

*NOTE: This plugin leverages salt-api, which requires it's own additional setup. For more information on how to setup salt-api please refer to it's documentation which can be found here: http://salt-api.readthedocs.org/en/latest/* 

The remote execution salt plugin provides three properties which need to be configured for the step:

- SALT_API_END_POINT: the URL of the salt-api endpoint, ie: http://saltmaster:8000
- Function: the function to be passed to salt-api call, minus the target 
-- For example, if you entered `test.ping` for the function value, the resulting salt call would be `salt <yourHostName> test.ping` this field would simply contain `test.ping`. The target will always default to the hostname where Rundeck is running.
- Eauth: the authentication mechanism salt-api should use
-- This would be the equivalent to the -a parameter being passed on the command line, for example `salt -a pam <target> test.ping`

##Troubleshooting

- Make sure that your salt-api setup is fully functional before attempting to execute jobs with this plugin
- Setting the job output level to debug will print out the raw JSON commands that are being sent as well as the returned output

##Setting up salt return response parsers
===================
The salt-step plugins interact with salt and salt-api requesting json payloads as output by default. Salt-step needs to be configured in order to parse this output and behave correctly with respect to exit codes, standard output, and standard error. Salt-step is configured through yaml configuration files.

###YAML Configuration File Format
```
handlerMappings:
  <salt module>[.<salt function>]: <java object implementing com.salesforce.rundeck.plugin.output.SaltReturnHandler>
```

Salt-step is configured in 2 different places:
* ```src/main/resources/defaultReturners.yaml```
* In rundeck-config.properties, the _saltStep.returnHandlers_ property allows for a comma separated list of additional configuration files.