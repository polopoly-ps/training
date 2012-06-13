package training.application;

import java.net.URL;

import com.polopoly.application.Application;
import com.polopoly.application.ConnectionProperties;
import com.polopoly.application.StandardApplication;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.DefaultMajorNames;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.client.EjbCmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.UserServer;

import example.content.article.StandardArticlePolicy;


/**
 * A simple test application that uses PAF connect the cmClient and create/update an standard article with external Id
 * (example.demo.article.MyNewArticle) and contentId (1.235).
 *
 * Try it from command line:
 * cd module-greenfieldtimes
 * mvn compile
 * cd target/classes
 * java -classpath .:$M2_REPO/com/polopoly/polopoly/[current version]/polopoly-[current version].jar:$M2_REPO/jboss/jbossall-        
 * client/4.0.5/jbossall-client-4.0.5.jar:$M2_REPO/repository/org/apache/lucene/lucene-core/2.3.1/lucene-core-2.3.1.jar 
 * training.application.PAFContentWriter
 */
public class PAFContentWriter {

    public static void main(String[] args) throws Exception {

        EjbCmClient cmClient = new EjbCmClient();
        ConnectionProperties connectionProperties = new ConnectionProperties(
                new URL("http://localhost:8081/connection-properties/connection.properties"));

        Application myTestApp = new StandardApplication("mytestapp");
        cmClient.readConnectionProperties(connectionProperties);
        myTestApp.addApplicationComponent(cmClient);
        myTestApp.init();

        PolicyCMServer cmServer = cmClient.getPolicyCMServer();
        UserServer userServer = cmClient.getUserServer();
        String userName="sysadmin";
        String password="sysadmin";
        Caller caller = userServer.loginAndMerge(userName, password, null);
        cmServer.setCurrentCaller(caller);

        ExternalContentId extID = new ExternalContentId("example.demo.article.MyNewArticle");
        String body = "Example article body";
        String name = "Example article name";
        String lead = "Example article lead";
        //ExternalContentId id = new ExternalContentId(extID);
        ExternalContentId inputTemplateId = new ExternalContentId("example.StandardArticle");
        StandardArticlePolicy policy = null;

        // check whether id exist to create new content/update existing content
        if (cmServer.contentExists(extID)) {
            policy = (StandardArticlePolicy)cmServer.getPolicy(extID);
            policy = (StandardArticlePolicy)cmServer.createContentVersion(policy.getContentId(), inputTemplateId);
        }else {
            int major = cmServer.getMajorByName(DefaultMajorNames.ARTICLE);
            policy = (StandardArticlePolicy)cmServer.createContent(major, inputTemplateId);
            policy.setExternalId(extID.getExternalId());
        }

        // set data
        ((SingleValued) policy.getChildPolicy("name")).setValue(name);
        ((SingleValued) policy.getChildPolicy("body")).setValue(body);
        ((SingleValued) policy.getChildPolicy("lead")).setValue(lead);

        //set the article under Greenfield Times department 2.163
        ExternalContentId greenfieldTimesId =
            new ExternalContentId("GreenfieldTimes.d");
        ContentId securityParentId = greenfieldTimesId.getContentId();
        policy.getContent().setSecurityParentId(securityParentId);

        cmServer.commitContent(policy);
        System.out.println("Article content id: "+policy.getContentId().getContentIdString() );
        myTestApp.destroy();
    }
}