[![Build Status](https://api.travis-ci.org/digital-wonderland/stash-plugins-jenkins.png)](https://travis-ci.org/digital-wonderland/stash-plugins-jenkins)

#Bitbucket Jenkins Plugin

This bitbucket server plugin notifies jenkins about commits to a certain repository. It can be enabled as a hook for each repository individually.


#Development

Here are the SDK commands you'll use immediately:

* atlas-run          -- installs this plugin into the product and starts it on localhost
* atlas-debug        -- same as atlas-run, but allows a debugger to attach at port 5005
* atlas-cli          -- after atlas-run or atlas-debug, opens a Maven command line window:
                        - 'pi' reinstalls the plugin into the running product instance
* atlas-help         -- prints description for all commands in the SDK

*atlas-mvn package    -- Build the plugin using the atlas sdk.
a*tlas-install-plugin -- Deploy it

Full documentation is always available at:

https://developer.atlassian.com/display/DOCS/Introduction+to+the+Atlassian+Plugin+SDK