package name.julatec.ekonomi.extract.command;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class ContextTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ContextTest.class);

    @Test
    void setAttribute_then_getAttribute_returnsValue() {
        Context ctx = new Context(null, LOGGER);
        ctx.setAttribute("key", "value");
        Optional<String> result = ctx.getAttribute("key");
        assertTrue(result.isPresent());
        assertEquals("value", result.get());
    }

    @Test
    void getAttribute_missingKey_returnsEmpty() {
        Context ctx = new Context(null, LOGGER);
        Optional<String> result = ctx.getAttribute("notfound");
        assertFalse(result.isPresent());
    }

    @Test
    void getAttribute_nullValue_returnsEmptyOptional() {
        Context ctx = new Context(null, LOGGER);
        ctx.setAttribute("nullkey", null);
        Optional<String> result = ctx.getAttribute("nullkey");
        assertFalse(result.isPresent());
    }

    @Test
    void getAttribute_walksUpParentChain() {
        Context parent = new Context(null, LOGGER);
        parent.setAttribute("parentKey", "parentValue");
        Context child = parent.push();
        Optional<String> result = child.getAttribute("parentKey");
        assertTrue(result.isPresent());
        assertEquals("parentValue", result.get());
    }

    @Test
    void getAttribute_childOverridesParent() {
        Context parent = new Context(null, LOGGER);
        parent.setAttribute("key", "parentValue");
        Context child = parent.push();
        child.setAttribute("key", "childValue");
        Optional<String> fromChild = child.getAttribute("key");
        assertEquals("childValue", fromChild.get());
        Optional<String> fromParent = parent.getAttribute("key");
        assertEquals("parentValue", fromParent.get());
    }

    @Test
    void getAttribute_grandparentChain() {
        Context grandparent = new Context(null, LOGGER);
        grandparent.setAttribute("gpKey", "gpValue");
        Context parent = grandparent.push();
        Context child = parent.push();
        Optional<String> result = child.getAttribute("gpKey");
        assertTrue(result.isPresent());
        assertEquals("gpValue", result.get());
    }

    @Test
    void push_createsChildWithSameLogger() {
        Context parent = new Context(null, LOGGER);
        Context child = parent.push();
        assertSame(LOGGER, child.logger);
        assertSame(parent, child.parentContext);
    }

    @Test
    void context_nullParent_getAttribute_missingKey_returnsEmpty() {
        Context ctx = new Context(null, LOGGER);
        Optional<String> result = ctx.getAttribute("absent");
        assertFalse(result.isPresent());
    }

    @Test
    void setAttribute_integerValue_retrievable() {
        Context ctx = new Context(null, LOGGER);
        ctx.setAttribute("count", 42);
        Optional<Integer> result = ctx.getAttribute("count");
        assertTrue(result.isPresent());
        assertEquals(42, result.get());
    }
}
