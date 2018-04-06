package gitlet;

import java.util.ArrayList;
import java.util.HashMap;

/** The functions that rely on some form of logging
 *  the information and printing.
 * @author Matt Brennan
 */

public class Logs {
    /** the path to the hash. */
    private static String pathHash = ".gitlet/hash";
    /** the path to the list. */
    private static String pathList = ".gitlet/list";
    /** the path to the stage. */
    private static String pathStage = ".gitlet/stage";

    /** Displays the log of the current branch of commits. */
    @SuppressWarnings("unchecked")
    public static void log() {
        Object stageObj = Utils.deserialize(pathStage);
        Object hashObj = Utils.deserialize(pathHash);
        Staging stage = (Staging) stageObj;
        HashMap<String, Commit> hash = (HashMap<String, Commit>) hashObj;

        Commit com = hash.get(stage.getbranchStage());
        while (com != null) {
            com.print();
            com = com.getParent();
        }
    }

    /** Displays the log of the all commits. */
    @SuppressWarnings("unchecked")
    public static void globallog() {
        Object arrObj = Utils.deserialize(pathList);
        ArrayList<Commit> lst = (ArrayList<Commit>) arrObj;

        for (Commit com : lst) {
            com.print();
        }
    }
}
