package net.kenevans.imagemodel;

import java.beans.PropertyChangeEvent;

/**
 * ImageModelEvent is currently the same as a PropertyChange Event. It may be
 * expanded in the future.
 * 
 * @author Kenneth Evans, Jr.
 */
public class ImageModelEvent extends PropertyChangeEvent
{
    private static final long serialVersionUID = 1L;

    public ImageModelEvent(Object source, String propertyName, Object oldValue,
        Object newValue) {
        super(source, propertyName, oldValue, newValue);
        // TODO Auto-generated constructor stub
    }

}
