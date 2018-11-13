package ovh.akio.cmb;

import org.json.JSONObject;
import ovh.akio.cmb.logging.Logger;
import ovh.akio.cmb.utils.BotUtils;

import java.io.File;
import java.sql.*;

public class Database {

    private boolean checkType(Object o, Class... clazz) {
        for (Class aClass : clazz) {
            if(o.getClass().getSimpleName().equals(aClass.getSimpleName())) return true;
        }
        return false;
    }

    private Connection connection;
    private JSONObject dbConfig;

    Database() {
        BotUtils.getFileContent(new File("data/database.json"), (content) -> {
            this.dbConfig = new JSONObject(content);
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
            int port = this.dbConfig.getInt("port");

            Logger.info("Connecting to database...");
            this.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + name + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false", user, pass);
            Logger.info("Connected.");
        } catch (SQLException e){
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
