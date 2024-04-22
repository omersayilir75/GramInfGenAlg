package util;

import java.util.Objects;

public class Triple<K, V1, V2> {

    private final K key;
    private final V1 value1;
    private final V2 value2;

    public Triple(K key, V1 value1, V2 value2) {
        this.key = key;
        this.value1 = value1;
        this.value2 = value2;
    }

    public K getKey() {
        return key;
    }

    public V1 getValue1() {
        return value1;
    }
    public V2 getValue2() {
        return value2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triple<?, ?, ?> pair = (Triple<?, ?, ?>) o;
        return Objects.equals(key, pair.key) &&
                Objects.equals(value1, pair.value1) &&
                Objects.equals(value2, pair.value2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value1, value2);
    }

    @Override
    public String toString() {
        return "Triple{" +
                "key=" + key +
                ", value1=" + value1 +
                ", value2=" + value2 +
                '}';
    }
}
