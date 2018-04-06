package gitlet;
import java.io.File;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  Collaborated w/: Jeff Burr, Sarah Mowris, Townsend Saunders.
 *  @author Matt Brennan
 *
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    @SuppressWarnings("unused")
    public static void main(String... args) {
        if (args == null || args.length == 0) {
            System.out.println("Please enter a command");
        } else if (args.length == 1 && args[0].equals("init")) {
            Gitlet gitlet = new Gitlet(args);
        } else if (!new File(".gitlet").exists()) {
            System.out.println("You should initialize a gitlet system first.");
        } else if (args[0].equals("add")) {
            AddRemove.add(args);
        } else if (args[0].equals("commit")) {
            Commit.commit(args);
        } else if (args[0].equals("rm")) {
            AddRemove.remove(args);
        } else if (args[0].equals("log")) {
            Logs.log();
        } else if (args[0].equals("global-log")) {
            Logs.globallog();
        } else if (args[0].equals("find")) {
            Gitlet.find(args);
        } else if (args[0].equals("status")) {
            Gitlet.status();
        } else if (args[0].equals("checkout")) {
            Gitlet.checkout(args);
        } else if (args[0].equals("branch")) {
            Branches.branch(args);
        } else if (args[0].equals("rm-branch")) {
            Branches.rmbranch(args);
        } else if (args[0].equals("reset")) {
            Gitlet.reset(args);
        } else if (args[0].equals("merge")) {
            Merge.merge(args);
        } else {
            System.out.println("No command with that name exists.");
        }
    }
}
