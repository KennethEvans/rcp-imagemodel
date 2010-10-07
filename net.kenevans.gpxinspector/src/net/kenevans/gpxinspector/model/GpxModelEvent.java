package net.kenevans.gpxinspector.model;

import java.beans.PropertyChangeEvent;

/**
 * GpxModelEvent is currently the same as a propertyChange Event. It may be
 * expanded in the future.
 * 
 * @author Kenneth Evans, Jr.
 */
public class GpxModelEvent extends PropertyChangeEvent
{
    private static final long serialVersionUID = 1L;

    public GpxModelEvent(Object source, String propertyName, Object oldValue,
        Object newValue) {
        super(source, propertyName, oldValue, newValue);
        // TODO Auto-generated constructor stub
    }

}
