package net.kenevans.gpxinspector.model;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

/*
 * Created on Aug 22, 2010
 * By Kenneth Evans, Jr.
 */

public class GpxFileSetModel extends GpxModel
{
    private LinkedList<GpxFileModel> gpxFileModels;
    private String name = "GPX Files";

    public GpxFileSetModel(String[] fileNames) throws JAXBException {
        gpxFileModels = new LinkedList<GpxFileModel>();
        for(String fileName : fileNames) {
            gpxFileModels.add(new GpxFileModel(this, fileName));
        }
        disposed = false;
    }

    public GpxFileSetModel(File[] files) throws JAXBException {
        gpxFileModels = new LinkedList<GpxFileModel>();
        for(File file : files) {
            gpxFileModels.add(new GpxFileModel(this, file));
        }
        disposed = false;
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
        for(GpxModel model : gpxFileModels) {
            model.dispose();
        }
        gpxFileModels.clear();
        removeAllGpxModelListeners();
        disposed = true;
    }

    /**
     * Removes an element from the GpxFileModel list.
     * 
     * @param model
     * @return true if this list contained the specified element.
     * @see java.util.List#remove
     */
    public boolean remove(GpxFileModel model) {
        boolean retVal = gpxFileModels.remove(model);
        if(retVal) {
            model.dispose();
            fireRemovedEvent(model);
        }
        return retVal;
    }

    /**
     * Adds an element to the GpxFileModel list.
     * 
     * @param model
     * @return true if the list changed (as specified by {@link Collection#add}
     *         ).
     * @see java.util.List#add
     */
    public boolean add(GpxFileModel model) {
        boolean retVal = gpxFileModels.add(model);
        if(retVal) {
            model.setParent(this);
            fireAddedEvent(model);
        }
        return retVal;
    }

    public void addFirst(GpxFileModel model) {
        gpxFileModels.addFirst(model);
        model.setParent(this);
        fireAddedEvent(model);
    }

    public void addLast(GpxFileModel model) {
        gpxFileModels.addLast(model);
        model.setParent(this);
        fireAddedEvent(model);
    }

    public void addBefore(GpxFileModel newModel, GpxFileModel curModel) {
        if(gpxFileModels.isEmpty()) {
            // Could throw an exception
            gpxFileModels.add(newModel);
            newModel.setParent(this);
        } else {
            int i = gpxFileModels.indexOf(curModel);
            if(i == -1) {
                // Add at the end. Could throw an exception
                gpxFileModels.add(newModel);
            } else {
                gpxFileModels.add(i, newModel);
            }
        }
        fireAddedEvent(newModel);
    }

    public void addAfter(GpxFileModel newModel, GpxFileModel curModel) {
        if(gpxFileModels.isEmpty()) {
            // Could throw an exception
            gpxFileModels.add(newModel);
            newModel.setParent(this);
        } else {
            int i = gpxFileModels.indexOf(curModel);
            if(i == -1) {
                // Add at the end. Could throw an exception
                gpxFileModels.add(newModel);
            } else if(i == gpxFileModels.size() - 1) {
                // Is the last element, add at the end
                gpxFileModels.add(newModel);
            } else {
                // Add it at the next position
                gpxFileModels.add(i + 1, newModel);
            }
        }
        fireAddedEvent(newModel);
    }

    public void replace(GpxFileModel newModel, GpxFileModel curModel) {
        if(gpxFileModels.isEmpty()) {
            // Could throw an exception
            gpxFileModels.add(newModel);
            newModel.setParent(this);
        } else {
            int i = gpxFileModels.indexOf(curModel);
            if(i == -1) {
                // Add at the end. Could throw an exception
                gpxFileModels.add(newModel);
            } else {
                // replacedModel should be the curModel
                GpxModel replacedModel = gpxFileModels.set(i, newModel);
                replacedModel.dispose();
                fireRemovedEvent(replacedModel);
            }
        }
        fireAddedEvent(newModel);
    }

    public void sort() {
        Collections.sort(gpxFileModels);
    }

    /**
     * @return The value of name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The new value for name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The value of gpxFileModels.
     */
    public List<GpxFileModel> getGpxFileModels() {
        return gpxFileModels;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#getLabel()
     */
    @Override
    public String getLabel() {
        return name;
    }

}
