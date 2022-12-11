import java.util.ArrayList;
import java.util.Scanner;

public class CommandPromptHandler {

    // todo: based on given args through the console in our "command prompt", call methods in other classes.
    public CommandPromptHandler(String fileName) {
        // create a new logfile object from the constructor
        Logfile lf = new Logfile(fileName);
        Scanner in = new Scanner(System.in);

        System.out.print("% ");

        while (in.hasNextLine()) {

            String line = in.nextLine();
            String argument;


            // is there a command to read?
            if(line.length() > 0) {
                char command = line.charAt(0);
                if (command != '#' && command != 'q') {
                    switch (command) {
                        case 'a': // append a log entry from the masterList to the excerptList
                            argument = line.substring(2);
                            int requestedIdx = Integer.parseInt(argument);
                            lf.appendExcerpt(requestedIdx);
                            break;
                        case 'p': // print out the excerpt list
                            lf.printExcerptList();
                            break;
                        case 't': // timestamp search

                            try {
                                String ts1 = line.substring(2, 16); // first of the two timestamps
                                String ts2 = line.substring(17); // second timestamp
                                lf.timeStampSearch(ts1, ts2);

                            } catch(IndexOutOfBoundsException e){
                                System.err.println("The timestamps given do not fit the correct format.");
                            }

                            break;
                        case 'd': // delete an entry from the excerpt list
                            argument = line.substring(2);
                            requestedIdx = Integer.parseInt(argument);
                            lf.deleteEntry(requestedIdx);
                            break;

                        case 'b': // move an excerpt list entry to the beginning of the list
                            argument = line.substring(2);
                            requestedIdx = Integer.parseInt(argument);
                            lf.entryToBeginning(requestedIdx);
                            break;
                        case 'e': // move an excerpt list entry to the end of the list
                            argument = line.substring(2);
                            requestedIdx = Integer.parseInt(argument);
                            lf.entryToEnd(requestedIdx);
                            break;
                        case 's': // sort excerpt list by timestamp
                            lf.sortExcListByTS();
                            break;
                        case 'l': // clear excerpt list
                            lf.clearList();
                            break;
                        case 'c': // category search
                            argument = line.substring(2);
                            lf.categorySearch(argument);
                            break;
                        case 'g': // print out the results of the last search
                            lf.printSearchResults();
                            break;
                        case 'r': // append the last search's results to the excerpt list
                            lf.appendSearchResults();
                            break;
                        case 'm': // match timestamps
                            String ts = line.substring(2, 16);
                            lf.timeStampSearch(ts, ts);
                            break;
                        case 'k':
                            String raw_keywords = line.substring(2);
                            String[] raw_split = raw_keywords.split("[^a-zA-Z0-9]+");
                            ArrayList<String> kwds = new ArrayList<>();
                            for(String s : raw_split) {
                                if(!s.isBlank()) {
                                    String kwd = s.toLowerCase();
                                    kwds.add(kwd);
                                }
                            }
                            lf.keywordSearch(kwds);
                            break;
                        case 'z': // personal tests
                            argument = line.substring(2);
                            if(argument.equals("keyword")) {
                                lf.keyWordTest();
                            }

                            break;
                        default:
                            System.err.println("Unexpected command: " + command);
                    } // end of switch


                }

                else {
                    if (command == 'q') {
                        System.exit(0);
                    }
                }
            } // end of valid command check
            else {
                System.err.println("There must be a command given to continue.");
            }


            // fixme: we need to only check for arguments in the case that it has arguments.

            // push another % before the end of the while loop.
            System.out.print("% ");

        } // end of while loop



    }
}
