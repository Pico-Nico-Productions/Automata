package pro.piconico.automata.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MapUtils {
    // TODO: Refactor nested maps with this
    @SuppressWarnings("unchecked")
    public static <ValueT> Optional<ValueT> getNested(Map<?, ?> map, Object... keys) {
        if (map == null || keys == null || keys.length == 0) {
            throw new IllegalArgumentException();
        }

        Object current = map;
        for (int i = 0; i < keys.length; i++) {
            if (!(current instanceof Map)) {
                throw new IllegalArgumentException();
            }
            Map<?, ?> currentMap = (Map<?, ?>)current;
            Object key = keys[i];

            if (!currentMap.containsKey(key)) {
                return Optional.empty();
            }
            current = currentMap.get(key);
        }

        return Optional.ofNullable((ValueT)current);
    }

    // TODO: Refactor nested maps with this
    @SuppressWarnings("unchecked")
    public static <ValueT> Optional<ValueT> removeNested(Map<?, ?> map, Object... keys) {
        if (map == null || keys == null || keys.length == 0) {
            throw new IllegalArgumentException();
        }

        Map<?, ?>[] maps = new Map[keys.length];
        
        Object current = map;
        for (int i = 0; i < keys.length; i++) {
            if (!(current instanceof Map)) {
                throw new IllegalArgumentException();
            }
            Map<?, ?> currentMap = (Map<?, ?>)current;
            Object key = keys[i];

            if (!currentMap.containsKey(key)) {
                return Optional.empty();
            }

            maps[i] = currentMap;
            current = currentMap.get(key);
        }

        int lastIndex = keys.length - 1;
        Map<Object, Object> deepestMap = (Map<Object, Object>)maps[lastIndex];
        Object targetValue = deepestMap.remove(keys[lastIndex]);

        for (int i = lastIndex; i > 0; i--) {
            if (maps[i].isEmpty()) {
                Map<Object, Object> parentMap = (Map<Object, Object>)maps[i - 1];
                parentMap.remove(keys[i - 1]);
            }
            else {
                break;
            }
        }

        return Optional.ofNullable((ValueT)targetValue);
    }

    public static <KeyT, OptionalValueT, ValueT> Map<KeyT, Optional<ValueT>> wrapValuesInOptional(Map<KeyT, OptionalValueT> sourceMap,
            Function<OptionalValueT, ValueT> innerTransformer) {
        if (sourceMap == null)
            return null;

        Map<KeyT, Optional<ValueT>> targetMap = new HashMap<>();
        for (Map.Entry<KeyT, OptionalValueT> entry : sourceMap.entrySet()) {
            OptionalValueT rawValue = entry.getValue();
            if (rawValue == null) {
                targetMap.put(entry.getKey(), Optional.empty());
            }
            else {
                targetMap.put(entry.getKey(), Optional.of(innerTransformer.apply(rawValue)));
            }
        }
        return targetMap;
    }

    public static <KeyT, ValueT, OptionalValueT> Map<KeyT, OptionalValueT> unwrapOptionalValues(Map<KeyT, Optional<ValueT>> sourceMap,
            Function<ValueT, OptionalValueT> innerTransformer) {
        if (sourceMap == null)
            return null;

        Map<KeyT, OptionalValueT> targetMap = new HashMap<>();
        for (Map.Entry<KeyT, Optional<ValueT>> entry : sourceMap.entrySet()) {
            Optional<ValueT> optionalValue = entry.getValue();
            if (optionalValue == null || !optionalValue.isPresent()) {
                targetMap.put(entry.getKey(), null);
            }
            else {
                targetMap.put(entry.getKey(), innerTransformer.apply(optionalValue.get()));
            }
        }
        return targetMap;
    }

    public static <KeyT, ValueT> Map<KeyT, ValueT> computeMapDelta(Map<KeyT, ValueT> oldMap, Map<KeyT, ValueT> newMap,
            BiFunction<ValueT, ValueT, ValueT> mergeFunction) {
        if (Objects.equals(oldMap, newMap)) {
            return null;
        }

        Map<KeyT, ValueT> deltaMap = new HashMap<>(newMap);

        for (Map.Entry<KeyT, ValueT> oldEntry : oldMap.entrySet()) {
            KeyT key = oldEntry.getKey();

            if (!newMap.containsKey(key)) {
                deltaMap.put(key, null);
                continue;
            }

            ValueT oldVal = oldEntry.getValue();
            ValueT newVal = newMap.get(key);
            ValueT mergedVal = mergeFunction.apply(oldVal, newVal);

            if (mergedVal == null) {
                deltaMap.remove(key);
            }
            else {
                deltaMap.put(key, mergedVal);
            }
        }

        return deltaMap.isEmpty() ? null : deltaMap;
    }

    public static <KeyT, ValueT> void applyMapDelta(Map<KeyT, ValueT> targetMap, Map<KeyT, ValueT> deltaMap, BiConsumer<ValueT, ValueT> innerMerger) {
        if (targetMap == null || deltaMap == null)
            return;

        for (Map.Entry<KeyT, ValueT> entry : deltaMap.entrySet()) {
            KeyT key = entry.getKey();
            ValueT deltaValue = entry.getValue();

            if (deltaValue == null) {
                targetMap.remove(key);
                continue;
            }

            ValueT targetValue = targetMap.get(key);
            if (targetValue == null) {
                targetMap.put(key, deltaValue);
            }
            else if (innerMerger != null) {
                innerMerger.accept(targetValue, deltaValue);

                if (targetValue instanceof Map && ((Map<?, ?>)targetValue).isEmpty()) {
                    targetMap.remove(key);
                }
            }
            else {
                targetMap.put(key, deltaValue);
            }
        }
    }
}
