package name.julatec.ekonomi.report.csv;

import org.apache.commons.collections4.comparators.ComparableComparator;
import org.apache.commons.collections4.comparators.ComparatorChain;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.collections4.comparators.NullComparator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LiteralComparator<T extends Comparable<T>> implements Comparator<T>, Serializable {
    private static final long serialVersionUID = 1L;
    private Comparator<T> c;

    /**
     * Constructor.
     *
     * @param predefinedOrder Objects that define the order of comparison
     */
    public LiteralComparator(T[] predefinedOrder) {
        List<T> predefinedList = predefinedOrder == null ? Collections.<T>emptyList() : Arrays.<T>asList(predefinedOrder);
        FixedOrderComparator<T> fixedComparator = new FixedOrderComparator<>(predefinedList);
        fixedComparator.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.AFTER);
        c = new ComparatorChain<>(Arrays.<Comparator<T>>asList(
                fixedComparator,
                new NullComparator<>(false),
                new ComparableComparator<>()));
    }

    @Override
    public int compare(T o1, T o2) {
        return c.compare(o1, o2);
    }
}

