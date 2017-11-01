package org.h2.test.jdbc;

import org.h2.api.PasswordDataTypeHandler;
import org.h2.test.TestBase;
import org.h2.util.JdbcUtils;
import org.h2.value.Password;

import java.sql.*;

/**
 * Test for Password DataType.
 */
public class TestPasswordDataType extends TestBase {
    /**
     * The database name.
     */
    public final static String DB_NAME = "customDataTypes";

    /**
     * The system property name.
     */
    public final static String HANDLER_NAME_PROPERTY = "h2.customDataTypesHandler";

    @Override
    public void test() throws Exception {
        try {
            JdbcUtils.customDataTypesHandler = new PasswordDataTypeHandler();

            deleteDb(DB_NAME);
            Connection conn = getConnection(DB_NAME);

            Statement stat = conn.createStatement();

            //Test cast
            ResultSet rs = stat.executeQuery("select CAST('Hello123123123' AS password) ");
            rs.next();
            assertTrue(rs.getObject(1).equals(new Password("Hello123123123")));

            //Test create table
            stat.execute("create table t(id int, val password)");
            rs = conn.getMetaData().getColumns(null, null, "T", "VAL");
            rs.next();
            assertEquals(rs.getString("TYPE_NAME"), "password");
            assertEquals(rs.getInt("DATA_TYPE"), Types.JAVA_OBJECT);

            rs = stat.executeQuery("select val from t");
            assertEquals(Password.class.getName(), rs.getMetaData().getColumnClassName(1));

            //Test insert
            PreparedStatement stmt = conn.prepareStatement(
                    "insert into t(id, val) values (0, 'Hi123123'), (1, ?), (2, ?) ");
            stmt.setObject(1, new Password("Test123123"));
            stmt.setObject(2, "Hello123123");
            stmt.executeUpdate();

            //Test selects
            Password[] expected = new Password[3];
            expected[0] = new Password("Hi123123");
            expected[1] = new Password("Test123123");
            ;
            expected[2] = new Password("Hello123123");
            ;

            for (int id = 0; id < expected.length; ++id) {
                PreparedStatement prepStat = conn.prepareStatement(
                        "select val from t where id = ?");
                prepStat.setInt(1, id);
                rs = prepStat.executeQuery();
                assertTrue(rs.next());
                Object object = rs.getObject(1);
                assertTrue(object.equals(expected[id]));
            }

            for (int id = 0; id < expected.length; ++id) {
                PreparedStatement prepStat = conn.prepareStatement(
                        "select id from t where val = ?");
                prepStat.setObject(1, expected[id]);
                rs = prepStat.executeQuery();
                assertTrue(rs.next());
                assertEquals(rs.getInt(1), id);
            }

            // Repeat selects with index
            stat.execute("create index val_idx on t(val)");

            for (int id = 0; id < expected.length; ++id) {
                PreparedStatement prepStat = conn.prepareStatement(
                        "select id from t where val = ?");
                prepStat.setObject(1, expected[id]);
                rs = prepStat.executeQuery();
                assertTrue(rs.next());
                assertEquals(rs.getInt(1), id);
            }

            conn.close();
            //deleteDb(DB_NAME);
        } finally {
            JdbcUtils.customDataTypesHandler = null;
        }

    }

    public static void main(String[] args) throws Exception {
        System.setProperty(HANDLER_NAME_PROPERTY, PasswordDataTypeHandler.class.getName());
        TestBase test = createCaller().init();
        test.config.traceTest = true;
        test.config.memory = true;
        test.config.networked = true;
        test.config.beforeTest();
        test.test();
        test.config.afterTest();
        System.clearProperty(HANDLER_NAME_PROPERTY);
    }

    public static TestBase createCaller() {
        org.h2.Driver.load();
        try {
            return (TestBase) new SecurityManager() {
                Class<?> clazz = getClassContext()[2];
            }.clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
