import java.io.FileNotFoundException;
import java.util.*;
import java.io.File;
public class Logfile {
    private ArrayList<LogEntry> masterList; // arraylist of log entry objects from our scanner
    private ArrayList<Integer> indexMap; // keep track of the indices of log entries.
    private ArrayList<LogEntry> excerptList;
    // arrayList contains the indices of the log entries w a specified category, String is the key word we look for
    private HashMap<String, ArrayList<Integer>> categoryMap;
    // for keyword searching
    private HashMap<String, ArrayList<Integer>> keywordMap;
    private Set<Integer> idxSet;
    // for last search results
    private ArrayList<Integer> hashSearchResults;
    private TimeStampSort sortByTS;
    private LastSearch searchKind;
    private int startIdx, endIdx;
    private LogEntry startLog;
    private LogEntry endLog;
    Logfile(String fileName) { // constructor
        // initialize ALs, enum, and indices for binary search.
        masterList = new ArrayList<>();
        sortByTS = new TimeStampSort();
        searchKind = LastSearch.None;
        startIdx = endIdx = -1;

        // try to grab the info from the file given there is a file to read.
        try {
            int entryId = 0;
            Scanner in = new Scanner(new File(fileName));
            while (in.hasNextLine()) {
                String line = in.nextLine();

                if (line.charAt(14) != '|') { // check for bad timestamps.
                    continue;
                }
                masterList.add(new LogEntry(line)); // valid log entry, so add it to our masterList
                masterList.get(entryId).setEntryId(entryId); // set the entry id for the LogEntry.
                entryId++; // increment the entry id.
            }


        } catch (FileNotFoundException e) { // no file found.
            System.err.println(fileName + " not found.");
            System.exit(1);
        }
        // the size of the masterList is the number of valid logentries we have read from the file.
        System.out.println(masterList.size() + " entries read");

        // call postProcess after we have the data we need.
        postProcess();
    }
    /**
     * This method does all the processing of the log entries
     * in order to prepare us for the user commands.
     **/
    private void postProcess() {
        // sorts the log file by timestamp, because it is not ordered when we read it in.
        Collections.sort(masterList, sortByTS);
        // regenerate a mapping of the original index locations to support outputs.
        indexMap = new ArrayList<>(masterList.size());
        excerptList = new ArrayList<>();

        for (int i = 0; i < masterList.size(); i++) {
            // insert a dummy value
            indexMap.add(0);
        }

        // set the mapping for the original locations in the master log file.
        for (int i = 0; i < masterList.size(); i++) {
            // after we sort the master list we need to be able to recall the OG entry ids after sort.
            LogEntry entry = masterList.get(i);
            indexMap.set(entry.getId(), i);
        }
        
        // to prepare in the event each search is called:
        prepCategorySearch();
        prepKeywordSearch();
        // for search results from searching
        hashSearchResults = new ArrayList<>();
    }

    private void prepKeywordSearch() {
        // keep a hashmap where k = the requested keyword, and v = the log entries that contain that keyword
        keywordMap = new HashMap<>();
        // iterate through the masterlist and
        for(int i = 0; i < masterList.size(); i++) {
            LogEntry currEntry = masterList.get(i);
            // split up the entry into an array of keywords
            String[] keywords = currEntry.getRaw().substring(15).toLowerCase().split("[^a-zA-Z0-9]+");

            for(int j = 0; j < keywords.length; j++) {
                String kwd = keywords[j];
                if (kwd.isBlank()) {
                    // blank; nothing to do here.
                    continue;
                }
                // else, add the index to the entry for the keyword
                if(!keywordMap.containsKey(kwd)) { // if we haven't already seen this keyword, create a key, val.
                    keywordMap.put(kwd, new ArrayList<>());
                    keywordMap.get(kwd).add(i);// add our current log INDEX to the arraylist of logs that match the key.
                }
                // otherwise, we have already seen this keyword. add idx to the arraylist at this key.
                if (keywordMap.get(kwd).size() > 0 //if there is more than one element & the prev element isnt a repeat
                        && keywordMap.get(kwd).get(keywordMap.get(kwd).size() - 1) != i) {
                        keywordMap.get(kwd).add(i);
                }
            }
        }

    }

    public int keywordSearch(ArrayList<String> keywords) {
        hashSearchResults.clear(); // we don't need the previous search results.
        searchKind = LastSearch.Keyword; // set enum to keyword
        boolean isNull = false; // boolean in order to catch keyword requests that dont exist in the file


        // if there is more than one keyword requested and we need to iterate through the keywords...
        if(keywords.size() > 1 && keywordMap.get(keywords.get(0)) != null) {
            // intialize the hashset in order to find the intersection of all keyword logs.
            idxSet = new HashSet<>();
            // add all the logs which contain the first requested keyword.
            idxSet.addAll(keywordMap.get(keywords.get(0)));


            for(int i = 1; i < keywords.size(); i++) {
                // if it hits null then there are NO logs that contain all of the keywords requested.
                if(keywordMap.get(keywords.get(i)) != null) {
                    // retain the INTERSECTION of our first keyword and logs that contain the NEXT requested keyword.
                    idxSet.retainAll(keywordMap.get(keywords.get(i)));
                }
                else { // then the keyword given does not exist in any file.
                    System.err.println("The keyword '" + keywords.get(i) + "' " + "does not exist in the file. ");
                    isNull = true; // we found a bad keyword request; no logs exist with the all requested keywords.
                    break;
                }
            }
            if(!isNull) { // otherwise there exists some log(s) that contain all keywords
                hashSearchResults.addAll(idxSet);
            }


            // now we need to sort the results
            Collections.sort(hashSearchResults);
        } // if there is only one keyword requested, add the logs that fit that one keyword
        else if (keywordMap.get(keywords.get(0)) != null){
            hashSearchResults.addAll(keywordMap.get(keywords.get(0)));
        }
        else {
            // for my own use; the first given keyword does not exist in the file.
            System.err.println("The keyword '" + keywords.get(0) + "' " + "does not exist in the file. ");
        }
        // print out how many logs we found.
        System.out.println("Keyword search: " + hashSearchResults.size() + " entries found");

        // return the size of the hashSearchResults as per the spec.
        return hashSearchResults.size();
    }





    /**
    - Prepare the category search by creating a hashmap of all the categories using
     lowercase categories for searching purposes; input as lowercase for searching
     */
    private void prepCategorySearch() {
        // hash map to contain the logs which fit the requested category
        categoryMap = new HashMap<>();
        for(int i = 0; i < masterList.size(); i++) {
            LogEntry curr = masterList.get(i);
            // check if our curr LogEntry category is already in the map.
            if(!categoryMap.containsKey(curr.getCategory().toLowerCase())) { // doesnt currently contain the category.
                // then we want to make the value a NEW arraylist for all the possible entries with the same category.
                categoryMap.put(curr.getCategory().toLowerCase(), new ArrayList<>());
            }
            // otherwise, the category is already in the hashmap. add it to the arraylist of the category type
            categoryMap.get(curr.getCategory().toLowerCase()).add(i);
        }

    }
    public int categorySearch(String category) {
        // select the type of search
        searchKind = LastSearch.Category;

        // clear the previous search results.
        hashSearchResults.clear();

        // grab all instances of the requested category from categoryMap and add to hashSearchResults
        if (categoryMap.get(category.toLowerCase()) != null) {
            hashSearchResults.addAll(categoryMap.get(category.toLowerCase()));
        }
        else { // personal message
            System.err.println("There is no such category in the master list.");
        }
        // print how many entries we fould
        System.out.println("Category search: " + hashSearchResults.size() + " entries found");
        // return the search results size as per spec.
        return hashSearchResults.size();
    }

    /**
     *
     * Uses binary search and two dummy log entries assigned to the requested bounds in order to
     * search through the master list and return the indices of the log entries with the desired start/end bounds.
     * @param start given starting ts bound
     * @param end given ending ts bound
     * @return num of entries within the timestamp range [start, end]
     */

    public int timeStampSearch(String start, String end) {
        // set the enum to timestamp
        searchKind = LastSearch.Timestamp;
        // clear whatever other search results were before; no longer interested.
        hashSearchResults.clear();
        this.startLog = new LogEntry(start); //fixme: these might be memory intensive; if so, maybe instantiate once
        this.endLog = new LogEntry(end);    //  and make a method to modify the timestamp within this method?
        if (startLog.getTimestamp() > endLog.getTimestamp()) {
            System.err.println("The starting timestamp cannot be later than the ending timestamp.");
        }
        //todo: check for if this is an 'm' search
        else if(startLog.getTimestamp() == endLog.getTimestamp()) {
            startIdx = (Collections.binarySearch(masterList, startLog, new LowerBound()) + 1) * -1;
            for(int i = startIdx; i < masterList.size(); i++) {
                if(masterList.get(i).getTimestamp() == masterList.get(startIdx).getTimestamp()) {
                    hashSearchResults.add(i);
                }
                else {
                    break;
                }
            }
            System.out.println("Timestamp search: " + hashSearchResults.size() + " entries found");
        }
        else {
            startIdx = (Collections.binarySearch(masterList, startLog, new LowerBound()) + 1) * -1;
            endIdx = (Collections.binarySearch(masterList, endLog, new UpperBound()) + 2) * -1;

            // add indices to the hashSearchResults arraylist
            for(int i = startIdx; i <= endIdx; i++) {
                hashSearchResults.add(i);
            }

            System.out.println("Timestamps search: " + hashSearchResults.size() + " entries found");
        }


        //fixme: return the num timestamps found within the range [start, end]
        return hashSearchResults.size();
    }

    public void printSearchResults() {
        for(int i = 0; i < hashSearchResults.size(); i++) {
            int masterListIdx = hashSearchResults.get(i);
            LogEntry currEntry = masterList.get(masterListIdx);
            System.out.println(currEntry.getId() + "|"
                    + currEntry.getStandardTimestamp() + "|"
                    + currEntry.getCategory() + "|" + currEntry.getMessage());
        }
    }

    public void appendSearchResults() {
        // if there was a search performed previously, iterate through and add the search results to the exerpt list
        if (searchKind != LastSearch.None) {
            for(int i = 0; i < hashSearchResults.size(); i++) {
                int masterListIdx = hashSearchResults.get(i);
                LogEntry appendLog = masterList.get(masterListIdx);
                excerptList.add(appendLog);
            }
            System.out.println(hashSearchResults.size() + " log entries appended");

        }
    }

    public void appendExcerpt(int entryIdx) {
        // within the indexMap, the indices of indexMap are associated with the entryId of the masterList.
        // therefore, we need to grab the value in the index of masterList by looking at the index of indexMap.
        // basically, if entryIdx was 1, we go indexMap[1] --> masterList[indexMap[1]] <---

        // make sure the request isnt out of bounds:
        if(entryIdx > masterList.size() - 1 || entryIdx < 0) {
            System.err.println("Bad index.");
        }
        else {
            // append the requested index to the excerpt list.
            int masterListIdx = indexMap.get(entryIdx);

            LogEntry appendLog = masterList.get(masterListIdx);
            excerptList.add(appendLog);
            System.out.println("log entry " + entryIdx + " appended");
        }


    }

    public void clearList() {
        if(excerptList.isEmpty()) { // check if the list is already empty
            System.out.println("excerpt list cleared");
            System.out.println("(previously empty)");
        }
        else {
            // otherwise, print the requested info and clear the list.
            System.out.println("excerpt list cleared");
            System.out.println("previous contents:");
            System.out.println("0" + "|"
                    + excerptList.get(0).getLogLine());
            System.out.println("...");
            System.out.println(excerptList.size() - 1 + "|"
                    + excerptList.get(excerptList.size() - 1).getLogLine());
            excerptList.clear();

        }
    }

    // iterate through the excerpt list and print out the position and message of the entry.
    public void printExcerptList() {
        for(int i = 0; i < excerptList.size(); i++) {
            LogEntry currEntry = excerptList.get(i);
            System.out.println(i + "|" + currEntry.getLogLine());
        }

    }


    public void deleteEntry(int entryIndex) {
        // check for proper indices
        if(entryIndex > excerptList.size() - 1 || entryIndex < 0) {
            System.err.println("That is not a valid excerpt list index. ");
        }
        else { // remove the entry at the requested index
            excerptList.remove(entryIndex);
            System.out.println("Deleted excerpt list entry " + entryIndex);
        }
    }

    public void entryToBeginning(int entryIndex) {
        // index valid check
        if(entryIndex > excerptList.size() - 1|| entryIndex < 0) {
            System.err.println("That is not a valid excerpt list index. ");
        } else {
            // add it at position 0
            excerptList.add(0, excerptList.get(entryIndex)); // move the requested entry to the beginning
            // remove the duplicate entry that we moved
            excerptList.remove(entryIndex + 1);

            System.out.println("Moved excerpt list entry " + entryIndex);
        }

    }
    public void entryToEnd(int entryIndex) {
        // check for valid index
        if(entryIndex > excerptList.size() - 1|| entryIndex < 0) {
            System.err.println("That is not a valid excerpt list index. ");
        }
        else {
            // add the entry to the end of the list
            LogEntry entryToEnd = excerptList.get(entryIndex);
            excerptList.add(entryToEnd);
            // remove the previous duplicate entry at the original location
            excerptList.remove(entryIndex);

            System.out.println("Moved excerpt list entry " + entryIndex);

        }

    }



    public void sortExcListByTS() {
        // if there are items in the excerpt list
        if(excerptList.size() > 0) {
            // print old ordering
            System.out.println("excerpt list sorted");
            System.out.println("previous ordering:");
            System.out.println("0" + "|"
                    + excerptList.get(0).getLogLine());
            System.out.println("...");
            System.out.println(excerptList.size() - 1 + "|"
                    + excerptList.get(excerptList.size() - 1).getLogLine());

            // sort by our timestamp comparator
            Collections.sort(excerptList, sortByTS);


            // print new ordering
            System.out.println("new ordering:");
            System.out.println("0" + "|"
                    + excerptList.get(0).getLogLine());
            System.out.println("...");
            System.out.println(excerptList.size() - 1 + "|"
                    + excerptList.get(excerptList.size() - 1).getLogLine());

        }
        else { // the excerpt list was empty, print info according to spec
            System.out.println("excerpt list sorted");
            System.out.println("(previously empty)");
        }


    }

    // print the masterlist out
    public int size() {
        return masterList.size();
    }


    private enum LastSearch {
        None,
        Timestamp,
        Category,
        Keyword
    }


}
