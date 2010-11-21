package net.kenevans.gpxinspector.model;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import net.kenevans.gpx.GpxType;
import net.kenevans.gpx.TrkType;
import net.kenevans.gpx.WptType;
import net.kenevans.gpxinspector.ui.FileInfoDialog;
import net.kenevans.gpxinspector.utils.SWTUtils;
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
    private LinkedList<GpxWaypointModel> waypointModels;
    private LinkedList<GpxPropertyModel> propertyModels;
    /** Indicates whether the file has changed or not. */
    private boolean dirty = false;

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
        waypointModels = new LinkedList<GpxWaypointModel>();
        List<WptType> waypoints = gpx.getWpt();
        for(WptType waypoint : waypoints) {
            waypointModels.add(new GpxWaypointModel(this, waypoint));
        }

        // Add properties for this element
        propertyModels = new LinkedList<GpxPropertyModel>();
        GpxPropertyModel model = new GpxPropertyModel(this, COLOR_KEY,
            COLOR_DEFAULT);
        propertyModels.add(model);
        model = new GpxPropertyModel(this, LINE_WIDTH_KEY, LINE_WIDTH_DEFAULT);
        propertyModels.add(model);
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
            SWTUtils.warnMsg(this.getLabel()
                + " has been modified and not saved");
        }
        for(GpxModel model : trackModels) {
            model.dispose();
        }
        trackModels.clear();
        for(GpxModel model : waypointModels) {
            model.dispose();
        }
        waypointModels.clear();
        for(GpxModel model : propertyModels) {
            model.dispose();
        }
        propertyModels.clear();
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
    public boolean remove(GpxTrackModel model) {
        boolean retVal = trackModels.remove(model);
        if(retVal) {
            model.dispose();
            fireRemovedEvent(model);
        }
        return retVal;
    }

    /**
     * Adds an element to the GpxFileModel track list.
     * 
     * @param model
     * @return true if the list changed (as specified by {@link Collection#add}
     *         ).
     * @see java.util.List#add
     */
    public boolean add(GpxTrackModel model) {
        boolean retVal = trackModels.add(model);
        if(retVal) {
            model.setParent(this);
            fireAddedEvent(model);
        }
        return retVal;
    }

    /**
     * Removes an element from the GpxFileModel waypoint list.
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
     * Adds an element to the GpxFileModel waypoint list.
     * 
     * @param model
     * @return true if the list changed (as specified by {@link Collection#add}
     *         ).
     * @see java.util.List#add
     */
    public boolean add(GpxWaypointModel model) {
        boolean retVal = waypointModels.add(model);
        if(retVal) {
            model.setParent(this);
            fireAddedEvent(model);
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
            GPXParser.save("GPX Inspector", gpx, file);
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
        
        List<GpxWaypointModel> waypoints = getWaypointModels();
        List<WptType> wptTypes = gpx.getWpt();
        wptTypes.clear();
        for(GpxWaypointModel model : waypoints) {
            wptTypes.add(model.getWaypoint());
        }
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
     * @return The value of waypointModels.
     */
    public List<GpxWaypointModel> getWaypointModels() {
        return waypointModels;
    }

    /**
     * @return The value of propertyModels.
     */
    public List<GpxPropertyModel> getPropertyModels() {
        return propertyModels;
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

}
