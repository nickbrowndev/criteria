package criteria;

import java.util.Objects;
import java.util.function.Predicate;

public interface StringPredicate extends Predicate<String> {
    Predicate<String> NULL = Objects::isNull;
    Predicate<String> NOT_NULL = Objects::nonNull;
    Predicate<String> EMPTY = String::isEmpty;
    Predicate<String> NOT_EMPTY = v -> !v.isEmpty();
    Predicate<String> NOT_NULL_NOT_EMPTY = v -> NOT_NULL.test(v) && !v.isEmpty();
    Predicate<String> NOT_NULL_EMPTY = (value) -> NOT_NULL.test(value) && value.isEmpty();
}
