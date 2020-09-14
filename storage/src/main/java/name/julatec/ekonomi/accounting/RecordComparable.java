package name.julatec.ekonomi.accounting;

public interface RecordComparable {

    Record.Key getKey();

    default int compareTo(RecordComparable that) {
        return this.getKey().compareTo(that.getKey());
    }

}
