import java.util.Comparator;

public class LowerBound implements Comparator<LogEntry> {
    /*
    * comparator for timestamps
    * if timestamp1 is less than timestamp 2, we return -1
    * in every other case, equal or greater than, we return 1.
    */

    // log1 is our key
    // log2 is the thing we are comparing against.
    @Override
    public int compare(LogEntry key, LogEntry timestamp) {
        long keyBound = key.getTimestamp();
        long ts = timestamp.getTimestamp();
        if(keyBound < ts) {
            return -1;
        }
        else { // returns greater than for both equal and greater than cases.
            return 1;
        }
    }
}
