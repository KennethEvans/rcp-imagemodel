package net.kenevans.gpxinspector.ui;

import java.beans.PropertyChangeEvent;

import net.kenevans.gpxinspector.model.GpxFileModel;
import net.kenevans.gpxinspector.model.GpxFileSetModel;
import net.kenevans.gpxinspector.model.GpxModel;
import net.kenevans.gpxinspector.model.GpxModelListener;
import net.kenevans.gpxinspector.model.GpxRouteModel;
import net.kenevans.gpxinspector.model.GpxTrackModel;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class GpxContentProvider implements ITreeContentProvider
{
    private static boolean DEBUG_LISTENER = true;
    private static Object[] EMPTY_ARRAY = new Object[0];
    protected CheckboxTreeViewer viewer;
    protected GpxModelListener gpxModelListener;

    public GpxContentProvider() {
        createGpxModelListener();
    }

    /*
     * @see IContentProvider#dispose()
     */
    public void dispose() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
     * .viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.viewer = (CheckboxTreeViewer)viewer;
        if(oldInput != null && gpxModelListener != null) {
            removeListenersFrom(new Object[] {oldInput});
        }
        if(newInput != null && gpxModelListener != null) {
            addListenersTo(new Object[] {newInput});
        }
    }

    /**
     * Recursively remove the listener from each element in the tree starting
     * with the given input.
     * 
     * @param input The starting element.
     */
    protected void removeListenersFrom(Object[] input) {
        for(Object obj : input) {
            GpxModel model = (GpxModel)obj;
            model.removeGpxModelListener(gpxModelListener);
            Object[] children = getChildren(model);
            if(children.length > 0) {
                removeListenersFrom(children);
            }
        }
    }

    /**
     * Recursively set the check state for each element in the tree according to
     * its internal value starting with the given input.
     * 
     * @param input The starting element.
     */
    protected void setCheckState(Object[] input) {
        for(Object obj : input) {
            GpxModel model = (GpxModel)obj;
            viewer.setChecked(model, model.getChecked());
            Object[] children = getChildren(model);
            if(children.length > 0) {
                setCheckState(children);
            }
        }
    }

    /**
     * Recursively add the listener to each element in the tree starting with
     * the given input.
     * 
     * @param input The starting element.
     */
    protected void addListenersTo(Object[] input) {
        for(Object obj : input) {
            GpxModel model = (GpxModel)obj;
            model.addGpxModelListener(gpxModelListener);
            Object[] children = getChildren(model);
            if(children.length > 0) {
                addListenersTo(children);
            }
        }
    }

    /*
     * @see ITreeContentProvider#getChildren(Object)
     */
    public Object[] getChildren(Object parentElement) {
        if(parentElement instanceof GpxFileSetModel) {
            GpxFileSetModel model = (GpxFileSetModel)parentElement;
            return model.getGpxFileModels().toArray();
        } else if(parentElement instanceof GpxFileModel) {
            GpxFileModel model = (GpxFileModel)parentElement;
            return concat(model.getTrackModels().toArray(), model
                .getRouteModels().toArray(), model.getWaypointModels()
                .toArray());
        } else if(parentElement instanceof GpxTrackModel) {
            // TODO Implement properties
            // GpxTrackModel model = (GpxTrackModel)parentElement;
            // return model.getPropertyModels().toArray();
        } else if(parentElement instanceof GpxRouteModel) {
            GpxRouteModel model = (GpxRouteModel)parentElement;
            return model.getWaypointModels().toArray();
        }
        return EMPTY_ARRAY;
    }

    protected Object[] concat(Object[] obj1, Object[] obj2) {
        Object[] both = new Object[obj1.length + obj2.length];
        System.arraycopy(obj1, 0, both, 0, obj1.length);
        System.arraycopy(obj2, 0, both, obj1.length, obj2.length);
        return both;
    }

    protected Object[] concat(Object[] obj1, Object[] obj2, Object[] obj3) {
        Object[] both = new Object[obj1.length + obj2.length + obj3.length];
        System.arraycopy(obj1, 0, both, 0, obj1.length);
        System.arraycopy(obj2, 0, both, obj1.length, obj2.length);
        System.arraycopy(obj3, 0, both, obj1.length + obj2.length, obj3.length);
        return both;
    }

    /*
     * @see ITreeContentProvider#getParent(Object)
     */
    public Object getParent(Object element) {
        if(element instanceof GpxModel) {
            return ((GpxModel)element).getParent();
        }
        return null;
    }

    /*
     * @see ITreeContentProvider#hasChildren(Object)
     */
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

    /*
     * @see IStructuredContentProvider#getElements(Object)
     */
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    /**
     * Creates the listener for GpxModel events.
     */
    private void createGpxModelListener() {
        if(gpxModelListener == null) {
            gpxModelListener = new GpxModelListener() {
                public void propertyChange(PropertyChangeEvent ev) {
                    if(ev.getPropertyName().equals(GpxModel.ADDED)) {
                        // Refresh the tree starting with the parent model
                        GpxModel model = (GpxModel)ev.getNewValue();
                        GpxModel parent = model.getParent();
                        // Add listeners to the model and its children
                        addListenersTo(new Object[] {model});
                        // model.addGpxModelListener(gpxModelListener);
                        if(parent != null) {
                            viewer.refresh(parent, false);
                        }
                        if(DEBUG_LISTENER) {
                            System.out.format("ADDED %s %s \n", model
                                .getClass().getSimpleName(), model.toString());
                        }
                    } else if(ev.getPropertyName().equals(GpxModel.REMOVED)) {
                        // Refresh the tree starting with the parent model
                        GpxModel model = (GpxModel)ev.getOldValue();
                        GpxModel parent = model.getParent();
                        model.dispose();
                        if(parent != null) {
                            viewer.refresh(parent, false);
                        }
                        if(DEBUG_LISTENER) {
                            System.out.format("REMOVED %s %s \n", model
                                .getClass().getSimpleName(), model.toString());
                        }
                    } else if(ev.getPropertyName().equals(GpxModel.CHANGED)) {
                        // Refresh the tree starting with the parent model
                        GpxModel model = (GpxModel)ev.getOldValue();
                        // GpxModel parent = model.getParent();
                        // if(parent != null) {
                        // viewer.refresh(parent, false);
                        // }
                        viewer.refresh(model, true);
                        if(DEBUG_LISTENER) {
                            System.out.format("CHANGED %s %s \n", model
                                .getClass().getSimpleName(), model.toString());
                        }
                    }
                }
            };
        }
    }

}
