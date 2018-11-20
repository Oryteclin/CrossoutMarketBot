package fr.alexpado.database.annotations;

import fr.alexpado.database.utils.SQLColumnType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String name();
    SQLColumnType columnType();
    boolean autoincrement() default false;
    boolean unique() default false;
    boolean primaryKey() default false;
    String additionnalData() default "";
}
