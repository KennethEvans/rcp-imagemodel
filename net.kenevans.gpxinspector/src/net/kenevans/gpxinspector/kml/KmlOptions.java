package net.kenevans.gpxinspector.kml;

/*
 * Created on Aug 24, 2010
 * By Kenneth Evans, Jr.
 */

public class KmlOptions
{
    private String kmlFileName;
    private static final String KML_FILE_NAME_DEFAULT = "c:/Users/evans/Documents/GPSLink/AAA.kml";
    private String trkColor;
    private static final String TRK_COLOR_DEFAULT = "ffff0000";
    private static final boolean PROMPT_TO_OVERWRITE_DEFAULT = false;
    private boolean promptToOverwrite;
    private static final boolean SEND_TO_GOOGLE_DEFAULT = true;
    private boolean sendToGoogle;

    public KmlOptions() {
        // TODO Use preferences here
        kmlFileName = KML_FILE_NAME_DEFAULT;
        promptToOverwrite = PROMPT_TO_OVERWRITE_DEFAULT;
        trkColor = TRK_COLOR_DEFAULT;
        sendToGoogle = SEND_TO_GOOGLE_DEFAULT;
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

}
