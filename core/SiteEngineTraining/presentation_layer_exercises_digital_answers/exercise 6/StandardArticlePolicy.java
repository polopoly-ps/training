package example.content.article;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.PublishingDateTime;
import com.polopoly.cm.app.inbox.InboxFlags;
import com.polopoly.cm.app.policy.CheckboxPolicy;
import com.polopoly.cm.app.policy.DateTimePolicy;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.app.policy.TimeStatePolicy;
import com.polopoly.cm.app.search.categorization.Categorization;
import com.polopoly.cm.app.search.categorization.CategorizationProvider;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.client.WorkflowAwareContent;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListRead;
import com.polopoly.cm.collections.impl.ReadOnlyContentListSimple;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyUtil;
import com.polopoly.cm.policy.UserDataPolicy;
import com.polopoly.cm.search.index.Candidate;
import com.polopoly.cm.search.index.IndexContext;
import com.polopoly.cm.search.index.builder.DocumentBuilder;
import com.polopoly.cm.search.index.impl.LuceneDocumentLegacyTranslator;
import com.polopoly.community.comment.CommentList;
import com.polopoly.index.annotation.Index;
import com.polopoly.model.ModelTypeDescription;
import com.polopoly.siteengine.standard.feed.Feedable;
import com.polopoly.textmining.TextRepresentation;

import example.content.ContentBasePolicy;
import example.content.rss.Rssable;
import example.layout.element.comments.CommentsElementPolicy;

/**
 * Policy for the standard article template.
 * 
 * @since 9.9
 */

@Index
public class StandardArticlePolicy extends ContentBasePolicy 
    implements ModelTypeDescription, 
               Rssable, DocumentBuilder, PublishingDateTime, 
               TextRepresentation, CategorizationProvider 
{
    private static final String GROUP_COMMENTS = "comments";

    private static String COMPONENT_LEAD = "lead";
    private static String COMPONENT_WORKFLOW_STATE = "workflowstate";
    private static String COMPONENT_AUTHOR = "author";

    protected static String CLASS = StandardArticlePolicy.class.getName();
    protected static Logger logger = Logger.getLogger(CLASS);

    public boolean populateDocument(IndexContext indexContext,
                                    Document document,
                                    Candidate candidate)
    {
        super.populateDocument(indexContext, document, candidate);

        // Add lead as an unindexed field for use in search hits
        String lead = getChildValue(COMPONENT_LEAD);

        if (lead != null) {
            LuceneDocumentLegacyTranslator.addToDocument(document, new Field(COMPONENT_LEAD, lead, Field.Store.YES, Field.Index.NO));
        }

        tryIndexWorkflowState(document, indexContext.getName());

        return false;
    }

    /**
     * Tries to index any workflow information that might exist on a document.
     * Works best if the index server receives workflow events on all the
     * transitions in the workflow (done by adding the sendEvent=true 
     * property.)
     */
    private void tryIndexWorkflowState(Document document,
                                       String indexName)
    {
        // Index the workflow if such a thing exists
        logger.logp(Level.FINEST, CLASS, "tryIndexWorkflowState", 
                    "(" + indexName + ") Trying to index workflow for content " 
                    + getContentId().getContentIdString());

        ContentRead possibleContent = getContentUnwrapped();
        if (possibleContent instanceof WorkflowAwareContent) {
            WorkflowAwareContent content = (WorkflowAwareContent) possibleContent;

            try {
                LuceneDocumentLegacyTranslator.addToDocument(document, 
                                                             new Field(COMPONENT_WORKFLOW_STATE, 
                                                                       content.getWorkflowState().getName(),
                                                                       Field.Store.YES, Field.Index.TOKENIZED));
            } catch (CMException e) {
                logger.logp(Level.WARNING, CLASS, "tryIndexWorkflowState", 
                "Failed indexing workflow.");
            }
        } else {
            logger.logp(Level.FINEST, CLASS, "tryIndexWorkflowState", 
            "Content has no associated workflow state.");
        }
    }

    public String getAuthor()
        throws CMException
    {
        String author = getChildValue(COMPONENT_AUTHOR, "");

        if (author.length() == 0) {
            UserDataPolicy user = getCreator();

            if (user != null) {
                if (user.getFirstname() != null) {
                    author = user.getFirstname();
                }

                if (user.getSurname() != null) {
                    author += " " + user.getSurname();
                }
            }
        }

        return author;
    }

    /**
     * Returns true if this article is a candidate for being searchable in the
     * public index
     */
    public boolean isPublicSearchCandidate()
    {
        return true;
    }

    /**
     * @see Rssable
     */
    public String getItemDescription()
    {
        return getChildValue(COMPONENT_LEAD);
    }

    /**
     * The date should be dependant on workflow state/time state
     * 
     * @see Rssable
     */
    public Date getItemPublishedDate()
    {
        Date ret = null;
        ret = new Date(getPublishingDateTime());

        return ret;
    }
    
    /**
     * @see Feedable
     */
    public String getItemTitle()
    {
        String ret = null;

        try {
            ret = getName();
        } catch (CMException cme) {
            logger.logp(Level.WARNING, CLASS, "getItemTitle", "Failed to get Feed Item title", cme);
        }

        return ret;
    }
    
    @Override
    public void postCreate()
        throws CMException
    {
        super.postCreate();

        // All articles should be in the Inbox by default.
        // If integrating with e.g. a print system, you might want to set this only on articles arriving from the print system.
        new InboxFlags().setShowInInbox(this, true);
    }

    /**
     * @see Rssable
     */
    public ContentId getItemContentId()
    {
        return getContentId();
    }

    /**
     * @see Rssable
     */
    public ContentId[] getItemParentIds()
    {
        return getParentIds();
    }

    public String getLead()
        throws CMException
    {
        return getChildValue("lead");
    }

    @Override
    public ContentList getContentList(String contentListName)
        throws CMException
    {
        ContentList contentList = null;

        if (GROUP_COMMENTS.equals(contentListName)) {
            CommentsElementPolicy commentsElement = findCommentsElementFromMainSlot();

            List<ContentId> contentIdList;
            if (commentsElement != null) {
                CommentList commentList = commentsElement.getCommentList();
                contentIdList = commentList.getSlice(0, Integer.MAX_VALUE).getContentIds();
            } else {
                contentIdList = Collections.emptyList();
            }

            contentList = new ReadOnlyContentListSimple(contentIdList,
                                                        GROUP_COMMENTS);
        } else {
            contentList = super.getContentList(contentListName);
        }

        return contentList;
    }

    @SuppressWarnings("unchecked")
    private CommentsElementPolicy findCommentsElementFromMainSlot()
        throws CMException
    {
        ContentListRead slotElements = getContentList("elements/slotElements");

        if (slotElements != null) {
            for (Iterator<?> iterator = slotElements.getListIterator(); iterator.hasNext(); ) {
                ContentReference contentReference = (ContentReference) iterator.next();
                ContentId elementId = contentReference.getReferredContentId();
                Policy element = getCMServer().getPolicy(elementId);

                if (element instanceof CommentsElementPolicy) {
                    return (CommentsElementPolicy) element;
                }
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see com.polopoly.cm.PublishingDateTime#getPublishingDateTime()
     */
    public long getPublishingDateTime()
    {
        long publishingDateTime = getContentCreationTime();

        try {            
            DateTimePolicy policy = (DateTimePolicy) getChildPolicy("publishedDate");

            if (policy != null) {
                publishingDateTime = policy.getTimeMillis();
            }
            
            TimeStatePolicy timeStatePolicy = (TimeStatePolicy) getChildPolicy("timestate");
            if (policy != null) {
                Date onTime = timeStatePolicy.getOnTime();
                if (onTime != null) {
                	long onTimeMillis = onTime.getTime();
                	if (onTimeMillis > publishingDateTime) {
                		publishingDateTime = onTimeMillis;
                	}
                }
            }
        } catch (CMException e) {
        	e.printStackTrace();
        }
        return publishingDateTime;
    }
    
    public String getTextRepresentation()
    {
        String text = null;

        try {
            SingleValuePolicy titlePolicy = (SingleValuePolicy)getChildPolicy("name");
            SingleValuePolicy bodyPolicy = (SingleValuePolicy)getChildPolicy("body");
            SingleValuePolicy leadPolicy = (SingleValuePolicy)getChildPolicy("lead");

            String title = titlePolicy.getValue();

            String lead = leadPolicy.getValue();
            String body = bodyPolicy.getValue();

            text = 
                (title != null ? title + "/n/n" :"")  + 
                (lead != null ? lead  + "/n/n" :"") + 
                (body != null ? body :"");
        } catch (CMException e) {
            logger.logp(Level.WARNING, CLASS, "getTextRepresentation", "Failed to get body policy", e);
        }

        return text;
    }

    private CategorizationProvider getCategorizationProvider()
        throws CMException
    {
        CategorizationProvider categorizationProvider =
            (CategorizationProvider) getChildPolicy("categorization");

        return categorizationProvider;
    }

    public Categorization getCategorization()
        throws CMException
    {
        return getCategorizationProvider().getCategorization();
    }

    public void setCategorization(Categorization categorization)
        throws CMException
    {
        getCategorizationProvider().setCategorization(categorization);
    }
    
    public ContentId getDefaultReferredImage() throws CMException {
        ContentList topImages = getContentList("topimages");
        if(topImages.size() > 0) {
            return topImages.getEntry(0).getReferredContentId();
        }
        ContentList images = getContentList("images");
        if (images.size() > 0) {
            return images.getEntry(0).getReferredContentId();
        }
        return null;
    }
    
    public boolean isOffline()
    {
        try {
            CheckboxPolicy onlineStatusPolicy = (CheckboxPolicy) getChildPolicy("onlineState");
            return PolicyUtil.getParameterAsBoolean("isDefaultOffline", false, onlineStatusPolicy);
        } catch (CMException e) {
            logger.log(Level.WARNING, "Could not retrieve onlineStatus field.", e);
        }

        return false;
    }
    
    @Override
    public void preCommit()
        throws CMException
    {
        super.preCommit();
    }
}