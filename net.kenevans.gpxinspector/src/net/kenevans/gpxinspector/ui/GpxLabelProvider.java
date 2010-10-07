package net.kenevans.gpxinspector.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.kenevans.gpxinspector.model.GpxModel;
import net.kenevans.gpxinspector.model.GpxFileModel;
import net.kenevans.gpxinspector.model.GpxPropertyModel;
import net.kenevans.gpxinspector.model.GpxTrackModel;
import net.kenevans.gpxinspector.model.GpxWaypointModel;
import net.kenevans.gpxinspector.plugin.Activator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class GpxLabelProvider extends LabelProvider
{
    private Map<ImageDescriptor, Image> imageCache = new HashMap<ImageDescriptor, Image>(
        11);

    /*
     * @see ILabelProvider#getImage(Object)
     */
    public Image getImage(Object element) {
        ImageDescriptor descriptor = null;
        if(element instanceof GpxFileModel) {
            descriptor = Activator.getImageDescriptor("icons/file_obj.gif");
        } else if(element instanceof GpxTrackModel) {
            descriptor = Activator.getImageDescriptor("icons/track.png");
        } else if(element instanceof GpxWaypointModel) {
            descriptor = Activator.getImageDescriptor("icons/waypoint.png");
        } else if(element instanceof GpxPropertyModel) {
            descriptor = Activator.getImageDescriptor("icons/properties.gif");
        } else {
            throw unknownElementException(element);
        }

        // Obtain the cached image corresponding to the descriptor
        Image image = (Image)imageCache.get(descriptor);
        if(image == null) {
            image = descriptor.createImage();
            imageCache.put(descriptor, image);
        }
        return image;
    }

    /*
     * @see ILabelProvider#getText(Object)
     */
    public String getText(Object element) {
        if(element instanceof GpxModel) {
            return ((GpxModel)element).getLabel();
        } else {
            throw unknownElementException(element);
        }
    }

    public void dispose() {
        for(Iterator<Image> i = imageCache.values().iterator(); i.hasNext();) {
            ((Image)i.next()).dispose();
        }
        imageCache.clear();
    }

    protected RuntimeException unknownElementException(Object element) {
        return new RuntimeException("Unknown type of element in tree of type "
            + element.getClass().getName());
    }

}
