package training.content.text;

import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyUtil;
import com.polopoly.cm.policy.PrepareResult;
import com.polopoly.util.StringUtil;

public class ConstrainedTextAreaPolicy extends SingleValuePolicy {
    private int maxLength;

    @Override
    public void initSelf() {
        super.initSelf();
        maxLength = PolicyUtil.getParameterAsInt("maxLength", -1, this);
    }
    
    @Override
    public PrepareResult prepareSelf() throws CMException {
         PrepareResult result = super.prepareSelf();
         
         String value = getValue();
         if(!StringUtil.isEmpty(value) && maxLength > -1 &&
                 value.length() > maxLength) {
             result.setError(true);
             result.setMessage("Maximum number of characters for this field is " +
                               maxLength + 
                               " and you have typed " + value.length() +
                               "characters. Please correct before saving.");
         }
         return result;
    }
}
