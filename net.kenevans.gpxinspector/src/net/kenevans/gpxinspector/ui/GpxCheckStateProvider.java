package net.kenevans.gpxinspector.ui;

import net.kenevans.gpxinspector.model.GpxModel;
import net.kenevans.gpxinspector.model.GpxPropertyModel;

import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.ui.internal.dialogs.TreeManager.CheckStateProvider;

/*
 * Created on Sep 1, 2010
 * By Kenneth Evans, Jr.
 */

public class GpxCheckStateProvider implements ICheckStateProvider
{

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ICheckStateProvider#isChecked(java.lang.Object)
     */
    @Override
    public boolean isChecked(Object element) {
        if(element instanceof GpxModel) {
            return ((GpxModel)element).getChecked();
        } else {
            throw unknownElementException(element);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ICheckStateProvider#isGrayed(java.lang.Object)
     */
    @Override
    public boolean isGrayed(Object element) {
        if(element instanceof GpxPropertyModel) {
            return true;
        }
        return false;
    }

    protected RuntimeException unknownElementException(Object element) {
        return new RuntimeException("Unknown type of element in tree of type "
            + element.getClass().getName());
    }

}
