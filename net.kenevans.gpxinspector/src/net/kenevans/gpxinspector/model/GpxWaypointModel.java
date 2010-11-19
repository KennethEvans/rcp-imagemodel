package net.kenevans.gpxinspector.model;

import java.util.ArrayList;
import java.util.List;

import net.kenevans.gpx.WptType;
import net.kenevans.gpxinspector.ui.WptInfoDialog;
import net.kenevans.gpxinspector.utils.SWTUtils;

import org.eclipse.swt.widgets.Display;

/*
 * Created on Aug 22, 2010
 * By Kenneth Evans, Jr.
 */

public class GpxWaypointModel extends GpxModel implements IGpxElementConstants
{
    private WptType waypoint;
    private List<GpxPropertyModel> propertyModels;

    public GpxWaypointModel(GpxModel parent, WptType waypoint) {
        this.parent = parent;
        this.waypoint = waypoint;

        // Add properties for this element
        propertyModels = new ArrayList<GpxPropertyModel>();
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
        WptInfoDialog dialog = null;
        boolean success = false;
        // Without this try/catch, the application hangs on error
        try {
            dialog = new WptInfoDialog(Display.getDefault().getActiveShell(),
                this);
            success = dialog.open();
            if(success) {
                fireChangedEvent(this);
                dirty = true;
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
        for(GpxModel model : propertyModels) {
            model.dispose();
        }
        propertyModels.clear();
        removeAllGpxModelListeners();
        disposed = true;
    }

    /**
     * @return The value of waypoint.
     */
    public WptType getWaypoint() {
        return waypoint;
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
     * @see net.kenevans.gpxinspector.model.GpxModel#getLabel()
     */
    @Override
    public String getLabel() {
        if(waypoint != null) {
            return waypoint.getName();
        }
        return "Null Waypoint";
    }

}
