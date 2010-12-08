package net.kenevans.gpxinspector.ui;

import net.kenevans.gpx.ExtensionsType;
import net.kenevans.gpx.TrksegType;
import net.kenevans.gpxinspector.model.GpxTrackModel;
import net.kenevans.gpxinspector.model.GpxTrackSegmentModel;
import net.kenevans.gpxinspector.utils.LabeledList;
import net.kenevans.gpxinspector.utils.LabeledText;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

public class TrksegInfoDialog extends InfoDialog
{
    private GpxTrackSegmentModel model;
    private Text trkPointsText;
    private List extensionsList;

    /**
     * Constructor.
     * 
     * @param parent
     */
    public TrksegInfoDialog(Shell parent,
        GpxTrackSegmentModel gpxTrackSegmentModel) {
        // We want this to be modeless
        this(parent, SWT.DIALOG_TRIM | SWT.NONE, gpxTrackSegmentModel);
    }

    /**
     * Constructor.
     * 
     * @param parent The parent of this dialog.
     * @param style Style passed to the parent.
     */
    public TrksegInfoDialog(Shell parent, int style,
        GpxTrackSegmentModel gpxTrackSegmentModel) {
        super(parent, style);
        this.model = gpxTrackSegmentModel;
        String title = "";
        if(model != null) {
            GpxTrackModel trackModel = (GpxTrackModel)model.getParent();
            if(trackModel != null && trackModel.getLabel() != null) {
                title = trackModel.getLabel() + " ";
            }
            if(model.getLabel() != null) {
                title += model.getLabel();
            }
        }
        if(title == null || title.length() == 0) {
            title = "Track Segment Info";
        }
        setTitle(title);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.kenevans.gpxinspector.ui.InfoDialog#createControls(org.eclipse.swt
     * .widgets.Composite)
     */
    @Override
    protected void createControls(Composite parent) {
        // Create the groups
        createInfoGroup(parent);
    }

    /**
     * Creates the info group.
     * 
     * @param parent
     */
    private void createInfoGroup(Composite parent) {
        Group box = new Group(parent, SWT.BORDER);
        box.setText("Track Segment");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        box.setLayout(gridLayout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

        // Extensions
        LabeledList labeledList = new LabeledList(box, "Extensions:",
            TEXT_COLS_LARGE, LIST_ROWS);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledList.getComposite());
        extensionsList = labeledList.getList();
        extensionsList.setToolTipText("Extensions (Read only).");

        // Trackpoints
        LabeledText labeledText = new LabeledText(box, "Trackpoints:",
            TEXT_COLS_LARGE);
        labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        trkPointsText = labeledText.getText();
        trkPointsText.setToolTipText("Number of trackpoints.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.ui.InfoDialog#setModelFromWidgets()
     */
    @Override
    protected void setModelFromWidgets() {
        // The only field is Extensions which we don't handle yet
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.ui.InfoDialog#setWidgetsFromModel()
     */
    @Override
    protected void setWidgetsFromModel() {
        TrksegType trkseg = model.getTrackseg();
        ExtensionsType extType = trkseg.getExtensions();
        if(extType == null) {
            extensionsList.add("null");
        } else {
            java.util.List<Object> objs = extType.getAny();
            for(Object obj : objs) {
                extensionsList.add(obj.getClass().getName() + " "
                    + obj.toString());
            }
        }

        // Calculated
        int nPoints = trkseg.getTrkpt().size();
        trkPointsText.setText(Integer.toString(nPoints));
    }

    /**
     * @return The value of model.
     */
    public GpxTrackSegmentModel getModel() {
        return model;
    }

}
