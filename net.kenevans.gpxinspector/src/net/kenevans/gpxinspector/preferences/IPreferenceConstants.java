package net.kenevans.gpxinspector.preferences;

import net.kenevans.gpxinspector.plugin.IPluginConstants;

/**
 * Constant definitions for plug-in preferences. Names of preferences start with
 * P_ and default values start with D_.
 */
public interface IPreferenceConstants extends IPluginConstants
{
    /** Files to load at startup */
    public static final String P_STARTUP_FILES = "startupFiles";
    public static final String D_STARTUP_FILES = "";
    /** Whether to use the startup files */
    public static final String P_USE_STARTUP_FILES = "useStartupFiles";
    public static final Boolean D_USE_STARTUP_FILES = true;

    public static final String P_GPX_DIR = "gpxDirectory";
    public static final String D_GPX_DIR = "c:/Users/evans/Documents/GPSLink";

    public static final String P_KML_FILENAME = "kmlFileName";
    public static final String D_KML_FILENAME = "c:/Users/evans/Documents/GPSLink/AAA.kml";
    
    /** The track color */
    public static final String P_TRK_COLOR = "trkColor";
    public static final String D_TRK_COLOR = "ff0000";
    /** The track alpha */
    public static final String P_TRK_ALPHA = "trkAlpha";
    public static final String D_TRK_ALPHA = "ff";
    /** The track line width. */
    public static final String P_TRK_LINEWIDTH = "trkLineWidth";
    public static final String D_TRK_LINEWIDTH = "2.0";
    /** The track color mode */
    public static final String P_TRK_COLOR_MODE = "trkColorMode";
    public static final int D_TRK_COLOR_MODE = KML_COLOR_MODE_RAINBOW;

    /** The route line width. */
    public static final String P_RTE_LINEWIDTH = "rteLineWidth";
    public static final String D_RTE_LINEWIDTH = "2.0";
    /** The route color mode */
    public static final String P_RTE_COLOR_MODE = "rteColorMode";
    public static final int D_RTE_COLOR_MODE = KML_COLOR_MODE_COLOR;
    /** The routepoint icon color, actually a mask to & the image with */
    public static final String P_RTE_COLOR = "rteIconColor";
    public static final String D_RTE_COLOR = "0000ff";
    /** The waypoint alpha */
    public static final String P_RTE_ALPHA = "rteAlpha";
    public static final String D_RTE_ALPHA = "ff";

    /** The waypoint icon color, actually a mask to & the image with */
    public static final String P_WPT_COLOR = "wptIconColor";
    public static final String D_WPT_COLOR = "ffcc66";
    /** The waypoint alpha */
    public static final String P_WPT_ALPHA = "wptAlpha";
    public static final String D_WPT_ALPHA = "ff";
    /** The track color mode */
    public static final String P_WPT_COLOR_MODE = "wptColorMode";
    public static final int D_WPT_COLOR_MODE = KML_COLOR_MODE_COLOR;

    /** The icon scale, Use 1 for normal and 0 for label only (no icon) */
    public static final String P_ICON_SCALE = "iconScale";
    public static final String D_ICON_SCALE = "1.0";
    /** Determines if icons are shown at the start of a track */
    public static final String P_USE_TRK_ICON = "useTrkIcon";
    public static final Boolean D_USE_TRK_ICON = true;
    /** Determines if icons are shown at the start of a route */
    public static final String P_USE_RTE_ICON = "useTrkIcon";
    public static final Boolean D_USE_RTE_ICON = true;
    /**
     * The URL for the home icon. Using white will allow the mask to be more
     * effective. (The default icon is Yellow = #ff00ffff).
     */
    public static final String P_TRK_ICON_URL = "homeIconUrl";
    public static final String D_TRK_ICON_URL = "http://maps.google.com/mapfiles/kml/pushpin/wht-pushpin.png";
    /**
     * The URL for the routepoint icon. Using white will allow the mask to be more
     * effective. (The default icon is Yellow = #ff00ffff).
     */
    public static final String P_RTE_ICON_URL = "rteIconUrl";
    public static final String D_RTE_ICON_URL = "http://maps.google.com/mapfiles/kml/paddle/wht-circle.png";
    /**
     * The URL for the waypoint icon. Using white will allow the mask to be more
     * effective. (The default icon is Yellow = #ff00ffff).
     */
    public static final String P_WPT_ICON_URL = "wptIconUrl";
    public static final String D_WPT_ICON_URL = "http://maps.google.com/mapfiles/kml/paddle/wht-circle.png";

    /** Whether to prompt before overwriting the KML file. */
    public static final String P_KML_PROMPT_TO_OVERWRITE = "kmlPromptToOverwrite";
    public static final Boolean D_KML_PROMPT_TO_OVERWRITE = false;
    /** Whether to send the KML file to Google Earth. */
    public static final String P_KML_SEND_TO_GOOGLE_EARTH = "kmlSendToGoogle";
    public static final Boolean D_KML_SEND_TO_GOOGLE_EARTH = true;
}
