package net.kenevans.gpxinspector.kml;

import net.kenevans.gpxinspector.utils.SWTUtils;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

public class SaveKmlDialog extends Dialog
{
    private static final int TEXT_COLS = 50;
    private boolean success = false;
    private KmlOptions kmlOptions;

    private Text kmlNameText;
    private Button promptToOverwriteButton;
    private Button sendToGoogleButton;

    /**
     * Constructor.
     * 
     * @param parent
     */
    public SaveKmlDialog(Shell parent, KmlOptions kmlOptions) {
        // We want this to be modeless
        this(parent, SWT.DIALOG_TRIM | SWT.NONE, kmlOptions);
    }

    /**
     * Constructor.
     * 
     * @param parent The parent of this dialog.
     * @param style Style passed to the parent.
     */
    public SaveKmlDialog(Shell parent, int style, KmlOptions kmlOptions) {
        super(parent, style);
        this.kmlOptions = kmlOptions;
    }

    /**
     * Convenience method to open the dialog.
     * 
     * @return Whether OK was selected or not.
     */
    public boolean open() {
        Shell shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
        shell.setText("Save KML");
        // It can take a long time to do this so use a wait cursor
        // Probably not, though
        Cursor waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
        if(waitCursor != null) getParent().setCursor(waitCursor);
        createContents(shell);
        setWidgetsFromKmlOptions();
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

        Group box = new Group(shell, SWT.BORDER);
        box.setText("KML File");
        gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        box.setLayout(gridLayout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

        // Make a zero margin composite
        Composite browseComposite = new Composite(box, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true,
            false).applyTo(browseComposite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 3;
        browseComposite.setLayout(gridLayout);

        Label label = new Label(browseComposite, SWT.NONE);
        label.setText("File name");
        GridDataFactory.fillDefaults().applyTo(label);

        kmlNameText = new Text(browseComposite, SWT.NONE);
        GridDataFactory.fillDefaults().hint(
            new Point(SWTUtils.getTextWidth(kmlNameText, TEXT_COLS),
                SWT.DEFAULT)).align(SWT.FILL, SWT.FILL).grab(true, true)
            .applyTo(kmlNameText);
        kmlNameText.setToolTipText("Full path for KML file.");
        kmlNameText.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ex) {
                if(ex.keyCode == SWT.CR || ex.keyCode == SWT.KEYPAD_CR) {
                    // TODO
                    // String filename = kmlNameText.getText();
                    // File file = new File(filename);

                }
            }
        });
        kmlNameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent ev) {
                // TODO Auto-generated method stub
            }
        });
        // Create a drop target
        DropTarget dropTarget = new DropTarget(kmlNameText, DND.DROP_COPY
            | DND.DROP_DEFAULT);
        dropTarget.setTransfer(new Transfer[] {TextTransfer.getInstance(),
            FileTransfer.getInstance()});
        dropTarget.addDropListener(new DropTargetAdapter() {
            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.swt.dnd.DropTargetAdapter#dragEnter(org.eclipse.swt
             * .dnd.DropTargetEvent)
             */
            public void dragEnter(DropTargetEvent event) {
                if(event.detail == DND.DROP_DEFAULT) {
                    if((event.operations & DND.DROP_COPY) != 0) {
                        event.detail = DND.DROP_COPY;
                    } else {
                        event.detail = DND.DROP_NONE;
                    }
                }
            }

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.swt.dnd.DropTargetAdapter#drop(org.eclipse.swt.dnd
             * .DropTargetEvent)
             */
            public void drop(DropTargetEvent event) {
                // See if it is selectionText
                if(TextTransfer.getInstance().isSupportedType(
                    event.currentDataType)) {
                    String fileName = (String)event.data;
                    kmlNameText.setText(fileName);
                } else if(FileTransfer.getInstance().isSupportedType(
                    event.currentDataType)) {
                    String[] fileNames = (String[])event.data;
                    kmlNameText.setText(fileNames[0]);
                }
            }
        });

        Button button = new Button(browseComposite, SWT.PUSH);
        button.setText("Browse");
        button.setToolTipText("Select or enter a KML file.");
        GridDataFactory.fillDefaults().applyTo(button);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                FileDialog fileDlg = new FileDialog(shell, SWT.NONE);
                fileDlg.setFilterExtensions(new String[] {"*.kml"});
                fileDlg.setFilterNames(new String[] {"KML Files (*.kml)"});
                // fileDlg.setFilterPath(initialDirGve);
                fileDlg.setText("Select a KML file for Saving");
                String file = fileDlg.open();
                if(file != null) {
                    kmlNameText.setText(file);
                }
            }
        });

        // Make a zero margin composite
        Composite checkComposite = new Composite(box, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true,
            false).applyTo(checkComposite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 3;
        // Note
        gridLayout.makeColumnsEqualWidth = true;
        checkComposite.setLayout(gridLayout);

        promptToOverwriteButton = new Button(checkComposite, SWT.CHECK);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL).grab(
            true, false).applyTo(promptToOverwriteButton);
        promptToOverwriteButton.setText("Prompt to overwrite");
        promptToOverwriteButton
            .setToolTipText("Set whether there will be a prompt before "
                + "overwriting an existing KML file.");

        sendToGoogleButton = new Button(checkComposite, SWT.CHECK);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL).grab(
            true, false).applyTo(sendToGoogleButton);
        sendToGoogleButton.setText("Send to Google");
        sendToGoogleButton
            .setToolTipText("Set whether the KML file will be sent to "
                + "Google afterward.");

        // Make a zero margin composite for the OK and Cancel buttons
        Composite buttonComposite = new Composite(shell, SWT.NONE);
        // Change END to FILL to center the buttons
        GridDataFactory.fillDefaults().align(SWT.END, SWT.FILL).grab(true,
            false).applyTo(buttonComposite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 2;
        buttonComposite.setLayout(gridLayout);

        button = new Button(buttonComposite, SWT.PUSH);
        button.setText("OK");
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL).grab(true,
            true).applyTo(button);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setKmlOptionsFromWidgets();
                success = true;
                shell.close();
            }
        });
        shell.setDefaultButton(button);

        button = new Button(buttonComposite, SWT.PUSH);
        button.setText("Cancel");
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL).grab(true,
            true).applyTo(button);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                success = false;
                shell.close();
            }
        });
    }

    private void setKmlOptionsFromWidgets() {
        kmlOptions.setKmlFileName(kmlNameText.getText());
        kmlOptions.setPromptToOverwrite(promptToOverwriteButton.getSelection());
        kmlOptions.setSendToGoogle(sendToGoogleButton.getSelection());
    }

    private void setWidgetsFromKmlOptions() {
        kmlNameText.setText(kmlOptions.getKmlFileName());
        promptToOverwriteButton.setSelection(kmlOptions.getPromptToOverwrite());
        sendToGoogleButton.setSelection(kmlOptions.getSendToGoogle());
    }

    /**
     * @return The value of kmlOptions.
     */
    public KmlOptions getKmlOptions() {
        return kmlOptions;
    }

}
