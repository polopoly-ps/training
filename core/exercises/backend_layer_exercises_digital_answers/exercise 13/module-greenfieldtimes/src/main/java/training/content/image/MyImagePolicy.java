package training.content.image;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyImplBase;

public class MyImagePolicy extends PolicyImplBase {
    public static final String COMPONENT_COUNT = "count";
    public static final String COMPONENT_BYLINE = "byline_";
    public static final String COMPONENT_IMAGE_PATH = "path_";

    public void addImage(String byline, String fileName, InputStream image)
            throws CMException, IOException {
        String path = fileName;
        int count = getNumberOfImages();
        getContent().importFile(path, image);
        saveImageMetaData(count, byline, path);
        setComponent(COMPONENT_COUNT, "" + (count + 1));
    }

    protected void saveImageMetaData(int index, String byline, String path)
            throws CMException {
        setComponent(COMPONENT_BYLINE + index, byline);
        setComponent(COMPONENT_IMAGE_PATH + index, path);
    }

    public void removeImage(int index) throws CMException, IOException {
        String path = getImagePath(index);
        int count = getNumberOfImages();
        getContent().deleteFile(path);
        setComponent(COMPONENT_BYLINE + count, null);
        setComponent(COMPONENT_IMAGE_PATH + count, null);

        for (int i = index + 1; i < count; i++) {
            saveImageMetaData(i - 1, getImageByline(i), getImagePath(i));
        }
        setComponent(COMPONENT_COUNT, "" + (count - 1));
    }

    public int getNumberOfImages() throws CMException {
        return getComponentAsInt(COMPONENT_COUNT, 0);
    }

    public String getImagePath(int index) throws CMException {
        return getComponent(COMPONENT_IMAGE_PATH + index);
    }

    public String getImageByline(int index) throws CMException {
        return getComponent(COMPONENT_BYLINE + index);
    }

    public void getImage(int index, OutputStream imageStream)
            throws CMException, IOException {
        String path = getImagePath(index);
        getContent().exportFile(path, imageStream);
    }
}