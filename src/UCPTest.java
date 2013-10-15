/* Do something... */
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.sql.SQLException;
 
import oracle.ucp.UniversalConnectionPoolAdapter;
import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.admin.UniversalConnectionPoolManagerMBean;
import oracle.ucp.admin.UniversalConnectionPoolManagerMBeanImpl;
import oracle.ucp.jdbc.PoolDataSource; 
import oracle.ucp.jdbc.PoolDataSourceFactory;

public class UCPTest 
{
  public static void main (String args[])
    throws SQLException
         , UniversalConnectionPoolException
  {
    /* Create pool */
    UniversalConnectionPoolManagerMBean poolMgr = UniversalConnectionPoolManagerMBeanImpl.getUniversalConnectionPoolManagerMBean();
    PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
    
    /* Setup pool */
    pds.setConnectionPoolName("My test pool");
    pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
    pds.setURL("jdbc:oracle:thin:@oramypg:1521/ORCL");
    pds.setUser("quest_opti");
    pds.setPassword("quest");
    
    /* Start Pool */    
    poolMgr.createConnectionPool((UniversalConnectionPoolAdapter) pds);
    
    /* Grab a connection */
    Connection conn = pds.getConnection();
    
    CallableStatement cstmt = conn.prepareCall("BEGIN ? := USER; END;");
    cstmt.registerOutParameter(1, Types.VARCHAR);
    cstmt.execute();
    
    String user = cstmt.getString(1);
    System.out.println("You are connected as : " + user);
    
    
    //execute a procedure
    cstmt = conn.prepareCall("{ call test_proc(2) }");
    cstmt.executeQuery();
    
 
    //run a query
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT emp_name\n");
    sql.append("  FROM employee\n");
    sql.append(" WHERE emp_id = 230432\n");

    String query = sql.toString();
    Statement stmt = conn.createStatement();
    ResultSet rset = stmt.executeQuery(query);
    while (rset.next())
      System.out.println(rset.getString(1));
    rset.close();
    stmt.close();
    
    
    /* Close resources */
    cstmt.close();
    /* Because I'm using the UCP, this will return the connection to the pool, NOT actually close it! */
    conn.close();
    poolMgr.destroyConnectionPool("My test pool");
  }
}