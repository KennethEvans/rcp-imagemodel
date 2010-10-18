package net.kenevans.gpxinspector.preferences;

import net.kenevans.gpxinspector.plugin.Activator;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * GpxInspectorPreferencePage
 * 
 * @author Kenneth Evans, Jr.
 */
public class KMLPreferencePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage, IPreferenceConstants
{

    public KMLPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */
    public void createFieldEditors() {
        Composite parent = getFieldEditorParent();
        addField(new FileFieldEditor(IPreferenceConstants.P_KML_FILENAME,
            "KML file name:", parent));
        addField(new BooleanFieldEditor(
            IPreferenceConstants.P_KML_PROMPT_TO_OVERWRITE,
            "Prompt before overwriting KML file:", parent));
        addField(new BooleanFieldEditor(
            IPreferenceConstants.P_KML_SEND_TO_GOOGLE_EARTH,
            "Send new KML file to Google Earth:", parent));

        addField(new StringFieldEditor(IPreferenceConstants.P_ICON_SCALE,
            "Icon scale:", parent));
        addField(new StringFieldEditor(IPreferenceConstants.P_WPT_ICON_URL,
            "Waypoint icon URL:", parent));
        addField(new StringFieldEditor(IPreferenceConstants.P_TRK_ICON_URL,
            "Track icon URL:", parent));
        addField(new StringFieldEditor(IPreferenceConstants.P_WPT_COLOR,
            "Waypoint color:", parent));
        addField(new StringFieldEditor(IPreferenceConstants.P_WPT_ALPHA,
            "Waypoint alpha:", parent));
        addField(new StringFieldEditor(IPreferenceConstants.P_TRK_COLOR,
            "Track color:", parent));
        addField(new StringFieldEditor(IPreferenceConstants.P_TRK_ALPHA,
            "Track alpha:", parent));
        addField(new StringFieldEditor(IPreferenceConstants.P_TRK_LINEWIDTH,
            "Track line width:", parent));
        addField(new ComboFieldEditor(IPreferenceConstants.P_TRK_COLOR_MODE,
            "Track color mode:", kmlColorModes, parent));
        addField(new BooleanFieldEditor(IPreferenceConstants.P_USE_TRK_ICON,
            "Use icon at start of track:", parent));

        Label label = new Label(parent, SWT.WRAP);
        label.setText("Note: Colors are text strings of the form bbggrr.\n"
            + "Alphas are text strings of the form aa and represent the\n" 
            + "transparency (00 is transparent and ff is opaque).  These\n"
            + "are combined internally to make a color of the form aabbggrr.\n"
            + "E.g. 77ff0000 is semi-transparent blue. Icon colors and\n"
            + "alphas are actually masks that are &&'ed with the icon URL's\n"
            + "colors.");
        GridDataFactory.fillDefaults().grab(false, false).span(2, 1)
            .applyTo(label);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
    }

}