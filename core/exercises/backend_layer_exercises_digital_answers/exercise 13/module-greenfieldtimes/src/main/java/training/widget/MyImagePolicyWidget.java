package training.widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import training.content.image.MyImagePolicy;

import com.polopoly.cm.app.ContentSession;
import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.widget.OFieldPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.cm.servlet.URLBuilder;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.event.OrchidEvent;
import com.polopoly.orchid.event.WidgetEventListener;
import com.polopoly.orchid.widget.OFileInput;
import com.polopoly.orchid.widget.OSubmitButton;
import com.polopoly.orchid.widget.OTextInput;
import com.polopoly.orchid.widget.OWidget;
import com.polopoly.orchid.widget.OWidgetBase;

public class MyImagePolicyWidget extends OFieldPolicyWidget
        implements Editor, Viewer {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OTextInput textInput;
    private OFileInput fileInput;
    private OSubmitButton uploadButton;
    private List<OWidget> images;
    private URLBuilder urlBuilder;

    public void initSelf(OrchidContext oc) throws OrchidException {
        urlBuilder = RequestPreparator.getURLBuilder(oc.getDevice().getRequest());
        images = new ArrayList<OWidget>();
        textInput = new OTextInput();
        addAndInitChild(oc, textInput);

        fileInput = new OFileInput();
        addAndInitChild(oc, fileInput);

        uploadButton = new OSubmitButton();
        addAndInitChild(oc, uploadButton);
        uploadButton.setLabel("Upload image");
        uploadButton.addSubmitListener(new AddImageListener());

        updateImageList(oc);
    }

    public void updateImageList(OrchidContext oc) throws OrchidException {
        for (int i = 0; i < images.size(); i++) {
            discardChild((OWidget) images.get(i));
        }

        images = new ArrayList<OWidget>();
        MyImagePolicy policy = (MyImagePolicy) getPolicy();

        try {
            int count = policy.getNumberOfImages();

            for (int i = 0; i < count; i++) {
                MyImageWidget image = new MyImageWidget(
                        urlBuilder.createFileUrl(
                                policy.getContentId(),
                                policy.getImagePath(i),
                                (HttpServletRequest) oc.getDevice().getRequest()),
                        policy.getImageByline(i), i);
                addAndInitChild(oc, image);
                images.add(image);
            }
        } catch (CMException cme) {
            handleError(oc, "An error occured");
        }
    }

    public void localRender(OrchidContext oc) throws OrchidException, IOException {
        Device device = oc.getDevice();

        device.println("<table>");
        if (getContentSession().getMode() == ContentSession.EDIT_MODE) {
            device.println("<tr>");
            device.println("<td>");
            device.println("Byline:");
            textInput.render(oc);
            device.println("<br />");
            device.println("Image:");
            fileInput.render(oc);
            device.println("</td>");
            device.println("<td>");
            uploadButton.render(oc);
            device.println("</td>");
            device.println("</tr>");
        }
        for (int i = 0; i < images.size(); i++) {
            device.println("<tr><td colspan='2'>");
            ((OWidget) images.get(i)).render(oc);
            device.println("</td></tr>");
        }
        device.println("</table>");
    }

    private class AddImageListener implements WidgetEventListener {

        public void processEvent(OrchidContext oc, OrchidEvent oe)
                throws OrchidException {
            if (fileInput.isFileEmpty()) {
                return;
            }

            if (textInput.getText() == null || textInput.getText().length() == 0) {
                handleError(oc, "A byline is required");
                return;
            }

            try {
                ((MyImagePolicy) getPolicy()).addImage(
                        textInput.getText(),
                        fileInput.getFileName(),
                        fileInput.getFileData());
                updateImageList(oc);
            } catch (Exception e) {
                handleError("An error occured", e, oc);
            }
            textInput.setText("");
        }
    }

    private class RemoveImageListener implements WidgetEventListener {
        private int index;

        public RemoveImageListener(int index) {
            this.index = index;
        }

        public void processEvent(OrchidContext oc, OrchidEvent oe)
                throws OrchidException {
            try {
                ((MyImagePolicy) getPolicy()).removeImage(index);
            } catch (Exception e) {
                handleError("An error occured", e, oc);
            }
            updateImageList(oc);
        }
    }

    private class MyImageWidget extends OWidgetBase {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String imageWebPath;
        private String imageByline;
        private int index;
        private OSubmitButton removeButton;

        public MyImageWidget(String imageWebPath, String imageByline, int index) {
            this.imageWebPath = imageWebPath;
            this.imageByline = imageByline;
            this.index = index;
        }

        public void initSelf(OrchidContext oc) throws OrchidException {
            removeButton = new OSubmitButton();
            addAndInitChild(oc, removeButton);
            removeButton.setLabel("Remove");
            removeButton.addSubmitListener(new RemoveImageListener(index));
        }

        public void localRender(OrchidContext oc)
                throws OrchidException, IOException {
            Device device = oc.getDevice();

            device.println("<table>");
            device.println("<tr>");
            device.println("<td>");
            device.println(
                    "<img src='" + imageWebPath +
                    "' width='30' height='50' alt='" + imageByline + "'>");
            device.println("</td>");
            device.println("<td>");
            device.println("<b>Byline:</b>");
            device.println(imageByline);
            device.println("</td>");
            if (getContentSession().getMode() == ContentSession.EDIT_MODE) {
                device.println("<td>");
                removeButton.render(oc);
                device.println("</td>");
            }
            device.println("</tr>");
            device.println("</table>");
        }
    }
}