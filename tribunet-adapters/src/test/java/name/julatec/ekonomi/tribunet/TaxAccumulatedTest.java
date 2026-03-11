package name.julatec.ekonomi.tribunet;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TaxAccumulatedTest {

    @Test
    void empty_isZeroZeroZero() {
        DetailedDocument.TaxAccumulated empty = DetailedDocument.TaxAccumulated.empty;
        assertEquals(0, BigDecimal.ZERO.compareTo(empty.subTotal));
        assertEquals(0, BigDecimal.ZERO.compareTo(empty.total));
        assertEquals(0, BigDecimal.ZERO.compareTo(empty.taxed));
    }

    @Test
    void constructor_setsFields() {
        DetailedDocument.TaxAccumulated ta = new DetailedDocument.TaxAccumulated(
                new BigDecimal("100.00"),
                new BigDecimal("113.00"),
                new BigDecimal("13.00"));
        assertEquals(new BigDecimal("100.00"), ta.subTotal);
        assertEquals(new BigDecimal("113.00"), ta.total);
        assertEquals(new BigDecimal("13.00"), ta.taxed);
    }

    @Test
    void constructor_nullSubTotal_usesZero() {
        DetailedDocument.TaxAccumulated ta = new DetailedDocument.TaxAccumulated(null, BigDecimal.TEN, BigDecimal.ONE);
        assertEquals(0, BigDecimal.ZERO.compareTo(ta.subTotal));
    }

    @Test
    void constructor_nullTotal_usesZero() {
        DetailedDocument.TaxAccumulated ta = new DetailedDocument.TaxAccumulated(BigDecimal.TEN, null, BigDecimal.ONE);
        assertEquals(0, BigDecimal.ZERO.compareTo(ta.total));
    }

    @Test
    void constructor_nullTaxed_usesZero() {
        DetailedDocument.TaxAccumulated ta = new DetailedDocument.TaxAccumulated(BigDecimal.TEN, BigDecimal.ONE, null);
        assertEquals(0, BigDecimal.ZERO.compareTo(ta.taxed));
    }

    @Test
    void add_combinesAllFields() {
        DetailedDocument.TaxAccumulated a = new DetailedDocument.TaxAccumulated(
                new BigDecimal("100"), new BigDecimal("113"), new BigDecimal("13"));
        DetailedDocument.TaxAccumulated b = new DetailedDocument.TaxAccumulated(
                new BigDecimal("200"), new BigDecimal("226"), new BigDecimal("26"));
        DetailedDocument.TaxAccumulated result = a.add(b);
        assertEquals(0, new BigDecimal("300").compareTo(result.subTotal));
        assertEquals(0, new BigDecimal("339").compareTo(result.total));
        assertEquals(0, new BigDecimal("39").compareTo(result.taxed));
    }

    @Test
    void add_withEmpty_returnsSameValues() {
        DetailedDocument.TaxAccumulated ta = new DetailedDocument.TaxAccumulated(
                new BigDecimal("100"), new BigDecimal("113"), new BigDecimal("13"));
        DetailedDocument.TaxAccumulated result = ta.add(DetailedDocument.TaxAccumulated.empty);
        assertEquals(0, new BigDecimal("100").compareTo(result.subTotal));
        assertEquals(0, new BigDecimal("113").compareTo(result.total));
        assertEquals(0, new BigDecimal("13").compareTo(result.taxed));
    }

    @Test
    void equals_sameValues_isTrue() {
        DetailedDocument.TaxAccumulated a = new DetailedDocument.TaxAccumulated(
                new BigDecimal("100"), new BigDecimal("113"), new BigDecimal("13"));
        DetailedDocument.TaxAccumulated b = new DetailedDocument.TaxAccumulated(
                new BigDecimal("100"), new BigDecimal("113"), new BigDecimal("13"));
        assertEquals(a, b);
    }

    @Test
    void equals_differentValues_isFalse() {
        DetailedDocument.TaxAccumulated a = new DetailedDocument.TaxAccumulated(
                new BigDecimal("100"), new BigDecimal("113"), new BigDecimal("13"));
        DetailedDocument.TaxAccumulated b = new DetailedDocument.TaxAccumulated(
                new BigDecimal("200"), new BigDecimal("226"), new BigDecimal("26"));
        assertNotEquals(a, b);
    }

    @Test
    void equals_sameInstance_isTrue() {
        DetailedDocument.TaxAccumulated a = new DetailedDocument.TaxAccumulated(
                new BigDecimal("100"), new BigDecimal("113"), new BigDecimal("13"));
        assertEquals(a, a);
    }

    @Test
    void equals_null_isFalse() {
        DetailedDocument.TaxAccumulated a = new DetailedDocument.TaxAccumulated(
                new BigDecimal("100"), new BigDecimal("113"), new BigDecimal("13"));
        assertNotEquals(null, a);
    }

    @Test
    void equals_otherClass_isFalse() {
        DetailedDocument.TaxAccumulated a = new DetailedDocument.TaxAccumulated(
                new BigDecimal("100"), new BigDecimal("113"), new BigDecimal("13"));
        assertNotEquals("string", a);
    }

    @Test
    void hashCode_sameValues_isEqual() {
        DetailedDocument.TaxAccumulated a = new DetailedDocument.TaxAccumulated(
                new BigDecimal("100"), new BigDecimal("113"), new BigDecimal("13"));
        DetailedDocument.TaxAccumulated b = new DetailedDocument.TaxAccumulated(
                new BigDecimal("100"), new BigDecimal("113"), new BigDecimal("13"));
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_containsSubTotal() {
        DetailedDocument.TaxAccumulated ta = new DetailedDocument.TaxAccumulated(
                new BigDecimal("100"), new BigDecimal("113"), new BigDecimal("13"));
        assertTrue(ta.toString().contains("100"));
    }

    @Test
    void toString_containsTotal() {
        DetailedDocument.TaxAccumulated ta = new DetailedDocument.TaxAccumulated(
                new BigDecimal("100"), new BigDecimal("113"), new BigDecimal("13"));
        assertTrue(ta.toString().contains("113"));
    }

    @Test
    void toString_containsTaxed() {
        DetailedDocument.TaxAccumulated ta = new DetailedDocument.TaxAccumulated(
                new BigDecimal("100"), new BigDecimal("113"), new BigDecimal("13"));
        assertTrue(ta.toString().contains("13"));
    }
}
