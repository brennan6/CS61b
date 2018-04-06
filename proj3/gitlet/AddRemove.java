package gitlet;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;

/** Add and remove files from the staging area.
 * @author Matt Brennan
 */

public class AddRemove {
    /** the path to the hash. */
    private static String pathHash = ".gitlet/hash";
    /** the path to the list. */
    private static String pathList = ".gitlet/list";
    /** the path to the stage. */
    private static String pathStage = ".gitlet/stage";

    /** Adding a file to the staging area of the Gitlet.
     * @param commands the commands to be implemented */
    @SuppressWarnings("unchecked")
    public static void add(String[] commands) {
        if (Gitlet.errorArgs(commands)) {
            return;
        }
        String addStr = commands[1];
        File addFil = new File(addStr);

        Object stageObj = Utils.deserialize(pathStage);
        Object hashObj = Utils.deserialize(pathHash);
        Staging stage = (Staging) stageObj;
        HashMap<String, Commit> hash = (HashMap<String, Commit>) hashObj;

        if (addFil.isFile()) {
            if (stage.getfamilyFiles() != null) {
                if (stage.getfamilyFiles().contains(addStr)) {
                    Commit com = hash.get(stage.getbranchStage());
                    while (!com.getFiles().contains(addStr) && com != null) {
                        com = com.getParent();
                    }
                    if (Utils.compareFiles(Paths.get(addStr),
                            Paths.get(".gitlet/"
                            + com.getSha() + "/" + addStr))) {
                        return;
                    }
                }
            }
            stage.getFiles().add(addStr);
            if (stage.getremovedFiles() != null) {
                if (stage.getremovedFiles().contains(addStr)) {
                    stage.getremovedFiles().remove(addStr);
                    stage.getFiles().remove(addStr);
                }
            }
            Utils.serialize(stage, pathStage);
        } else {
            System.out.println("File does not exist.");
        }
    }

    /** Removing a file from the staging are of the Gitlet.
     * @param commands the commands to be implemented */
    @SuppressWarnings("unchecked")
    public static void remove(String[] commands) {
        if (Gitlet.errorArgs(commands)) {
            return;
        }
        String remStr = commands[1];

        Object stageObj = Utils.deserialize(pathStage);
        Object hashObj = Utils.deserialize(pathHash);
        Staging stage = (Staging) stageObj;
        HashMap<String, Commit> hash = (HashMap<String, Commit>) hashObj;

        Commit branchCom = hash.get(stage.getbranchStage());

        if (!stage.getFiles().contains(remStr)
                && !branchCom.getFiles().contains(remStr)) {
            System.out.println("No reason to remove the file.");
        }

        if (stage.getfamilyFiles().contains(remStr)) {
            stage.getfamilyFiles().remove(remStr);
        }

        if (stage.getFiles().contains(remStr)) {
            stage.getFiles().remove(remStr);
        }

        if (branchCom.getFiles().contains(remStr)) {
            File remFil = new File(remStr);
            if (remFil.isFile()) {
                Utils.restrictedDelete(remFil);
            }
            branchCom.getFiles().remove(remStr);
            stage.getremovedFiles().add(remStr);
        }

        Utils.serialize(stage, pathStage);
    }

}
