package net.kenevans.gpxinspector.ui;

import java.math.BigDecimal;

import javax.xml.datatype.XMLGregorianCalendar;

import net.kenevans.gpx.WptType;
import net.kenevans.gpxinspector.model.GpxWaypointModel;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import utils.LabeledText;

/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

public class WptInfoDialog extends Dialog
{
    private static final int TEXT_COLS_LARGE = 50;
    // private static final int TEXT_COLS_SMALL = 10;
    private boolean success = false;

    private GpxWaypointModel model;
    private Text nameText;
    private Text descText;
    private Text latText;
    private Text lonText;
    private Text eleText;
    private Text cmtText;
    private Text typeText;
    private Text timeText;
    private Text fixText;
    private Text srcText;
    private Text symText;

    /**
     * Constructor.
     * 
     * @param parent
     */
    public WptInfoDialog(Shell parent, GpxWaypointModel model) {
        // We want this to be modeless
        this(parent, SWT.DIALOG_TRIM | SWT.NONE, model);
    }

    /**
     * Constructor.
     * 
     * @param parent The parent of this dialog.
     * @param style Style passed to the parent.
     */
    public WptInfoDialog(Shell parent, int style, GpxWaypointModel model) {
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
        shell.setText("Waypoint");
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
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        shell.setLayout(gridLayout);

        // Create the groups
        createInfoGroup(shell);

        // Create the buttons
        // Make a zero margin composite for the OK and Cancel buttons
        Composite composite = new Composite(shell, SWT.NONE);
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
    }

    /**
     * Creates the icons group.
     * 
     * @param shell
     */
    private void createInfoGroup(Shell shell) {
        Group box = new Group(shell, SWT.BORDER);
        box.setText("Waypoint");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        box.setLayout(gridLayout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

        // Name
        LabeledText labeledText = new LabeledText(box, "Name:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().applyTo(labeledText.getComposite());
        nameText = labeledText.getText();
        nameText.setToolTipText("The GPS name of the element.");

        // Desc
        labeledText = new LabeledText(box, "Desc:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().applyTo(labeledText.getComposite());
        descText = labeledText.getText();
        descText.setToolTipText("A text description of the element. Holds "
            + "additional information about the element intended for the "
            + "user, not the GPS. ");

        // Comment
        labeledText = new LabeledText(box, "Cmt:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().applyTo(labeledText.getComposite());
        cmtText = labeledText.getText();
        cmtText.setToolTipText("GPS waypoint comment. Sent to GPS as comment.");

        // Lat
        labeledText = new LabeledText(box, "Lat:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().applyTo(labeledText.getComposite());
        latText = labeledText.getText();
        latText.setToolTipText("The latitude of the point.");

        // Lon
        labeledText = new LabeledText(box, "Lon:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().applyTo(labeledText.getComposite());
        lonText = labeledText.getText();
        lonText.setToolTipText("The longitude of the point.");

        // Ele
        labeledText = new LabeledText(box, "Ele:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().applyTo(labeledText.getComposite());
        eleText = labeledText.getText();
        eleText.setToolTipText("Elevation in meters of the point.");

        // Symbol
        labeledText = new LabeledText(box, "Sym:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().applyTo(labeledText.getComposite());
        symText = labeledText.getText();
        symText.setToolTipText("Text of GPS symbol name. For interchange with "
            + "other programs, use the exact spelling of the symbol on the "
            + "GPS, if known.");

        // Fix
        labeledText = new LabeledText(box, "Fix:", TEXT_COLS_LARGE);
        labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().applyTo(labeledText.getComposite());
        fixText = labeledText.getText();
        fixText.setToolTipText("Type of GPX fix.");

        // Source
        labeledText = new LabeledText(box, "Src:", TEXT_COLS_LARGE);
        labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().applyTo(labeledText.getComposite());
        srcText = labeledText.getText();
        srcText.setToolTipText("Source of data. Included to give user some "
            + "idea of reliability and accuracy of data.");

        // Type
        labeledText = new LabeledText(box, "Type:", TEXT_COLS_LARGE);
        labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().applyTo(labeledText.getComposite());
        typeText = labeledText.getText();
        typeText.setToolTipText("Type (classification) of element.");

        // Time
        labeledText = new LabeledText(box, "Time:", TEXT_COLS_LARGE);
        labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().applyTo(labeledText.getComposite());
        timeText = labeledText.getText();
        timeText.setToolTipText("Creation/modification timestamp for element. "
            + "Date and time in are in Univeral Coordinated Time (UTC), not "
            + "local time! Conforms to ISO 8601 specification for date/time "
            + "representation. Fractional seconds are allowed for millisecond "
            + "timing in tracklogs. ");
    }

    private void setModelFromWidgets() {
        WptType wpt = model.getWaypoint();

        wpt.setName(nameText.getText());
        wpt.setDesc(descText.getText());
        wpt.setSym(symText.getText());
        double val = Double.parseDouble(latText.getText());
        wpt.setLat(BigDecimal.valueOf(val));
        val = Double.parseDouble(lonText.getText());
        wpt.setLon(BigDecimal.valueOf(val));
        val = Double.parseDouble(eleText.getText());
        wpt.setEle(BigDecimal.valueOf(val));
    }

    private void setWidgetsFromModel() {
        WptType wpt = model.getWaypoint();
        String stringVal = wpt.getName();
        if(stringVal != null) {
            nameText.setText(stringVal);
        } else {
            nameText.setText("");
        }
        stringVal = wpt.getDesc();
        if(stringVal != null) {
            descText.setText(stringVal);
        } else {
            descText.setText("");
        }
        stringVal = wpt.getCmt();
        if(stringVal != null) {
            cmtText.setText(stringVal);
        } else {
            cmtText.setText("");
        }
        stringVal = wpt.getType();
        if(stringVal != null) {
            typeText.setText(stringVal);
        } else {
            typeText.setText("null");
        }
        stringVal = wpt.getFix();
        if(stringVal != null) {
            fixText.setText(stringVal);
        } else {
            fixText.setText("null");
        }
        stringVal = wpt.getSrc();
        if(stringVal != null) {
            srcText.setText(stringVal);
        } else {
            srcText.setText("null");
        }
        stringVal = wpt.getSym();
        if(stringVal != null) {
            symText.setText(stringVal);
        } else {
            symText.setText("null");
        }
        BigDecimal bigDecimalVal = wpt.getLat();
        if(bigDecimalVal != null) {
            stringVal = String.format("%g", bigDecimalVal);
            latText.setText(stringVal);
        } else {
            latText.setText("0");
        }
        bigDecimalVal = wpt.getLon();
        if(bigDecimalVal != null) {
            stringVal = String.format("%g", bigDecimalVal);
            lonText.setText(stringVal);
        } else {
            lonText.setText("0");
        }
        bigDecimalVal = wpt.getEle();
        if(bigDecimalVal != null) {
            stringVal = String.format("%g", bigDecimalVal);
            eleText.setText(stringVal);
        } else {
            eleText.setText("0");
        }
        bigDecimalVal = wpt.getEle();
        if(bigDecimalVal != null) {
            stringVal = String.format("%g", bigDecimalVal);
            eleText.setText(stringVal);
        } else {
            eleText.setText("0");
        }
        XMLGregorianCalendar calendarVal = wpt.getTime();
        if(calendarVal != null) {
            stringVal = calendarVal.toString();
            timeText.setText(stringVal);
        } else {
            timeText.setText("null");
        }
    }

    /**
     * @return The value of model.
     */
    public GpxWaypointModel getModel() {
        return model;
    }

}
