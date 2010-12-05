package net.kenevans.gpxinspector.model;

import net.kenevans.gpx.RteType;
import net.kenevans.gpxinspector.ui.RteInfoDialog;
import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.parser.GPXClone;

import org.eclipse.swt.widgets.Display;

/*
 * Created on Aug 22, 2010
 * By Kenneth Evans, Jr.
 */

public class GpxRouteModel extends GpxModel implements IGpxElementConstants
{
    private RteType route;

    /**
     * GpxRouteModel constructor which is private with no arguments for use in
     * clone.
     */
    private GpxRouteModel() {
    }

    public GpxRouteModel(GpxModel parent, RteType route) {
        this.parent = parent;
        this.route = route;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#showInfo()
     */
    @Override
    public void showInfo() {
        RteInfoDialog dialog = null;
        boolean success = false;
        // Without this try/catch, the application hangs on error
        try {
            dialog = new RteInfoDialog(Display.getDefault().getActiveShell(),
                this);
            success = dialog.open();
            if(success) {
                // This also sets dirty
                fireChangedEvent(this);
            }
        } catch(Exception ex) {
            SWTUtils.excMsgAsync("Error with RteInfoDialog", ex);
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

    /* (non-Javadoc)
     * @see net.kenevans.gpxinspector.model.GpxModel#clone()
     */
    @Override
    public Object clone() {
        GpxRouteModel clone = new GpxRouteModel();
        clone.parent = this.parent;
        clone.route = GPXClone.clone(this.route);
        
        return clone;
    }

    /**
     * @return The value of route.
     */
    public RteType getRoute() {
        return route;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#getLabel()
     */
    @Override
    public String getLabel() {
        if(route != null) {
            return route.getName();
        }
        return "Null Route";
    }

    /* (non-Javadoc)
     * @see net.kenevans.gpxinspector.model.GpxModel#setParent(net.kenevans.gpxinspector.model.GpxModel)
     */
    @Override
    public void setParent(GpxModel parent) {
        this.parent = parent;
    }

}
