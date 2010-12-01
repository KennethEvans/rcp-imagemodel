package net.kenevans.gpxinspector.views;

/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import net.kenevans.gpxinspector.model.GpxFileModel;
import net.kenevans.gpxinspector.model.GpxFileSetModel;
import net.kenevans.gpxinspector.model.GpxModel;
import net.kenevans.gpxinspector.model.GpxRouteModel;
import net.kenevans.gpxinspector.model.GpxTrackModel;
import net.kenevans.gpxinspector.model.GpxWaypointModel;
import net.kenevans.gpxinspector.plugin.Activator;
import net.kenevans.gpxinspector.preferences.IPreferenceConstants;
import net.kenevans.gpxinspector.ui.GpxCheckStateProvider;
import net.kenevans.gpxinspector.ui.GpxContentProvider;
import net.kenevans.gpxinspector.ui.GpxLabelProvider;
import net.kenevans.gpxinspector.ui.LocalSelection;
import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.gpxinspector.utils.ScrolledTextDialog;
import net.kenevans.gpxinspector.utils.find.FindNear;
import net.kenevans.gpxinspector.utils.find.FindNear.Mode;
import net.kenevans.gpxinspector.utils.find.FindNearOptions;
import net.kenevans.gpxinspector.utils.find.FindNearOptions.Units;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

/**
 * Insert the type's description here.
 * 
 * @see ViewPart
 */
public class GpxView extends ViewPart implements IPreferenceConstants
{
    private boolean showSelectionText = false;
    // private static String[] INITIAL_FILES = {
    // // "C:/Users/evans/Documents/GPSLink/AAA.gpx",
    // "C:/Users/evans/Documents/GPSLink/CabinWaypoints.gpx",
    // "C:/Users/evans/Documents/GPSLink/CM2001.gpx",
    // "C:/Users/evans/Documents/GPSLink/CM2002.gpx",
    // "C:/Users/evans/Documents/GPSLink/CM2003.gpx",
    // "C:/Users/evans/Documents/GPSLink/CM2004.gpx",
    // "C:/Users/evans/Documents/GPSLink/CM2005.gpx",
    // "C:/Users/evans/Documents/GPSLink/CM2006.gpx",
    // "C:/Users/evans/Documents/GPSLink/CM2007.gpx",
    // "C:/Users/evans/Documents/GPSLink/CM2005.gpx",
    // "C:/Users/evans/Documents/GPSLink/CM2008.gpx",
    // "C:/Users/evans/Documents/GPSLink/CM2009.gpx",
    // "C:/Users/evans/Documents/GPSLink/CM2010.gpx",
    // // "C:/Users/evans/Documents/GPSLink/M082010.gpx",
    // // "C:/Users/evans/Documents/GPSLink/Segmented.gpx",
    // };

    /** The separator used for the initial files preference */
    public static final String STARTUP_FILE_SEPARATOR = ",";
    /** Maximum number of messages printed for possible error storms */
    private static final int MAX_MESSAGES = 5;
    /** A listener on preferences property change. */
    private IPropertyChangeListener preferencesListener;

    protected CheckboxTreeViewer treeViewer;
    protected Text selectionText;
    protected GpxLabelProvider labelProvider;
    protected GpxCheckStateProvider checkStateProvider;

    // protected Action onlyBoardGamesAction, atLeatThreeItems;
    // protected Action booksBoxesGamesAction, noArticleAction;
    // protected ViewerFilter onlyBoardGamesFilter, atLeastThreeFilter;
    // protected ViewerSorter booksBoxesGamesSorter, noArticleSorter;

    protected GpxFileSetModel gpxFileSetModel;

    protected String initialPath;

    private double findLatitude = 46.068393;
    private double findLongitude = -89.596687;
    private double findRadius = FindNearOptions.DEFAULT_RADIUS;
    private Units findUnits = FindNearOptions.DEFAULT_UNITS;
    protected String gpxDirectory = D_GPX_DIR;

    /**
     * The maximum level to which the buttons can expand the tree. This should
     * be the maximum level it is possible to expand it. treeLevel is restricted
     * to be less than or equal to this value.
     */
    public static final int MAX_TREE_LEVEL = 3;
    /** The initial tree level */
    public static final int INITIAL_TREE_LEVEL = 1;
    /** The current level to which the tree is expanded. Should be non-negative. */
    private int treeLevel = INITIAL_TREE_LEVEL;

    /** The local clipboard */
    private Clipboard clipboard = new Clipboard("local");

    /**
     * The constructor.
     */
    public GpxView() {
    }

    /*
     * @see IWorkbenchPart#createPartControl(Composite)
     */
    public void createPartControl(Composite parent) {
        // Get preferences
        final IPreferenceStore prefs = Activator.getDefault()
            .getPreferenceStore();
        gpxDirectory = prefs.getString(P_GPX_DIR);
        preferencesListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if(event.getProperty().equals(P_GPX_DIR)) {
                    gpxDirectory = prefs.getString(P_GPX_DIR);
                }
            }
        };

        prefs.addPropertyChangeListener(preferencesListener);

        // DEBUG
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
                        SWTUtils.excMsgAsync("Error parsing " + fileName, ex);
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
                            SWTUtils.excMsgAsync("Error parsing " + fileName,
                                ex);
                        }
                    }
                }
            }
        });

        // Create menu, toolbars, filters, sorters.
        createFiltersAndSorters();
        createHandlers();
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

    /**
     * Adds a menu listener to hook the context menu when it is invoked.
     * 
     * @param control
     */
    private void hookContextMenu(Control control) {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        Menu menu = menuMgr.createContextMenu(control);
        control.setMenu(menu);
        getSite().registerContextMenu(menuMgr, treeViewer);
    }

    /**
     * Adds a ISelectionChangedListener to the tree. It is used to display the
     * selection in a Text. It only does something if showSelectionText is true,
     * and is only intended for debugging.
     */
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
                    // Remove the trailing comma space pair
                    if(toShow.length() > 0) {
                        toShow.setLength(toShow.length() - 2);
                    }
                    selectionText.setText(toShow.toString());
                }
            }
        });
    }

    /**
     * Creates handlers.
     */
    protected void createHandlers() {
        // Get the handler service from the view site
        IHandlerService handlerService = (IHandlerService)getSite().getService(
            IHandlerService.class);

        // Add
        AbstractHandler handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                openGpxFile();
                return null;
            }
        };
        String id = "net.kenevans.gpxinspector.add";
        handlerService.activateHandler(id, handler);

        // Save
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                saveGpxFiles();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.save";
        handlerService.activateHandler(id, handler);

        // SaveAs
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                saveGpxFilesAs();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.saveAs";
        handlerService.activateHandler(id, handler);

        // Remove
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                removeSelected();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.remove";
        handlerService.activateHandler(id, handler);

        // Collapse
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                collapseTreeToNextLevel();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.collapse";
        handlerService.activateHandler(id, handler);

        // Collapse all
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                collapseAll();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.collapseAll";
        handlerService.activateHandler(id, handler);

        // Expand
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                expandTreeToNextLevel();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.expand";
        handlerService.activateHandler(id, handler);

        // Check all
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                checkAll(true);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.checkAll";
        handlerService.activateHandler(id, handler);

        // Uncheck all
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                checkAll(false);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.uncheckAll";
        handlerService.activateHandler(id, handler);

        // Add startup files from preferences
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                addStartupFilesFromPreferences();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.addStartupFilesFromPreferences";
        handlerService.activateHandler(id, handler);

        // Save Checked as Startup Preference
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                saveCheckedAsStartupPreference();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.saveAsStartupPreference";
        handlerService.activateHandler(id, handler);

        // Remove all
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                removeAll();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.removeAll";
        handlerService.activateHandler(id, handler);

        // Show info
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                showInfo();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.showInfo";
        handlerService.activateHandler(id, handler);

        // Refresh
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                if(treeViewer != null) {
                    treeViewer.refresh();
                }
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.refreshTree";
        handlerService.activateHandler(id, handler);

        // Copy
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                copy();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.copy";
        handlerService.activateHandler(id, handler);

        // Paste
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                paste();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.paste";
        handlerService.activateHandler(id, handler);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        if(preferencesListener != null) {
            Activator.getDefault().getPreferenceStore()
                .removePropertyChangeListener(preferencesListener);
        }
        super.dispose();
    }

    /**
     * Show information for the selected item. If nothing is selected do
     * nothing.
     */
    protected void showInfo() {
        if(treeViewer.getSelection().isEmpty()) {
            SWTUtils.errMsg("Nothing selected");
            return;
        }
        IStructuredSelection selection = (IStructuredSelection)treeViewer
            .getSelection();
        for(Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            GpxModel model = (GpxModel)iterator.next();
            model.showInfo();
            // Only do the first one
            break;
        }
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
                } else if(model instanceof GpxRouteModel) {
                    ((GpxFileModel)parent).remove((GpxRouteModel)model);
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
     * Removes all items from the tree
     */
    private void removeAll() {
        // Tell the tree to not redraw until we finish removing everything
        treeViewer.getTree().setRedraw(false);
        List<GpxFileModel> fileSetModels = gpxFileSetModel.getGpxFileModels();
        fileSetModels.clear();
        treeViewer.getTree().setRedraw(true);
        treeViewer.refresh();
        treeLevel = 1;
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
            for(GpxRouteModel rteModel : fileModel.getRouteModels()) {
                check(rteModel, checked);
            }
            for(GpxWaypointModel wptModel : fileModel.getWaypointModels()) {
                check(wptModel, checked);
            }
        } else if(model instanceof GpxTrackModel) {
            model.setChecked(checked);
        } else if(model instanceof GpxRouteModel) {
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
                    SWTUtils.excMsgAsync("Error parsing " + fileName, ex);
                }
            }
        }
    }

    /**
     * Saves the selected GPX files.
     */
    public void saveGpxFiles() {
        if(treeViewer.getSelection().isEmpty()) {
            SWTUtils.errMsg("No files selected");
            return;
        }
        IStructuredSelection selection = (IStructuredSelection)treeViewer
            .getSelection();
        int count = 0;
        for(Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            GpxModel model = (GpxModel)iterator.next();
            if(model instanceof GpxFileModel) {
                ((GpxFileModel)model).save();
                treeViewer.refresh();
                count++;
            }
        }
        if(count == 0) {
            SWTUtils.errMsg("No files selected");
        } else {
            // Reset the input to cause the listeners to change
            Object oldInput = treeViewer.getInput();
            treeViewer.setInput(oldInput);
            treeViewer.expandToLevel(treeLevel);
        }
    }

    /**
     * Saves the selected GPX files with new names.
     */
    public void saveGpxFilesAs() {
        if(treeViewer.getSelection().isEmpty()) {
            SWTUtils.errMsg("No files selected");
            return;
        }
        IStructuredSelection selection = (IStructuredSelection)treeViewer
            .getSelection();
        int count = 0;
        for(Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            GpxModel model = (GpxModel)iterator.next();
            if(model instanceof GpxFileModel) {
                GpxFileModel fileModel = (GpxFileModel)model;
                // Open a FileDialog
                FileDialog dlg = new FileDialog(Display.getDefault()
                    .getActiveShell(), SWT.NONE);

                dlg.setFilterPath(fileModel.getFile().getPath());
                dlg.setFilterExtensions(new String[] {"*.gpx"});
                dlg.setFileName(fileModel.getFile().getName());
                String selectedPath = dlg.open();
                if(selectedPath != null) {
                    // Extract the directory part of the selectedPath
                    String initialDirectory = initialPath;
                    int index = selectedPath.lastIndexOf(File.separator);
                    if(index > 0) {
                        initialDirectory = selectedPath.substring(0, index);
                    }
                    initialPath = selectedPath;
                    // Loop over the selected file
                    String fileName = dlg.getFileName();
                    String filePath = initialDirectory + File.separator
                        + fileName;
                    File file = new File(filePath);
                    boolean doIt = true;
                    if(file.exists()) {
                        Boolean res = SWTUtils.confirmMsg("File exists: "
                            + file.getPath() + "\nOK to overwrite?");
                        if(!res) {
                            doIt = false;
                        }
                    }
                    if(doIt) {
                        fileModel.saveAs(file);
                        // Reset the input to cause the listeners to change
                        Object oldInput = treeViewer.getInput();
                        treeViewer.setInput(oldInput);
                        treeViewer.expandToLevel(treeLevel);
                    }
                }
                count++;
            }
        }
        if(count == 0) {
            SWTUtils.errMsg("No files selected");
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

    public GpxFileSetModel getInitalInput() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        boolean useStartupFiles = prefs.getBoolean(P_USE_STARTUP_FILES);
        String initalFilesString = prefs.getString(P_STARTUP_FILES);
        String[] initialFiles;
        try {
            // Note a blank string will give 1 blank item, not 0.
            if(!useStartupFiles || initalFilesString == null
                || initalFilesString.length() == 0) {
                initialFiles = new String[0];
            } else {
                initialFiles = initalFilesString.split(STARTUP_FILE_SEPARATOR);
            }
            gpxFileSetModel = new GpxFileSetModel(initialFiles);
        } catch(Exception ex) {
            SWTUtils.excMsgAsync("Error parsing initial input", ex);
        }
        return gpxFileSetModel;
    }

    /*
     * @see IWorkbenchPart#setFocus()
     */
    public void setFocus() {
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
     * Save the file in the cuurent GpxFileSetModel as the startup preferences,
     */
    private void saveCheckedAsStartupPreference() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        StringBuffer buf;
        List<GpxFileModel> fileSetModels = gpxFileSetModel.getGpxFileModels();
        buf = new StringBuffer(fileSetModels.size());
        for(GpxFileModel model : fileSetModels) {
            if(!model.getChecked()) {
                continue;
            }
            if(buf.length() > 0) {
                buf.append(STARTUP_FILE_SEPARATOR);
            }
            buf.append(model.getFile().getPath());
        }
        prefs.setValue(P_STARTUP_FILES, buf.toString());
    }

    /**
     * Save the file in the cuurent GpxFileSetModel as the startup preferences,
     */
    private void addStartupFilesFromPreferences() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        String initalFilesString = prefs.getString(P_STARTUP_FILES);
        String[] initialFiles = initalFilesString.split(STARTUP_FILE_SEPARATOR);
        GpxFileModel newModel = null;
        for(String fileName : initialFiles) {
            try {
                newModel = new GpxFileModel(gpxFileSetModel, fileName);
                gpxFileSetModel.add(newModel);
            } catch(JAXBException ex) {
                SWTUtils.excMsgAsync("Error parsing " + fileName, ex);
            }
        }
    }

    private void copy() {
        if(treeViewer.getSelection().isEmpty()) {
            SWTUtils.errMsg("Nothing selected");
            return;
        }
        IStructuredSelection selection = (IStructuredSelection)treeViewer
            .getSelection();
        List<GpxModel> list = new ArrayList<GpxModel>();
        GpxModel model = null;
        for(Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            model = (GpxModel)iterator.next();
            try {
                list.add((GpxModel)model.clone());
            } catch(Exception ex) {
                SWTUtils.excMsg("Problem adding to the clipboard", ex);
                return;
            }
        }
        if(list != null && list.size() > 0) {
            LocalSelection sel = new LocalSelection(list);
            clipboard.setContents(sel, null);
        }
    }

    private void paste() {
        if(treeViewer.getSelection().isEmpty()) {
            SWTUtils.errMsg("Nothing selected");
            return;
        }
        DataFlavor[] flavors = clipboard.getAvailableDataFlavors();
        if(flavors.length == 0) {
            SWTUtils.errMsg("Clipboard is empty");
            return;
        }
        Object data = null;
        try {
            DataFlavor flavor = new DataFlavor(
                "application/x-java-jvm-local-objectref;class=java.util.List");
            if(!clipboard.isDataFlavorAvailable(flavor)) {
                SWTUtils.errMsg("No compatible items in the clipboard");
                return;
            }
            data = clipboard.getData(flavor);
        } catch(Exception ex) {
            SWTUtils.excMsg("Error getting clipboard data", ex);
            return;
        }
        if(data == null) {
            return;
        }
        // Avoid unchecked cast warning in the smallest possible scope. To check
        // here you would have to use List<?> since you cannot perform
        // instanceof check against parameterized type List<GpxModel>.
        // That generic type information is erased at runtime. Using List<?>
        // doesn't eliminate the warning. Use tempList in the try block as the
        // @SuppressWarnings("unchecked") has to be where the value is declared
        // _and_ set.
        List<GpxModel> clipboardList = null;
        try {
            @SuppressWarnings("unchecked")
            List<GpxModel> tempList = (List<GpxModel>)data;
            clipboardList = tempList;
        } catch(Exception ex) {
            SWTUtils.excMsg("Error using clipboard data", ex);
            return;
        }
        if(clipboardList == null || clipboardList.size() == 0) {
            return;
        }
        treeViewer.getTree().setRedraw(false);
        IStructuredSelection selection = (IStructuredSelection)treeViewer
            .getSelection();
        GpxModel model = null;
        GpxModel parent = null;
        for(Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            model = (GpxModel)iterator.next();
            parent = model.getParent();
            if(parent == null) {
                // FIXME
                break;
            }
            if(parent instanceof GpxFileSetModel) {
                GpxFileSetModel fileSetModel = (GpxFileSetModel)parent;
                GpxFileModel fileModel = (GpxFileModel)model;
                for(GpxModel clipboardModel : clipboardList) {
                    if(clipboardModel instanceof GpxFileModel) {
                        fileSetModel.add((GpxFileModel)clipboardModel);
                    } else if(clipboardModel instanceof GpxTrackModel) {
                        fileModel.add((GpxTrackModel)clipboardModel);
                    } else if(clipboardModel instanceof GpxRouteModel) {
                        fileModel.add((GpxRouteModel)clipboardModel);
                    } else if(clipboardModel instanceof GpxWaypointModel) {
                        fileModel.add((GpxWaypointModel)clipboardModel);
                    }
                }
            } else if(parent instanceof GpxFileModel) {
                GpxFileModel fileModel = (GpxFileModel)parent;
                for(GpxModel clipboardModel : clipboardList) {
                    if(clipboardModel instanceof GpxTrackModel) {
                        fileModel.add((GpxTrackModel)clipboardModel);
                    } else if(clipboardModel instanceof GpxRouteModel) {
                        fileModel.add((GpxRouteModel)clipboardModel);
                    } else if(clipboardModel instanceof GpxWaypointModel) {
                        fileModel.add((GpxWaypointModel)clipboardModel);
                    }
                }
                // Only do one item for now
                // FIXME
                break;
            }
        }
        treeViewer.getTree().setRedraw(true);
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
     * @return The value of gpxDirectory.
     */
    public String getGpxDirectory() {
        return gpxDirectory;
    }

    /**
     * @param gpxDirectory The new value for gpxDirectory.
     */
    public void setGpxDirectory(String gpxDirectory) {
        this.gpxDirectory = gpxDirectory;
    }

}
