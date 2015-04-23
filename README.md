xacmlclient
===========

A command line client to invoke entitlement service of a WSO2 Identity Server.

Usage
=====

`java -jar lib/xacmlclient-1.0-SNAPSHOT-jar-with-dependencies.jar -help`

options:

    -help                   prints this message
    -password <arg>         password of the user who accesses the entitlement
                            service
    -request <arg>          path of the entitlement request
    -server <arg>           server url where entitlement service is running
    -trustStore <arg>       Java key store with trusted ssl certificates of
                            the server
    -trustStorePass <arg>   Password of the key store specified with
                            trustStore option
    -username <arg>         user who accesses the entitlement service
    -verbose                runs on verbose mode


e.g.

    java -jar lib/xacmlclient-1.0-SNAPSHOT-jar-with-dependencies.jar
        -request ~/xacml/request.xml
        -trustStore ~/ssl/wso2carbon.jks
        -trustStorePass wso2carbon
        -verbose