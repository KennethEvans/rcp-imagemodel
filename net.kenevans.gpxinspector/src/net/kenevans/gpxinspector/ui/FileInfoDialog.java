package net.kenevans.gpxinspector.ui;

import java.io.File;
import java.util.Date;

import net.kenevans.gpx.GpxType;
import net.kenevans.gpx.TrkType;
import net.kenevans.gpxinspector.model.GpxFileModel;

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

public class FileInfoDialog extends Dialog
{
    private static final int TEXT_COLS_LARGE = 50;
    // private static final int TEXT_COLS_SMALL = 10;
    private boolean success = false;

    private GpxFileModel model;
    private Text nameText;
    private Text dateText;
    private Text sizeText;
    private Text creatorText;
    private Text versionText;

    /**
     * Constructor.
     * 
     * @param parent
     */
    public FileInfoDialog(Shell parent, GpxFileModel model) {
        // We want this to be modeless
        this(parent, SWT.DIALOG_TRIM | SWT.NONE, model);
    }

    /**
     * Constructor.
     * 
     * @param parent The parent of this dialog.
     * @param style Style passed to the parent.
     */
    public FileInfoDialog(Shell parent, int style, GpxFileModel model) {
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
        shell.setText("File");
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
        box.setText("File");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        box.setLayout(gridLayout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

        // Name
        LabeledText labeledText = new LabeledText(box, "Name:", TEXT_COLS_LARGE);
        labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().applyTo(labeledText.getComposite());
        nameText = labeledText.getText();
        nameText.setToolTipText("Name.");

        // Size
        labeledText = new LabeledText(box, "Size (bytes):", TEXT_COLS_LARGE);
        labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().applyTo(labeledText.getComposite());
        sizeText = labeledText.getText();
        sizeText.setToolTipText("Size.");

        // Date
        labeledText = new LabeledText(box, "Last Modified:", TEXT_COLS_LARGE);
        labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().applyTo(labeledText.getComposite());
        dateText = labeledText.getText();
        dateText.setToolTipText("Last modified date.");

        // Creator
        labeledText = new LabeledText(box, "Creator:", TEXT_COLS_LARGE);
        // labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().applyTo(labeledText.getComposite());
        creatorText = labeledText.getText();
        creatorText
            .setToolTipText("All GPX files must include the name of the application or\n"
                + "web service which created the file. This is to assist\n"
                + "developers in tracking down the source of a mal-formed GPX\n"
                + "document, and to provide a hint to users as to how they can\n"
                + "open the file.");

        // Version
        labeledText = new LabeledText(box, "Version:", TEXT_COLS_LARGE);
        labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().applyTo(labeledText.getComposite());
        versionText = labeledText.getText();
        versionText
            .setToolTipText("All GPX files must include the version of the GPX schema\n"
                + "which the file references.");
    }

    /**
     * Sets the values from the Text's to the model. Only does this if the Text
     * is editable.
     */
    private void setModelFromWidgets() {
        GpxType gpx = model.getGpx();
        Text text = creatorText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            gpx.setCreator(LabeledText.toString(text));
        }
        text = versionText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            gpx.setVersion(LabeledText.toString(text));
        }
    }

    /**
     * Sets the values form the model to the Text's.
     */
    private void setWidgetsFromModel() {
        File file = model.getFile();
        nameText.setText(file.getPath());
        long longVal = file.length();
        sizeText.setText(String.format("%d", longVal));
        longVal = file.lastModified();
        Date date = new Date(longVal);
        dateText.setText(date.toString());

        GpxType gpx = model.getGpx();
        LabeledText.read(creatorText, gpx.getCreator());
        LabeledText.read(versionText, gpx.getVersion());
    }

    /**
     * @return The value of model.
     */
    public GpxFileModel getModel() {
        return model;
    }

}
