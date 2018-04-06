package gitlet;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.IOException;


/** Representation of the version-control system Gitlet.
 * @author Matt Brennan
 * */

public class Gitlet {
    /** the path to the hash. */
    private static String pathHash = ".gitlet/hash";
    /** the path to the list. */
    private static String pathList = ".gitlet/list";
    /** the path to the stage. */
    private static String pathStage = ".gitlet/stage";

    /** The constuctor for Gitlet which takes in the commands.
     * @param commands the commands to be implemented */
    public Gitlet(String[] commands) {
        File dir = new File(".gitlet");
        if (!dir.exists()) {
            dir.mkdir();
            HashMap<String, Commit> hashCommits = new HashMap<String, Commit>();
            Commit initCommit = new Commit("initial commit", null, "master");
            hashCommits.put("master", initCommit);
            Staging initStage = new Staging("master");
            ArrayList<Commit> listCommits = new ArrayList<>();
            listCommits.add(initCommit);
            Utils.serialize(hashCommits, pathHash);
            Utils.serialize(listCommits, pathList);
            Utils.serialize(initStage, pathStage);
        } else {
            System.out.println("A gitlet version control system "
                   + "already exists in the current directory.");
        }
    }

    /** Finds the command with this commit msg and returns the sha-1.
     * @param commands the commands to be implemented */
    @SuppressWarnings("unchecked")
    public static void find(String[] commands) {
        if (commands.length == 1) {
            System.out.println("Did not enter a commit message.");
            return;
        }
        int totalPrinted = 0;
        String findMsg = commands[1];

        Object arrObj = Utils.deserialize(pathList);
        ArrayList<Commit> lst = (ArrayList<Commit>) arrObj;

        for (Commit com : lst) {
            if (com.getMsg().equals(findMsg)) {
                System.out.println(com.getSha());
                totalPrinted += 1;
            }
        }
        if (totalPrinted == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** Helper function for status in order to add blank rows. */
    public static void addGap() {
        System.out.println();
    }

    /** Helper function to add all elements to a list and sort.
     * @param keys the keys that are transformed into an array
     * @return ArrayList<String>*/
    public static ArrayList<String> settoArray(Set<String> keys) {
        ArrayList<String> recordedList = new ArrayList<>();
        for (String s : keys) {
            recordedList.add(s);
        }
        Collections.sort(recordedList);
        return recordedList;
    }

    /** Lists the branches, staged files,
     *  removed, not staged, and untracked for the gitlet. */
    @SuppressWarnings("unchecked")
    public static void status() {
        System.out.println("=== Branches ===");

        Object stageObj = Utils.deserialize(pathStage);
        Object hashObj = Utils.deserialize(pathHash);
        Staging stage = (Staging) stageObj;
        HashMap<String, Commit> hash = (HashMap<String, Commit>) hashObj;

        ArrayList<String> recordedList = settoArray(hash.keySet());
        for (String s : recordedList) {
            if (s.equals(stage.getbranchStage())) {
                System.out.println("*" + s);
            } else {
                System.out.println(s);
            }
        }
        addGap();

        System.out.println("=== Staged Files ===");
        for (String file : stage.getFiles()) {
            System.out.println(file);
        }
        addGap();

        System.out.println("=== Removed Files ===");
        for (String file : stage.getremovedFiles()) {
            System.out.println(file);
        }
        addGap();

        System.out.println("=== Modifications Not Staged For Commit ===");
        addGap();

        System.out.println("=== Untracked Files ===");
        addGap();
    }

    /** Checks out the specified commands depending on the arguments.
     * @param commands the commands to be implemented */
    @SuppressWarnings("unchecked")
    public static void checkout(String[] commands) {
        if (errorArgs(commands)) {
            return;
        }
        String input = commands[1];
        int len = commands.length;

        Object stageObj = Utils.deserialize(pathStage);
        Object hashObj = Utils.deserialize(pathHash);
        Staging stage = (Staging) stageObj;
        HashMap<String, Commit> hash = (HashMap<String, Commit>) hashObj;


        if (input.equals(stage.getbranchStage())) {
            System.out.println("No need to checkout the current branch.");
            return;
        } else if (len == 4) {
            String id = commands[1];
            input = commands[3];
            for (String branch : hash.keySet()) {
                Commit com = hash.get(branch);
                String curBranch = com.getBranch();
                while (com != null && com.getBranch().equals(curBranch)) {
                    if (com.getSha().equals(id)
                            || com.getSha().startsWith(id, 0)) {
                        id = com.getSha();
                        if (check(com, id, input)) {
                            return;
                        }
                        while (!com.getFiles().contains(input)) {
                            com = com.getParent();
                        }
                        String address = ".gitlet/"
                                + com.getSha() + "/" + input;
                        Path oldPath = Paths.get(address);
                        Path newPath = Paths.get(input);
                        copy(oldPath, newPath);
                    }
                    com = com.getParent();
                }
            }
            System.out.println("No commit with that id exists.");
        } else {
            if (len == 3) {
                input = commands[2];
            }
            for (String branch : hash.keySet()) {
                if (branch.equals(input)) {
                    wholebranchCheck(stage, hash, input);
                    return;
                }
            }
            specificfileCheck(stage, hash, input);
        }

    }

    /** Takes specific file and puts it into a working directory,
     * overwriting the current file.
     * @param stage the current stage
     * @param hash the commits
     * @param input the file name
     */
    public static void specificfileCheck(Staging stage,
           HashMap<String, Commit> hash, String input) {
        Commit com = hash.get(stage.getbranchStage());
        if (stage.getfamilyFiles().contains(input)) {
            while (!com.getFiles().contains(input)) {
                com = com.getParent();
            }
            String address = ".gitlet/" + com.getSha() + "/" + input;
            Path oldPath = Paths.get(address);
            Path newPath = Paths.get(input);
            copy(oldPath, newPath);
        } else {
            System.out.println("No such branch exists.");
        }
    }

    /** Takes all of the files of a branch and puts them into a working,
     * directory overwriting the current file, and changing the head pointer.
     * @param stage the current stage
     * @param hash the commits
     * @param input the file name
     */
    public static void wholebranchCheck(Staging stage,
           HashMap<String, Commit> hash, String input) {
        Commit com = hash.get(stage.getbranchStage());
        for (String file : Utils.plainFilenamesIn(".")) {
            int size = file.length();
            if (file.substring(size - 4, size).equals(".txt")) {
                if (!com.getFiles().contains(file)
                        && !com.getfamilyFiles().contains(file)
                        && !stage.getFiles().contains(file)) {
                    System.out.println("There is an untracked file "
                            + "in the way; delete it or add it first.");
                    return;
                }
            }
        }
        stage.setbranchStage(input);
        stage.clearStage();
        stage.setfamilyFiles(new HashSet<String>());
        Commit pointHead = hash.get(input);
        stage.modifyStage(pointHead);
        Utils.serialize(stage, pathStage);
        for (String file : Utils.plainFilenamesIn(".")) {
            int size = file.length();
            if (file.substring(size - 4, size).equals(".txt")) {
                if (!pointHead.getFiles().contains(file)) {
                    File f = new File(file);
                    Utils.restrictedDelete(f);
                }
            }
        }
        for (String file : pointHead.getFiles()) {
            String address =
                    ".gitlet/" + pointHead.getSha() + "/" + file;
            Path newPath = Paths.get(file);
            Path oldPath = Paths.get(address);
            copy(oldPath, newPath);
        }
        for (String file : pointHead.getfamilyFiles()) {
            Commit temp = pointHead;
            while (!temp.getFiles().contains(file)) {
                temp = temp.getParent();
            }
            String address = ".gitlet/" + temp.getSha() + "/" + file;
            Path newPath = Paths.get(file);
            Path oldPath = Paths.get(address);
            copy(oldPath, newPath);
        }
    }

    /** Clears the current stage and checkout the commit.
     * @param commands the commands to be interpreted */
    @SuppressWarnings("unchecked")
    public static void reset(String[] commands) {
        if (errorArgs(commands)) {
            return;
        }
        String id = commands[1];

        Object stageObj = Utils.deserialize(pathStage);
        Object hashObj = Utils.deserialize(pathHash);
        Staging stage = (Staging) stageObj;
        HashMap<String, Commit> hash = (HashMap<String, Commit>) hashObj;

        for (String s : hash.keySet()) {
            Commit com = hash.get(s);
            String branchCom = com.getBranch();
            while (com != null && com.getBranch().equals(branchCom)) {
                if (com.getSha().equals(id) || com.getSha().startsWith(id, 0)) {
                    stage.clearStage();
                    stage.modifyStage(com);
                    hash.put(stage.getbranchStage(), com);
                    Utils.serialize(stage, pathStage);
                    Utils.serialize(hash, pathHash);
                    for (String fil : com.getfamilyFiles()) {
                        Commit modCom = com;
                        while (!modCom.getFiles().contains(fil)) {
                            modCom = modCom.getParent();
                        }
                        String address = ".gitlet/"
                                + modCom.getSha() + "/" + fil;
                        Path oldPath = Paths.get(address);
                        Path newPath = Paths.get(fil);
                        copy(oldPath, newPath);
                    }

                    for (String fil : com.getFiles()) {
                        String strPath = ".gitlet/" + com.getSha() + "/" + fil;
                        Path oldPath = Paths.get(strPath);
                        Path newPath = Paths.get(fil);
                        copy(oldPath, newPath);
                    }
                    return;
                }
                com = com.getParent();
            }
        }
        System.out.println("No commit with that id exists");
    }

    /** Copies a file from one path to another.
     * @param oldpath the path oldpath
     * @param newpath the new path */
    public static void copy(Path oldpath, Path newpath) {
        try {
            Files.copy(oldpath, newpath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("IOException when coping.");
        }
    }

    /** Checks to see if file is in commit files or inherited.
     * @param com the commit needed for check
     * @param id the id needed for check
     * @param input the input needed for check
     * @return boolean*/
    private static boolean check(Commit com, String id, String input) {
        if (!com.getfamilyFiles().contains(input)
                && !com.getFiles().contains(input)) {
            System.out.println(
                    "File does not exist in that commit.");
            return true;
        }
        if (com.getFiles().contains(input)) {
            String address = ".gitlet/" + id + "/" + input;
            Path newPath = Paths.get(input);
            Path oldPath = Paths.get(address);
            copy(oldPath, newPath);
            return true;
        }
        return false;
    }

    /** Determines whether there are any initial errors with the commands.
     * @param commands the commands to be interpreted
     * @return
     */
    public static boolean errorArgs(String[] commands) {
        if (commands.length == 1) {
            System.out.print("Did not enter argument after command.");
            return true;
        } else {
            return false;
        }
    }





}
