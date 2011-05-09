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
import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputAdapter;

import net.kenevans.imagemodel.utils.ImageUtils;
import net.kenevans.imagemodel.utils.Utils;

/**
 * ScrolledImagePanel is a self-contained JPanel that displays a scrolled image
 * and has methods to manipulate that image. It consists of a simple JPanel
 * container that has a JScrollPane whose client is an image panel JPanel. The
 * image panel may be larger than the container and handles the painting of the
 * image. The image itself is managed by an ImageModel.
 * 
 * @author Kenneth Evans, Jr.
 */
public class ScrolledImagePanel extends JPanel
{
    private static final long serialVersionUID = 1L;
    private static final double ZOOM_FACTOR = 1.5;
    private double zoom = 1.0;
    private boolean useStatusBar = false;

    private ImageModel imageModel;
    private ImageModelListener imageModelListener;
    private MouseInputAdapter mouseAdapter;
    private DropTargetAdapter dropTargetAdapter;
    private DropTarget dropTarget;
    private JScrollPane scrollPane;
    private JPanel imagePanel;
    private JLabel statusBar;

    public static enum Mode {
        NONE, CROP
    };

    private Mode mode = Mode.NONE;
    private Rectangle clipRectangle = new Rectangle(0, 0, 0, 0);
    private Rectangle oldClipRectangle;

    private PageFormat pageFormat = PrinterJob.getPrinterJob().defaultPage();
    private PrintService printService;
    private PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();
    private String defaultPath;

    private boolean dragging = false;
    private Point mouseStart = new Point(0, 0);
    private Point mouseCur = new Point(0, 0);

    // Property change names
    public static final String CLIP_RECTANGLE_CHANGED = ScrolledImagePanel.class
        .getName() + ".ClipRectangleChanged";
    public static final String MODE_CHANGED = ScrolledImagePanel.class
        .getName() + ".ModeChanged";

    /**
     * ScrolledImagePanel constructor. Does not use status bar.
     * 
     * @param imageModel The image model to use.
     */
    public ScrolledImagePanel(ImageModel imageModel) {
        this(imageModel, false);
    }

    /**
     * ScrolledImagePanel constructor.
     * 
     * @param imageModel The image model to use.
     * @param useStatusBar Whether to have a status bar or not.
     */
    public ScrolledImagePanel(ImageModel imageModel, boolean useStatusBar) {
        this.useStatusBar = useStatusBar;
        // Set up the ImageModel and add an ImageModelListener
        setImageModel(imageModel);

        // Set up drop
        dropTargetAdapter = new DropTargetAdapter() {
            public void drop(DropTargetDropEvent ev) {
                ImageModel model = ScrolledImagePanel.this.getImageModel();
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

        // Set up mouse listeners
        mouseAdapter = new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent ev) {
                ScrolledImagePanel.this.mousePressed(ev);
            }

            @Override
            public void mouseReleased(MouseEvent ev) {
                ScrolledImagePanel.this.mouseReleased(ev);
            }

            @Override
            public void mouseDragged(MouseEvent ev) {
                ScrolledImagePanel.this.mouseDragged(ev);
            }

            @Override
            public void mouseMoved(MouseEvent ev) {
                ScrolledImagePanel.this.mouseMoved(ev);
            }

            @Override
            public void mouseExited(MouseEvent ev) {
                ScrolledImagePanel.this.mouseExited(ev);
            }

        };
        imagePanel.addMouseListener(mouseAdapter);
        imagePanel.addMouseMotionListener(mouseAdapter);
    }

    /**
     * Initializes the user interface.
     */
    private void uiInit() {
        this.setLayout(new BorderLayout());

        imagePanel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                imagePanelPaintComponent(g);
            }

            @Override
            public Dimension getPreferredSize() {
                // This is essential for scroll bars to appear
                if(imageModel == null) return new Dimension(0, 0);
                BufferedImage image = imageModel.getCurrentImage();
                if(image == null)
                    return new Dimension(0, 0);
                else
                    return new Dimension((int)(zoom * image.getWidth()),
                        (int)(zoom * image.getHeight()));
            }
        };

        scrollPane = new JScrollPane(imagePanel);
        scrollPane.setAutoscrolls(true);
        this.add(scrollPane, BorderLayout.CENTER);

        // Status bar
        // Call setUseStatus bar to set it up or not
        setUseStatusBar(useStatusBar);
    }

    /**
     * The implementation of the image panel's paintComponent method.
     * 
     * @param g
     */
    public void imagePanelPaintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage image = getImage();
        if(image == null) return;
        int width0 = imagePanel.getWidth();
        int height0 = imagePanel.getHeight();
        if(width0 <= 0 || height0 <= 0) return;
        int width = image.getWidth();
        int height = image.getHeight();
        if(width <= 0 || height <= 0) return;

        // Do zoom
        if(zoom != 1.0) {
            Graphics2D g2D = (Graphics2D)g;
            g2D.scale(zoom, zoom);
        }

        // Draw the image
        g.drawImage(image, 0, 0, null);

        // Draw the selection rectangle
        // It seems to be OK to redraw the image each time during dragging.
        // Alternatively we could draw over the image elsewhere using XOR.
        if(mode == Mode.CROP) {
            double scaleFactor = zoom != 0 ? 1 / zoom : 1;
            Rectangle2D.Double rect = new Rectangle2D.Double(scaleFactor
                * clipRectangle.x, scaleFactor * clipRectangle.y, scaleFactor
                * clipRectangle.width, scaleFactor * clipRectangle.height);
            Graphics2D g2D = (Graphics2D)g;
            g2D.setColor(Color.WHITE);
            g2D.setXORMode(Color.BLACK);
            g2D.draw(rect);
        }
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

    public void restoreClipRectangle() {
        if(oldClipRectangle != null) {
            setClipRectangle(oldClipRectangle);
        }
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
        oldClipRectangle = clipRectangle;
        clipRectangle = new Rectangle(0, 0, 0, 0);
        if(imagePanel != null) {
            imagePanel.repaint();
            imagePanel.revalidate();
        }
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
        imagePanel.repaint();
        imagePanel.revalidate();
    }

    /**
     * Saves the image to a file
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
            File file = ImageUtils.saveImageToFile(image, defaultPath);
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
        if(imagePanel != null) {
            imagePanel.repaint();
            imagePanel.revalidate();
        }
    }

    public void zoomIn() {
        zoom *= ZOOM_FACTOR;
        if(imagePanel != null) {
            imagePanel.repaint();
            imagePanel.revalidate();
        }
    }

    public void zoomOut() {
        zoom /= ZOOM_FACTOR;
        if(imagePanel != null) {
            imagePanel.repaint();
            imagePanel.revalidate();
        }
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
        if(imagePanel != null) {
            imagePanel.repaint();
            imagePanel.revalidate();
        }
    }

    /**
     * Zooms to fit the display panel.
     */
    public void zoomFit() {
        if(imagePanel == null) return;
        Rectangle rect = this.getBounds();
        zoomToRectangle(rect.width, rect.height);
        imagePanel.repaint();
        imagePanel.revalidate();
    }

    /**
     * Zooms to fit the display panel if the image is larger than the display
     * panel.
     */
    public void zoomFitIfLarger() {
        if(imagePanel == null) return;
        Rectangle rect = this.getBounds();
        int imageWidth = 0;
        int imageHeight = 0;
        BufferedImage image = this.getImage();
        if(image != null) {
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();
        }
        if(rect.width < imageWidth || rect.height < imageHeight) {
            zoomToRectangle(rect.width, rect.height);
        } else {
            zoomReset();
        }
        imagePanel.repaint();
        imagePanel.revalidate();
    }

    /**
     * Centers the viewport on the specified coordinates.
     * 
     * @param point (x,y) coordinates.
     */
    public void centerViewport(Point point) {
        if(scrollPane == null) {
            return;
        }
        // DEBUG
        boolean debug = false;
        if(debug) {
            System.out.println("centerViewport: point=" + point);
            System.out.println("centerViewport: imagePanel.isValid: "
                + imagePanel.isValid());
            System.out.println("centerViewport: scrollPane:"
                + scrollPane.isValid());
        }
        // Call validate, not revalidate as it has to be done now to use the new
        // values
        this.validate();
        JViewport jv = scrollPane.getViewport();
        if(debug) {
            System.out.println("centerViewport: imagePanel.isValid: "
                + imagePanel.isValid());
            System.out.println("centerViewport: scrollPane:"
                + scrollPane.isValid());
            System.out.println("centerViewport: viewport: rect= "
                + jv.getViewRect());
            System.out.println("centerViewport: imagePanel: bounds="
                + imagePanel.getBounds());
        }
        if(jv != null) {
            Point newPoint = new Point();
            newPoint.x = point.x - jv.getWidth() / 2;
            newPoint.y = point.y - jv.getHeight() / 2;
            int maxX = imagePanel.getWidth() - jv.getWidth();
            int maxY = imagePanel.getHeight() - jv.getHeight();
            if(newPoint.x < 0) {
                newPoint.x = 0;
            }
            if(newPoint.y < 0) {
                newPoint.y = 0;
            }
            if(newPoint.x > maxX) {
                newPoint.x = maxX;
            }
            if(newPoint.y > maxY) {
                newPoint.y = maxY;
            }
            if(debug) {
                System.out.println("centerViewport: newPoint=" + newPoint);
                System.out.println("    Before: position="
                    + jv.getViewPosition());
            }
            jv.setViewPosition(newPoint);
            if(debug) {
                System.out.println("    After: position="
                    + jv.getViewPosition());
                System.out.println("centerViewport: viewport: rect= "
                    + jv.getViewRect());
                System.out.println("centerViewport: imagePanel: bounds="
                    + imagePanel.getBounds());
                System.out.println();
            }
        }
    }

    // Mouse listener implementation

    /**
     * Implementation of MouseInputAdapter.mousePressed.
     * 
     * @param ev
     */
    private void mousePressed(MouseEvent ev) {
        mouseMoved(ev);
        // Needed for keyboard events
        imagePanel.requestFocusInWindow();

        // Print position
        if(ev.getButton() == MouseEvent.BUTTON3) {
            System.out.println("x=" + ev.getX() + " y=" + ev.getY());
        }

        // Cancel rectangle
        if(ev.getClickCount() >= 2) {
            setClipRectangle(new Rectangle(0, 0, 0, 0));
            return;
        }

        // Zoom in
        if(ev.isShiftDown() && !ev.isControlDown() && !ev.isAltDown()) {
            Point point = new Point(ev.getPoint());
            zoomIn();
            // Calculate new x, y
            point.x *= ZOOM_FACTOR;
            point.y *= ZOOM_FACTOR;
            centerViewport(point);
            return;
        }

        // Zoom out
        if(!ev.isShiftDown() && ev.isControlDown() && !ev.isAltDown()) {
            Point point = new Point(ev.getPoint());
            zoomOut();
            // Calculate new x, y
            point.x /= ZOOM_FACTOR;
            point.y /= ZOOM_FACTOR;
            centerViewport(point);
            return;
        }

        // Zoom reset
        if(!ev.isShiftDown() && !ev.isControlDown() && ev.isAltDown()) {
            zoomReset();
            return;
        }

        // Start drag
        if(dragging) return;
        mouseCur = ev.getPoint();
        mouseStart = mouseCur;
        Rectangle newRectangle = new Rectangle();
        newRectangle.setFrameFromDiagonal(mouseStart, mouseCur);
        setClipRectangle(newRectangle);
        dragging = true;
    }

    /**
     * Implementation of MouseInputAdapter.mouseReleased.
     * 
     * @param ev
     */
    private void mouseReleased(MouseEvent ev) {
        mouseMoved(ev);
        if(dragging) {
            dragging = false;
            mouseCur = ev.getPoint();
            Rectangle newRectangle = new Rectangle();
            newRectangle.setFrameFromDiagonal(mouseStart, mouseCur);
            setClipRectangle(newRectangle);
        }
    }

    /**
     * Implementation of MouseInputAdapter.mouseDragged.
     * 
     * @param ev
     */
    private void mouseDragged(MouseEvent ev) {
        if(dragging) {
            mouseCur = ev.getPoint();
            Rectangle newRectangle = new Rectangle();
            newRectangle.setFrameFromDiagonal(mouseStart, mouseCur);
            setClipRectangle(newRectangle);
            if(useStatusBar || statusBar != null || getImage() == null) {
                int x = (int)(ev.getX() / zoom);
                int y = (int)(ev.getY() / zoom);
                int width = (int)(newRectangle.width / zoom);
                int height = (int)(newRectangle.height / zoom);
                String text = "x=" + x + " y=" + y + " [ " + width + " x "
                    + height + " ]";
                updateStatus(text);
            }
        } else {
            mouseMoved(ev);
        }
    }

    /**
     * Implementation of MouseInputAdapter.mouseMoved.
     * 
     * @param ev
     */
    private void mouseMoved(MouseEvent ev) {
        if(useStatusBar || statusBar != null || getImage() == null) {
            int x = (int)(ev.getX() / zoom);
            int y = (int)(ev.getY() / zoom);
            String text = "x=" + x + " y=" + y;
            updateStatus(text);
        }
    }

    /**
     * Implementation of MouseInputAdapter.mouseExited.
     * 
     * @param ev
     */
    private void mouseExited(MouseEvent ev) {
        if(useStatusBar || statusBar != null || getImage() == null) {
            // Use space to keep it from resizing
            updateStatus(" ");
        }
    }

    /**
     * Removes references. Use this to allow it to be garbage collected.
     */
    public void finish() {
        setImageModel(null);
        imageModelListener = null;
        if(imagePanel != null && mouseAdapter != null) {
            imagePanel.removeMouseListener(mouseAdapter);
            removeMouseMotionListener(mouseAdapter);
            mouseAdapter = null;
        }
        imagePanel = null;
        scrollPane = null;
        if(dropTarget != null) {
            if(dropTargetAdapter != null) {
                dropTarget.removeDropTargetListener(dropTargetAdapter);
                dropTargetAdapter = null;
            }
            dropTarget = null;
        }
    }

    // TODO Check if this is needed. If so, override other repaint overloads.
    // /*
    // * (non-Javadoc)
    // *
    // * @see java.awt.Component#repaint()
    // */
    // @Override
    // public void repaint() {
    // if(imagePanel != null) {
    // imagePanel.repaint();
    // }
    // super.repaint();
    // }

    /**
     * Updates the status bar with the given text.
     * 
     * @param text
     */
    public void updateStatus(String text) {
        if(!useStatusBar || statusBar == null || text == null) {
            return;
        }
        statusBar.setText(text);
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
     * @return The value of defaultPath.
     */
    public String getDefaultPath() {
        return defaultPath;
    }

    /**
     * @param defaultPath The new value for defaultPath.
     */
    public void setDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
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

    /**
     * @return The value of useStatusBar.
     */
    public boolean getUseStatusBar() {
        return useStatusBar;
    }

    /**
     * Sets up the status bar or removes it depending on the value of
     * useStatusBar.
     * 
     * @param useStatusBar The new value for useStatusBar.
     */
    public void setUseStatusBar(boolean useStatusBar) {
        this.useStatusBar = useStatusBar;
        if(useStatusBar) {
            if(statusBar == null) {
                statusBar = new JLabel();
                // Use space to keep it from resizing
                statusBar.setText(" ");
                statusBar.setToolTipText("Status");
                statusBar.setText("");
                statusBar.setBorder(BorderFactory
                    .createBevelBorder(BevelBorder.LOWERED));
                this.add(statusBar, BorderLayout.PAGE_END);
            }
        } else {
            if(statusBar != null) {
                this.remove(statusBar);
                statusBar = null;
                this.revalidate();
            }
        }
    }
}
