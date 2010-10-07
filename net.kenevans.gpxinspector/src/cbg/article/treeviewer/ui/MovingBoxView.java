package cbg.article.treeviewer.ui;

import java.util.Iterator;

import net.kenevans.gpxinspector.plugin.Activator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import cbg.article.model.BoardGame;
import cbg.article.model.Book;
import cbg.article.model.Model;
import cbg.article.model.MovingBox;

/**
 * Insert the type's description here.
 * 
 * @see ViewPart
 */
public class MovingBoxView extends ViewPart
{
    protected TreeViewer treeViewer;
    protected Text text;
    protected MovingBoxLabelProvider labelProvider;

    protected Action onlyBoardGamesAction, atLeatThreeItems;
    protected Action booksBoxesGamesAction, noArticleAction;
    protected Action addBookAction, removeAction;
    protected ViewerFilter onlyBoardGamesFilter, atLeastThreeFilter;
    protected ViewerSorter booksBoxesGamesSorter, noArticleSorter;

    protected MovingBox root;

    protected Action collapseAction;
    protected Action collapseAllAction;
    protected Action expandAction;

    /**
     * The maximum level to which the buttons can expand the tree. This should
     * be the maximum level it is possible to expand it. treeLevel is restricted
     * to be less than or equal to this value.
     */
    public static final int MAX_TREE_LEVEL = 4;
    /** The current level to which the tree is expanded. Should be non-negative. */
    private int treeLevel = 1;

    /**
     * The constructor.
     */
    public MovingBoxView() {
    }

    /*
     * @see IWorkbenchPart#createPartControl(Composite)
     */
    public void createPartControl(Composite parent) {
        /*
         * Create a grid layout object so the selectionText and treeviewer are layed out
         * the way I want.
         */
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.verticalSpacing = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 2;
        parent.setLayout(layout);

        // Text to show the current selection
        text = new Text(parent, SWT.READ_ONLY | SWT.SINGLE | SWT.BORDER);
        GridData layoutData = new GridData();
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.horizontalAlignment = GridData.FILL;
        text.setLayoutData(layoutData);

        // TreeViewer
        treeViewer = new TreeViewer(parent);
        treeViewer.setContentProvider(new MovingBoxContentProvider());
        labelProvider = new MovingBoxLabelProvider();
        treeViewer.setLabelProvider(labelProvider);
        treeViewer.setUseHashlookup(true);
        layoutData = new GridData();
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        layoutData.horizontalAlignment = GridData.FILL;
        layoutData.verticalAlignment = GridData.FILL;
        treeViewer.getControl().setLayoutData(layoutData);

        // Create menu, toolbars, filters, sorters.
        createFiltersAndSorters();
        createActions();
        createMenus();
        createToolbar();
        hookListeners();
        hookContextMenu(treeViewer.getControl());

        // Initialize the TreeViewer
        treeViewer.setInput(getInitalInput());
        treeViewer.expandToLevel(treeLevel);
    }

    protected void createFiltersAndSorters() {
        atLeastThreeFilter = new ThreeItemFilter();
        onlyBoardGamesFilter = new BoardgameFilter();
        booksBoxesGamesSorter = new BookBoxBoardSorter();
        noArticleSorter = new NoArticleSorter();
    }

    protected void hookListeners() {
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                // if the selection is empty clear the label
                if(event.getSelection().isEmpty()) {
                    text.setText("");
                    return;
                }
                if(event.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection)event
                        .getSelection();
                    StringBuffer toShow = new StringBuffer();
                    for(Iterator<?> iterator = selection.iterator(); iterator
                        .hasNext();) {
                        Object domain = (Model)iterator.next();
                        String value = labelProvider.getText(domain);
                        toShow.append(value);
                        toShow.append(", ");
                    }
                    // remove the trailing comma space pair
                    if(toShow.length() > 0) {
                        toShow.setLength(toShow.length() - 2);
                    }
                    text.setText(toShow.toString());
                }
            }
        });
    }

    protected void createActions() {
        onlyBoardGamesAction = new Action("Only Board Games") {
            public void run() {
                updateFilter(onlyBoardGamesAction);
            }
        };
        onlyBoardGamesAction.setChecked(false);

        atLeatThreeItems = new Action("Boxes With At Least Three Items") {
            public void run() {
                updateFilter(atLeatThreeItems);
            }
        };
        atLeatThreeItems.setChecked(false);

        booksBoxesGamesAction = new Action("Books, Boxes, Games") {
            public void run() {
                updateSorter(booksBoxesGamesAction);
            }
        };
        booksBoxesGamesAction.setChecked(false);

        noArticleAction = new Action("Ignoring Articles") {
            public void run() {
                updateSorter(noArticleAction);
            }
        };
        noArticleAction.setChecked(false);

        addBookAction = new Action("Add Book") {
            public void run() {
                addNewBook();
            }
        };
        addBookAction.setToolTipText("Add a New Book");
        addBookAction.setImageDescriptor(Activator
            .getImageDescriptor("newBook.gif"));

        removeAction = new Action("Delete") {
            public void run() {
                removeSelected();
            }
        };
        removeAction.setToolTipText("Delete");
        removeAction.setImageDescriptor(Activator
            .getImageDescriptor("remove.gif"));

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
        collapseAction.setToolTipText("Collapse the tree entirely");
        collapseAction.setImageDescriptor(Activator.imageDescriptorFromPlugin(
            Activator.PLUGIN_ID, "icons/collapseall.gif"));

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
    }

    /**
     * Add a new book to the selected moving box. If a moving box is not
     * selected, use the selected obect's moving box.
     * 
     * If nothing is selected add to the root box.
     */
    protected void addNewBook() {
        MovingBox receivingBox;
        if(treeViewer.getSelection().isEmpty()) {
            receivingBox = root;
        } else {
            IStructuredSelection selection = (IStructuredSelection)treeViewer
                .getSelection();
            Model selectedDomainObject = (Model)selection.getFirstElement();
            if(!(selectedDomainObject instanceof MovingBox)) {
                receivingBox = selectedDomainObject.getParent();
            } else {
                receivingBox = (MovingBox)selectedDomainObject;
            }
        }
        receivingBox.add(Book.newBook());
    }

    /**
     * Remove the selected domain object(s). If multiple objects are selected
     * remove all of them.
     * 
     * If nothing is selected do nothing.
     */
    protected void removeSelected() {
        if(treeViewer.getSelection().isEmpty()) {
            return;
        }
        IStructuredSelection selection = (IStructuredSelection)treeViewer
            .getSelection();
        /*
         * Tell the tree to not redraw until we finish removing all the selected
         * children.
         */
        treeViewer.getTree().setRedraw(false);
        for(Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            Model model = (Model)iterator.next();
            MovingBox parent = model.getParent();
            parent.remove(model);
        }
        treeViewer.getTree().setRedraw(true);
    }

    protected void createMenus() {
        IMenuManager rootMenuManager = getViewSite().getActionBars()
            .getMenuManager();
        rootMenuManager.setRemoveAllWhenShown(true);
        rootMenuManager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager mgr) {
                fillMenu(mgr);
            }
        });
        fillMenu(rootMenuManager);
    }

    protected void fillMenu(IMenuManager rootMenuManager) {
        IMenuManager filterSubmenu = new MenuManager("Filters");
        rootMenuManager.add(filterSubmenu);
        filterSubmenu.add(onlyBoardGamesAction);
        filterSubmenu.add(atLeatThreeItems);

        IMenuManager sortSubmenu = new MenuManager("Sort By");
        rootMenuManager.add(sortSubmenu);
        sortSubmenu.add(booksBoxesGamesAction);
        sortSubmenu.add(noArticleAction);

        rootMenuManager.add(new Separator());
        rootMenuManager.add(addBookAction);
        rootMenuManager.add(removeAction);

        rootMenuManager.add(new Separator());
        rootMenuManager.add(expandAction);
        rootMenuManager.add(collapseAction);
        rootMenuManager.add(collapseAllAction);
    }

    protected void updateSorter(Action action) {
        if(action == booksBoxesGamesAction) {
            noArticleAction.setChecked(!booksBoxesGamesAction.isChecked());
            if(action.isChecked()) {
                treeViewer.setSorter(booksBoxesGamesSorter);
            } else {
                treeViewer.setSorter(null);
            }
        } else if(action == noArticleAction) {
            booksBoxesGamesAction.setChecked(!noArticleAction.isChecked());
            if(action.isChecked()) {
                treeViewer.setSorter(noArticleSorter);
            } else {
                treeViewer.setSorter(null);
            }
        }

    }

    /* Multiple filters can be enabled at a time. */
    protected void updateFilter(Action action) {
        if(action == atLeatThreeItems) {
            if(action.isChecked()) {
                treeViewer.addFilter(atLeastThreeFilter);
            } else {
                treeViewer.removeFilter(atLeastThreeFilter);
            }
        } else if(action == onlyBoardGamesAction) {
            if(action.isChecked()) {
                treeViewer.addFilter(onlyBoardGamesFilter);
            } else {
                treeViewer.removeFilter(onlyBoardGamesFilter);
            }
        }
    }

    protected void createToolbar() {
        IToolBarManager toolbarManager = getViewSite().getActionBars()
            .getToolBarManager();
        toolbarManager.add(addBookAction);
        toolbarManager.add(removeAction);
        toolbarManager.add(expandAction);
        toolbarManager.add(collapseAction);
    }

    public MovingBox getInitalInput() {
        root = new MovingBox();
        MovingBox someBooks = new MovingBox("Books");
        MovingBox games = new MovingBox("Games");
        MovingBox books = new MovingBox("More books");
        MovingBox games2 = new MovingBox("More games");

        root.add(someBooks);
        root.add(games);
        root.add(new MovingBox());

        someBooks.add(books);
        games.add(games2);

        books.add(new Book("The Lord of the Rings", "J.R.R.", "Tolkien"));
        books.add(new BoardGame("Taj Mahal", "Reiner", "Knizia"));
        books.add(new Book("Cryptonomicon", "Neal", "Stephenson"));
        books.add(new Book("Smalltalk, Objects, and Design", "Chamond", "Liu"));
        books.add(new Book("A Game of Thrones", "George R. R.", " Martin"));
        books.add(new Book("The Hacker Ethic", "Pekka", "Himanen"));
        // books.add(new MovingBox());

        books.add(new Book("The Code Book", "Simon", "Singh"));
        books.add(new Book("The Chronicles of Narnia", "C. S.", "Lewis"));
        books.add(new Book("The Screwtape Letters", "C. S.", "Lewis"));
        books.add(new Book("Mere Christianity ", "C. S.", "Lewis"));
        games.add(new BoardGame("Tigris & Euphrates", "Reiner", "Knizia"));
        games.add(new BoardGame("La Citta", "Gerd", "Fenchel"));
        games.add(new BoardGame("El Grande", "Wolfgang", "Kramer"));
        games
            .add(new BoardGame("The Princes of Florence", "Richard", "Ulrich"));
        games.add(new BoardGame("The Traders of Genoa", "Rudiger", "Dorn"));
        games2.add(new BoardGame("Tikal", "M.", "Kiesling"));
        games2.add(new BoardGame("Modern Art", "Reiner", "Knizia"));
        return root;
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
                MovingBoxView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(control);
        control.setMenu(menu);
    }

    /**
     * Adds the contents to the context menu.
     * 
     * @param manager
     */
    private void fillContextMenu(IMenuManager manager) {
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

}
