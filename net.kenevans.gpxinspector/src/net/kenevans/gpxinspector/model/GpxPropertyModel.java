package net.kenevans.gpxinspector.model;

/*
 * Created on Aug 22, 2010
 * By Kenneth Evans, Jr.
 */

public class GpxPropertyModel extends GpxModel implements IGpxElementConstants
{
    private String key;
    private String value;

    public GpxPropertyModel(GpxModel parent, String key, String value) {
        this.parent = parent;
        this.key = key;
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#dispose()
     */
    public void dispose() {
        if(disposed) {
            return;
        }
        removeAllGpxModelListeners();
        disposed = true;
    }

    /**
     * @return The value of key.
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key The new value for key.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return The value of value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value The new value for value.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#getLabel()
     */
    @Override
    public String getLabel() {
        if(key != null) {
            return key + ": " + value;
        }
        return "Null key";
    }

}
