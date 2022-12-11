import java.util.Comparator;

public class UpperBound implements Comparator<LogEntry> {

    @Override
    public int compare(LogEntry key, LogEntry timestamp) {
        long upBound = key.getTimestamp();
        long ts = timestamp.getTimestamp();
        if(upBound <= ts) {
            return -1;
        }
        else {
            return 1;
        }
    }
}
