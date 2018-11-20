package fr.alexpado.database.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum SQLColumnType {

    BOOLEAN(Boolean.class, boolean.class),
    TINYINT(Byte.class, byte.class),
    SMALLINT(Short.class, short.class),
    INT(Integer.class, int.class),
    BIGINT(Long.class, long.class),
    FLOAT(Float.class, float.class),
    DOUBLE(Double.class, double.class),
    CHAR(Character.class, char.class),
    VARCHAR(String.class, String.class),
    TEXT(String.class, String.class);

    ArrayList<Class> classes = new ArrayList<>();
    SQLColumnType(Class... classes) {
        this.classes.addAll(Arrays.stream(classes).collect(Collectors.toList()));
    }

    public boolean isCompatible(Class clazz) {
        return this.classes.contains(clazz);
    }

}
