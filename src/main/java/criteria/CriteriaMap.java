package criteria;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CriteriaMap<T> implements Map<String, T>{
    public static final Predicate<Object> NULL = Objects::isNull;
    public static final Predicate<Object> NOT_NULL = Objects::nonNull;

    private final Map<String, T> criteria;

    private CriteriaMap() {
        criteria = new LinkedHashMap<>();;
    }

    private CriteriaMap(int initialCapacity) {
        criteria = new LinkedHashMap<>(initialCapacity);
    }

    private CriteriaMap(Map<String, T> map) {
        criteria = new LinkedHashMap<>(map);
    }

    public static <T> CriteriaMap<T> create() {
        return new CriteriaMap<T>();
    }

    /**
     * Creates a new CriteriaMap populated with the values specified in the provided map.
     *
     * @param map a set of keys and values to populate the CriteriaMap with
     * @param <T> the type of object stored in the CriteriaMap
     * @return a new CriteriaMap populated with the values in the 'map' parameter
     * @throws NullPointerException if the 'map' parameter is null, or contains null keys
     */
    public static <T> CriteriaMap<T> from(final Map<String, T> map) {
        Objects.requireNonNull(map, "Parameter 'map' cannot be null");
        if (map.containsKey(null)) {
            throw new NullPointerException("CriteriaMap cannot contain null keys");
        }
        return new CriteriaMap<>(map);
    }

    @Override
    public int size() {
        return criteria.size();
    }

    @Override
    public boolean isEmpty() {
        return criteria.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        Objects.requireNonNull(key, "Parameter 'key' cannot be null");
        return criteria.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return criteria.containsValue(value);
    }

    @Override
    public T get(Object key) {
        Objects.requireNonNull(key, "Parameter 'key' cannot be null");
        return criteria.get(key);
    }

    /**
     * Puts the object into this CriteriaMap.
     *
     * @param key - the key of the object to be stored
     * @param object - the object to be stored
     * @throws NullPointerException when the 'key' parameters are null
     */
    @Override
    public T put(final String key, final T object) {
        Objects.requireNonNull(key, "Parameter 'key' cannot be null");
        return criteria.put(key, object);
    }

    @Override
    public T remove(Object key) {
        Objects.requireNonNull(key, "Parameter 'key' cannnot be null");
        return criteria.remove(key);
    }

    @Override
    public void clear() {
        criteria.clear();
    }

    @Override
    public Set<String> keySet() {
        return criteria.keySet();
    }

    @Override
    public Collection<T> values() {
        return criteria.values();
    }

    @Override
    public Set<Entry<String, T>> entrySet() {
        return criteria.entrySet();
    }

    /**
     * Adds all of the elements contained within the map to this CriteriaMap.
     *
     * @param map the map containing additional items to be added to this CriteriaMap
     * @throws NullPointerException if the 'map' parameter contains a null key
     */
    @Override
    public void putAll(final Map<? extends String, ? extends T> map) {
        Objects.requireNonNull(map, "Parameter 'map' cannot be null");

        if (map.containsKey(null)) {
            throw new NullPointerException("CriteriaMap cannot contain null keys");
        }

        criteria.putAll(map);
    }

    // TODO test
    public <E extends T> void putAllIf(final Map<? extends String, ? extends T> map,  Predicate<? super T>... conditions) {
        Objects.requireNonNull(map, "Parameter 'map' cannot be null");
        Objects.requireNonNull(conditions, "Parameter 'conditions' cannot be null");

        map.entrySet().stream().filter(((entry) -> testAll(entry.getValue(), conditions))).forEach(entry -> criteria.put(entry.getKey(), entry.getValue()));
    }

    /**
     * Puts the object into this CriteriaMap if it matches all of the conditions.
     *
     * Note: Predicates can be combined if required by using {@link Predicate and()} and {@link Predicate or()}.
     *
     * @param key - the key of the object to be stored
     * @param object - the object to be stored
     * @param conditions - the {@link Predicate}s that the object must meet to be stored in this CriteriaMap
     * @param <E> the generic type of the object to be stored
     * @throws NullPointerException when the 'key' or 'condition' parameters are null
     */
    @SafeVarargs
    public final <E extends T>  boolean putIf(String key, E object, Predicate<? super E>... conditions) {
        Objects.requireNonNull(conditions, "Parameter 'conditions' cannot be null");
        boolean result = false;

        if (testAll(object, conditions)) {
            put(key, object);
            result = true;
        }

        return result;
    }

    /**
     * Returns whether this criteria has a value matching the specified key.
     *
     * @param key the key to check
     * @return whether this CriteriaMap contains the provided key
     * @throws NullPointerException if the 'key' parameter is null
     */
    public boolean has(String key) {
        Objects.requireNonNull(key, "Parameter 'key' cannot be null");
        return criteria.containsKey(key);
    }

    /**
     * Performs the provided {@link Consumer} if the key is present. The value of associated
     * to the key will be passed to the {@link Consumer} to execute.
     *
     * Note: the value passed to the consumer may be null.
     *
     * @param key the key of the value to be processed
     * @param operation the operation to be performed on the value
     * @throws NullPointerException if the 'key' or 'operation' parameters are null
     */
    public boolean ifHas(String key, Consumer<? super T> operation) {
        Objects.requireNonNull(operation, "Parameter 'operation' cannot be null");
        boolean result = false;
        if (has(key)) {
            operation.accept(criteria.get(key));
            result = true;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public final <E extends T> boolean ifHas(String key, Consumer<? super E> operation, Predicate<? super E> conditions) {
        Objects.requireNonNull(key, "Parameter 'key' cannot be null");
        Objects.requireNonNull(operation, "Parameter 'operation' cannot be null");
        Objects.requireNonNull(conditions, "Parameter 'conditions' cannnot be null");

        boolean result = false;
        if (has(key)) {
            E value = (E) criteria.get(key);

            if (testAll(value, conditions)) {
                operation.accept(value);
                result = true;
            }
        }

        return result;
    }

    /**
     * Retrieves a value from this CriteriaMap. This will return an {@link Optional} containing the value if it
     * is non-null, or an empty {@link Optional} otherwise.
     *
     * @param key the key of the object
     * @return an Optional containing the value, or an empty Optional if the value was null.
     */
    public Optional<T> getOptional(String key) {
        return Optional.ofNullable(criteria.get(key));
    }

    /**
     * Returns an Optional if it meets the provided conditions.
     *
     * Note; this method performs an implicit cast of the value to the type of the Predicates provided.
     * If the value associated to the key cannot be cast to this type, a ClassCastException will be thrown.
     *
     * @param key the key of the object to retrieve
     * @param conditions one or more Predicates that the parameter should meet
     * @param <E> the generic type of the object within the map
     * @return an Optional containing the value. The Optional will be empty if the value was null, or the conditions didn't pass.
     * @throws ClassCastException if the value associate with the Key cannot be cast to the type of the conditions.
     */
    @SuppressWarnings("unchecked")
    public <E extends T> Optional<E> getIf(String key, Predicate<? super E>... conditions) {
        Optional<E> result = Optional.empty();
        if (has(key)) {
            E object = (E) criteria.get(key);
            if (testAll(object, conditions)) {
                result = Optional.ofNullable(object);
            }
        }
        return result;
    }

    @SafeVarargs
    private final <E extends T> boolean testAll(E object, Predicate<? super E>... conditions) {
        return Arrays.stream(conditions).allMatch((predicate) -> {
            Objects.requireNonNull(predicate, "Parameter 'condition' cannot be null");
            return predicate.test(object);
        });
    }
}