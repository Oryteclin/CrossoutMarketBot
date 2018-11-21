package fr.alexpado.database;

import org.json.JSONObject;
import ovh.akio.cmb.logging.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;
import java.util.function.Consumer;

public class Database {

    private boolean checkType(Object o, Class... clazz) {
        for (Class aClass : clazz) {
            if(o.getClass().getSimpleName().equals(aClass.getSimpleName())) return true;
        }
        return false;
    }

    private Connection connection;
    private JSONObject dbConfig;

    private void getConfiguration(File file, Consumer<JSONObject> onSuccess, Consumer<Exception> onFailure) {
        try {
            byte[] readAllBytes = Files.readAllBytes(Paths.get( file.getAbsolutePath() ));
            onSuccess.accept(new JSONObject(new String(readAllBytes)));
        } catch (Exception e) {
            onFailure.accept(e);
        }
    }

    public Database() {
        this("");
    }

    public Database(String pathPrefix) {
        this.getConfiguration(new File((pathPrefix.endsWith("/") ? pathPrefix : pathPrefix + "/") + "database.json"), (content) -> {
            this.dbConfig = content;
            this.createConnection();
        }, (error) -> {
            Logger.fatal("Can't read database config file. Exiting...");
            System.exit(-1);
        });
    }

    private void createConnection() {
        try {
            String host = this.dbConfig.getString("host");
            String user = this.dbConfig.getString("user");
            String pass = this.dbConfig.getString("pass");
            String name = this.dbConfig.getString("name");
            int port = 3306;
            if(this.dbConfig.has("port")) {
                port = this.dbConfig.getInt("port");
            }


            Logger.info("Connecting to database...");
            this.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + name + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false", user, pass);
            Logger.info("Connected.");
        } catch (Exception e){
            Logger.error(e.getMessage());
            Logger.fatal("Can't connect to database. Exiting...");
            System.exit(-1);
        }
    }

    private void reloadConnection() throws SQLException {
        if(connection == null || connection.isClosed() || !connection.isValid(5)) {
            this.createConnection();
        }
    }

    private boolean setParameter(PreparedStatement statement, Object parameter, int i) throws SQLException {
        if(checkType(parameter, int.class, Integer.class)) {
            statement.setInt(i, ((int) parameter));
            return true;
        }else if(checkType(parameter, long.class, Long.class)) {
            statement.setLong(i, ((long) parameter));
            return true;
        }else if(checkType(parameter, boolean.class, Boolean.class)) {
            statement.setBoolean(i, ((boolean) parameter));
            return true;
        }else if(checkType(parameter, String.class)) {
            statement.setString(i, ((String) parameter));
            return true;
        }else if(checkType(parameter, double.class, Double.class)) {
            statement.setDouble(i, ((double) parameter));
            return true;
        }else if(checkType(parameter, Object.class)) {
            statement.setObject(i, parameter);
            return true;
        }
        return false;
    }

    public boolean execute(String sql, Object... parameters) {
        int i = 1;
        try {
            this.reloadConnection();
            PreparedStatement statement = this.connection.prepareStatement(sql);
            for (Object parameter : parameters) {
                if(this.setParameter(statement, parameter, i)) {
                    i++;
                }
            }
            boolean b = statement.execute();
            statement.close();
            return b;
        } catch (SQLException e){
            Logger.error(e.getMessage());
            Logger.warn(sql);
            Logger.warn(Arrays.toString(parameters));
        }
        return false;
    }

    public int executeAndAutoIncrement(String sql, Object... parameters) {
        int i = 1;
        try {
            this.reloadConnection();
            PreparedStatement statement = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (Object parameter : parameters) {
                if(this.setParameter(statement, parameter, i)) {
                    i++;
                }
            }
            statement.execute();

            int id = -1;

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                   id = generatedKeys.getInt(1);
                }
            }
            statement.close();
            return id;
        } catch (SQLException e){
            Logger.error(e.getMessage());
            Logger.warn(sql);
            Logger.warn(Arrays.toString(parameters));
        }
        return -1;
    }

    private PreparedStatement statement;

    public ResultSet query(String sql, Object... parameters) {
        int i = 1;
        try {
            this.reloadConnection();
            statement = this.connection.prepareStatement(sql);
            for (Object parameter : parameters) {
                if(this.setParameter(statement, parameter, i)) {
                    i++;
                }
            }
            return statement.executeQuery();
        } catch (SQLException e){
            Logger.error(e.getMessage());
            Logger.warn(sql);
            Logger.warn(Arrays.toString(parameters));
        }
        return null;
    }

    public void close(ResultSet rs) {
        try {
            rs.close();
            statement.close();
        } catch (Exception e){
            Logger.error(e.getMessage());
        }
    }

}
