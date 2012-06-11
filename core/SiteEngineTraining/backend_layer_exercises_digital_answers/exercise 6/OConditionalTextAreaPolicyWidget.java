package training.widget;

import java.io.IOException;

import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.app.widget.OFieldPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.ajax.AjaxEvent;
import com.polopoly.orchid.ajax.JSCallback;
import com.polopoly.orchid.ajax.event.ChangeEvent;
import com.polopoly.orchid.ajax.listener.StandardAjaxEventListener;
import com.polopoly.orchid.ajax.trigger.OAjaxTriggerOnChange;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OCheckbox;
import com.polopoly.orchid.widget.OTextArea;

public class OConditionalTextAreaPolicyWidget extends OFieldPolicyWidget implements Editor {
	private OAjaxTriggerOnChange onChangeTrigger;
	private OCheckbox checkbox;
	private boolean show = false;
	private OTextArea textField;

	public void initSelf(OrchidContext oc) throws OrchidException {

		super.initSelf(oc);
		checkbox = new OCheckbox();
		addAndInitChild(oc, checkbox);

		// Create the trigger
		onChangeTrigger = new OAjaxTriggerOnChange(checkbox);
		addAndInitChild(oc, onChangeTrigger);

		textField = new OTextArea();
		textField.setRows(4);
		textField.setColumns(50);
		textField.setEnabled(true);
		textField.setEditable(true);
		addAndInitChild(oc, textField);

		// Create the listener
		StandardAjaxEventListener onChangeListener =
			new ToogleTextBoxEventListener();

		// Render this widget when the event listener has been triggered
		onChangeListener.addRenderWidget(this);

		// Attach the listener to the button
		getTree().registerAjaxEventListener(checkbox, onChangeListener);
	}

	@Override
	public void initValueFromPolicy() throws CMException {
		textField.setText(((SingleValuePolicy)getPolicy()).getValue());
	}

	@Override
	public void storeSelf() throws CMException {
		((SingleValuePolicy)getPolicy()).setValue(textField.getText());
	}

	public void localRender(OrchidContext oc) throws IOException, OrchidException {
		Device dev = oc.getDevice();
		dev.print("<p>");
		checkbox.render(oc);
		dev.println(" Show text area</p>");

		onChangeTrigger.render(oc);
		if (show) {
			dev.print("<p>");
			textField.render(oc);
			dev.print("</p>");
		}
	}

	public boolean isAjaxTopWidget() {
		// Return true to enable the widget to be rendered asynchronously
		return true;
	}

	private class ToogleTextBoxEventListener extends StandardAjaxEventListener {

		public boolean triggeredBy(OrchidContext oc, AjaxEvent e) {
			return e instanceof ChangeEvent;
		}

		public JSCallback processEvent(OrchidContext oc, AjaxEvent event)
		throws OrchidException {
			if (show) {
				show = false;
				checkbox.setChecked(false);
			} else {
				show = true;
				checkbox.setChecked(true);
			}
			return null; // No callback
		}
	};
}
