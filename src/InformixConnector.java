import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.informix.jdbcx.IfxDataSource;

public class InformixConnector {

	Connection con;
	Statement stmt; 
   	Properties prop; 
	
	public InformixConnector() throws Exception {
    	prop = InformixConnector.loadProperties("db.properties");
		Connect(prop);
	}

	public boolean Connect(Properties prop) throws Exception {
	    String driver;
	    boolean rc = true;
	    String conUrl;
	   
	         conUrl = "jdbc:informix-sqli://" + prop.getProperty("host") + ":" + prop.getProperty("port") + "/" +
	         prop.getProperty("dbname") + ":INFORMIXSERVER=" + prop.getProperty("dbservername") + ";user=" +
	    	 prop.getProperty("user") + ";password=" + prop.getProperty("password");	
	    		
	    		
	    System.out.println("Loading Driver");
		try {
	    	System.out.println("conurl = " + conUrl);
	    	driver = "com.informix.jdbc.IfxDriver";
	    	Class.forName(driver).newInstance();
	    } catch ( Exception e ) {
	    	System.out.println("Failed to load Informix driver: " );
	    	e.printStackTrace();
	    	rc = false;
	    }
	    System.out.println("Loading Driver: Success");
	    try {
	    	con = DriverManager.getConnection(conUrl);
	    	stmt = con.createStatement();
	    } catch ( SQLException e) {
	    	e.printStackTrace();
			System.out.println("Connect: " + conUrl);						
	    	rc = false;
	    }
	    return rc;
	}

	public void Disconnect() throws Exception {
		try {
			con.close();
		} catch ( SQLException e) {
			e.printStackTrace();
			System.out.println("Disconnect: ");
		}
	}

    public static IfxDataSource getIfxDataSource() throws Exception {
    	Properties prop = InformixConnector.loadProperties("db.properties");
    	
        IfxDataSource ifxds = new IfxDataSource();
        
        ifxds.setIfxIFXHOST(prop.getProperty("host"));
        ifxds.setPortNumber(Integer.parseInt(prop.getProperty("port")));
        ifxds.setUser(prop.getProperty("user"));
        ifxds.setPassword(prop.getProperty("password"));
        ifxds.setDatabaseName(prop.getProperty("dbname"));
       
    	
    	return ifxds;
    }
	
	public static Properties loadProperties(String filename) {
		Properties prop = new Properties();
		InputStream in = null;
		try {
			in = new FileInputStream(filename);
			prop.load(in);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return prop;
    } 	

    public static String getCurrentTS() {
    	Date date = new Date();	
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS00");
		String ts = sd.format(date);
		return ts;
    }
}