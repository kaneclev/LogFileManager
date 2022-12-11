public class LogEntry {

    private String line;
    private int entryId;
    private String category;
    private long timestamp;
    private String standardTimeStamp;
    private String message;

    /**
     * Within this class we must splice up the log entry into separate usable pieces that we can return.
     */
    public LogEntry(String inLine) {
        line = inLine;

        // timestamp shows up first in the line.

        if(line.length() > 14) {
            readCategory();
            setStandardTimeStamp(line.substring(0, 14));
        }

        timestamp = readTimeStamp(line);
    }
    public void setStandardTimeStamp(String timeStamp) {
        standardTimeStamp = timeStamp;
    }

    public static long readTimeStamp(String time) {

        // format : M M : D D : h h : m m  :  s  s
        // indices: 0 1 2 3 4 5 6 7 8 9 10 11 12 13

        return (time.charAt(13) - '0') * 1L +
                (time.charAt(12) - '0') * 10L +
                (time.charAt(10) - '0') * 100L +
                (time.charAt(9) - '0') * 1000L +
                (time.charAt(7) - '0') * 10000L +
                (time.charAt(6) - '0') * 100000L +
                (time.charAt(4) - '0') * 1000000L +
                (time.charAt(3) - '0') * 10000000L +
                (time.charAt(1) - '0') * 100000000L +
                (time.charAt(0) - '0') * 1000000000L;
    }


    // read category doubles as a message reader.
    private void readCategory() {
        int startIdx = 15; // for printing purposes I need to know when the category ends and message begins.
        StringBuilder c = new StringBuilder();
        for(int i = 15; line.charAt(i) != '|'; i++) {
            c.append(line.charAt(i));
            startIdx++;
        }
        message = line.substring(startIdx + 1);
        category = c.toString();
    }
    public void setEntryId(int id) {
        entryId = id;
    }
    public int getId() {
        return entryId;
    }
    public String getCategory() {
        return category;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public String getStandardTimestamp() { return standardTimeStamp; }
    public String getMessage() {
        return message;
    }
    public String getRaw() {
        return line;
    }
    public String getLogLine() {
        return this.entryId + "|" + line;
    }

}
