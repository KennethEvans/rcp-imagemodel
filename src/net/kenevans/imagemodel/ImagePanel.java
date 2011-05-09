package net.kenevans.imagemodel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.io.File;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import net.kenevans.imagemodel.utils.ImageUtils;
import net.kenevans.imagemodel.utils.Utils;

/**
 * ImagePanel is a self-contained JPanel that displays an image and has methods
 * to manipulate that image. There is no internal scrolling. The image itself is
 * managed by an ImageModel. It should be marked as deprecated but many
 * applications are using it.
 * 
 * @author Kenneth Evans, Jr.
 */
public class ImagePanel extends JPanel
{
    private static final long serialVersionUID = 1L;
    private static final double ZOOM_FACTOR = 1.5;
    private double zoom = 1.0;

    private ImageModel imageModel;
    private ImageModelListener imageModelListener;
    private MouseInputAdapter mouseAdapter;
    private DropTargetAdapter dropTargetAdapter;
    private DropTarget dropTarget;

    private boolean scaled = false;

    public static enum Mode {
        NONE, CROP
    };

    private Mode mode = Mode.NONE;
    private Rectangle clipRectangle = new Rectangle(0, 0, 0, 0);

    private PageFormat pageFormat = PrinterJob.getPrinterJob().defaultPage();
    private PrintService printService;
    private PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();
    private String defaultPath;
    private String selectedFile;

    private double scale = 1;
    private double x0 = 0;
    private double y0 = 0;

    private boolean dragging = false;
    private Point mouseStart = new Point(0, 0);
    private Point mouseCur = new Point(0, 0);

    // Property change names
    public static final String CLIP_RECTANGLE_CHANGED = ImagePanel.class
        .getName() + ".ClipRectangleChanged";
    public static final String MODE_CHANGED = ImagePanel.class.getName()
        + ".ModeChanged";

    public ImagePanel(ImageModel imageModel) {

        // Set up the ImageModel and add an ImageModelListener
        setImageModel(imageModel);

        // Set up mouse listeners
        mouseAdapter = new MouseInputAdapter() {
            public void mousePressed(MouseEvent ev) {
                ImagePanel.this.mousePressed(ev);
            }

            public void mouseReleased(MouseEvent ev) {
                ImagePanel.this.mouseReleased(ev);
            }

            public void mouseDragged(MouseEvent ev) {
                ImagePanel.this.mouseDragged(ev);
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        // Set up drop
        dropTargetAdapter = new DropTargetAdapter() {
            public void drop(DropTargetDropEvent ev) {
                ImageModel model = ImagePanel.this.getImageModel();
                if(model != null) {
                    Cursor oldCursor = getCursor();
                    try {
                        setCursor(Cursor
                            .getPredefinedCursor(Cursor.WAIT_CURSOR));
                        boolean retCode = model.drop(ev);
                        if(retCode == false)
                            Toolkit.getDefaultToolkit().beep();
                    } finally {
                        setCursor(oldCursor);
                    }
                }
            }
        };
        dropTarget = new DropTarget(this, dropTargetAdapter);

        // Initialize the UI
        uiInit();
    }

    private void uiInit() {
        this.setLayout(new BorderLayout());
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage image = getImage();
        if(image == null) return;
        int width0 = this.getWidth();
        int height0 = this.getHeight();
        if(width0 <= 0 || height0 <= 0) return;
        int width = image.getWidth();
        int height = image.getHeight();
        if(width <= 0 || height <= 0) return;

        // Do zoom
        if(zoom != 1.0) {
            Graphics2D g2D = (Graphics2D)g;
            g2D.scale(zoom, zoom);
        }

        // Calculate the current scaling. May be inefficient to do it each time,
        // but
        // it keeps it in one place and avoids logic errors. The scaling should
        // fit
        // what is currently drawn.
        if(!scaled) {
            scale = 1;
            x0 = 0;
            y0 = 0;
        } else {
            // Is scaled
            x0 = 0;
            y0 = 0;
            if(width <= width0 && height <= height0) {
                // Leave as is
                scale = 1;
            } else if(width <= width0 && height > height0) {
                // Scale to height0, keeping aspect ratio
                scale = (double)height0 / (double)height;
            } else {
                double aspect0 = (double)width0 / (double)height0;
                double aspect = (double)width / (double)height;
                if(aspect > aspect0) {
                    // Scale to width0, keeping aspect ratio
                    scale = (double)width0 / (double)width;
                } else {
                    // Scale to height0, keeping aspect ratio
                    scale = (double)height0 / (double)height;
                }
            }
        }
        int x00 = (int)(x0 + .5);
        int y00 = (int)(y0 + .5);
        int width00 = (int)(scale * width + .5);
        int height00 = (int)(scale * height + .5);
        if(scale == 1 && x0 == 0 & y0 == 0) {
            g.drawImage(image, x00, y00, null);
        } else {
            g.drawImage(image, x00, y00, width00, height00, null);
        }

        // It seems to be OK to redraw the image each time during dragging.
        // Alternatively we could draw over the image elsewhere using XOR.
        if(mode == Mode.CROP) {
            double scaleFactor = zoom != 0 ? scale / zoom : scale;
            Rectangle2D.Double rect = new Rectangle2D.Double(x0 + scaleFactor
                * clipRectangle.x, y0 + scaleFactor * clipRectangle.y,
                scaleFactor * clipRectangle.width, scaleFactor
                    * clipRectangle.height);
            Graphics2D g2D = (Graphics2D)g;
            g2D.setColor(Color.WHITE);
            g2D.setXORMode(Color.BLACK);
            g2D.draw(rect);
        }
    }

    /**
     * Convert a Rectangle to unscaled coordinates.
     * 
     * @param rect
     * @return
     */
    Rectangle unscaleRectangle(Rectangle rect) {
        if(scale == 1 && x0 == 0 && y0 == 0) return rect;
        if(scale == 0) return new Rectangle(0, 0, 0, 0);
        Rectangle newRect = new Rectangle((int)((rect.x - x0) / scale + .5),
            (int)((rect.y - y0) / scale + .5), (int)(rect.width / scale + .5),
            (int)(rect.height / scale + .5));
        return newRect;
    }

    /**
     * Gets the image.
     * 
     * @return The image or null if it is not available.
     */
    public BufferedImage getImage() {
        if(imageModel == null) return null;
        BufferedImage image = imageModel.getCurrentImage();
        return image;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        if(imageModel == null) return new Dimension(0, 0);
        BufferedImage image = imageModel.getCurrentImage();
        if(image == null)
            return new Dimension(0, 0);
        else
            return new Dimension((int)(zoom * image.getWidth()),
                (int)(zoom * image.getHeight()));
    }

    /**
     * Sets the background with a JColorChooser.
     */
    public void setBackground() {
        Color color = JColorChooser.showDialog(this, "Select Background Color",
            getBackground());
        if(color != null) setBackground(color);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JComponent#setBackground(java.awt.Color)
     */
    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        repaint();
    }

    public void resetClipRectangle() {
        BufferedImage image = getImage();
        Rectangle newRectangle = null;
        if(image == null) {
            newRectangle = new Rectangle(0, 0, 0, 0);
        } else {
            newRectangle = new Rectangle(0, 0, image.getWidth(),
                image.getHeight());
        }
        setClipRectangle(newRectangle);
    }

    public void crop() {
        if(mode != Mode.CROP) return;
        if(imageModel == null) return;
        Rectangle rect = new Rectangle(clipRectangle);
        // Scale the rectangle to the current zoom
        if(zoom != 0) {
            rect.x /= zoom;
            rect.y /= zoom;
            rect.width /= zoom;
            rect.height /= zoom;
        }
        imageModel.cropImage(rect);
        // Reset the clip rectangle
        clipRectangle = new Rectangle(0, 0, 0, 0);
    }

    /**
     * Implements print setup.
     */
    public void pageSetup() {
        Cursor oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            PrinterJob job = PrinterJob.getPrinterJob();
            pageFormat = job.pageDialog(printAttributes);
        } finally {
            setCursor(oldCursor);
        }
    }

    /**
     * Implements print preview.
     */
    public void printPreview() {
        if(imageModel == null) return;
        Cursor oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            PrintPreviewDialog dialog = new PrintPreviewDialog(imageModel,
                pageFormat, 1);
            dialog.setVisible(true);
        } finally {
            setCursor(oldCursor);
        }
    }

    /**
     * Implements print.
     */
    public void print() {
        if(imageModel == null) return;
        Cursor oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            PrinterJob job = PrinterJob.getPrinterJob();
            // Use the PrintService (printer) specified last time
            if(printService != null) job.setPrintService(printService);
            if(job.printDialog(printAttributes)) {
                job.setPrintable(imageModel);
                job.print(printAttributes);
                printService = job.getPrintService();
            }
        } catch(Exception ex) {
            Utils.excMsg("Printing failed:", ex);
        } finally {
            setCursor(oldCursor);
        }
    }

    /**
     * Saves the image to the clipboard.
     */
    public void copy() {
        if(imageModel == null) return;
        BufferedImage image = imageModel.getCurrentImage();
        if(image == null) {
            Utils.errMsg("No image");
            return;
        }
        Cursor oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            imageModel.copy();
        } finally {
            setCursor(oldCursor);
        }
    }

    /**
     * Pastes an image.
     */
    public void paste() {
        if(imageModel == null) return;
        Cursor oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            imageModel.paste();
        } finally {
            setCursor(oldCursor);
        }
    }

    /**
     * Saves the image to a file. Sets the defaultDirectory if it is not null.
     * Saves the defaultDirectory on success.
     * 
     * @see net.kenevans.imagemodel.utils.ImageUtils#saveImageToFile(
     *      BufferedImage,
     */
    public void saveAs() {
        if(imageModel == null) return;
        BufferedImage image = imageModel.getCurrentImage();
        if(image == null) {
            Utils.errMsg("No image");
            return;
        }
        Cursor oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            File file = ImageUtils.saveImageToFile(image, defaultPath, null);
            if(file != null) {
                // Save the selected path for next time
                defaultPath = file.getParentFile().getPath();
                imageModel.setFile(file);
            }
        } finally {
            setCursor(oldCursor);
        }
    }

    /**
     * Saves the image to a file. Sets the selectedFile if it is not null. Saves
     * the selectedFile on success.
     * 
     * @see net.kenevans.imagemodel.utils.ImageUtils#saveImageToFile(
     *      BufferedImage,
     */
    public void saveAsWithSelectedFile() {
        if(imageModel == null) return;
        BufferedImage image = imageModel.getCurrentImage();
        if(image == null) {
            Utils.errMsg("No image");
            return;
        }
        Cursor oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            File file = ImageUtils.saveImageToFile(image, null, selectedFile);
            if(file != null) {
                selectedFile = file.getPath();
                imageModel.setFile(file);
            }
        } finally {
            setCursor(oldCursor);
        }
    }

    /**
     * Saves the image to a file
     */
    public boolean save(File file) {
        boolean retVal = false;
        if(imageModel == null) return false;
        BufferedImage image = imageModel.getCurrentImage();
        if(image == null) {
            Utils.errMsg("No image");
            return false;
        }
        Cursor oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            retVal = ImageUtils.saveImage(image, file);
            setCursor(Cursor.getDefaultCursor());
        } finally {
            setCursor(oldCursor);
        }
        return retVal;
    }

    public void zoomReset() {
        zoom = 1.0;
    }

    public void zoomIn() {
        zoom *= ZOOM_FACTOR;
    }

    public void zoomOut() {
        zoom /= ZOOM_FACTOR;
    }

    public void zoomToRectangle(double width, double height) {
        BufferedImage image = getImage();
        if(image == null) {
            return;
        }
        int width0 = image.getWidth();
        int height0 = image.getHeight();
        if(width == 0 || height == 0) {
            return;
        }
        double zoomX = width / width0;
        double zoomY = height / height0;

        zoom = zoomX > zoomY ? zoomY : zoomX;
    }

    // Mouse listener implementation

    private void mousePressed(MouseEvent ev) {
        requestFocusInWindow();
        // Needed for keyboard events
        if(dragging) return;
        mouseCur = ev.getPoint();
        mouseStart = mouseCur;
        Rectangle newRectangle = new Rectangle();
        newRectangle.setFrameFromDiagonal(mouseStart, mouseCur);
        setClipRectangle(unscaleRectangle(newRectangle));
        dragging = true;
    }

    private void mouseReleased(MouseEvent ev) {
        if(dragging) {
            dragging = false;
            mouseCur = ev.getPoint();
            Rectangle newRectangle = new Rectangle();
            newRectangle.setFrameFromDiagonal(mouseStart, mouseCur);
            setClipRectangle(unscaleRectangle(newRectangle));
        }
    }

    private void mouseDragged(MouseEvent ev) {
        if(dragging) {
            mouseCur = ev.getPoint();
            Rectangle newRectangle = new Rectangle();
            newRectangle.setFrameFromDiagonal(mouseStart, mouseCur);
            setClipRectangle(unscaleRectangle(newRectangle));
        }
    }

    /**
     * Removes references. Use this to allow it to be garbage collected.
     */
    public void finish() {
        setImageModel(null);
        imageModelListener = null;
        if(mouseAdapter != null) {
            removeMouseListener(mouseAdapter);
            removeMouseMotionListener(mouseAdapter);
            mouseAdapter = null;
        }
        if(dropTarget != null) {
            if(dropTargetAdapter != null) {
                dropTarget.removeDropTargetListener(dropTargetAdapter);
                dropTargetAdapter = null;
            }
            dropTarget = null;
        }
    }

    // Getters and setters

    /**
     * @return The value of imageModel.
     */
    public ImageModel getImageModel() {
        return imageModel;
    }

    /**
     * @param imageModel The new value for imageModel.
     */
    public void setImageModel(ImageModel imageModel) {
        if(imageModel == this.imageModel) return;
        if(imageModelListener != null && this.imageModel != null) {
            this.imageModel.removeImageModelListener(imageModelListener);
        }
        this.imageModel = imageModel;
        if(imageModel != null) {
            // Create a listener if not done yet
            if(imageModelListener == null) {
                imageModelListener = new ImageModelListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        repaint();
                    }
                };
            }
            imageModel.addImageModelListener(imageModelListener);
        }
    }

    /**
     * @return The value of clipRectangle.
     */
    public Rectangle getClipRectangle() {
        return clipRectangle;
    }

    /**
     * @param clipRectangle The new value for clipRectangle.
     */
    public void setClipRectangle(Rectangle clipRectangle) {
        Rectangle oldRectangle = this.clipRectangle;
        if(clipRectangle.equals(oldRectangle)) return;
        this.clipRectangle = clipRectangle;
        if(mode == Mode.CROP) repaint();
        firePropertyChange(CLIP_RECTANGLE_CHANGED, oldRectangle, clipRectangle);
    }

    /**
     * @return The value of mode.
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * @param mode The new value for mode.
     */
    public void setMode(Mode mode) {
        Mode oldMode = this.mode;
        if(this.mode == mode) return;
        this.mode = mode;
        repaint();
        firePropertyChange(MODE_CHANGED, oldMode, mode);
    }

    /**
     * @return The value of defaultPath. saveAs() uses the defaultFile whereas
     *         saveAsWithSelectedFile() uses the selectedFile.
     * 
     * @see net.kenevans.imagemodel.utils.ImageUtils#saveImageToFile(BufferedImage,
     *      String, String)
     */
    public String getDefaultPath() {
        return defaultPath;
    }

    /**
     * @param defaultPath The new value for defaultPath. saveAs() uses the
     *            defaultFile whereas saveAsWithSelectedFile() uses the
     *            selectedFile.
     * 
     * @see net.kenevans.imagemodel.utils.ImageUtils#saveImageToFile(BufferedImage,
     *      String, String)
     */
    public void setDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
    }

    /**
     * @return The value of selectedFile. saveAs() uses the defaultFile whereas
     *         saveAsWithSelectedFile() uses the selectedFile.
     * 
     * @see net.kenevans.imagemodel.utils.ImageUtils#saveImageToFile(BufferedImage,
     *      String, String)
     */
    public String getSelectedFile() {
        return selectedFile;
    }

    /**
     * @param selectedFile The new value for selectedFile. saveAs() uses the
     *            defaultFile whereas saveAsWithSelectedFile() uses the
     *            selectedFile.
     * 
     * @see net.kenevans.imagemodel.utils.ImageUtils#saveImageToFile(BufferedImage,
     *      String, String)
     */
    public void setSelectedFile(String selectedFile) {
        this.selectedFile = selectedFile;
    }

    /**
     * @return The value of scaled.
     */
    public boolean isScaled() {
        return scaled;
    }

    /**
     * @param scaled The new value for scaled.
     */
    public void setScaled(boolean scaled) {
        boolean oldScaled = this.scaled;
        if(this.scaled == scaled) return;
        this.scaled = scaled;
        repaint();
        firePropertyChange(MODE_CHANGED, oldScaled, scaled);
    }

    /**
     * @return The value of zoom.
     */
    public double getZoom() {
        return zoom;
    }

    /**
     * @param zoom The new value for zoom.
     */
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

}
