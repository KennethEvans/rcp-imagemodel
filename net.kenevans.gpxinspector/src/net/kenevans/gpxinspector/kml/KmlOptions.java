package net.kenevans.gpxinspector.kml;

import net.kenevans.gpxinspector.plugin.Activator;
import net.kenevans.gpxinspector.preferences.IPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;

/*
 * Created on Aug 24, 2010
 * By Kenneth Evans, Jr.
 */

public class KmlOptions implements IPreferenceConstants
{
    private String kmlFileName;
    private double iconScale;

    private String trkColor;
    private String trkAlpha;
    private double trkLineWidth;
    private int trkColorMode;
    private Boolean useTrkIcon;
    private String trkIconUrl;

    private String rteColor;
    private String rteAlpha;
    private double rteLineWidth;
    private int rteColorMode;
    private Boolean useRteIcon;
    private String rteIconUrl;

    private String wptColor;
    private String wptAlpha;
    private String wptIconUrl;
    private int wptColorMode;

    private boolean promptToOverwrite;
    private boolean sendToGoogle;

    public KmlOptions() {
        // Get the preferences
        // Don't use a preferences listener as this class is used so that the
        // last user options are retained
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        kmlFileName = prefs.getString(P_KML_FILENAME);
        promptToOverwrite = prefs.getBoolean(P_KML_PROMPT_TO_OVERWRITE);
        sendToGoogle = prefs.getBoolean(P_KML_SEND_TO_GOOGLE_EARTH);

        String stringVal = prefs.getString(P_ICON_SCALE);
        iconScale = Double.parseDouble(stringVal);

        trkColor = prefs.getString(P_TRK_COLOR);
        trkAlpha = prefs.getString(P_TRK_ALPHA);
        stringVal = prefs.getString(P_TRK_LINEWIDTH);
        trkLineWidth = Double.parseDouble(stringVal);
        trkColorMode = prefs.getInt(P_TRK_COLOR_MODE);
        useTrkIcon = prefs.getBoolean(P_USE_TRK_ICON);
        trkIconUrl = prefs.getString(P_TRK_ICON_URL);

        rteColor = prefs.getString(P_RTE_COLOR);
        rteAlpha = prefs.getString(P_RTE_ALPHA);
        stringVal = prefs.getString(P_RTE_LINEWIDTH);
        rteLineWidth = Double.parseDouble(stringVal);
        useRteIcon = prefs.getBoolean(P_USE_RTE_ICON);
        rteIconUrl = prefs.getString(P_RTE_ICON_URL);

        wptColor = prefs.getString(P_WPT_COLOR);
        wptAlpha = prefs.getString(P_WPT_ALPHA);
        wptIconUrl = prefs.getString(P_WPT_ICON_URL);
    }

    /**
     * @return The value of kmlFileName.
     */
    public String getKmlFileName() {
        return kmlFileName;
    }

    /**
     * @param kmlFileName The new value for kmlFileName.
     */
    public void setKmlFileName(String kmlFileName) {
        this.kmlFileName = kmlFileName;
    }

    /**
     * @return The value of iconScale.
     */
    public double getIconScale() {
        return iconScale;
    }

    /**
     * @param iconScale The new value for iconScale.
     */
    public void setIconScale(double iconScale) {
        this.iconScale = iconScale;
    }

    /**
     * @return The value of trkColor.
     */
    public String getTrkColor() {
        return trkColor;
    }

    /**
     * @param trkColor The new value for trkColor.
     */
    public void setTrkColor(String trkColor) {
        this.trkColor = trkColor;
    }

    /**
     * @return The value of trkAlpha.
     */
    public String getTrkAlpha() {
        return trkAlpha;
    }

    /**
     * @param trkAlpha The new value for trkAlpha.
     */
    public void setTrkAlpha(String trkAlpha) {
        this.trkAlpha = trkAlpha;
    }

    /**
     * @return The value of trkLineWidth.
     */
    public double getTrkLineWidth() {
        return trkLineWidth;
    }

    /**
     * @param trkLineWidth The new value for trkLineWidth.
     */
    public void setTrkLineWidth(double trkLineWidth) {
        this.trkLineWidth = trkLineWidth;
    }

    /**
     * @return The value of trkColorMode.
     */
    public int getTrkColorMode() {
        return trkColorMode;
    }

    /**
     * @param trkColorMode The new value for trkColorMode.
     */
    public void setTrkColorMode(int trkColorMode) {
        this.trkColorMode = trkColorMode;
    }

    /**
     * @return The value of useTrkIcon.
     */
    public Boolean getUseTrkIcon() {
        return useTrkIcon;
    }

    /**
     * @param useTrkIcon The new value for useTrkIcon.
     */
    public void setUseTrkIcon(Boolean useTrkIcon) {
        this.useTrkIcon = useTrkIcon;
    }

    /**
     * @return The value of trkIconUrl.
     */
    public String getTrkIconUrl() {
        return trkIconUrl;
    }

    /**
     * @param trkIconUrl The new value for trkIconUrl.
     */
    public void setTrkIconUrl(String trkIconUrl) {
        this.trkIconUrl = trkIconUrl;
    }

    /**
     * @return The value of rteColor.
     */
    public String getRteColor() {
        return rteColor;
    }

    /**
     * @param rteColor The new value for rteColor.
     */
    public void setRteColor(String rteColor) {
        this.rteColor = rteColor;
    }

    /**
     * @return The value of rteAlpha.
     */
    public String getRteAlpha() {
        return rteAlpha;
    }

    /**
     * @param rteAlpha The new value for rteAlpha.
     */
    public void setRteAlpha(String rteAlpha) {
        this.rteAlpha = rteAlpha;
    }

    /**
     * @return The value of rteLineWidth.
     */
    public double getRteLineWidth() {
        return rteLineWidth;
    }

    /**
     * @param rteLineWidth The new value for rteLineWidth.
     */
    public void setRteLineWidth(double rteLineWidth) {
        this.rteLineWidth = rteLineWidth;
    }

    /**
     * @return The value of rteColorMode.
     */
    public int getRteColorMode() {
        return rteColorMode;
    }

    /**
     * @param rteColorMode The new value for rteColorMode.
     */
    public void setRteColorMode(int rteColorMode) {
        this.rteColorMode = rteColorMode;
    }

    /**
     * @return The value of useRteIcon.
     */
    public Boolean getUseRteIcon() {
        return useRteIcon;
    }

    /**
     * @param useRteIcon The new value for useRteIcon.
     */
    public void setUseRteIcon(Boolean useRteIcon) {
        this.useRteIcon = useRteIcon;
    }

    /**
     * @return The value of rteIconUrl.
     */
    public String getRteIconUrl() {
        return rteIconUrl;
    }

    /**
     * @param rteIconUrl The new value for rteIconUrl.
     */
    public void setRteIconUrl(String rteIconUrl) {
        this.rteIconUrl = rteIconUrl;
    }

    /**
     * @return The value of wptColor.
     */
    public String getWptColor() {
        return wptColor;
    }

    /**
     * @param wptColor The new value for wptColor.
     */
    public void setWptColor(String wptColor) {
        this.wptColor = wptColor;
    }

    /**
     * @return The value of wptAlpha.
     */
    public String getWptAlpha() {
        return wptAlpha;
    }

    /**
     * @param wptAlpha The new value for wptAlpha.
     */
    public void setWptAlpha(String wptAlpha) {
        this.wptAlpha = wptAlpha;
    }

    /**
     * @return The value of wptIconUrl.
     */
    public String getWptIconUrl() {
        return wptIconUrl;
    }

    /**
     * @param wptIconUrl The new value for wptIconUrl.
     */
    public void setWptIconUrl(String wptIconUrl) {
        this.wptIconUrl = wptIconUrl;
    }

    /**
     * @return The value of promptToOverwrite.
     */
    public boolean getPromptToOverwrite() {
        return promptToOverwrite;
    }

    /**
     * @param promptToOverwrite The new value for promptToOverwrite.
     */
    public void setPromptToOverwrite(boolean promptToOverwrite) {
        this.promptToOverwrite = promptToOverwrite;
    }

    /**
     * @return The value of sendToGoogle.
     */
    public boolean getSendToGoogle() {
        return sendToGoogle;
    }

    /**
     * @param sendToGoogle The new value for sendToGoogle.
     */
    public void setSendToGoogle(boolean sendToGoogle) {
        this.sendToGoogle = sendToGoogle;
    }

    /**
     * @return The value of wptColorMode.
     */
    public int getWptColorMode() {
        return wptColorMode;
    }

    /**
     * @param wptColorMode The new value for wptColorMode.
     */
    public void setWptColorMode(int wptColorMode) {
        this.wptColorMode = wptColorMode;
    }

}
