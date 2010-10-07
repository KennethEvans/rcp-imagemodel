package net.kenevans.gpxinspector.plugin;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor
{
    public MenuManager fileMenu;
    public MenuManager windowMenu;
    public MenuManager helpMenu;

    // Actions - important to allocate these only in makeActions, and then use
    // them
    // in the fill methods. This ensures that the actions aren't recreated
    // when fillActionBars is called with FILL_PROXY.
    private IWorkbenchAction exitAction;

    private IWorkbenchAction preferencesAction;
    private IWorkbenchAction perspectiveCustomizeAction;
    private IWorkbenchAction perspectiveSaveAsAction;
    private IWorkbenchAction perspectiveResetAction;
    private IWorkbenchAction perspectiveCloseAction;
    private IWorkbenchAction perspectiveCloseAllAction;
    private IContributionItem perspectivesShortList;
    private IContributionItem viewsShortList;

    private IWorkbenchAction helpContentsAction;
    private IWorkbenchAction aboutAction;

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }

    protected void makeActions(final IWorkbenchWindow window) {
        // Creates the actions and registers them.
        // Registering is needed to ensure that key bindings work.
        // The corresponding commands keybindings are defined in the plugin.xml
        // file.
        // Registering also provides automatic disposal of the actions when
        // the window is closed.

        exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);
        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);

        perspectivesShortList = ContributionItemFactory.PERSPECTIVES_SHORTLIST
            .create(window);
        viewsShortList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
        perspectiveCustomizeAction = ActionFactory.EDIT_ACTION_SETS
            .create(window);
        register(perspectiveCustomizeAction);
        perspectiveSaveAsAction = ActionFactory.SAVE_PERSPECTIVE.create(window);
        register(perspectiveSaveAsAction);
        perspectiveResetAction = ActionFactory.RESET_PERSPECTIVE.create(window);
        register(perspectiveResetAction);
        perspectiveCloseAction = ActionFactory.CLOSE_PERSPECTIVE.create(window);
        register(perspectiveCloseAction);
        perspectiveCloseAllAction = ActionFactory.CLOSE_ALL_PERSPECTIVES
            .create(window);
        register(perspectiveCloseAllAction);
        preferencesAction = ActionFactory.PREFERENCES.create(window);
        register(preferencesAction);

        helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
        register(helpContentsAction);
        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);
    }

    protected void fillMenuBar(IMenuManager menuBar) {
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.M_FILE));
        createMenuFile();
        menuBar.add(fileMenu);

        menuBar.add(new GroupMarker(IWorkbenchActionConstants.M_WINDOW));
        createMenuWindows();
        menuBar.add(windowMenu);

        menuBar.add(new GroupMarker(IWorkbenchActionConstants.M_HELP));
        createMenuHelp();
        menuBar.add(helpMenu);
    }

    /**
     * Creates the File menu.
     */
    private void createMenuFile() {
        fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
        fileMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        fileMenu.add(new Separator());
        fileMenu.add(exitAction);
        // if(TEST) {
        // fileMenu.add(new Separator());
        // fileMenu.add(new Action() {
        // @Override
        // public void run() {
        // String info = "";
        // // Get the context and the current bundles in it
        // BundleContext context = FrameworkUtil.getBundle(
        // this.getClass()).getBundleContext();
        // if(context == null) {
        // info = "Could not find bundle context";
        // } else {
        // // Get the symbolic name in a list
        // ArrayList<String> list = new ArrayList<String>();
        // Bundle[] bundles = context.getBundles();
        // for(Bundle bundle : bundles) {
        // list.add(bundle.getSymbolicName());
        // }
        // // Sort the list
        // Collections.sort(list);
        // // Parse the list
        // for(String str : list) {
        // info += str + "\n";
        // }
        // }
        // if(false) {
        // System.out.println(info);
        // } else {
        // Shell shell = PlatformUI.getWorkbench()
        // .getActiveWorkbenchWindow().getShell();
        // ScrolledTextDialog dialog = new ScrolledTextDialog(
        // shell);
        // dialog.setDialogTitle("Information");
        // dialog.setGroupLabel("Bundles");
        // dialog.setMessage(info);
        // dialog.open();
        // }
        // }
        //
        // @Override
        // public String getText() {
        // return "Get Bundles";
        // }
        // });
        // }
    }

    /**
     * Creates the Window menu.
     */
    private void createMenuWindows() {
        windowMenu = new MenuManager("&Window",
            IWorkbenchActionConstants.M_WINDOW);

        MenuManager perspectiveMenu = new MenuManager("Open Perspective");
        windowMenu.add(perspectiveMenu);
        perspectiveMenu.add(perspectivesShortList);
        MenuManager viewsMenu = new MenuManager("Show View");
        viewsMenu.add(viewsShortList);
        windowMenu.add(viewsMenu);

        windowMenu.add(new Separator());
        windowMenu.add(perspectiveCustomizeAction);
        windowMenu.add(perspectiveSaveAsAction);
        windowMenu.add(perspectiveResetAction);
        windowMenu.add(perspectiveCloseAction);
        windowMenu.add(perspectiveCloseAllAction);

        windowMenu.add(new Separator());
        windowMenu.add(preferencesAction);
    }

    /**
     * Creates the Help menu.
     */
    private void createMenuHelp() {
        helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
        helpMenu.add(helpContentsAction);
        helpMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        helpMenu.add(new Separator());
        helpMenu.add(aboutAction);
    }

}