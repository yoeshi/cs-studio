package org.csstudio.alarm.beast.msghist;

import org.eclipse.osgi.util.NLS;

/** Externalized Strings
 *  @author Kay Kasemir
 *  @author Eclipse String Externalization Wiard
 */
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.csstudio.sns.msghist.messages"; //$NON-NLS-1$

    public static String Error;
    public static String Export;
    public static String ExportErrorFmt;
    public static String ExportTitle;
    public static String Filter_and;
    public static String Filter_Message;
    public static String Filter_NoFilter;
    public static String Filter_Property;
    public static String Filter_PropertyTT;
    public static String Filter_Value;
    public static String Filter_ValuePatternHelp;
    public static String Filter_ValueTT;
    public static String MessageDetail;

    public static String MessageHistory;
    public static String Pref_Password;

    public static String Pref_Schema;

    public static String Pref_Starttime;

    public static String Pref_URL;

    public static String Pref_User;

    public static String Property;
    public static String ShowDetail;
    public static String Value;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
        // Prevent instantiation
    }
}