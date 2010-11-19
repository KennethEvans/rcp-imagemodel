package utils;

import net.kenevans.gpxinspector.utils.SWTUtils;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/*
 * Created on Nov 18, 2010
 * By Kenneth Evans, Jr.
 */

/**
 * LabeledText creates a zero-margin Composite with a Label and a Text.
 * 
 * @author Kenneth Evans, Jr.
 */
public class LabeledText
{
    private Composite composite;
    private Label label;
    private Text text;

    /**
     * LabeledText constructor.
     * 
     * @param parent The parent of the composite.
     * @param labelText The text for the Label.
     * @param textWidth The suggested width in columns for the Text.
     */
    public LabeledText(Composite parent, String labelText, int textWidth) {
        // Make a zero margin composite
        composite = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
            .grab(true, false).applyTo(composite);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);

        label = new Label(composite, SWT.NONE);
        label.setText(labelText);
        GridDataFactory.fillDefaults().applyTo(label);

        text = new Text(composite, SWT.NONE);
        GridDataFactory
            .fillDefaults()
            .hint(
                new Point(SWTUtils.getTextWidth(text, textWidth), SWT.DEFAULT))
            .align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(text);
    }

    /**
     * @return The value of composite.
     */
    public Composite getComposite() {
        return composite;
    }

    /**
     * @return The value of label.
     */
    public Label getLabel() {
        return label;
    }

    /**
     * @return The value of text.
     */
    public Text getText() {
        return text;
    }
}
