package net.kenevans.gpxinspector.ui;

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
import org.eclipse.swt.widgets.Shell;

/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

/**
 * A super class for an information dialog. The super class contains the
 * scrolling and the buttons at the bottom. Subclasses provide the rest of the
 * contents.
 * 
 * @author Kenneth Evans, Jr.
 */
public abstract class InfoDialog extends Dialog
{
    protected static final int TEXT_COLS_LARGE = 50;
    protected static final int LIST_ROWS = 2;
    protected static final int TEXT_COLS_SMALL = 10;
    protected boolean success = false;
    String title = "Info";
    Shell shell;

    /**
     * Constructor.
     * 
     * @param parent
     */
    public InfoDialog(Shell parent) {
        // We want this to be modeless
        this(parent, SWT.DIALOG_TRIM | SWT.NONE);
    }

    /**
     * Constructor.
     * 
     * @param parent The parent of this dialog.
     * @param style Style passed to the parent.
     */
    public InfoDialog(Shell parent, int style) {
        super(parent, style);
    }

    /**
     * Convenience method to open the dialog.
     * 
     * @return Whether OK was selected or not.
     */
    public boolean open() {
        shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
        shell.setText(title);
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
     * Creates the contents of the dialog. Makes a ScrolledCompostite with a
     * 1-column gridLayout in the Shell and creates the buttons at the bottom.
     * 
     * @param shell
     */
    protected void createContents(final Shell shell) {
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
        createControls(parent);

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
     * Creates the main contents of the dialog.
     * 
     * @param parent
     */
    abstract protected void createControls(Composite parent);

    /**
     * Sets the values from the Controls to the model. Should only do this for
     * editable Controls.
     */
    abstract protected void setModelFromWidgets();

    /**
     * Sets the values from the model to the Controls.
     */
    abstract protected void setWidgetsFromModel();

    /**
     * @return The value of shell.
     */
    public Shell getShell() {
        return shell;
    }

    /**
     * @return The value of title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title The new value for title.
     */
    public void setTitle(String title) {
        if(title != null) {
            this.title = title;
        }
    }

}
