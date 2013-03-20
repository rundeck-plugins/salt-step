#Rundeck Salt-Step Plugin
=========================

##Description

A Rundeck NodeStepPlugin that allows Rundeck to delegate tasks to a Salt master, either by issuing a salt command 
on the same host as Rundeck is running or by executing

##Build / Deploy

- To build the project from source issue: `./gradlew clean build`
- You will then find the resulting jar files under build/libs. 
- Copy the rundeck-salt-plugin-<version>.jar file to your $RDECK_BASE/libext folder
- Restart Rundeck

##Configuration

###Local execution on the salt master

The local salt excution plugin will use the salt binary installed on the same host as Rundeck, and will therefore 
inherit the host's configuration. There are three properties that must be set with a local execution job:

- The location of the salt binary (default: /usr/bin/salt)
- The function to be passed to the salt binary, minus the target 
-- For example, if you entered `test.ping` for the function value, the resulting salt call would be `salt <yourHostName> test.ping` this field would simply contain `test.ping`. The target will always default to the hostname where Rundeck is running.
- The location of salt configuration (default: /etc/salt) 

###Remote execution over salt-api

*NOTE: This plugin leverages salt-api, which requires it's own additional setup. For more information on how to setup salt-api
please refer to it's documentation which can be found here: http://salt-api.readthedocs.org/en/latest/ *

The remote execution salt plugin provides three properties which need to be configured for the step:

- SALT_API_END_POINT: the URL of the salt-api endpoint, ie: http://saltmaster:8000
- Function: the function to be passed to salt-api call, minus the target 
-- For example, if you entered `test.ping` for the function value, the resulting salt call would be `salt <yourHostName> test.ping` this field would simply contain `test.ping`. The target will always default to the hostname where Rundeck is running.
- Eauth: the authentication mechanism salt-api should use
-- This would be the equivalent to the -a parameter being passed on the command line, for example `salt -a pam <target> test.ping`

In addition, the following two job-level params should be configured to provide authentication input fields:
- SALT_USER - standard input field, required.
- SALT_PASSWORD - secure input field, required. *note this should **not** be secure remote authentication*

