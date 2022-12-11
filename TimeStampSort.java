import java.util.Comparator;

public class TimeStampSort implements Comparator<LogEntry> {

    // returns -1 if the first argument is of lesser priority than the second
    // returns 1 if the first argument is of higher priority than the second
    @Override
    public int compare(LogEntry log1, LogEntry log2) {
        //fixme: compare by timestamp. hint: use <, > rather than subtraction for the long
        // return -1 or 1
        // fixme: next compare categories
        // fixme: next compare by entryID
        if(log1.getTimestamp() != log2.getTimestamp()) { // timestamps not equal
            if(log1.getTimestamp() < log2.getTimestamp()) {
                return -1;
            }
            else {
                return 1;
            }
        }
        else if(!log1.getCategory().equals(log2.getCategory())) { // timestamps are equal
                return log1.getCategory().compareTo(log2.getCategory());
        }
        else { // timestamps and categories are equal; sort by entryID.
            if(log1.getId() < log2.getId()) {
                return -1;
            }
            else {
                return 1;
            }
        }



    }

}
