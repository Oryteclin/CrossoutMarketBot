package fr.alexpado.database.utils;

import fr.alexpado.database.Database;
import fr.alexpado.database.query.QueryBuilder;

import java.sql.ResultSet;

public class SQLObject {

    private Object object;
    private Database database;
    private QueryBuilder queryBuilder;

    public SQLObject(Database database, Object object) throws Exception {
        this.object = object;
        this.database = database;
        this.queryBuilder = new QueryBuilder(object);
    }

    public void createTable() {
        this.queryBuilder.getCreateTableQuery(database::execute);
    }

    public void insert() {
        this.queryBuilder.getInsertQuery((sql, objects) -> database.execute(sql, objects.toArray()));
    }

    public void update() {
        this.queryBuilder.getUpdateQuery((sql, objects) -> database.execute(sql, objects.toArray()));
    }

    public void delete() {
        this.queryBuilder.getDeleteQuery((sql, objects) -> database.execute(sql, objects.toArray()));
    }

    public void select() {

        this.queryBuilder.getSelectQuery((sql, objects) -> {
            ResultSet rs = database.query(sql, objects.toArray());
            try {
                if(rs.next()) {
                    for (DataField dataField : this.queryBuilder.getValues()) {
                        dataField.setValue(this.object, rs.getObject(dataField.getName()));
                    }
                }else{
                    throw new Exception("Record doesn't exists.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            database.close(rs);
        });
    }

}
