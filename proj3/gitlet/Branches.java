package gitlet;

import java.util.HashMap;

/** The Branch functions that can be utilized by the gitlet.
 * @author Matt Brennan
 */

public class Branches {
    /** the path to the hash. */
    private static String pathHash = ".gitlet/hash";
    /** the path to the list. */
    private static String pathList = ".gitlet/list";
    /** the path to the stage. */
    private static String pathStage = ".gitlet/stage";

    /** Creates a new branch that acts as a pointer.
     * @param commands the commands to be implemented */
    @SuppressWarnings("unchecked")
    public static void branch(String[] commands) {
        if (Gitlet.errorArgs(commands)) {
            return;
        }
        String branch = commands[1];

        Object stageObj = Utils.deserialize(pathStage);
        Object hashObj = Utils.deserialize(pathHash);
        Staging stage = (Staging) stageObj;
        HashMap<String, Commit> hash = (HashMap<String, Commit>) hashObj;

        if (hash.containsKey(branch)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        Commit targetCom = hash.get(stage.getbranchStage());
        hash.put(branch, targetCom);
        Utils.serialize(hash, pathHash);
    }

    /** Removes a branch pointer.
     * @param commands the commands to be implemented */
    @SuppressWarnings("unchecked")
    public static void rmbranch(String[] commands) {
        if (Gitlet.errorArgs(commands)) {
            return;
        }
        String branchRm = commands[1];
        Boolean exists = false;

        Object stageObj = Utils.deserialize(pathStage);
        Object hashObj = Utils.deserialize(pathHash);
        Staging stage = (Staging) stageObj;
        HashMap<String, Commit> hash = (HashMap<String, Commit>) hashObj;


        for (String s: hash.keySet()) {
            if (s.equals(branchRm)) {
                exists = true;
            }
        }

        if (exists) {
            if (stage.getbranchStage().equals(branchRm)) {
                System.out.println("Cannot remove the current branch.");
                return;
            }
        } else {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        hash.remove(branchRm);
        Utils.serialize(hash, pathHash);
    }
}
