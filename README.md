Rundeck Salt-Step Plugin
=========================

## Description

A Rundeck <a href="http://rundeck.org/docs/developer/workflow-step-plugin-development.html#workflow-node-step-plugin">NodeStepPlugin</a> that allows Rundeck to delegate tasks to a Salt master by executing the request over <a href="https://github.com/saltstack/salt-api">salt-api</a>.

## Build / Deploy

- To build the project from source, issue: `./gradlew clean build`
- The resulting jar files can be found under `build/libs`. 
- Copy the `rundeck-salt-plugin-<version>.jar` file to your `$RDECK_BASE/libext` folder
- Restart Rundeck
- You should now have an additional "salt remote" option when configuring jobs

## Configuration

- Rundeck node resource IDs *MUST* match salt minion IDs

## Usage

The following job-level params must be configured to provide authentication input fields:

- `SALT_USER` - standard input field, required.
- `SALT_PASSWORD` - secure input field, required. *NOTE the option should *not* be set as a secure remote authentication option*

Additionally:
- Workflow configuration must be set to dispatch to nodes.

### Remote execution over salt-api

*NOTE: This plugin leverages salt-api, which requires its own additional setup. For more information on how to setup salt-api please refer to its documentation which can be found <a href="http://salt-api.readthedocs.org/en/latest/">here</a>.* 

This plugin requires three properties that need to be configured for each step:

- `SALT_API_END_POINT`: the URL of the salt-api endpoint (e.g. https://localhost:8000)
- `Function`: the function to be passed to the salt-api call (excluding the target) 
-- For example, if you enter `test.ping` for the function value, the resulting salt call will be `salt <​yourHostName>​ test.ping`. The target will always default to the hostname of the Rundeck server. You can enter for example `cmd.run_all "ls -l /tmp"` or `state.sls test001` for the function value.
- `SALT_API_EAUTH`: the authenticati​on mechanism that should be used by salt-api
-- This would be the equivalent to the `-a` parameter being passed on the command line 	(e.g. `salt -a pam <target> test.ping`)
- `SALT_API_VERSION` (optional): The expected version of salt-api. Defaults to latest if left blank.


## Troubleshooting

- Ensure that your salt-api setup is fully functional before attempting to execute jobs with this plugin
- Set the job output level to `debug` to print the raw JSON data and returned output
- Ensure the API endpoint is correct
-- http vs https

## Setting up salt return response parsers
===================
This plugin interacts with salt and salt-api. By default, it requests JSON payloads. YAML configuratio​n files are used to determine how it should parse the output and behave with respect to exit codes, standard output, and standard error.

### YAML Configuration File Format
```
handlerMappings:
  <salt module>[.<salt function>]: <java object implementing org.rundeck.plugin.salt.output.SaltReturnHandler>
```

Salt-step is configured in two locations:
* ```src/main/resources/defaultReturners.yaml```
* `​rundeck-​config.​properties`: The `_saltStep.​return​Handlers_` property accepts a comma separated list of additional configuratio​n files

## Developer Guidelines

Thanks for contributing to the project!

### Code style
* Same line braces.
* 4 spaces for tabs.
* Clarity over brevity.
* When in doubt, follow what's already there.

### Javadocs
* At the very minimum, please ensure that class-level and method-level javadocs are present.

### Unit tests
* While we don't expect 100% coverage, we do expect repeatable, automated tests.
* Include unit tests that confirm that your change performs as expected (e.g. new feature or bug fix).

### Before submitting a pull request
* Merge the latest master branch before submitting a pull request.
* Perform a build (`./gradlew clean build`) and confirm that all tests are passing.
* Ensure that all documentation (e.g. `README.md` or other supporting links) is updated.
* Rebase changes into as few commits as makes sense.
* Ensure that all commit messages accurately describe the changes.

###​ Submitting a pull request
* Submit a pull request to the master branch of this project.
* Ensure that the pull request has a clear description of the included changes.
