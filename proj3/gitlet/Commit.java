package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/** Representation of a Commit which is needed for the Gitlet to be complete.
 * @author Matt Brennan
 * */

public class Commit implements Serializable {
    /** the path to the hash. */
    private static String pathHash = ".gitlet/hash";
    /** the path to the list. */
    private static String pathList = ".gitlet/list";
    /** the path to the stage. */
    private static String pathStage = ".gitlet/stage";

    /** The constructor for the Commit.
     * @param msg the message of the commit
     * @param parent the parent commit
     * @param branch the branch that the commit associates*/
    public Commit(String msg, Commit parent, String branch) {
        _msg = msg;
        SimpleDateFormat formatter
                = new SimpleDateFormat("EEE MMM d hh:mm:ss yyyy Z");
        setParent(parent);
        setBranch(branch);
        Date dateCom = new Date();
        _timeCom = formatter.format(dateCom);
        _sha = Utils.sha1(_msg, _timeCom);

    }

    /** Committing the staging area to the larger Commit Tree.
     * @param commands the implemented commands */
    @SuppressWarnings("unchecked")
    public static void commit(String[] commands) {
        if (commands.length == 1 || commands[1].equals("")) {
            System.out.println("Please enter a commit message.");
        }
        Object stageObj = Utils.deserialize(pathStage);
        Object hashObj = Utils.deserialize(pathHash);
        Staging stage = (Staging) stageObj;
        HashMap<String, Commit> hash = (HashMap<String, Commit>) hashObj;
        if (stage.getFiles().isEmpty()) {
            if (!stage.getremovedFiles().isEmpty()) {
                stage.setremovedFiles(new HashSet<String>());
                Utils.serialize(stage, pathStage);
            } else {
                System.out.println("No changes added to the commit.");
                return;
            }
        }

        Commit currentCom = new Commit(commands[1],
                hash.get(stage.getbranchStage()),
                stage.getbranchStage());
        currentCom.commitAll(stage);

        ArrayList<Commit> lst =
                (ArrayList<Commit>) Utils.deserialize(pathList);
        lst.add(currentCom);
        Utils.serialize(lst, pathList);

        hash.put(stage.getbranchStage(), currentCom);
        String strFil = ".gitlet/" + currentCom.getSha();
        File dir = new File(strFil);
        dir.mkdir();

        for (String fil : stage.getFiles()) {
            stage.getfamilyFiles().add(fil);
            String address = strFil + "/" + fil;
            Path oldPath = Paths.get(fil);
            Path newPath = Paths.get(address);
            File newFile = new File(address);
            newFile.getParentFile().mkdirs();
            Gitlet.copy(oldPath, newPath);
        }

        Utils.serialize(hash, pathHash);
        stage.clear();
        Utils.serialize(stage, pathStage);
    }
    /** A function to commit all of the files associated.
     * @param stage the stage that the files come from*/
    public void commitAll(Staging stage) {
        commitFiles(stage);
        commitremovedFiles(stage);
        commitfamilyFiles(stage);
    }

    /** Add the added files of the staging area to the commit.
     * @param stage the stage that the files come from */
    private void commitFiles(Staging stage) {
        if (!stage.getFiles().isEmpty()) {
            for (String file : stage.getFiles()) {
                _files.add(file);
            }
        }
    }

    /** Add the removed files of the staging area to the commit.
     *  @param stage the stage that the files come from */
    private void commitremovedFiles(Staging stage) {
        if (!stage.getremovedFiles().isEmpty()) {
            for (String file : stage.getremovedFiles()) {
                _removedFiles.add(file);
            }
        }
    }

    /** Add the incorporated files of the staging area to the commit.
     * @param stage the stage that the files come from */
    private void commitfamilyFiles(Staging stage) {
        if (!stage.getfamilyFiles().isEmpty()) {
            for (String file : stage.getfamilyFiles()) {
                _familyFiles.add(file);
            }
        }
    }

    /** print the commit contents in specified format. */
    public void print() {
        System.out.println("===");
        System.out.println("commit " + _sha);
        System.out.println("Date: " + _timeCom);
        System.out.println(_msg);
        System.out.println();
    }

    /** Get the parent of the commit.
     * @return Commit */
    public Commit getParent() {
        return _parentCom;
    }

    /** Set the parent of the commit.
     * @param parent the new set parent */
    public void setParent(Commit parent) {
        this._parentCom = parent;
    }

    /** Get the parent of the commit.
     * @return String */
    public String getBranch() {
        return _branchCom;
    }

    /** Set the branch of the commit.
     * @param branch the new set branch */
    public void setBranch(String branch) {
        this._branchCom = branch;
    }

    /** Get the id of sha-1 of the commit.
     * @return String */
    public String getSha() {
        return _sha;
    }

    /** Get the commit msg.
     * @return String */
    public String getMsg() {
        return _msg;
    }

    /** Get the added files of this commit.
     * @return HashSet<String> */
    public HashSet<String> getFiles() {
        return _files;
    }

    /** get the removed files of this commit.
     * @return HashSet<String> */
    public HashSet<String> getremovedFiles() {
        return _removedFiles;
    }

    /** get the inherited files of this commit.
     * @return HashSet<String> */
    public HashSet<String> getfamilyFiles() {
        return _familyFiles;
    }


    /** the message associated with the commit. */
    private String _msg;

    /** the parent commit for the given commit. */
    private Commit _parentCom;

    /** the branch commit for the given commit. */
    private String _branchCom;

    /** the branch time for the given commit. */
    private String _timeCom;

    /** the sha-1 for the given commit. */
    private String _sha;

    /** the files add for this commit. */
    private HashSet<String> _files = new HashSet<String>();

    /** the files removed for this commit. */
    private HashSet<String> _removedFiles = new HashSet<String>();

    /** the files inherited for this commit. */
    private HashSet<String> _familyFiles = new HashSet<String>();
}

