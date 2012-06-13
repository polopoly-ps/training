package training.widget;

import java.io.IOException;
import com.polopoly.cm.app.util.LayoutUtil;
import com.polopoly.cm.app.widget.OLayoutWidget;
import com.polopoly.cm.app.widget.OPolicyWidget;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;

public class MyColumnLayoutWidget extends OLayoutWidget {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int columns;

    public void initSelf(OrchidContext oc) throws OrchidException {
        columns = LayoutUtil.getLayoutParameterAsInt("columns", 2,
            getLayoutDefinition(), getPolicy());
    }

    public void localRender(OrchidContext oc) throws OrchidException,
            IOException {
        Device device = oc.getDevice();
        device.println("<table>");
        device.println("<tr>");

        boolean first = true;
        int currentCol = 0;
        for (int i = 0; i < childPolicyWidgets.size(); i++) {
            if(first) {
                device.println("<td width='" + (100 / columns) +
                               "%' valign='top'>");
                first = false;
                currentCol++;
            }
            ((OPolicyWidget) childPolicyWidgets.get(i)).render(oc);
            if((i + 2) >= childPolicyWidgets.size() * currentCol / columns) {
                device.println("</td>");
                first = true;
            }
        }
        device.println("</tr>");
        device.println("</table>");
    }
}