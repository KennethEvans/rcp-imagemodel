package net.kenevans.gpxinspector.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/*
 * Created on Nov 29, 2010
 * By Kenneth Evans, Jr.
 */

/**
 * This class is a wrapper for the data transfer of object references that are
 * transferred within the same virtual machine.
 */
public class LocalSelection implements Transferable
{
    /**
     * Constructs the selection.
     * 
     * @param o any object
     */
    public LocalSelection(Object o) {
        obj = o;
    }

    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] flavors = new DataFlavor[1];
        Class<?> type = obj.getClass();
        String mimeType = "application/x-java-jvm-local-objectref;class="
            + type.getName();
        try {
            flavors[0] = new DataFlavor(mimeType);
            return flavors;
        } catch(ClassNotFoundException e) {
            return new DataFlavor[0];
        }
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return "application".equals(flavor.getPrimaryType())
            && "x-java-jvm-local-objectref".equals(flavor.getSubType())
            && flavor.getRepresentationClass().isAssignableFrom(obj.getClass());
    }

    public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException {
        if(!isDataFlavorSupported(flavor))
            throw new UnsupportedFlavorException(flavor);

        return obj;
    }

    private Object obj;
}
