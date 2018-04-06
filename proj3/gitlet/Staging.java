package gitlet;


import java.io.Serializable;
import java.util.HashSet;

/** Representation of Set-up Zone which is needed for the Gitlet to add/remove.
 * @author Matt Brennan
 * */
public class Staging implements Serializable {

    /** The constructor for a stage.
     * @param branch The branch that this stage associates */
    public Staging(String branch) {
        setbranchStage(branch);
    }

    /** Clear the added files in the staging area. */
    public void clear() {
        setFiles(emptyHash());
    }

    /** Clears the whole stage. */
    public void clearStage() {
        setFiles(emptyHash());
        setremovedFiles(emptyHash());
        setfamilyFiles(emptyHash());
    }

    /** Takes files from a commit and adds them to the stages lists.
     * @param com the com where the files are associated */
    public void modifyStage(Commit com) {
        for (String fil : com.getfamilyFiles()) {
            this.getfamilyFiles().add(fil);
        }
        for (String fil : com.getFiles()) {
            this.getfamilyFiles().add(fil);
        }
    }

    /** Provides an empty Hashset for any file system.
     * @return HashSet<String> */
    public HashSet<String> emptyHash() {
        return new HashSet<String>();
    }

    /** Get the branch of this staging area.
     * @return String */
    public String getbranchStage() {
        return _branchStage;
    }

    /** Set the branch of this staging area.
     * @param branch the branch to be set */
    public void setbranchStage(String branch) {
        this._branchStage = branch;
    }

    /** Get the removed files of this staging area.
     * @return HashSet<String> */
    public HashSet<String> getremovedFiles() {
        return _removedFiles;
    }

    /** Set the removed files of this staging area.
     * @param removedFiles the new removedfiles to be used */
    public void setremovedFiles(HashSet<String> removedFiles) {
        this._removedFiles = removedFiles;
    }

    /** Get the files that have been added to staging area.
     * @return HashSet<String> */
    public HashSet<String> getFiles() {
        return _files;
    }

    /** Set the files that have been added in this staging area.
     * @param files the new files to be used */
    public void setFiles(HashSet<String> files) {
        this._files = files;
    }

    /** Get the files that have been incorporated in this staging area.
     * @return HashSet<String> */
    public HashSet<String> getfamilyFiles() {
        return _familyFiles;
    }

    /** Set the files that have been incorporated in this staging area.
     * @param familyFiles the files to be used as the new family files */
    public void setfamilyFiles(HashSet<String> familyFiles) {
        this._familyFiles = familyFiles;
    }


    /** the branch associated with the staging area. */
    private String _branchStage;

    /** the removed files of this staging area. */
    private HashSet<String> _removedFiles = new HashSet<String>();

    /** the added files of this staging area. */
    private HashSet<String> _files = new HashSet<String>();

    /** the inherited files of this staging area. */
    private HashSet<String> _familyFiles = new HashSet<String>();


}
