package training.application;

import java.net.URL;

import com.polopoly.application.Application;
import com.polopoly.application.ConnectionProperties;
import com.polopoly.application.StandardApplication;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.client.EjbCmClient;
import com.polopoly.cm.policy.PolicyCMServer;


/**
 * A simple test application that uses PAF connect the cmClient and get the and of the content with the contentId 2.10.
 *
 * Try it from command line:
 * cd module-greenfieldtimes
 * mvn compile
 * cd target/classes
 * java -classpath .:$M2_REPO/com/polopoly/polopoly/[current version]/polopoly-[current version].jar:$M2_REPO/jboss/jbossall-        
 * client/4.0.5/jbossall-client-4.0.5.jar training.application.PAFExampleApplication
 */

public class PAFExampleApplication {

    public static void main(String[] args) throws Exception {

        EjbCmClient cmClient = new EjbCmClient();
        ConnectionProperties connectionProperties = new ConnectionProperties(
                new URL("http://localhost:8081/connection-properties/connection.properties"));

        Application myTestApp = new StandardApplication("mytestapp");
        cmClient.readConnectionProperties(connectionProperties);
        myTestApp.addApplicationComponent(cmClient);
        myTestApp.init();

        PolicyCMServer server = cmClient.getPolicyCMServer();
        ContentRead testContent = server.getContent(new ContentId(2, 10));
        System.out.println("name: " + testContent.getName());
        myTestApp.destroy();
    }
}