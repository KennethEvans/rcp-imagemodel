package net.kenevans.gpxinspector.ui;

import net.kenevans.gpx.TrkType;
import net.kenevans.gpx.TrksegType;
import net.kenevans.gpxinspector.model.GpxTrackModel;
import net.kenevans.gpxinspector.utils.LabeledText;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

public class TrkInfoDialog extends Dialog
{
    private static final int TEXT_COLS_LARGE = 50;
    // private static final int TEXT_COLS_SMALL = 10;
    private boolean success = false;

    private GpxTrackModel model;
    private Text nameText;
    private Text descText;
    private Text numberText;
    private Text srcText;
    private Text typeText;
    private Text segText;
    private Text trkPointsText;

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
    }

    /**
     * Convenience method to open the dialog.
     * 
     * @return Whether OK was selected or not.
     */
    public boolean open() {
        Shell shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
        shell.setText("Track");
        // It can take a long time to do this so use a wait cursor
        // Probably not, though
        Cursor waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
        if(waitCursor != null) getParent().setCursor(waitCursor);
        createContents(shell);
        setWidgetsFromModel();
        getParent().setCursor(null);
        waitCursor.dispose();
        shell.pack();
        shell.open();
        Display display = getParent().getDisplay();
        while(!shell.isDisposed()) {
            if(!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return success;
    }

    /**
     * Creates the contents of the dialog.
     * 
     * @param shell
     */
    private void createContents(final Shell shell) {
        shell.setLayout(new FillLayout());

        // Make it scroll
        ScrolledComposite scrolledComposite = new ScrolledComposite(shell,
            SWT.H_SCROLL | SWT.V_SCROLL);
        Composite parent = new Composite(scrolledComposite, SWT.NONE);
        scrolledComposite.setContent(parent);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        parent.setLayout(gridLayout);

        // Create the groups
        createInfoGroup(parent);

        // Create the buttons
        // Make a zero margin composite for the OK and Cancel buttons
        Composite composite = new Composite(parent, SWT.NONE);
        // Change END to FILL to center the buttons
        GridDataFactory.fillDefaults().align(SWT.END, SWT.FILL)
            .grab(true, false).applyTo(composite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 3;
        composite.setLayout(gridLayout);

        Button button = new Button(composite, SWT.PUSH);
        button.setText("Reset");
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL)
            .grab(true, true).applyTo(button);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setWidgetsFromModel();
            }
        });

        button = new Button(composite, SWT.PUSH);
        button.setText("Save");
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL)
            .grab(true, true).applyTo(button);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setModelFromWidgets();
                success = true;
                shell.close();
            }
        });

        button = new Button(composite, SWT.PUSH);
        button.setText("Cancel");
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL)
            .grab(true, true).applyTo(button);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                success = false;
                shell.close();
            }
        });
        shell.setDefaultButton(button);

        scrolledComposite.setMinSize(parent.computeSize(SWT.DEFAULT,
            SWT.DEFAULT));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
    }

    /**
     * Creates the icons group.
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

    /**
     * Sets the values from the Text's to the model. Only does this if the Text
     * is editable.
     */
    private void setModelFromWidgets() {
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

    /**
     * Sets the values form the model to the Text's.
     */
    private void setWidgetsFromModel() {
        TrkType trk = model.getTrack();
        LabeledText.read(descText, trk.getDesc());
        LabeledText.read(nameText, trk.getName());
        LabeledText.read(numberText, trk.getNumber());
        LabeledText.read(srcText, trk.getSrc());
        LabeledText.read(typeText, trk.getType());

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
        nPointsString = nPointsTotal + "=" + nPointsString;
        trkPointsText.setText(nPointsString);
    }

    /**
     * @return The value of model.
     */
    public GpxTrackModel getModel() {
        return model;
    }

}
