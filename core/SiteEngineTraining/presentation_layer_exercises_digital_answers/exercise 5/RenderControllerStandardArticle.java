package example.content.article;

import java.util.Locale;

import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import example.content.BodyTranslator;
import example.content.RenderControllerExtended;

/**
 * Render controller for Standard Article.
 */
public class RenderControllerStandardArticle extends RenderControllerExtended {

	public static final String DEFAULT_DATE_FORMAT = "EEEE, d/MM/yyyy hh:ss";
	
    @Override
    public void populateModelBeforeCacheKey(RenderRequest request, TopModel m,
            ControllerContext context) {


	/* Make sure you leave any other code of this method intact!*/


        super.populateModelBeforeCacheKey(request, m, context);
        ModelWrite localModel = m.getLocal();
        
        /*
         * Setting date format and locale according to client browser
         */
        String dateFormat = DEFAULT_DATE_FORMAT;
        try {
        	String acceptLanguage = request.getHeader("Accept-Language");
        	Locale clientLocale;
        	if (acceptLanguage != null && acceptLanguage.length() > 0) {
        		clientLocale = new Locale(acceptLanguage.substring(0, acceptLanguage.indexOf(',')));
        	}
        	else {
        		clientLocale = m.getContext().getSite().getBean().getResources().getLocale();
        	}
        	localModel.setAttribute("clientLocale", clientLocale);

        	if (acceptLanguage != null && acceptLanguage.startsWith("en")) {
        		dateFormat = "EEEE, MMMM d yyyy";
        	}
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        localModel.setAttribute("dateFormat", dateFormat);
        
        
        /*
         * Translating WYSIWYG body text into proper HTML
         */
        boolean inPreviewMode = m.getRequest().getPreview().isInPreviewMode();

        BodyTranslator bodyTranslator = new BodyTranslator();
        bodyTranslator.translateBody(request, localModel, "body/value", inPreviewMode);
        
    }
}
