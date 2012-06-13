package training.layout.element.urlviewer;

import java.io.InputStreamReader;
import java.net.URL;

import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

public class RenderControllerUrlViewerElement extends RenderControllerBase {

	public void populateModelAfterCacheKey(RenderRequest request, TopModel m,
			CacheInfo info, ControllerContext context) {
		Model contentModel = context.getContentModel();
		String url = (String) ModelPathUtil.get(contentModel, "url/value");
		try {
			if (url != null && url.length() > 0) {
				java.io.InputStream is = new URL(url).openStream();
				InputStreamReader reader = new InputStreamReader(is);

				char[] charBuff = new char[is.available()];
				reader.read(charBuff);
				ModelPathUtil.set(m.getLocal(), "urlResponse",
						new String(charBuff));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
