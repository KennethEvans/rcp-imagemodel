package net.kenevans.gpxinspector.ui;

import net.kenevans.gpx.ExtensionsType;
import net.kenevans.gpx.TrkType;
import net.kenevans.gpx.TrksegType;
import net.kenevans.gpxinspector.model.GpxTrackModel;
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

public class TrkInfoDialog extends InfoDialog
{
    private GpxTrackModel model;
    private Text nameText;
    private Text descText;
    private Text numberText;
    private Text srcText;
    private Text typeText;
    private Text segText;
    private Text trkPointsText;
    private List extensionsList;

    /**
     * Constructor.
     * 
     * @param parent
     */
    public TrkInfoDialog(Shell parent, GpxTrackModel model) {
        // We want this to be modeless
        this(parent, SWT.DIALOG_TRIM | SWT.NONE, model);
    }

    /**
     * Constructor.
     * 
     * @param parent The parent of this dialog.
     * @param style Style passed to the parent.
     */
    public TrkInfoDialog(Shell parent, int style, GpxTrackModel model) {
        super(parent, style);
        this.model = model;
        if(model != null && model.getLabel() != null) {
            setTitle(model.getLabel());
        } else {
            setTitle("Track Info");
        }
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
        box.setText("Track");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        box.setLayout(gridLayout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

        // Name
        LabeledText labeledText = new LabeledText(box, "Name:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        nameText = labeledText.getText();
        nameText.setToolTipText("The GPS name of the element.");

        // Desc
        labeledText = new LabeledText(box, "Desc:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        descText = labeledText.getText();
        descText.setToolTipText("A text description of the element.Holds "
            + "additional\n"
            + "information about the element intended for the\n"
            + "user, not the GPS.");

        // Number
        labeledText = new LabeledText(box, "Number:", TEXT_COLS_LARGE);
        // labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        numberText = labeledText.getText();
        numberText.setToolTipText("GPS slot number for element.");

        // Source
        labeledText = new LabeledText(box, "Source:", TEXT_COLS_LARGE);
        // labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        srcText = labeledText.getText();
        srcText.setToolTipText("Source of data. Included to give user some\n"
            + "idea of reliability and accuracy of data.");

        // Type
        labeledText = new LabeledText(box, "Type:", TEXT_COLS_LARGE);
        // labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        typeText = labeledText.getText();
        typeText.setToolTipText("Type (classification) of element.");

        // Extensions
        LabeledList labeledList = new LabeledList(box, "Extensions:",
            TEXT_COLS_LARGE, LIST_ROWS);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledList.getComposite());
        extensionsList = labeledList.getList();
        extensionsList.setToolTipText("Extensions (Read only).");

        // Segments
        labeledText = new LabeledText(box, "Segments:", TEXT_COLS_LARGE);
        labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        segText = labeledText.getText();
        segText.setToolTipText("Number of segments.");

        // Trackpoints
        labeledText = new LabeledText(box, "Trackpoints:", TEXT_COLS_LARGE);
        labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        trkPointsText = labeledText.getText();
        trkPointsText.setToolTipText("Number of trackpoints by segment.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.ui.InfoDialog#setModelFromWidgets()
     */
    @Override
    protected void setModelFromWidgets() {
        TrkType trk = model.getTrack();
        Text text = descText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            trk.setDesc(LabeledText.toString(text));
        }
        text = nameText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            trk.setName(LabeledText.toString(text));
        }
        text = numberText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            trk.setNumber(LabeledText.toBigInteger(text));
        }
        text = srcText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            trk.setSrc(LabeledText.toString(text));
        }
        text = typeText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            trk.setType(LabeledText.toString(text));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.ui.InfoDialog#setWidgetsFromModel()
     */
    @Override
    protected void setWidgetsFromModel() {
        TrkType trk = model.getTrack();
        LabeledText.read(descText, trk.getDesc());
        LabeledText.read(nameText, trk.getName());
        LabeledText.read(numberText, trk.getNumber());
        LabeledText.read(srcText, trk.getSrc());
        LabeledText.read(typeText, trk.getType());
        ExtensionsType extType = trk.getExtensions();
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
        int intVal = trk.getTrkseg().size();
        segText.setText(String.format("%d", intVal));
        String nPointsString = "";
        int nPointsTotal = 0;
        int nPoints = 0;
        for(TrksegType seg : trk.getTrkseg()) {
            nPoints = seg.getTrkpt().size();
            nPointsString += "+" + nPoints;
            nPointsTotal += nPoints;
        }
        // Lose the first +
        if(nPointsString.length() > 0) {
            nPointsString = nPointsString.substring(1);
        }
        if(nPointsString.length() == 0 || intVal < 2) {
            nPointsString = Integer.toString(nPointsTotal);
        } else {
            nPointsString = nPointsTotal + "=" + nPointsString;
        }
        trkPointsText.setText(nPointsString);
    }

    /**
     * @return The value of model.
     */
    public GpxTrackModel getModel() {
        return model;
    }

}
