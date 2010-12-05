package net.kenevans.gpxinspector.model;

import net.kenevans.gpx.WptType;
import net.kenevans.gpxinspector.ui.WptInfoDialog;
import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.parser.GPXClone;

import org.eclipse.swt.widgets.Display;

/*
 * Created on Aug 22, 2010
 * By Kenneth Evans, Jr.
 */

public class GpxWaypointModel extends GpxModel implements IGpxElementConstants
{
    private WptType waypoint;

    /**
     * GpxWaypointModel constructor which is private with no arguments for use
     * in clone.
     */
    private GpxWaypointModel() {
    }

    public GpxWaypointModel(GpxModel parent, WptType waypoint) {
        this.parent = parent;
        this.waypoint = waypoint;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#showInfo()
     */
    @Override
    public void showInfo() {
        WptInfoDialog dialog = null;
        boolean success = false;
        // Without this try/catch, the application hangs on error
        try {
            dialog = new WptInfoDialog(Display.getDefault().getActiveShell(),
                this);
            success = dialog.open();
            if(success) {
                // This also sets dirty
                fireChangedEvent(this);
            }
        } catch(Exception ex) {
            SWTUtils.excMsgAsync("Error with WptInfoDialog", ex);
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
        removeAllGpxModelListeners();
        disposed = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#clone()
     */
    @Override
    public Object clone() {
        GpxWaypointModel clone = new GpxWaypointModel();
        clone.parent = this.parent;
        clone.waypoint = GPXClone.clone(this.waypoint);

        return clone;
    }

    /**
     * @return The value of waypoint.
     */
    public WptType getWaypoint() {
        return waypoint;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#getLabel()
     */
    @Override
    public String getLabel() {
        if(waypoint != null) {
            return waypoint.getName();
        }
        return "Null Waypoint";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.kenevans.gpxinspector.model.GpxModel#setParent(net.kenevans.gpxinspector
     * .model.GpxModel)
     */
    @Override
    public void setParent(GpxModel parent) {
        this.parent = parent;
    }

}
