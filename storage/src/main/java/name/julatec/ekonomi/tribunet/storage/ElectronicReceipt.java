package name.julatec.ekonomi.tribunet.storage;

import name.julatec.ekonomi.accounting.Record;
import name.julatec.ekonomi.accounting.RecordComparable;

import java.util.Optional;

public interface ElectronicReceipt extends RecordComparable, Comparable<ElectronicReceipt> {

    Optional<Emisor> getEmisor();

    Optional<Receptor> getReceptor();

    Record.Key getKey();

    String getRecordKey();

    String getRecordSequential();

    String getClave();

    Documento getDocumento();

    @Override
    default int compareTo(ElectronicReceipt that) {
        return this.getKey().compareTo(that.getKey());
    }

}

