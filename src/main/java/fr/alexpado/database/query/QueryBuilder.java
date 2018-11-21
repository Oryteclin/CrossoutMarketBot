package fr.alexpado.database.query;

import fr.alexpado.database.annotations.Column;
import fr.alexpado.database.annotations.Table;
import fr.alexpado.database.utils.DataField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class QueryBuilder {

    private Object data;

    private String tableName = null;
    private ArrayList<DataField> values = new ArrayList<>();

    public QueryBuilder(Object data) throws Exception {
        this.data = data;

        for (Annotation annotation : this.data.getClass().getAnnotations()) {
            if(annotation instanceof Table) {
                tableName = ((Table) annotation).name();
            }
        }
        if(tableName == null) throw new Exception("Can't find Table annotation on object.");


        for (Field declaredField : this.data.getClass().getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(Column.class)) {
                this.values.add(new DataField(declaredField));
            }

        }
    }

    public QueryBuilder(Class target) throws Exception {
        for (Annotation annotation : target.getAnnotations()) {
            if(annotation instanceof Table) {
                tableName = ((Table) annotation).name();
            }
        }
        if(tableName == null) throw new Exception("Can't find Table annotation on object.");


        for (Field declaredField : target.getDeclaredFields()) {
            this.values.add(new DataField(declaredField));
        }
    }

    public String getTableName() {
        return tableName;
    }

    private String buildColumnStructure(DataField dataField) {
        return String.format("`%s` %s%s", dataField.getName(), dataField.getColumnType().name(), dataField.getAdditionnalData());
    }

    public void getUpdateQuery(BiConsumer<String, ArrayList<Object>> onSuccess) {
        String query = "UPDATE %s SET %s WHERE %s";
        ArrayList<Object> data = new ArrayList<>();
        StringBuilder setters = new StringBuilder();
        this.values.stream().filter(dataField -> !dataField.isPrimaryKey()).forEach(dataField -> {
            dataField.getField().setAccessible(true);
            setters.append("`").append(dataField.getName()).append("` = ?, ");
            try {
                data.add(dataField.getField().get(this.data));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        StringBuilder selecters = new StringBuilder();
        this.values.stream().filter(DataField::isPrimaryKey).forEach(dataField -> {
            dataField.getField().setAccessible(true);
            selecters.append(dataField.getName()).append(" = ? AND ");
            try {
                data.add(dataField.getField().get(this.data));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        String _setters = setters.toString().substring(0, setters.toString().length() - 2);
        String _selecters = selecters.toString().substring(0, selecters.toString().length() - 4);

        onSuccess.accept(String.format(query, tableName, _setters, _selecters), data);
    }

    public void getDeleteQuery(BiConsumer<String, ArrayList<Object>> onSuccess) {
        String query = "DELETE FROM %s WHERE %s";
        ArrayList<Object> data = new ArrayList<>();

        StringBuilder selecters = new StringBuilder();
        this.values.stream().filter(DataField::isPrimaryKey).forEach(dataField -> {
            dataField.getField().setAccessible(true);
            selecters.append("`").append(dataField.getName()).append("` = ? AND ");
            try {
                data.add(dataField.getField().get(this.data));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        String _selecters = selecters.toString().substring(0, selecters.toString().length() - 4);

        onSuccess.accept(String.format(query, tableName, _selecters), data);
    }

    public void getInsertQuery(BiConsumer<String, ArrayList<Object>> onSuccess) {
        String query = "INSERT IGNORE INTO %s (%s) VALUE (%s)";
        ArrayList<Object> data = new ArrayList<>();

        StringBuilder columns = new StringBuilder();
        StringBuilder dataTag = new StringBuilder();

        this.values.forEach(dataField -> {
            if(!dataField.isAutoincrement()) {
                columns.append("`").append(dataField.getName()).append("`, ");
                dataTag.append("?, ");
                try {
                    dataField.getField().setAccessible(true);
                    data.add(dataField.getField().get(this.data));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });

        String _column = columns.toString().substring(0, columns.toString().length() -2);
        String _dataTag = dataTag.toString().substring(0, dataTag.toString().length() -2);

        onSuccess.accept(String.format(query, tableName, _column, _dataTag), data);
    }

    public void getSelectQuery(BiConsumer<String, ArrayList<Object>> onSuccess) {
        String query = "SELECT * FROM %s WHERE %s";
        ArrayList<Object> data = new ArrayList<>();

        StringBuilder selecters = new StringBuilder();
        this.values.stream().filter(DataField::isPrimaryKey).forEach(dataField -> {
            dataField.getField().setAccessible(true);
            selecters.append(dataField.getName()).append(" = ? AND ");
            try {
                data.add(dataField.getField().get(this.data));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        String _selecters = selecters.toString().substring(0, selecters.toString().length() - 4);

        onSuccess.accept(String.format(query, tableName, _selecters), data);
    }

    public void getCreateTableQuery(Consumer<String> onSuccess) {


        String query = "CREATE TABLE IF NOT EXISTS %s (%s, PRIMARY KEY (%s))";
        String queryForeign = "CREATE TABLE IF NOT EXISTS %s (%s, PRIMARY KEY (%s), %s)";

        StringBuilder structure = new StringBuilder();
        StringBuilder primaryKey = new StringBuilder();

        this.values.forEach(dataField -> {
            structure.append(this.buildColumnStructure(dataField)).append(" ");
            if(dataField.isPrimaryKey()) primaryKey.append("`").append(dataField.getName()).append("`, ");
            if(dataField.isAutoincrement()) structure.append("AUTO_INCREMENT");
            if(dataField.isUnique()) structure.append(" UNIQUE");
            structure.append(", ");
        });

        String _structure = structure.toString().substring(0, structure.toString().length() - 2);
        String _primaryKey = primaryKey.toString().substring(0, primaryKey.toString().length() - 2);
        onSuccess.accept(String.format(query, tableName, _structure, _primaryKey));
    }

    public ArrayList<DataField> getValues() {
        return values;
    }
}
