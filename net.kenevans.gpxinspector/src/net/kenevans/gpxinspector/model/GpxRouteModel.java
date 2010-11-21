package net.kenevans.gpxinspector.model;

import net.kenevans.gpx.RteType;
import net.kenevans.gpxinspector.ui.RteInfoDialog;
import net.kenevans.gpxinspector.utils.SWTUtils;

import org.eclipse.swt.widgets.Display;

/*
 * Created on Aug 22, 2010
 * By Kenneth Evans, Jr.
 */

public class GpxRouteModel extends GpxModel implements IGpxElementConstants
{
    private RteType route;

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

}
