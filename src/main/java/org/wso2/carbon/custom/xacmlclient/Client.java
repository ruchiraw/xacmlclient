package org.wso2.carbon.custom.xacmlclient;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceException;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceStub;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;

public class Client {

    private static Logger log = Logger.getLogger(Client.class);

    private static final String XACML_3_0_NS = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17";

    private static final String DEFAULT_SERVER_URL = "https://localhost:9443";

    private static final String DEFAULT_USERNAME = "admin";

    private static final String DEFAULT_PASSWORD = "admin";

    private static final String ENTITLEMENT_SERVICE = "/services/EntitlementService";

    private static final String EnTITLEMENT_SERVICE_ACTION = "urn:getDecision";

    private static CommandLineParser parser = new BasicParser();
    private static Options options = new Options();

    private static boolean isVerbose = false;

    public static void main(String[] args) throws Exception {
        initOptions();

        CommandLine line = parser.parse(options, args);
        if (line.hasOption("help") || !line.hasOption("request")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ant", options);
            return;
        }

        initSSL(line);

        isVerbose = line.hasOption("verbose");

        String request = line.getOptionValue("request");
        String server = line.getOptionValue("server");
        if (server == null) {
            server = DEFAULT_SERVER_URL;
        }
        String serviceUrl = server + ENTITLEMENT_SERVICE;

        String username = line.getOptionValue("username");
        if (username == null) {
            username = DEFAULT_USERNAME;
        }

        String password = line.getOptionValue("password");
        if (password == null) {
            password = DEFAULT_PASSWORD;
        }

        String decision = invoke(serviceUrl, username, password, getRequest(request));
        log.info(String.format("Decision: %s", decision));
    }

    private static String invoke(String serviceURL, String username, String password, String request)
            throws RemoteException, EntitlementServiceException {
        EntitlementServiceStub client = getEntitlementClient(serviceURL, username, password);
        if (isVerbose) {
            log.info("Entitlement Request:");
            log.info(request);
        }
        OMElement response = buildResponse(client.getDecision(request));

        String decision = getDecision(response);
        if (isVerbose) {
            log.info(String.format("Entitlement Decision: %s", decision));
        }
        return decision;
    }

    private static void initSSL(CommandLine line) {
        String trustStore = line.getOptionValue("trustStore");
        if (trustStore != null) {
            System.setProperty("javax.net.ssl.trustStore", trustStore);
        }
        String trustStorePass = line.getOptionValue("trustStorePass");
        if (trustStorePass != null) {
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePass);
        }
    }

    private static void initOptions() {
        options.addOption("help", false, "prints this message");
        options.addOption("verbose", false, "runs on verbose mode");
        options.addOption("trustStore", true, "Java key store with trusted ssl certificates of the server");
        options.addOption("trustStorePass", true, "Password of the key store specified with trustStore option");
        options.addOption("request", true, "path of the entitlement request");
        options.addOption("server", true, "server url where entitlement service is running");
        options.addOption("username", true, "user who accesses the entitlement service");
        options.addOption("password", true, "password of the user who accesses the entitlement service");
    }

    private static EntitlementServiceStub getEntitlementClient(String serviceURL, String username, String password)
            throws AxisFault {
        if (isVerbose) {
            log.info(String.format("Initializing Entitlement stub with serviceURL: %s, username: %s", serviceURL, username));
        }
        EntitlementServiceStub stub = new EntitlementServiceStub(serviceURL);

        HttpTransportProperties.Authenticator authenticator = new HttpTransportProperties.Authenticator();
        authenticator.setUsername(username);
        authenticator.setPassword(password);
        org.apache.axis2.client.Options options = stub._getServiceClient().getOptions();
        options.setAction(EnTITLEMENT_SERVICE_ACTION);
        options.setProperty(HTTPConstants.AUTHENTICATE, authenticator);
        return stub;
    }

    private static OMElement buildResponse(String response) {
        if (isVerbose) {
            log.info("Entitlement Response:");
            log.info(response);
        }
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(new StringReader(response));
        return builder.getDocumentElement();
    }

    private static String getDecision(OMElement response) {
        OMElement resultElement = response.getFirstChildWithName(new QName(XACML_3_0_NS, "Result"));
        OMElement decisionElement = resultElement.getFirstChildWithName(new QName(XACML_3_0_NS, "Decision"));
        return decisionElement.getText();
    }

    private static String getRequest(String path) throws IOException {
        byte[] request = Files.readAllBytes(Paths.get(path));
        return new String(request);
    }

}
