package fr.alexpado.database.utils;

import fr.alexpado.database.annotations.Column;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class DataField {

    private Field field;

    private String name = null;
    private boolean unique;
    private boolean autoincrement;
    private boolean primaryKey;
    private String additionnalData = "";
    private SQLColumnType columnType;

    private Field getForeignFieldReference(Class clazz) {
        for (Field declaredField : clazz.getDeclaredFields()) {
            for (Annotation declaredFieldAnnotation : declaredField.getAnnotations()) {
                if(declaredFieldAnnotation instanceof Column) {
                    if(((Column) declaredFieldAnnotation).primaryKey()) {
                        return declaredField;
                    }
                }
            }
        }
        return null;
    }

    public DataField(Field field) throws Exception {
        this.field = field;

        for (Annotation annotation : field.getAnnotations()) {
            if(annotation instanceof Column) {
                name = ((Column) annotation).name();
                unique = ((Column) annotation).unique();
                autoincrement = ((Column) annotation).autoincrement();
                primaryKey = ((Column) annotation).primaryKey();
                additionnalData = ((Column) annotation).additionnalData();
                columnType = ((Column) annotation).columnType();
            }
        }
        if(name == null) throw new Exception("Can't find annotation on field.");
        if(this.field.getType() == ArrayList.class) throw new Exception("Arrays are not supported.");
        if(!columnType.isCompatible(this.field.getType())){
            throw new IllegalArgumentException("Can't apply " + columnType.name() + " on field of type " + this.field.getType().getSimpleName() + " ( Column : " + this.name + " )");
        }
    }

    public Field getField() {
        return field;
    }

    public String getName() {
        return name;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isAutoincrement() {
        return autoincrement;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public String getAdditionnalData() {
        return additionnalData;
    }

    public SQLColumnType getColumnType() {
        return columnType;
    }

    public void setValue(Object parent, Object value) throws Exception {
        this.field.setAccessible(true);
        this.field.set(parent, value);
    }

    public Object getValue(Object parent) throws Exception {
        this.field.setAccessible(true);
        return this.field.get(parent);
    }
}
