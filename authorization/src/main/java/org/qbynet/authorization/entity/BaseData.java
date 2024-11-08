package org.qbynet.authorization.entity;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Consumer;

public interface BaseData {
    default <V> V asViewObject(Class<V> clazz, @NotNull Consumer<V> consumer) {
        V v = this.asViewObject(clazz); // get vo
        consumer.accept(v);
        return v;
    }

    default <V> V asViewObject(@NotNull Class<V> clazz) {
        try {
            Field[] declaredFields = clazz.getDeclaredFields();
            Constructor<V> constructor = clazz.getConstructor();
            V v = constructor.newInstance();
            for (Field declaredField : declaredFields) {
                convert(declaredField, v);
            }
            return v;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void convert(Field field, Object vo) {
        try {
            Field source = this.getClass().getDeclaredField(field.getName());
            field.setAccessible(true); // make accessible
            source.setAccessible(true);
            field.set(vo, source.get(this));
        } catch (IllegalAccessException | NoSuchFieldException ignored) {

        }
    }
}
