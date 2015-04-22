xacmlclient
===========

A command line client to invoke entitlement service of a WSO2 Identity Server.

Usage
=====

`java -jar lib/xacmlclient-1.0-SNAPSHOT-jar-with-dependencies.jar -help`

e.g.

`java -jar lib/xacmlclient-1.0-SNAPSHOT-jar-with-dependencies.jar -request ~/xacml/request.xml -trustStore /Users/ruchira/binaries/wso2/products/is/wso2is-5.0.0/repository/resources/security/wso2carbon.jks -trustStorePass wso2carbon -verbose`
