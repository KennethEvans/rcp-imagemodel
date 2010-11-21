package net.kenevans.gpxinspector.model;

import java.util.EventListener;

import javax.swing.event.EventListenerList;

import net.kenevans.gpxinspector.utils.SWTUtils;

/*
 * Created on Aug 22, 2010
 * By Kenneth Evans, Jr.
 */

/**
 * This is the abstract super class of the GPX models.
 * 
 * @author Kenneth Evans, Jr.
 */
public abstract class GpxModel
{
    protected EventListenerList listenerList = new EventListenerList();
    /** The parent of this model. */
    protected GpxModel parent;
    /** A flag indicating whether this model is checked or not. */
    protected boolean checked = true;
    /**
     * A flag indicating whether this model is disposed or not. It should be set
     * to false in the constructor and true in the dispose method.
     */
    protected boolean disposed = false;

    // Property change names
    /**
     * Denotes that a GpxModel was added.
     * 
     * @see #fireAddedEvent(GpxModel)
     */
    public static final String ADDED = "gpxModel.added";
    /**
     * Denotes that a GpxModel was removed.
     * 
     * @see #fireRemovedEvent(GpxModel)
     */
    public static final String REMOVED = "gpxModel.removed";
    /**
     * Denotes that a check state was changed.
     * 
     * @see #fireCheckStateChangedEvent(GpxModel)
     */
    public static final String CHECKSTATE_CHANGED = "gpxModel.checkStateChanged";
    /**
     * Denotes that a model was changed.
     * 
     * @see #fireChangedEvent(GpxModel)
     */
    public static final String CHANGED = "gpxModel.changed";

    public GpxModel() {
    }

    /**
     * Adds the listener.
     * 
     * @param l
     */
    public void addGpxModelListener(GpxModelListener l) {
        listenerList.add(GpxModelListener.class, l);
    }

    /**
     * Removes the listener.
     * 
     * @param l
     */
    public void removeGpxModelListener(GpxModelListener l) {
        listenerList.remove(GpxModelListener.class, l);
    }

    /**
     * Removes all the listeners.
     * 
     * @param l
     */
    public void removeAllGpxModelListeners() {
        EventListener[] listeners = listenerList
            .getListeners(GpxModelListener.class);
        for(EventListener listener : listeners) {
            listenerList.remove(GpxModelListener.class,
                (GpxModelListener)listener);
        }
    }

    /**
     * Fires an GpxModelEvent with the given parameters.
     * 
     * @param name Should be one of the GpxModel.xxx_CHANGED names.
     * @param oldValue
     * @param newValue
     */
    protected void fireGpxModelEvent(String name, Object oldValue,
        Object newValue) {
        EventListener[] listeners = listenerList
            .getListeners(GpxModelListener.class);
        for(EventListener listener : listeners) {
            GpxModelEvent imageModelEvent = new GpxModelEvent(this, name,
                oldValue, newValue);
            ((GpxModelListener)listener).propertyChange(imageModelEvent);
        }
    }

    /**
     * Fires an event denoting a model was added. The oldValue is null, and the
     * newValue is the model added. Also sets the model to be dirty.
     * 
     * @param model The model added.
     */
    protected void fireAddedEvent(GpxModel model) {
        model.setDirty(true);
        fireGpxModelEvent(ADDED, null, model);
    }

    /**
     * Fires an event denoting a model was changed. The oldValue and the
     * newValue are the model. Also sets the model to be dirty.
     * 
     * @param model The model changed.
     */
    protected void fireChangedEvent(GpxModel model) {
        model.setDirty(true);
        fireGpxModelEvent(CHANGED, model, model);
    }

    /**
     * Fires an event denoting a model was removed. The oldValue is the model
     * removed, and the newValue is null. Also sets the model to be dirty.
     * 
     * @param model The model removed.
     */
    protected void fireRemovedEvent(GpxModel model) {
        model.setDirty(true);
        fireGpxModelEvent(REMOVED, model, null);
    }

    /**
     * Fires an event denoting a check state was changes. The oldValue and the
     * newValue is the new value of checked.
     * 
     * @param model The model checked.
     */
    protected void fireCheckStateChangedEvent(GpxModel model) {
        fireGpxModelEvent(CHECKSTATE_CHANGED, null, model.getChecked());
    }

    /**
     * Brings up a dialog with information about the model.
     */
    public void showInfo() {
        String className = this.getClass().toString();
        int lastDot = className.lastIndexOf('.');
        String shortName = className.substring(lastDot + 1);
        SWTUtils.errMsg("No information implemented for " + shortName + "'s");
    }

    /**
     * Disposes of all resources used by this model and calls dispose() on its
     * children. This method should call removeAllGpxModelListeners and also
     * dispose of all of its children, then set disposed to true or do nothing
     * if it is already disposed.
     */
    public void dispose() {
        if(disposed) {
            return;
        }
        removeAllGpxModelListeners();
        disposed = true;
    }

    public GpxModel getParent() {
        return parent;
    }

    /**
     * @param parent The new value for parent.
     */
    public void setParent(GpxModel parent) {
        this.parent = parent;
    }

    public abstract String getLabel();

    /**
     * @return The value of checked.
     */
    public boolean getChecked() {
        return checked;
    }

    /**
     * @param checked The new value for checked.
     */
    public void setChecked(boolean checked) {
        this.checked = checked;
        fireCheckStateChangedEvent(this);
    }

    /**
     * @return The value of dirty for the GpxFileModel in the hierarchy.
     */
    public boolean isDirty() {
        GpxFileModel fileModel = getGpxFileModel();
        if(fileModel == null) {
            return false;
        } else {
            return fileModel.isDirty();
        }
    }

    /**
     * @param dirty The new value for dirty for the GpxFileModel in the
     *            hierarchy.
     */
    public void setDirty(boolean dirty) {
        GpxFileModel fileModel = getGpxFileModel();
        if(fileModel != null) {
            fileModel.setDirty(dirty);
        }
    }

    /**
     * Finds the GpxFileModel parent of the current model. Note that the
     * GpxFileSetModel does not have such a parent.
     * 
     * @return The GpxFileModel or null if not found.
     */
    public GpxFileModel getGpxFileModel() {
        GpxModel parent = this;
        while(!(parent instanceof GpxFileSetModel) && parent != null) {
            if(parent instanceof GpxFileModel) {
                return (GpxFileModel)parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

}
