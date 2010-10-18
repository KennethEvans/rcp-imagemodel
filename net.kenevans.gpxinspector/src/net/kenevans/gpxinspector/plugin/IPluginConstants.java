package net.kenevans.gpxinspector.plugin;

/*
 * Created on Oct 11, 2010
 * By Kenneth Evans, Jr.
 */

public interface IPluginConstants
{
    public static final int KML_COLOR_MODE_COLOR = 0;
    public static final int KML_COLOR_MODE_COLORSET = 1;
    public static final int KML_COLOR_MODE_RAINBOW = 2;
    public static final String[][] kmlColorModes = {
        {"Color", Integer.toString(KML_COLOR_MODE_COLOR)},
        {"Color Set", Integer.toString(KML_COLOR_MODE_COLORSET)},
        {"Rainbow", Integer.toString(KML_COLOR_MODE_RAINBOW)},};

}
