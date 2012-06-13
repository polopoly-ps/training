package training.widget;

import java.io.IOException;

import com.polopoly.cm.app.widget.OTextAreaPolicyWidget;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;

public class MyTextAreaPolicyWidget extends OTextAreaPolicyWidget {

    public void localRender(OrchidContext oc) throws OrchidException, IOException 
    { 
        Device device = oc.getDevice(); 
        
        device.println("This is my comment!"); 
        device.println("<br />"); 
        textAreaWidget.render(oc); 
    } 
}
