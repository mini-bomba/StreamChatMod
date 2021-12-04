package me.mini_bomba.streamchatmod.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Unmodifiable
public class LazyUnmodifiableMap<K, V> implements Map<K, V> {
    @Nullable
    private Supplier<Map<K, V>> mapSupplier;
    @Nullable
    private Map<K, V> map;
    private Set<K> keySet;

    private LazyUnmodifiableMap(@NotNull Supplier<Map<K, V>> mapSupplier) {
        this.mapSupplier = Objects.requireNonNull(mapSupplier);
    }

    private void prepareMap() {
        if (map != null) return;
        assert mapSupplier != null : "Map supplier cleared too early!";
        map = Collections.unmodifiableMap(mapSupplier.get());
        mapSupplier = null;
    }

    @Unmodifiable
    public static <K, V> LazyUnmodifiableMap<K, V> from(@NotNull Supplier<Map<K, V>> mapSupplier) {
        return new LazyUnmodifiableMap<>(mapSupplier);
    }

    @Override
    public int size() {
        if (map == null) prepareMap();
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        if (map == null) prepareMap();
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        if (map == null) prepareMap();
        return map.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        if (map == null) prepareMap();
        return map.containsValue(o);
    }

    @Override
    public V get(Object o) {
        if (map == null) prepareMap();
        return map.get(o);
    }

    @Nullable
    @Override
    public V put(K k, V v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        if (map == null) prepareMap();
        return map.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        if (map == null) prepareMap();
        return map.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        if (map == null) prepareMap();
        return map.entrySet();
    }

    @Override
    public V getOrDefault(Object o, V v) {
        if (map == null) prepareMap();
        return map.getOrDefault(o, v);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> biConsumer) {
        if (map == null) prepareMap();
        map.forEach(biConsumer);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> biFunction) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public V putIfAbsent(K k, V v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o, Object o1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(K k, V v, V v1) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public V replace(K k, V v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfAbsent(K k, @NotNull Function<? super K, ? extends V> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfPresent(K k, @NotNull BiFunction<? super K, ? super V, ? extends V> biFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V compute(K k, @NotNull BiFunction<? super K, ? super V, ? extends V> biFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V merge(K k, @NotNull V v, @NotNull BiFunction<? super V, ? super V, ? extends V> biFunction) {
        throw new UnsupportedOperationException();
    }
}
