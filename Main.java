import java.util.Scanner;
import java.util.logging.LoggingPermission;

public class Main {

    public static void printHelp() {
        System.out.println("Usage: Logman LOGFILE | -h --help ");
        System.exit(1);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            printHelp();
        }
        if (args[0].equals("-h") || args[0].equals("--help")) {
            printHelp();
            System.exit(0);
        }


        CommandPromptHandler cmd = new CommandPromptHandler(args[0]);



    }
}