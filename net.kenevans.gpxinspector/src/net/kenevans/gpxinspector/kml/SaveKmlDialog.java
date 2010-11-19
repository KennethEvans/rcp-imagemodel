package net.kenevans.gpxinspector.kml;

import net.kenevans.gpxinspector.plugin.Activator;
import net.kenevans.gpxinspector.preferences.IPreferenceConstants;
import net.kenevans.gpxinspector.utils.SWTUtils;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import utils.LabeledText;

/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

public class SaveKmlDialog extends Dialog implements IPreferenceConstants
{
    private static final int TEXT_COLS_LARGE = 50;
    private static final int TEXT_COLS_SMALL = 10;
    private boolean success = false;
    private KmlOptions kmlOptions;

    private Text kmlNameText;
    private Text iconScaleText;
    private Text wptColorText;
    private Text wptAlphaText;
    private Text trkIconUrlText;
    private Text wptIconUrlText;
    private Text trkLineWidthText;
    private Text trkColorText;
    private Text trkAlphaText;
    private Combo trkColorModeCombo;
    private Button promptToOverwriteButton;
    private Button sendToGoogleButton;
    private Button useTrkIconButton;

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

        // Create the groups
        createKmlFileGroup(shell);
        createIconGroup(shell);
        createWaypointGroup(shell);
        createTrackGroup(shell);

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
        button.setText("Preferences");
        button.setToolTipText("Replace all settings with the current "
            + "preferences.");
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL)
            .grab(true, true).applyTo(button);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setWidgetsFromPreferences();
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

        button = new Button(composite, SWT.PUSH);
        button.setText("OK");
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL)
            .grab(true, true).applyTo(button);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setKmlOptionsFromWidgets();
                success = true;
                shell.close();
            }
        });
        shell.setDefaultButton(button);

    }

    /**
     * Creates the KML file group.
     * 
     * @param shell
     */
    private void createKmlFileGroup(final Shell shell) {
        Group box = new Group(shell, SWT.BORDER);
        box.setText("KML File");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        box.setLayout(gridLayout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

        // Make a zero margin composite
        Composite browseComposite = new Composite(box, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
            .grab(true, false).applyTo(browseComposite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 3;
        browseComposite.setLayout(gridLayout);

        Label label = new Label(browseComposite, SWT.NONE);
        label.setText("File name:");
        GridDataFactory.fillDefaults().applyTo(label);

        kmlNameText = new Text(browseComposite, SWT.NONE);
        GridDataFactory
            .fillDefaults()
            .hint(
                new Point(SWTUtils.getTextWidth(kmlNameText, TEXT_COLS_LARGE),
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
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
            .grab(true, false).applyTo(checkComposite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 3;
        // Note
        gridLayout.makeColumnsEqualWidth = true;
        checkComposite.setLayout(gridLayout);

        promptToOverwriteButton = new Button(checkComposite, SWT.CHECK);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL)
            .grab(true, false).applyTo(promptToOverwriteButton);
        promptToOverwriteButton.setText("Prompt to overwrite");
        promptToOverwriteButton
            .setToolTipText("Whether there will be a prompt before "
                + "overwriting an existing KML file.");

        sendToGoogleButton = new Button(checkComposite, SWT.CHECK);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL)
            .grab(true, false).applyTo(sendToGoogleButton);
        sendToGoogleButton.setText("Send to Google Earth");
        sendToGoogleButton
            .setToolTipText("Whether the KML file will be sent to "
                + "Google Earth afterward.");

    }

    /**
     * Creates the icons group.
     * 
     * @param shell
     */
    private void createIconGroup(Shell shell) {
        Group box = new Group(shell, SWT.BORDER);
        box.setText("Icons");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        box.setLayout(gridLayout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

        // Make a zero margin composite
        Composite composite = new Composite(box, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
            .grab(true, false).applyTo(composite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 2;
        // Note
        gridLayout.makeColumnsEqualWidth = true;
        gridLayout.horizontalSpacing = 10;
        composite.setLayout(gridLayout);

        // Wpt icon URL
        LabeledText labeledText = new LabeledText(composite, "Wpt icon URL:",
            TEXT_COLS_SMALL);
        GridDataFactory.fillDefaults().span(2, 1)
            .applyTo(labeledText.getComposite());
        wptIconUrlText = labeledText.getText();
        wptIconUrlText.setToolTipText("The URL for the waypoint icons.");

        // Trk icon URL
        labeledText = new LabeledText(composite, "Trk icon URL:",
            TEXT_COLS_SMALL);
        GridDataFactory.fillDefaults().span(2, 1)
            .applyTo(labeledText.getComposite());
        trkIconUrlText = labeledText.getText();
        trkIconUrlText.setToolTipText("The URL for the track icons.");

        // Icon scale
        labeledText = new LabeledText(composite, "Icon scale:", TEXT_COLS_SMALL);
        iconScaleText = labeledText.getText();
        iconScaleText.setToolTipText("The scale for all icons.");
    }

    /**
     * Creates the track group.
     * 
     * @param shell
     */
    private void createWaypointGroup(Shell shell) {
        Group box = new Group(shell, SWT.BORDER);
        box.setText("Waypoints");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        box.setLayout(gridLayout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

        // Make a zero margin composite
        Composite composite = new Composite(box, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
            .grab(true, false).applyTo(composite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 2;
        // Note
        gridLayout.makeColumnsEqualWidth = true;
        gridLayout.horizontalSpacing = 10;
        composite.setLayout(gridLayout);

        // Wpt alpha
        LabeledText labeledText = new LabeledText(composite, "Wpt alpha:",
            TEXT_COLS_SMALL);
        wptAlphaText = labeledText.getText();
        wptAlphaText.setToolTipText("The alpha of the waypoints.");

        // Wpt color
        labeledText = new LabeledText(composite, "Wpt color:", TEXT_COLS_SMALL);
        wptColorText = labeledText.getText();
        wptColorText.setToolTipText("The color of the waypoints.");
    }

    /**
     * Creates the track group.
     * 
     * @param shell
     */
    private void createTrackGroup(Shell shell) {
        Group box = new Group(shell, SWT.BORDER);
        box.setText("Tracks");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        box.setLayout(gridLayout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

        // Make a zero margin composite
        Composite composite = new Composite(box, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
            .grab(true, false).applyTo(composite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 2;
        // Note
        gridLayout.makeColumnsEqualWidth = true;
        gridLayout.horizontalSpacing = 10;
        composite.setLayout(gridLayout);

        // Trk alpha
        LabeledText labeledText = new LabeledText(composite, "Trk alpha:",
            TEXT_COLS_SMALL);
        trkAlphaText = labeledText.getText();
        trkAlphaText.setToolTipText("The alpha of the tracks.");

        // Trk color
        labeledText = new LabeledText(composite, "Trk color:", TEXT_COLS_SMALL);
        trkColorText = labeledText.getText();
        trkColorText.setToolTipText("The color of the tracks when "
            + "the color mode is Color.");

        // Trk line width
        labeledText = new LabeledText(composite, "Trk linewidth:",
            TEXT_COLS_SMALL);
        trkLineWidthText = labeledText.getText();
        trkLineWidthText.setToolTipText("The linewidth of the tracks.");

        // Trk color mode
        Composite composite1 = new Composite(composite, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
            .grab(true, false).applyTo(composite1);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 2;
        composite1.setLayout(gridLayout);

        Label label = new Label(composite1, SWT.NONE);
        label.setText("Trk color mode:");
        GridDataFactory.fillDefaults().applyTo(label);

        trkColorModeCombo = new Combo(composite1, SWT.NULL);
        GridDataFactory.fillDefaults().grab(true, true)
            .applyTo(trkColorModeCombo);
        int len = kmlColorModes.length;
        String[] items = new String[len];
        for(int i = 0; i < len; i++) {
            items[i] = kmlColorModes[i][0];
        }
        trkColorModeCombo.setItems(items);
        trkColorModeCombo.setToolTipText("The color mode for tracks.");

        // Use trk icon
        useTrkIconButton = new Button(composite, SWT.CHECK);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL)
            .grab(true, false).applyTo(useTrkIconButton);
        useTrkIconButton.setText("Use track icon");
        useTrkIconButton
            .setToolTipText("Whether the tracks will have a placemark "
                + "and icon at the start.");
    }

    private void setKmlOptionsFromWidgets() {
        kmlOptions.setKmlFileName(kmlNameText.getText());
        double val = Double.parseDouble(iconScaleText.getText());
        kmlOptions.setIconScale(val);
        kmlOptions.setWptColor(wptColorText.getText());
        kmlOptions.setWptAlpha(wptAlphaText.getText());
        kmlOptions.setTrkIconUrl(trkIconUrlText.getText());
        kmlOptions.setWptIconUrl(wptIconUrlText.getText());
        val = Double.parseDouble(trkLineWidthText.getText());
        kmlOptions.setTrkLineWidth(val);
        kmlOptions.setTrkColor(trkColorText.getText());
        kmlOptions.setTrkAlpha(trkAlphaText.getText());
        kmlOptions.setPromptToOverwrite(promptToOverwriteButton.getSelection());
        kmlOptions.setSendToGoogle(sendToGoogleButton.getSelection());
        kmlOptions.setUseTrkIcon(useTrkIconButton.getSelection());
        kmlOptions.setTrkColorMode(trkColorModeCombo.getSelectionIndex());
    }

    private void setWidgetsFromKmlOptions() {
        kmlNameText.setText(kmlOptions.getKmlFileName());
        String stringVal = String.format("%g", kmlOptions.getIconScale());
        iconScaleText.setText(stringVal);
        wptColorText.setText(kmlOptions.getWptColor());
        wptAlphaText.setText(kmlOptions.getWptAlpha());
        trkIconUrlText.setText(kmlOptions.getTrkIconUrl());
        wptIconUrlText.setText(kmlOptions.getWptIconUrl());
        stringVal = String.format("%g", kmlOptions.getTrkLineWidth());
        trkLineWidthText.setText(stringVal);
        trkColorText.setText(kmlOptions.getTrkColor());
        trkAlphaText.setText(kmlOptions.getTrkAlpha());
        promptToOverwriteButton.setSelection(kmlOptions.getPromptToOverwrite());
        sendToGoogleButton.setSelection(kmlOptions.getSendToGoogle());
        useTrkIconButton.setSelection(kmlOptions.getUseTrkIcon());
        trkColorModeCombo.select(kmlOptions.getTrkColorMode());
    }

    private void setWidgetsFromPreferences() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        kmlNameText.setText(prefs.getString(P_KML_FILENAME));
        iconScaleText.setText(prefs.getString(P_ICON_SCALE));
        wptColorText.setText(prefs.getString(P_WPT_COLOR));
        wptAlphaText.setText(prefs.getString(P_WPT_ALPHA));
        trkIconUrlText.setText(prefs.getString(P_TRK_ICON_URL));
        wptIconUrlText.setText(prefs.getString(P_WPT_ICON_URL));
        trkLineWidthText.setText(prefs.getString(P_TRK_LINEWIDTH));
        trkColorText.setText(prefs.getString(P_TRK_COLOR));
        trkAlphaText.setText(prefs.getString(P_TRK_ALPHA));
        promptToOverwriteButton.setSelection(prefs
            .getBoolean(P_KML_PROMPT_TO_OVERWRITE));
        sendToGoogleButton.setSelection(prefs
            .getBoolean(P_KML_SEND_TO_GOOGLE_EARTH));
        useTrkIconButton.setSelection(prefs.getBoolean(P_USE_TRK_ICON));
        trkColorModeCombo.select(prefs.getInt(P_TRK_COLOR_MODE));
    }

    /**
     * @return The value of kmlOptions.
     */
    public KmlOptions getKmlOptions() {
        return kmlOptions;
    }

}
