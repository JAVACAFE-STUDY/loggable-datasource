package net.chandol.logjdbc.logging.collector.resultset;

import net.chandol.logjdbc._fixture.DummyDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ResultSetCollectorTest {

    Connection connection;

    @Before
    public void setup() throws SQLException {
        DataSource dataSource = DummyDataSource.getDummyH2DataSource();

        connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        // FIXME 테이블을 만드는 과정이 진행됨. 회귀테스트 불가능한부분
        // 테스트 종료후 테이블을 삭제하는 로직 필요
        statement.execute("CREATE TABLE EMPLOYEE (ID INT NOT NULL, NAME VARCHAR (20) NOT NULL, AGE INT NOT NULL, ADDRESS CHAR (25), PRIMARY KEY (ID));");
        statement.execute("INSERT INTO EMPLOYEE (ID, NAME, AGE, ADDRESS) VALUES (2, 'Khilan', 25, 'Delhi' );");
        statement.execute("INSERT INTO EMPLOYEE (ID, NAME, AGE, ADDRESS) VALUES (3, 'kaushik', 23, 'Kota' );");
        statement.execute("INSERT INTO EMPLOYEE (ID, NAME, AGE, ADDRESS) VALUES (4, 'Chaitali', 25, 'Mumbai' );");

        statement.close();
    }

    @Test
    public void addAndGet() throws SQLException {
        //given
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT ID, NAME, AGE, ADDRESS FROM EMPLOYEE");

        //when
        ResultSetCollector collector = new ResultSetCollector(resultSet.getMetaData());

        resultSet.next();
        collector.collectCurrentCursorResultSetData(resultSet);
        resultSet.next();
        collector.collectCurrentCursorResultSetData(resultSet);
        resultSet.next();
        collector.collectCurrentCursorResultSetData(resultSet);

        //then
        resultSet.close();

        ResultSetData resultSetData = collector.getResultSetData();
        assertThat(resultSetData.getColumns(), hasItems("ID", "NAME", "AGE", "ADDRESS"));

        List<String[]> datas = resultSetData.getRows();
        assertThat(datas.size(), is(3));
        String[] firstData = datas.get(0);
        assertThat(firstData[0], is("2"));
    }

    @After
    public void close() throws SQLException {
        connection.rollback();
        connection.close();
    }

}