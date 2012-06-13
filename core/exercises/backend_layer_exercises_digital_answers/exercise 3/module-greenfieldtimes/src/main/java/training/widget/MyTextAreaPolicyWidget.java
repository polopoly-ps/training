package training.widget;

import java.io.IOException;

import com.polopoly.cm.app.widget.OTextAreaPolicyWidget;
import com.polopoly.cm.policy.PolicyUtil;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;

public class MyTextAreaPolicyWidget extends OTextAreaPolicyWidget {
    private String comment;

    public void initSelf(OrchidContext oc) throws OrchidException 
    { 
        super.initSelf(oc); 
        comment = PolicyUtil.getParameter("comment", "Default comment",
            getPolicy()); 
    } 

    public void localRender(OrchidContext oc) throws OrchidException, IOException 
    { 
        Device device = oc.getDevice(); 
        
        device.println(comment); 
        device.println("<br />"); 
        textAreaWidget.render(oc); 
    } 
}
