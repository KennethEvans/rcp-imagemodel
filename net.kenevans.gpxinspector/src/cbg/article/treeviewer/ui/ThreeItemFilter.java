package cbg.article.treeviewer.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import cbg.article.model.MovingBox;

public class ThreeItemFilter extends ViewerFilter
{

    /*
     * @see ViewerFilter#select(Viewer, Object, Object)
     */
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        return parentElement instanceof MovingBox
            && ((MovingBox)parentElement).size() >= 3;
    }

}
