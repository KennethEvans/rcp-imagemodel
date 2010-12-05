package net.kenevans.gpxinspector.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import net.kenevans.gpx.GpxType;
import net.kenevans.gpx.RteType;
import net.kenevans.gpx.TrkType;
import net.kenevans.gpx.WptType;
import net.kenevans.gpxinspector.ui.FileInfoDialog;
import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.parser.GPXClone;
import net.kenevans.parser.GPXParser;

import org.eclipse.swt.widgets.Display;

/*
 * Created on Aug 22, 2010
 * By Kenneth Evans, Jr.
 */

public class GpxFileModel extends GpxModel implements IGpxElementConstants,
    Comparable<GpxFileModel>
{
    private File file;
    private GpxType gpx;
    private LinkedList<GpxTrackModel> trackModels;
    private LinkedList<GpxRouteModel> routeModels;
    private LinkedList<GpxWaypointModel> waypointModels;
    /** Indicates whether the file has changed or not. */
    private boolean dirty = false;

    /**
     * GpxFileModel constructor which is private with no arguments for use in
     * clone.
     */
    private GpxFileModel() {
    }

    public GpxFileModel(GpxModel parent, String fileName) throws JAXBException {
        this(parent, new File(fileName));
    }

    public GpxFileModel(GpxModel parent, File file) throws JAXBException {
        this.parent = parent;
        reset(file);
    }

    /**
     * Resets the contents of this model from the given file.
     * 
     * @param file
     * @throws JAXBException
     */
    public void reset(File file) throws JAXBException {
        dirty = false;
        // try {
        gpx = GPXParser.parse(file);
        // } catch(JAXBException ex) {
        // SWTUtils.excMsg("Error parsing " + file.getPath(), ex);
        // }
        this.file = file;
        trackModels = new LinkedList<GpxTrackModel>();
        List<TrkType> tracks = gpx.getTrk();
        for(TrkType track : tracks) {
            trackModels.add(new GpxTrackModel(this, track));
        }
        routeModels = new LinkedList<GpxRouteModel>();
        List<RteType> routes = gpx.getRte();
        for(RteType route : routes) {
            routeModels.add(new GpxRouteModel(this, route));
        }
        waypointModels = new LinkedList<GpxWaypointModel>();
        List<WptType> waypoints = gpx.getWpt();
        for(WptType waypoint : waypoints) {
            waypointModels.add(new GpxWaypointModel(this, waypoint));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#showInfo()
     */
    @Override
    public void showInfo() {
        FileInfoDialog dialog = null;
        boolean success = false;
        // Without this try/catch, the application hangs on error
        try {
            dialog = new FileInfoDialog(Display.getDefault().getActiveShell(),
                this);
            success = dialog.open();
            if(success) {
                // This also sets dirty
                fireChangedEvent(this);
            }
        } catch(Exception ex) {
            SWTUtils.excMsgAsync("Error with FileInfoDialog", ex);
        }
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
        if(isDirty()) {
            // FIXME
            boolean res = SWTUtils.confirmMsg(this.getLabel()
                + "\nhas been modified and not saved.\n"
                + "Select OK to save it or Cancel to continue without saving.");
            if(res) {
                save();
            }
        }
        for(GpxModel model : trackModels) {
            model.dispose();
        }
        trackModels.clear();
        for(GpxModel model : routeModels) {
            model.dispose();
        }
        routeModels.clear();
        for(GpxModel model : waypointModels) {
            model.dispose();
        }
        waypointModels.clear();
        removeAllGpxModelListeners();
        disposed = true;
    }

    /**
     * Removes an element from the GpxRouteModel list.
     * 
     * @param model
     * @return true if this list contained the specified element.
     * @see java.util.List#remove
     */
    public boolean remove(GpxRouteModel model) {
        boolean retVal = routeModels.remove(model);
        if(retVal) {
            model.dispose();
            fireRemovedEvent(model);
        }
        return retVal;
    }

    /**
     * Adds an element to the GpxFileModel list at the end.
     * 
     * @param newModel The model to be added.
     * @return true if the add appears to be successful.
     */
    public boolean add(GpxRouteModel model) {
        return add(null, model, PasteMode.END);
    }

    /**
     * Adds an element to the GpxRouteModel list at the position specified by
     * the mode relative to the position of the old model.
     * 
     * @param oldModel The old model that specifies the relative location for
     *            the new one. Ignored if the mode is BEGINNING or END.
     * @param newModel The model to be added.
     * @param mode The PasteMode that determines where to place the new model
     *            relative to the old one.
     * @return true if the add appears to be successful.
     */
    public boolean add(GpxRouteModel oldModel, GpxRouteModel newModel,
        PasteMode mode) {
        boolean retVal = true;
        int i = -1;
        switch(mode) {
        case BEGINNING:
            routeModels.addFirst(newModel);
            break;
        case BEFORE:
            i = routeModels.indexOf(oldModel);
            if(i == -1) {
                retVal = false;
            } else {
                routeModels.add(i, newModel);
            }
            break;
        case REPLACE:
        case AFTER:
            i = routeModels.indexOf(oldModel);
            routeModels.add(i + 1, newModel);
            break;
        case END:
            retVal = routeModels.add(newModel);
            break;
        }
        if(retVal) {
            newModel.setParent(this);
            fireAddedEvent(newModel);
        }
        return retVal;
    }

    /**
     * Removes an element from the GpxTrackModel list.
     * 
     * @param model
     * @return true if this list contained the specified element.
     * @see java.util.List#remove
     */
    public boolean remove(GpxTrackModel model) {
        boolean retVal = trackModels.remove(model);
        System.out.println("remove " + model + " " + model.getParent());
        System.out.println("  from " + this + " " + this.getParent());
        // DEBUG
        int n = 0;
        for(GpxTrackModel item : trackModels) {
            System.out.printf("%d %s %s\n", n++, item.toString(), item
                .getParent().toString());
        }
        if(retVal) {
            model.dispose();
            fireRemovedEvent(model);
        }
        return retVal;
    }

    /**
     * Adds an element to the GpxFileModel list at the end.
     * 
     * @param newModel The model to be added.
     * @return true if the add appears to be successful.
     */
    public boolean add(GpxTrackModel model) {
        return add(null, model, PasteMode.END);
    }

    /**
     * Adds an element to the GpxTrackModel list at the position specified by
     * the mode relative to the position of the old model.
     * 
     * @param oldModel The old model that specifies the relative location for
     *            the new one. Ignored if the mode is BEGINNING or END.
     * @param newModel The model to be added.
     * @param mode The PasteMode that determines where to place the new model
     *            relative to the old one.
     * @return true if the add appears to be successful.
     */
    public boolean add(GpxTrackModel oldModel, GpxTrackModel newModel,
        PasteMode mode) {
        boolean retVal = true;
        int i = -1;
        switch(mode) {
        case BEGINNING:
            trackModels.addFirst(newModel);
            break;
        case BEFORE:
            i = trackModels.indexOf(oldModel);
            if(i == -1) {
                retVal = false;
            } else {
                trackModels.add(i, newModel);
            }
            break;
        case REPLACE:
        case AFTER:
            i = trackModels.indexOf(oldModel);
            trackModels.add(i + 1, newModel);
            break;
        case END:
            retVal = trackModels.add(newModel);
            break;
        }
        if(retVal) {
            newModel.setParent(this);
            fireAddedEvent(newModel);
        }
        return retVal;
    }

    /**
     * Removes an element from the GpxWaypointModel list.
     * 
     * @param model
     * @return true if this list contained the specified element.
     * @see java.util.List#remove
     */
    public boolean remove(GpxWaypointModel model) {
        boolean retVal = waypointModels.remove(model);
        if(retVal) {
            model.dispose();
            fireRemovedEvent(model);
        }
        return retVal;
    }

    /**
     * Adds an element to the GpxFileModel list at the end.
     * 
     * @param newModel The model to be added.
     * @return true if the add appears to be successful.
     */
    public boolean add(GpxWaypointModel model) {
        return add(null, model, PasteMode.END);
    }

    /**
     * Adds an element to the GpxWaypointModel list at the position specified by
     * the mode relative to the position of the old model.
     * 
     * @param oldModel The old model that specifies the relative location for
     *            the new one. Ignored if the mode is BEGINNING or END.
     * @param newModel The model to be added.
     * @param mode The PasteMode that determines where to place the new model
     *            relative to the old one.
     * @return true if the add appears to be successful.
     */
    public boolean add(GpxWaypointModel oldModel, GpxWaypointModel newModel,
        PasteMode mode) {
        boolean retVal = true;
        int i = -1;
        switch(mode) {
        case BEGINNING:
            waypointModels.addFirst(newModel);
            break;
        case BEFORE:
            i = waypointModels.indexOf(oldModel);
            if(i == -1) {
                retVal = false;
            } else {
                waypointModels.add(i, newModel);
            }
            break;
        case REPLACE:
        case AFTER:
            i = waypointModels.indexOf(oldModel);
            waypointModels.add(i + 1, newModel);
            break;
        case END:
            retVal = waypointModels.add(newModel);
            break;
        }
        if(retVal) {
            newModel.setParent(this);
            fireAddedEvent(newModel);
        }
        return retVal;
    }

    /**
     * Saves the GpxType in the original file.
     */
    public void save() {
        saveAs(file);
    }

    /**
     * Saves the GpxType in a new file.
     */
    public void saveAs(File file) {
        try {
            synchronizeGpx();
            GPXParser.save(
                "GPX Inspector "
                    + SWTUtils.getPluginVersion("net.kenevans.gpxinspector"),
                gpx, file);
            reset(file);
            fireChangedEvent(this);
            // Reset dirty, which was set by fireChangedEvent to true
            setDirty(false);
        } catch(JAXBException ex) {
            SWTUtils.excMsg("Error saving " + file.getPath(), ex);
        }
    }

    /**
     * Synchronizes the GpxType to the current model.
     */
    public void synchronizeGpx() {
        List<GpxTrackModel> tracks = getTrackModels();
        List<TrkType> trkTypes = gpx.getTrk();
        trkTypes.clear();
        for(GpxTrackModel model : tracks) {
            trkTypes.add(model.getTrack());
        }

        List<GpxRouteModel> routes = getRouteModels();
        List<RteType> rteTypes = gpx.getRte();
        rteTypes.clear();
        for(GpxRouteModel model : routes) {
            rteTypes.add(model.getRoute());
        }

        List<GpxWaypointModel> waypoints = getWaypointModels();
        List<WptType> wptTypes = gpx.getWpt();
        wptTypes.clear();
        for(GpxWaypointModel model : waypoints) {
            wptTypes.add(model.getWaypoint());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#clone()
     */
    @Override
    public Object clone() {
        GpxFileModel clone = new GpxFileModel();
        clone.parent = this.parent;
        clone.dirty = this.dirty;
        clone.file = new File(this.file.getPath());
        clone.gpx = GPXClone.clone(this.gpx);

        clone.trackModels = new LinkedList<GpxTrackModel>();
        for(GpxTrackModel model : trackModels) {
            clone.trackModels.add((GpxTrackModel)model.clone());
        }
        clone.routeModels = new LinkedList<GpxRouteModel>();
        for(GpxRouteModel model : routeModels) {
            clone.routeModels.add((GpxRouteModel)model.clone());
        }
        clone.waypointModels = new LinkedList<GpxWaypointModel>();
        for(GpxWaypointModel model : waypointModels) {
            clone.waypointModels.add((GpxWaypointModel)model.clone());
        }
        
        return clone;
    }

    /**
     * Overrides GpxModel.isDirty() and does not search for a parent
     * GpxFileModel.
     * 
     * @return The value of dirty for this GpxFileModel.
     */
    @Override
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Overrides GpxModel.setDirty() and does not search for a parent
     * GpxFileModel.
     * 
     * @param dirty The new value for dirty for this GpxFileModel.
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * @return The value of file.
     */
    public File getFile() {
        return file;
    }

    /**
     * @return The value of gpx.
     */
    public GpxType getGpx() {
        return gpx;
    }

    /**
     * @return The value of trackModels.
     */
    public List<GpxTrackModel> getTrackModels() {
        return trackModels;
    }

    /**
     * @return The value of routeModels.
     */
    public LinkedList<GpxRouteModel> getRouteModels() {
        return routeModels;
    }

    /**
     * @return The value of waypointModels.
     */
    public List<GpxWaypointModel> getWaypointModels() {
        return waypointModels;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(GpxFileModel o) {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#getLabel()
     */
    @Override
    public String getLabel() {
        if(file != null) {
            return file.getPath();
        }
        return "Null File";
    }

    /* (non-Javadoc)
     * @see net.kenevans.gpxinspector.model.GpxModel#setParent(net.kenevans.gpxinspector.model.GpxModel)
     */
    @Override
    public void setParent(GpxModel parent) {
        this.parent = parent;
        for(GpxTrackModel model : this.getTrackModels()) {
            model.setParent(this);
        }
        for(GpxRouteModel model : this.getRouteModels()) {
            model.setParent(this);
        }
        for(GpxWaypointModel model : this.getWaypointModels()) {
            model.setParent(this);
        }
    }

    /**
     * Prints the hierarchy of the given GpxFileModel.
     * 
     * @param fileModel
     * @return
     */
    public static String hierarchyInfo(GpxFileModel fileModel) {
        if(fileModel == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        buf.append(String.format("Hierarchy for %s %x\n", fileModel.getFile()
            .getName(), fileModel.hashCode()));
        buf.append(String.format("Parent%s %x\n", fileModel.getParent()
            .getClass().getName(), fileModel.getParent().hashCode()));
        buf.append(String.format("Tracks:\n"));
        for(GpxTrackModel model : fileModel.getTrackModels()) {
            buf.append(String.format("  %s %x parent %x\n", model.getLabel(),
                model.hashCode(), model.getParent().hashCode()));
        }
        buf.append(String.format("Routes:\n"));
        for(GpxRouteModel model : fileModel.getRouteModels()) {
            buf.append(String.format("  %s %x parent %x\n", model.getLabel(),
                model.hashCode(), model.getParent().hashCode()));
        }
        buf.append(String.format("Waypoints:\n"));
        for(GpxWaypointModel model : fileModel.getWaypointModels()) {
            buf.append(String.format("  %s %x parent %x\n", model.getLabel(),
                model.hashCode(), model.getParent().hashCode()));
        }

        return buf.toString();
    }
}
