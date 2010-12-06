package net.kenevans.gpxinspector.model;

import net.kenevans.gpx.TrkType;
import net.kenevans.gpxinspector.ui.TrkInfoDialog;
import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.parser.GPXClone;

import org.eclipse.swt.widgets.Display;

/*
 * Created on Aug 22, 2010
 * By Kenneth Evans, Jr.
 */

public class GpxTrackModel extends GpxModel implements IGpxElementConstants
{
    private TrkType track;

    /**
     * GpxTrackModel constructor which is private with no arguments for use in
     * clone.
     */
    private GpxTrackModel() {
    }

    public GpxTrackModel(GpxModel parent, TrkType track) {
        this.parent = parent;
        if(track == null) {
            this.track = new TrkType();
            this.track.setName("New Track");
       } else {
            this.track = track;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#showInfo()
     */
    @Override
    public void showInfo() {
        TrkInfoDialog dialog = null;
        boolean success = false;
        // Without this try/catch, the application hangs on error
        try {
            dialog = new TrkInfoDialog(Display.getDefault().getActiveShell(),
                this);
            success = dialog.open();
            if(success) {
                // This also sets dirty
                fireChangedEvent(this);
            }
        } catch(Exception ex) {
            SWTUtils.excMsgAsync("Error with TrkInfoDialog", ex);
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
        GpxTrackModel clone = new GpxTrackModel();
        clone.parent = this.parent;
        clone.track = GPXClone.clone(this.track);

        return clone;
    }

    /**
     * @return The value of track.
     */
    public TrkType getTrack() {
        return track;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#getLabel()
     */
    @Override
    public String getLabel() {
        if(track != null) {
            return track.getName();
        }
        return "Null Track";
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
