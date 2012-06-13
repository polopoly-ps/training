package training.widget;

import java.io.IOException;

import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.app.widget.OFieldPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.event.OrchidEvent;
import com.polopoly.orchid.event.WidgetEventListener;
import com.polopoly.orchid.widget.OSubmitButton;
import com.polopoly.orchid.widget.OTextInput;

public class MyEventPolicyWidget extends OFieldPolicyWidget implements Editor {
    private OSubmitButton setTextButton;
    private OSubmitButton removeTextButton; 
    private OTextInput textInput; 
    private OTextInput textOutput; 

    @Override
    public void initSelf(OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
        
        setTextButton = new OSubmitButton();
        addAndInitChild(oc, setTextButton);
        setTextButton.setLabel("Set text");
        setTextButton.addSubmitListener(new SetTextListener());
        
        removeTextButton = new OSubmitButton(); 
        addAndInitChild(oc, removeTextButton); 
        removeTextButton.setLabel("Remove text"); 
        removeTextButton.addSubmitListener(new RemoveTextListener()); 
        
        textInput = new OTextInput(); 
        addAndInitChild(oc, textInput); 
        
        textOutput = new OTextInput(); 
        addAndInitChild(oc, textOutput); 
        textOutput.setEditable(false); 
    }
    
    @Override
    public void initValueFromPolicy() throws CMException {
        textOutput.setText(((SingleValuePolicy)getPolicy()).getValue());
    }
    
    @Override
    public void storeSelf() throws CMException {
        ((SingleValuePolicy)getPolicy()).setValue(textOutput.getText()); 
    }
    
    public void localRender(OrchidContext oc) 
        throws OrchidException, IOException 
    { 
        Device device = oc.getDevice(); 
        device.println("<table>"); 
        device.println("<tr><td>"); 
        device.println("Enter text:</td><td>"); 
        textInput.render(oc); 
        device.println("</td><td>"); 
        setTextButton.render(oc); 
        device.println("</td></tr><tr><td>"); 
        device.println("This is the text to save:</td><td>"); 
        textOutput.render(oc); 
        device.println("</td><td>"); 
        removeTextButton.render(oc); 
        device.println("</td></tr></table>"); 
    } 

    private class SetTextListener implements WidgetEventListener { 
        public void processEvent(OrchidContext oc, OrchidEvent oe) { 
            textOutput.setText(textInput.getText()); 
        } 
    }
    
    private class RemoveTextListener implements WidgetEventListener { 
       public void processEvent(OrchidContext oc, OrchidEvent oe) { 
           textOutput.setText(null); 
       } 
   } 
}
