package net.kenevans.gpxinspector.views;

/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.Iterator;

import javax.xml.bind.JAXBException;

import net.kenevans.gpxinspector.model.GpxFileModel;
import net.kenevans.gpxinspector.model.GpxFileSetModel;
import net.kenevans.gpxinspector.model.GpxModel;
import net.kenevans.gpxinspector.model.GpxTrackModel;
import net.kenevans.gpxinspector.model.GpxWaypointModel;
import net.kenevans.gpxinspector.plugin.Activator;
import net.kenevans.gpxinspector.ui.GpxCheckStateProvider;
import net.kenevans.gpxinspector.ui.GpxContentProvider;
import net.kenevans.gpxinspector.ui.GpxLabelProvider;
import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.gpxinspector.utils.find.FindNear;
import net.kenevans.gpxinspector.utils.find.FindNear.Mode;
import net.kenevans.gpxinspector.utils.find.FindNearOptions;
import net.kenevans.gpxinspector.utils.find.FindNearOptions.Units;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import utils.ScrolledTextDialog;

/**
 * Insert the type's description here.
 * 
 * @see ViewPart
 */
public class GpxView extends ViewPart
{
    private static final String SEARCH_DIR = "C:/Users/evans/Documents/GPSLink";
    private boolean showSelectionText = false;
    private static String[] TEST_FILES = {
        // "C:/Users/evans/Documents/GPSLink/AAA.gpx",
        "C:/Users/evans/Documents/GPSLink/CabinWaypoints.gpx",
        "C:/Users/evans/Documents/GPSLink/CM2001.gpx",
        "C:/Users/evans/Documents/GPSLink/CM2002.gpx",
        "C:/Users/evans/Documents/GPSLink/CM2003.gpx",
        "C:/Users/evans/Documents/GPSLink/CM2004.gpx",
        "C:/Users/evans/Documents/GPSLink/CM2005.gpx",
        "C:/Users/evans/Documents/GPSLink/CM2006.gpx",
        "C:/Users/evans/Documents/GPSLink/CM2007.gpx",
        "C:/Users/evans/Documents/GPSLink/CM2005.gpx",
        "C:/Users/evans/Documents/GPSLink/CM2008.gpx",
        "C:/Users/evans/Documents/GPSLink/CM2009.gpx",
        "C:/Users/evans/Documents/GPSLink/CM2010.gpx",
    // "C:/Users/evans/Documents/GPSLink/M082010.gpx",
    // "C:/Users/evans/Documents/GPSLink/Segmented.gpx",
    };

    /** Maximum number of messages printed for possible error storms */
    private static final int MAX_MESSAGES = 5;

    protected CheckboxTreeViewer treeViewer;
    protected Text selectionText;
    protected GpxLabelProvider labelProvider;
    protected GpxCheckStateProvider checkStateProvider;

    // protected Action onlyBoardGamesAction, atLeatThreeItems;
    // protected Action booksBoxesGamesAction, noArticleAction;
    // protected ViewerFilter onlyBoardGamesFilter, atLeastThreeFilter;
    // protected ViewerSorter booksBoxesGamesSorter, noArticleSorter;

    protected GpxFileSetModel gpxFileSetModel;

    protected Action addAction, removeAction;
    protected Action collapseAction;
    protected Action collapseAllAction;
    protected Action expandAction;
    protected Action checkAllAction;
    protected Action uncheckAllAction;

    protected String initialPath;

    private double findLatitude = 46.068393;
    private double findLongitude = -89.596687;
    private double findRadius = FindNearOptions.DEFAULT_RADIUS;
    private Units findUnits = FindNearOptions.DEFAULT_UNITS;
    protected String searchDirectory = SEARCH_DIR;

    /**
     * The maximum level to which the buttons can expand the tree. This should
     * be the maximum level it is possible to expand it. treeLevel is restricted
     * to be less than or equal to this value.
     */
    public static final int MAX_TREE_LEVEL = 4;
    /** The current level to which the tree is expanded. Should be non-negative. */
    private int treeLevel = 2;

    /**
     * The constructor.
     */
    public GpxView() {
    }

    /*
     * @see IWorkbenchPart#createPartControl(Composite)
     */
    public void createPartControl(Composite parent) {
        if(false) {
            try {
                Bundle bundle = Platform.getBundle("net.kenevans.jaxb");
                System.out.println("bundle: " + bundle);
                String symbolicName = bundle.getSymbolicName();
                System.out.println(symbolicName);
                Path path = new Path("/");
                URL url = FileLocator.resolve(FileLocator.find(bundle, path,
                    null));
                String pluginPath = url.getPath();
                System.out.println(pluginPath);
                if(pluginPath.startsWith("/")) {
                    pluginPath = pluginPath.substring(1);
                }
                File file = new File(pluginPath);
                System.setProperty("java.endorsed.dirs", file.getPath());
                System.out.println(System.getProperty("java.endorsed.dirs"));
                file = new File(file.getPath() + File.separator
                    + "jaxb-impl.jar");
                System.out.println(file.getPath() + ": " + file.exists());
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.verticalSpacing = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 2;
        parent.setLayout(layout);

        // Text to show the current selection
        if(showSelectionText) {
            selectionText = new Text(parent, SWT.READ_ONLY | SWT.SINGLE
                | SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false)
                .align(SWT.FILL, SWT.FILL).applyTo(selectionText);
        }

        // CheckboxTreeViewer (SWT.MULTI is needed to get multiple selection)
        treeViewer = new CheckboxTreeViewer(parent, SWT.MULTI | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, true)
            .align(SWT.FILL, SWT.FILL).applyTo(treeViewer.getControl());
        treeViewer.setContentProvider(new GpxContentProvider());
        labelProvider = new GpxLabelProvider();
        treeViewer.setLabelProvider(labelProvider);
        checkStateProvider = new GpxCheckStateProvider();
        treeViewer.setCheckStateProvider(checkStateProvider);
        treeViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent ev) {
                GpxModel model = (GpxModel)ev.getElement();
                if(model != null) {
                    model.setChecked(ev.getChecked());
                }
            }
        });
        treeViewer.setUseHashlookup(true);
        // Create a drop target
        DropTarget dropTarget = new DropTarget(treeViewer.getControl(),
            DND.DROP_COPY | DND.DROP_DEFAULT);
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
                // See if it is Text
                if(TextTransfer.getInstance().isSupportedType(
                    event.currentDataType)) {
                    String fileName = (String)event.data;
                    GpxFileModel model;
                    try {
                        model = new GpxFileModel(gpxFileSetModel, fileName);
                        gpxFileSetModel.add(model);
                    } catch(JAXBException ex) {
                        SWTUtils.excMsg("Error parsing " + fileName, ex);
                    }
                } else if(FileTransfer.getInstance().isSupportedType(
                    event.currentDataType)) {
                    String[] fileNames = (String[])event.data;
                    for(String fileName : fileNames) {
                        GpxFileModel model;
                        try {
                            model = new GpxFileModel(gpxFileSetModel, fileName);
                            gpxFileSetModel.add(model);
                        } catch(JAXBException ex) {
                            SWTUtils.excMsg("Error parsing " + fileName, ex);
                        }
                    }
                }
            }
        });

        // Create menu, toolbars, filters, sorters.
        createFiltersAndSorters();
        createActions();
        createMenus();
        createToolbar();
        hookListeners();
        hookContextMenu(treeViewer.getControl());

        // Initialize the CheckboxTreeViewer
        treeViewer.setInput(getInitalInput());
        treeViewer.expandToLevel(treeLevel);
    }

    protected void createFiltersAndSorters() {
        // atLeastThreeFilter = new ThreeItemFilter();
        // onlyBoardGamesFilter = new BoardgameFilter();
        // booksBoxesGamesSorter = new BookBoxBoardSorter();
        // noArticleSorter = new NoArticleSorter();
    }

    protected void hookListeners() {
        // DEBUG
        if(!showSelectionText) {
            return;
        }
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                // if the selection is empty clear the label
                if(event.getSelection().isEmpty()) {
                    selectionText.setText("");
                    return;
                }
                if(event.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection)event
                        .getSelection();
                    StringBuffer toShow = new StringBuffer();
                    for(Iterator<?> iterator = selection.iterator(); iterator
                        .hasNext();) {
                        Object domain = (GpxModel)iterator.next();
                        String value = labelProvider.getText(domain);
                        toShow.append(value);
                        toShow.append(", ");
                    }
                    // remove the trailing comma space pair
                    if(toShow.length() > 0) {
                        toShow.setLength(toShow.length() - 2);
                    }
                    selectionText.setText(toShow.toString());
                }
            }
        });
    }

    protected void createActions() {
        // onlyBoardGamesAction = new Action("Only Board Games") {
        // public void run() {
        // updateFilter(onlyBoardGamesAction);
        // }
        // };
        // onlyBoardGamesAction.setChecked(false);
        //
        // atLeatThreeItems = new Action("Boxes With At Least Three Items") {
        // public void run() {
        // updateFilter(atLeatThreeItems);
        // }
        // };
        // atLeatThreeItems.setChecked(false);
        //
        // booksBoxesGamesAction = new Action("Books, Boxes, Games") {
        // public void run() {
        // updateSorter(booksBoxesGamesAction);
        // }
        // };
        // booksBoxesGamesAction.setChecked(false);
        //
        // noArticleAction = new Action("Ignoring Articles") {
        // public void run() {
        // updateSorter(noArticleAction);
        // }
        // };
        // noArticleAction.setChecked(false);

        // Add
        addAction = new Action("Add") {
            public void run() {
                addSelected();
            }
        };
        addAction.setToolTipText("Add a new item to the tree.");
        addAction.setImageDescriptor(Activator.imageDescriptorFromPlugin(
            Activator.PLUGIN_ID, "icons/add.gif"));

        // Remove
        removeAction = new Action("Delete") {
            public void run() {
                removeSelected();
            }
        };
        removeAction
            .setToolTipText("Delete the selected item(s) from the tree.");
        removeAction.setImageDescriptor(Activator.imageDescriptorFromPlugin(
            Activator.PLUGIN_ID, "icons/remove.gif"));

        // Collapse
        collapseAction = new Action("Collapse Level") {
            @Override
            public void run() {
                collapseTreeToNextLevel();
            }
        };
        collapseAction.setToolTipText("Collapse the tree one level");
        collapseAction.setImageDescriptor(Activator.imageDescriptorFromPlugin(
            Activator.PLUGIN_ID, "icons/collapseall.gif"));

        // Collapse all
        collapseAllAction = new Action("Collapse All") {
            @Override
            public void run() {
                collapseAll();
            }
        };
        collapseAllAction.setToolTipText("Collapse the tree entirely");
        collapseAllAction.setImageDescriptor(Activator
            .imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                "icons/collapseall.gif"));

        // Expand
        expandAction = new Action("Expand Level") {
            @Override
            public void run() {
                expandTreeToNextLevel();
            }
        };
        expandAction.setToolTipText("Expand the tree one level");
        expandAction.setImageDescriptor(Activator.imageDescriptorFromPlugin(
            Activator.PLUGIN_ID, "icons/expandall.gif"));

        // Check all
        checkAllAction = new Action("Check All") {
            @Override
            public void run() {
                checkAll(true);
            }
        };
        checkAllAction.setToolTipText("Check this element and sub elements");

        // Unheck all
        uncheckAllAction = new Action("Uncheck All") {
            @Override
            public void run() {
                checkAll(false);
            }
        };
        uncheckAllAction
            .setToolTipText("Uncheck this element and sub elements");

    }

    /**
     * Remove the selected domain object(s). If multiple objects are selected
     * remove all of them.
     * 
     * If nothing is selected do nothing.
     */
    protected void addSelected() {
        GpxModel model = null;
        if(treeViewer.getSelection().isEmpty()) {
            // Use the root, should be the same as using gpxFileSetModel
            model = (GpxModel)treeViewer.getInput();
        } else {
            IStructuredSelection selection = (IStructuredSelection)treeViewer
                .getSelection();
            model = (GpxModel)selection.getFirstElement();
        }
        if(model == null) {
            return;
        }
        // Eventually fix this to add elements of the type supported by the
        // selected model. For now always add a file to the GpxFileSetModel
        openGpxFile();
        // if(model instanceof GpxFileSetModel) {
        // openGpxFile();
        // } else {
        // SWTUtils.errMsg("Add not implemented for "
        // + model.getClass().getName());
        // }
    }

    /**
     * Remove the selected domain object(s). If multiple objects are selected
     * remove all of them.
     * 
     * If nothing is selected do nothing.
     */
    protected void removeSelected() {
        if(treeViewer.getSelection().isEmpty()) {
            SWTUtils.errMsg("Nothing selected");
            return;
        }
        IStructuredSelection selection = (IStructuredSelection)treeViewer
            .getSelection();
        /*
         * Tell the tree to not redraw until we finish removing all the selected
         * children.
         */
        treeViewer.getTree().setRedraw(false);
        int count = 0;
        for(Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            GpxModel model = (GpxModel)iterator.next();
            GpxModel parent = model.getParent();
            if(parent instanceof GpxFileSetModel) {
                if(model instanceof GpxFileModel) {
                    ((GpxFileSetModel)parent).remove((GpxFileModel)model);
                }
            } else if(parent instanceof GpxFileModel) {
                if(model instanceof GpxTrackModel) {
                    ((GpxFileModel)parent).remove((GpxTrackModel)model);
                } else if(model instanceof GpxWaypointModel) {
                    ((GpxFileModel)parent).remove((GpxWaypointModel)model);
                }
            } else {
                if(count < MAX_MESSAGES) {
                    String parentClass = (parent == null) ? null : parent
                        .getClass().getName();
                    SWTUtils.errMsg("Delete not implemented for "
                        + model.getClass().getName() + " with parent "
                        + parentClass);
                    count++;
                } else if(count == MAX_MESSAGES) {
                    // Avoid error storms
                    SWTUtils
                        .errMsg("Will not show any more not-implemented messages");
                    count++;
                }
            }
            treeViewer.getTree().setRedraw(true);
        }
    }

    /**
     * Check all selected elements and their children to the specified state. If
     * no selection, check everything.
     * 
     * @param checked
     */
    protected void checkAll(boolean checked) {
        // Note that we cannot use treeviewer.setSubtreeChecked, etc. as they
        // do not notify listeners so the underlying model is not changed. And
        // they tend to only apply to visible children.
        treeViewer.getTree().setRedraw(false);
        if(treeViewer.getSelection().isEmpty()) {
            check(gpxFileSetModel, checked);
        } else {
            IStructuredSelection selection = (IStructuredSelection)treeViewer
                .getSelection();
            for(Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
                GpxModel model = (GpxModel)iterator.next();
                check(model, checked);
            }
        }
        treeViewer.getTree().setRedraw(true);
        treeViewer.refresh();
    }

    /**
     * Recursively sets the check state to the specified value for the model and
     * its children.
     * 
     * @param model
     * @param checked
     */
    protected void check(GpxModel model, boolean checked) {
        if(model instanceof GpxFileSetModel) {
            GpxFileSetModel fileSetModel = (GpxFileSetModel)model;
            model.setChecked(checked);
            for(GpxFileModel fileModel : fileSetModel.getGpxFileModels()) {
                check(fileModel, checked);
            }
        } else if(model instanceof GpxFileModel) {
            GpxFileModel fileModel = (GpxFileModel)model;
            model.setChecked(checked);
            for(GpxTrackModel trkModel : fileModel.getTrackModels()) {
                check(trkModel, checked);
            }
            for(GpxWaypointModel wptModel : fileModel.getWaypointModels()) {
                check(wptModel, checked);
            }
        } else if(model instanceof GpxTrackModel) {
            model.setChecked(checked);
        } else if(model instanceof GpxWaypointModel) {
            model.setChecked(checked);
        }
    }

    /**
     * Brings up a FileDialog to prompt for a GPX file and then adds it to the
     * GpxFileSetModel.
     */
    public void openGpxFile() {
        // Open a FileDialog
        FileDialog dlg = new FileDialog(Display.getDefault().getActiveShell(),
            SWT.MULTI);

        dlg.setFilterPath(initialPath);
        dlg.setFilterExtensions(new String[] {"*.gpx"});
        String selectedPath = dlg.open();
        if(selectedPath != null) {
            initialPath = selectedPath;
            // Extract the directory part of the selectedPath
            String initialDirectory = initialPath;
            int index = selectedPath.lastIndexOf(File.separator);
            if(index > 0) {
                initialDirectory = selectedPath.substring(0, index);

            }
            // Loop over the selected files
            String fileNames[] = dlg.getFileNames();
            GpxFileModel newModel = null;
            String filePath = null;
            for(String fileName : fileNames) {
                filePath = initialDirectory + File.separator + fileName;
                try {
                    newModel = new GpxFileModel(gpxFileSetModel, filePath);
                    gpxFileSetModel.add(newModel);
                } catch(JAXBException ex) {
                    SWTUtils.excMsg("Error parsing " + fileName, ex);
                }
            }
        }
    }

    /**
     * Sets the check state based on the given FindNearOptions.
     * 
     * @param options
     */
    public void setCheckedFromFindNear(FindNearOptions options) {
        if(options == null) {
            SWTUtils.errMsgAsync("FindNearOptions is null");
            return;
        }
        FindNear fn = new FindNear(options.getLatitude(),
            options.getLongitude(), options.getRadius(), options.getUnits());
        if(fn == null) {
            SWTUtils.errMsgAsync("Could not create FindNear instance");
            return;
        }
        Shell shell = Display.getCurrent().getActiveShell();
        // TODO Fix this
        Cursor waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
        if(waitCursor != null) shell.setCursor(waitCursor);
        treeViewer.getTree().setRedraw(false);
        for(GpxFileModel model : gpxFileSetModel.getGpxFileModels()) {
            fn.processGpxFileModel(model, Mode.CHECK, options);
        }
        treeViewer.getTree().setRedraw(true);
        treeViewer.refresh();
        shell.setCursor(null);
        waitCursor.dispose();
    }

    /**
     * Sets the check state based on the given FindFilesNearOptions. The
     * implementation is inefficient and based on parsing the output from the
     * command-line find. Use addFromFindFilesNear(FindNearOptions) instead.
     * 
     * 
     * @param options
     * @deprecated
     * @see #addFromFindFilesNear(FindNearOptions)
     */
    public void setCheckedFromFindFilesNear(FindNearOptions options) {
        if(options == null) {
            SWTUtils.errMsgAsync("FindNearOptions is null");
            return;
        }
        FindNear fn = new FindNear(options.getLatitude(),
            options.getLongitude(), options.getRadius(), options.getUnits());
        if(fn == null) {
            SWTUtils.errMsgAsync("Could not create FindNear instance");
            return;
        }
        Shell shell = Display.getCurrent().getActiveShell();
        // TODO Fix this
        Cursor waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
        if(waitCursor != null) shell.setCursor(waitCursor);
        treeViewer.getTree().setRedraw(false);
        String errors = fn.findAndAddToFileSetModel(gpxFileSetModel, options);
        // All, done, restore things
        treeViewer.getTree().setRedraw(true);
        treeViewer.refresh();
        shell.setCursor(null);
        waitCursor.dispose();

        // Display errors
        if(errors != null && errors.length() > 0) {
            boolean res = SWTUtils.confirmMsg(shell,
                "Errors occurred. Want to see them?");
            if(res) {
                ScrolledTextDialog dialog = new ScrolledTextDialog(shell);
                dialog.setDialogTitle("Find Files Near Errors");
                dialog.setGroupLabel("Errors");
                dialog.setMessage(errors);
                dialog.setWidth(600);
                dialog.setHeight(400);
                dialog.open();
            }
        }
    }

    /**
     * Adds trimmed GpxFileModels to the gpxFileSetModel based on the given
     * FindFilesNearOptions.
     * 
     * @param options
     */
    public void addFromFindFilesNear(FindNearOptions options) {
        if(options == null) {
            SWTUtils.errMsgAsync("FindNearOptions is null");
            return;
        }
        FindNear fn = new FindNear(options.getLatitude(),
            options.getLongitude(), options.getRadius(), options.getUnits());
        if(fn == null) {
            SWTUtils.errMsgAsync("Could not create FindNear instance");
            return;
        }
        Shell shell = Display.getCurrent().getActiveShell();
        // TODO Fix this
        Cursor waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
        if(waitCursor != null) shell.setCursor(waitCursor);
        treeViewer.getTree().setRedraw(false);

        // Null the out PrintStream
        // DEBUG Comment out nulling the out PrintStream
        fn.setOutStream(null);
        // Capture the err PrintStream
        ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(baosErr);
        fn.setErrStream(err);

        // Don't do GPSL
        options.setDoGpsl(false);
        // Do the search
        fn.find(gpxFileSetModel, options, Mode.ADD);
        err.close();

        // Restore things
        treeViewer.getTree().setRedraw(true);
        treeViewer.refresh();
        treeViewer.expandToLevel(treeLevel);
        shell.setCursor(null);
        waitCursor.dispose();

        // Display errors
        String errors = baosErr.toString();
        if(errors != null && errors.length() > 0) {
            boolean res = SWTUtils.confirmMsg(shell,
                "Errors occurred. Want to see them?");
            if(res) {
                ScrolledTextDialog dialog = new ScrolledTextDialog(shell);
                dialog.setDialogTitle("Find Files Near Errors");
                dialog.setGroupLabel("Errors");
                dialog.setMessage(errors);
                dialog.setWidth(600);
                dialog.setHeight(400);
                dialog.open();
            }
        }
    }

    protected void createMenus() {
        IMenuManager rootMenuManager = getViewSite().getActionBars()
            .getMenuManager();
        rootMenuManager.setRemoveAllWhenShown(false);
        // rootMenuManager.setRemoveAllWhenShown(true);
        // rootMenuManager.addMenuListener(new IMenuListener() {
        // public void menuAboutToShow(IMenuManager mgr) {
        // fillMenu(mgr);
        // }
        // });
        fillMenu(rootMenuManager);
    }

    protected void fillMenu(IMenuManager rootMenuManager) {
        // IMenuManager filterSubmenu = new MenuManager("Filters");
        // rootMenuManager.add(filterSubmenu);
        // filterSubmenu.add(onlyBoardGamesAction);
        // filterSubmenu.add(atLeatThreeItems);
        //
        // IMenuManager sortSubmenu = new MenuManager("Sort By");
        // rootMenuManager.add(sortSubmenu);
        // sortSubmenu.add(booksBoxesGamesAction);
        // sortSubmenu.add(noArticleAction);
        //
        // rootMenuManager.add(new Separator());
        rootMenuManager.add(addAction);
        rootMenuManager.add(removeAction);
        rootMenuManager.add(new Separator());
        rootMenuManager.add(checkAllAction);
        rootMenuManager.add(uncheckAllAction);
        rootMenuManager.add(new Separator());
        rootMenuManager.add(expandAction);
        rootMenuManager.add(collapseAction);
        rootMenuManager.add(collapseAllAction);
        rootMenuManager.add(new Separator(
            IWorkbenchActionConstants.MB_ADDITIONS));
    }

    protected void updateSorter(Action action) {
        // if(action == booksBoxesGamesAction) {
        // noArticleAction.setChecked(!booksBoxesGamesAction.isChecked());
        // if(action.isChecked()) {
        // treeViewer.setSorter(booksBoxesGamesSorter);
        // } else {
        // treeViewer.setSorter(null);
        // }
        // } else if(action == noArticleAction) {
        // booksBoxesGamesAction.setChecked(!noArticleAction.isChecked());
        // if(action.isChecked()) {
        // treeViewer.setSorter(noArticleSorter);
        // } else {
        // treeViewer.setSorter(null);
        // }
        // }
    }

    /* Multiple filters can be enabled at a time. */
    protected void updateFilter(Action action) {
        // if(action == atLeatThreeItems) {
        // if(action.isChecked()) {
        // treeViewer.addFilter(atLeastThreeFilter);
        // } else {
        // treeViewer.removeFilter(atLeastThreeFilter);
        // }
        // } else if(action == onlyBoardGamesAction) {
        // if(action.isChecked()) {
        // treeViewer.addFilter(onlyBoardGamesFilter);
        // } else {
        // treeViewer.removeFilter(onlyBoardGamesFilter);
        // }
        // }
    }

    protected void createToolbar() {
        IToolBarManager toolbarManager = getViewSite().getActionBars()
            .getToolBarManager();
        toolbarManager.add(addAction);
        toolbarManager.add(removeAction);
        toolbarManager.add(expandAction);
        toolbarManager.add(collapseAction);
    }

    public GpxFileSetModel getInitalInput() {
        try {
            gpxFileSetModel = new GpxFileSetModel(TEST_FILES);
        } catch(JAXBException ex) {
            SWTUtils.excMsg("Error parsing initial input", ex);
        }
        return gpxFileSetModel;
    }

    /*
     * @see IWorkbenchPart#setFocus()
     */
    public void setFocus() {
    }

    /**
     * Adds a menu listener to hook the context menu when it is invoked.
     * 
     * @param control
     */
    private void hookContextMenu(Control control) {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                GpxView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(control);
        control.setMenu(menu);
        getSite().registerContextMenu(menuMgr, treeViewer);
    }

    /**
     * Adds the contents to the context menu.
     * 
     * @param manager
     */
    private void fillContextMenu(IMenuManager manager) {
        manager.add(checkAllAction);
        manager.add(uncheckAllAction);
        manager.add(new Separator());
        manager.add(expandAction);
        manager.add(collapseAction);
        manager.add(collapseAllAction);
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    /**
     * Collapses the tree one level if not already fully collapsed.
     */
    private void collapseTreeToNextLevel() {
        if(treeLevel > 1) {
            // KE: Can't use viewer.collapseToLevel(elementOrTreePath, level)
            // It appears to collapse the levels up to level, rather than above
            // level. There is no viewer.collapseToLevel(level). If this _were_
            // used, the right thing to use for elementOrTreePath is
            // viewer.getInput(), which is the same as the protected
            // viewer.getRoot(). We need to do use this kludge.
            treeLevel--;
            treeViewer.collapseAll();
            treeViewer.expandToLevel(treeLevel);
        }
    }

    /**
     * Expands the tree one level.
     */
    private void expandTreeToNextLevel() {
        if(treeLevel <= MAX_TREE_LEVEL) {
            treeLevel++;
            treeViewer.expandToLevel(treeLevel);
        }
    }

    /**
     * Expands the tree one level.
     */
    private void collapseAll() {
        treeViewer.collapseAll();
        treeLevel = 1;
    }

    /**
     * @return The value of gpxFileSetModel.
     */
    public GpxFileSetModel getGpxFileSetModel() {
        return gpxFileSetModel;
    }

    /**
     * @return The value of findLatitude.
     */
    public double getFindLatitude() {
        return findLatitude;
    }

    /**
     * @param findLatitude The new value for findLatitude.
     */
    public void setFindLatitude(double findLatitude) {
        this.findLatitude = findLatitude;
    }

    /**
     * @return The value of findLongitude.
     */
    public double getFindLongitude() {
        return findLongitude;
    }

    /**
     * @param findLongitude The new value for findLongitude.
     */
    public void setFindLongitude(double findLongitude) {
        this.findLongitude = findLongitude;
    }

    /**
     * @return The value of findRadius.
     */
    public double getFindRadius() {
        return findRadius;
    }

    /**
     * @param findRadius The new value for findRadius.
     */
    public void setFindRadius(double findRadius) {
        this.findRadius = findRadius;
    }

    /**
     * @return The value of findUnits.
     */
    public Units getFindUnits() {
        return findUnits;
    }

    /**
     * @param findUnits The new value for findUnits.
     */
    public void setFindUnits(Units findUnits) {
        this.findUnits = findUnits;
    }

    /**
     * @return The value of searchDirectory.
     */
    public String getSearchDirectory() {
        return searchDirectory;
    }

    /**
     * @param searchDirectory The new value for searchDirectory.
     */
    public void setSearchDirectory(String searchDirectory) {
        this.searchDirectory = searchDirectory;
    }

}
