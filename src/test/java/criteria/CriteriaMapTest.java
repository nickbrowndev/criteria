package criteria;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;

class CriteriaMapTest {
    private CriteriaMap<Object> criteria;

    @BeforeEach
    void setUp() {
        criteria = CriteriaMap.create();
    }

    @Test
    void shouldCreateCriteriaFromMap() {
        final Map<String, String> map = new HashMap<>();
        map.put("1", "one");
        map.put("2", "two");
        map.put("3", "three");

        final CriteriaMap<String> criteria = CriteriaMap.from(map);
        assertTrue(criteria.has("1"));
        assertTrue(criteria.has("2"));
        assertTrue(criteria.has("3"));
    }

    @Test
    void shouldConfirmKnownCriteriaPresent() {
        final String key = "criteria1";

        criteria.put(key, new Object());
        assertTrue(criteria.has(key));
    }

    @Test
    void shouldReturnCorrectSizeAndEmpty() {
        assertTrue(criteria.isEmpty());
        assertEquals(0, criteria.size());

        criteria.put("key1", new Object());
        assertFalse(criteria.isEmpty());
        assertEquals(1, criteria.size());

        criteria.put ("key2", new Object());
        criteria.put("key3", new Object());
        assertFalse(criteria.isEmpty());
        assertEquals(3, criteria.size());
    }

    @Test
    void shouldReportWhenContains() {
        final String key1 = "key1";
        final String value1 = "value1";
        final String key2 = "key2";
        final String value2 = "value2";
        assertFalse(criteria.containsKey(key1));
        assertFalse(criteria.containsKey(key2));
        assertFalse(criteria.containsValue(value1));
        assertFalse(criteria.containsValue(value2));

        criteria.put(key1, value1);
        assertTrue(criteria.containsKey(key1));
        assertFalse(criteria.containsKey(key2));
        assertTrue(criteria.containsValue(value1));
        assertFalse(criteria.containsValue(value2));


        criteria.put(key2, value2);
        assertTrue(criteria.containsKey(key1));
        assertTrue(criteria.containsKey(key2));
        assertTrue(criteria.containsValue(value1));
        assertTrue(criteria.containsValue(value2));

        criteria.remove(key1);
        assertFalse(criteria.containsKey(key1));
        assertFalse(criteria.containsValue(value1));
        assertTrue(criteria.containsKey(key2));
        assertTrue(criteria.containsValue(value2));
    }

    @Test
    void shouldReturnNullWhenEmptyAndObjectWhenPopulated() {

        final String key1 = "key1";
        final String value1 = "value1";
        final String key2 = "key2";
        final String value2 = "value2";

        assertNull(criteria.get(key1));

        criteria.put(key1, value1);
        assertEquals(value1, criteria.get(key1));
    }

    @Test
    void shouldPreventCheckForNullKey() {
        assertThrows(NullPointerException.class, () -> criteria.has(null));
    }

    @Test
    void shouldPreventGetForNullKey() {
        assertThrows(NullPointerException.class, () -> criteria.get(null));
    }

    @Test
    void shouldExecuteConsumerIfValuePresent() {
        final List<Object> values = new ArrayList<>();
        final String key = "key1";
        final String value = "value1";

        criteria.put(key, value);
        criteria.ifHas(key, values::add);
        assertFalse(values.isEmpty());
        assertEquals(value, values.get(0));
    }

    @Test
    void shouldFailToExecuteConsumerIfOperationNull() {
        final String key = "key1";
        final String value = "value1";

        criteria.put(key, value);

        assertThrows(NullPointerException.class, () -> criteria.ifHas(key, null));
    }

    @Test
    void shouldNotExecuteConsumerIfValueNotPresent() {
        final List<Object> values = new ArrayList<>();
        final String key = "key1";
        final String value = "value1";

        criteria.put(key, value);
        criteria.ifHas("unknownkey", values::add);
        assertTrue(values.isEmpty());
    }

    @Test
    void shouldDenyUnknownCriteriaPresent() {
        final String key = "criteria1";

        criteria.put(key, new Object());
        assertFalse(criteria.has("criteria2"));
    }

    @Test
    void shouldPreventAddingNullKey() {
        assertThrows(NullPointerException.class, () -> criteria.put(null, new Object()));
    }

    @Test
    void shouldPreventRetrievingNullKey() {
        assertThrows(NullPointerException.class, () -> criteria.get(null));
    }

    @Test
    void shouldPreventRemovingNullKey() {
        assertThrows(NullPointerException.class, () -> criteria.remove(null));
    }

    @Test
    void shouldPreventAddingExistingKeyUsingPut() {
        final String key = "key1";
        final Object originalObject= new Object();

        criteria.put(key, originalObject);
        assertEquals(originalObject, criteria.put(key, new Object()));
    }

    @Test
    void shouldReturnObjectByKey() {
        final String key = "criteria1";
        final Object object = new Object();

        criteria.put(key, object);
        final Optional<Object> result = criteria.getOptional(key);
        assertTrue(result.isPresent());
        assertEquals(object, result.get());
    }

    @Test
    void shouldAllowNullValue() {
        final String key = "criteria1";
        criteria.put(key, null);
        final Optional<Object> result = criteria.getOptional(key);
        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnNullForToUnknownKey() {
        assertNull(criteria.get("unknownkey"));
    }

    @Test
    void shouldAllowAddingOfAllElementsOfMap() {
        Map<String, Object> otherMap = new HashMap<>();
        otherMap.put("key1", "value1");
        otherMap.put("key2", "value2");

        criteria.putAll(otherMap);
        assertEquals(2, criteria.size());
    }

    @Test
    void shouldPreventAddingAllOfNullMap() {
        assertThrows(NullPointerException.class, () -> criteria.putAll(null));
    }

    @Test
    void shouldPreventAddingMapWithNullKeys() {
        Map<String, Object> otherMap = new HashMap<>();
        otherMap.put(null, "value1");

        assertThrows(NullPointerException.class, () -> criteria.putAll(otherMap));
    }

    @Test
    void shouldAddObjectIfMeetsCriteria() {
        final String key = "criteria1";
        final Object object = new Object();

        criteria.putIf(key, object, CriteriaMap.NOT_NULL);
        assertTrue(criteria.has(key));
    }

    @Test
    void shouldAddObjectIfMeetsMultipleCriteria() {
        final String key = "criteria1";
        final String value = "value1";

        criteria.putIf(key, value, CriteriaMap.NOT_NULL, StringPredicate.NOT_EMPTY, v -> v.equalsIgnoreCase("value1"));
        assertTrue(criteria.has(key));
    }

    @Test
    void shouldPreventAddingIfConditionsNull() {
        assertThrows(NullPointerException.class, () -> criteria.putIf("key", "value", null));
    }

    @Test
    void shouldPreventAddingIfOneConditionIsNull() {
        assertThrows(NullPointerException.class, () -> criteria.putIf("key", "value", CriteriaMap.NOT_NULL, null, CriteriaMap.NOT_NULL));
    }

    @Test
    void shouldDenyObjectIfFailsCriteria() {
        final String key = "criteria1";
        final Object object = new Object();

        criteria.putIf(key, object, CriteriaMap.NULL);
        assertFalse(criteria.has(key));
    }

    @Test
    void shouldReturnObjectIfMeetsAllConditions() {
        final String key = "criteria1";
        final Object object = "value1";

        criteria.put(key, object);
        assertTrue(criteria.getIf(key, CriteriaMap.NOT_NULL).isPresent());
        assertFalse(criteria.getIf(key, CriteriaMap.NULL).isPresent());
    }

    @Test
    void shouldPerformTheActionIfHasValueAndMeetsCriteria() {
        final String key = "criteria1";
        final String value = "value1";
        final ArrayList<Object> values = new ArrayList<>();


        criteria.put(key, value);

        boolean result = criteria.ifHas(key, values::add, CriteriaMap.NULL);
        assertFalse(result);
        assertTrue(values.isEmpty());

        result = criteria.ifHas(key, values::add, CriteriaMap.NOT_NULL);
        assertTrue(result);
        assertFalse(values.isEmpty());
    }
// validate return types of methods are correct
}
