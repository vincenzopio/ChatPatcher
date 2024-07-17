package it.vincenzopio.chatpatcher.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ReflectionUtils {

    private ReflectionUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static boolean setField(@NotNull Object obj,  @NotNull String fieldName, @Nullable Object value) throws IllegalAccessException, NoSuchFieldException {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
            return true;
    }

    public static boolean isFieldInstance(@NotNull Object obj, @NotNull String fieldName, @NotNull Class<?> clazz) throws IllegalAccessException, NoSuchFieldException {
        var field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return clazz.isInstance(field.get(obj));
    }
}
