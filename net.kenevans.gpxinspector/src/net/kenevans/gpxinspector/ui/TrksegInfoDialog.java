package net.kenevans.gpxinspector.ui;

import net.kenevans.gpx.ExtensionsType;
import net.kenevans.gpx.TrksegType;
import net.kenevans.gpxinspector.model.GpxTrackSegmentModel;
import net.kenevans.gpxinspector.utils.LabeledList;
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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

public class TrksegInfoDialog extends Dialog
{
    private static final int TEXT_COLS_LARGE = 50;
    private static final int LIST_ROWS = 2;
    // private static final int TEXT_COLS_SMALL = 10;
    private boolean success = false;

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
    }

    /**
     * Convenience method to open the dialog.
     * 
     * @return Whether OK was selected or not.
     */
    public boolean open() {
        Shell shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
        shell.setText("Track Segment");
        // It can take a long time to do this so use a wait cursor
        // Probably not, though
        Cursor waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
        if(waitCursor != null) getParent().setCursor(waitCursor);
        createContents(shell);
        setWidgetsFromModel();
        getParent().setCursor(null);
        waitCursor.dispose();
        shell.pack();
        // Resize it to fit the display
        int width = shell.getSize().x;
        int height = shell.getSize().y;
        int displayHeight = shell.getDisplay().getBounds().height;
        int displayWidth = shell.getDisplay().getBounds().width;
        if(displayHeight < height) {
            // Set the height to 2/3 the display height
            height = (20 * height / 30);
        }
        if(displayWidth < width) {
            // Set the width to 2/3 the display height
            width = (20 * width / 30);
        }
        shell.setSize(width, height);
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

    /**
     * Sets the values from the Text's to the model. Only does this if the Text
     * is editable.
     */
    private void setModelFromWidgets() {
        // The only field is Extensions which we don't handle yet
    }

    /**
     * Sets the values form the model to the Text's.
     */
    private void setWidgetsFromModel() {
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
