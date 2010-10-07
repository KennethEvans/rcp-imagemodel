package cbg.article.model;

/**
 * KE: This listener does nothing. That may be why it is called Null.
 * NullDeltaListener
 * 
 * @author Kenneth Evans, Jr.
 */
public class NullDeltaListener implements IDeltaListener
{
    protected static NullDeltaListener soleInstance = new NullDeltaListener();

    public static NullDeltaListener getSoleInstance() {
        return soleInstance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cbg.article.model.IDeltaListener#add(cbg.article.model.DeltaEvent)
     */
    public void add(DeltaEvent event) {
        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cbg.article.model.IDeltaListener#remove(cbg.article.model.DeltaEvent)
     */
    public void remove(DeltaEvent event) {
        // Do nothing
    }

}
