package tv.qahub;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class BackUpDb implements ProcessExecutor {
	private static Logger logger = Logger.getLogger(BackUpDb.class);

	public void execute(Object...objects) throws BackUpException {
		logger.debug("database backup. execute()");
		Connection con = null;
		PreparedStatement statement = null;
		try {
			Properties props = (Properties)objects[0];
			Class.forName(DRIVER_NAME).newInstance();
			con = DriverManager.getConnection(props.getProperty(Main.PROPS_KEY_DATABASE_URL)
					, props.getProperty(Main.PROPS_KEY_DATABASE_USER)
					, props.getProperty(Main.PROPS_KEY_DATABASE_PASSWORD));
			
			statement = con.prepareStatement(SQL_BACKUP_QUERY);
			int parameterIndex = 1;
			statement.setString(parameterIndex++, props.getProperty(Main.PROPS_KEY_DATABASE_NAME));
			statement.setString(parameterIndex++, props.getProperty(Main.PROPS_KEY_DATABASE_BACKUP_PATH));
			statement.execute();
		} catch (InstantiationException e) {
			throw new BackUpException(e);
		} catch (IllegalAccessException e) {
			throw new BackUpException(e);
		} catch (ClassNotFoundException e) {
			throw new BackUpException(e);
		} catch (SQLException e) {
			throw new BackUpException(e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (Exception ex) {}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Exception ex) {}
			}
		}
		return;
	}
	/** Backup Query */
	private static final String SQL_BACKUP_QUERY = "BACKUP DATABASE ? TO disk= ?";
	/** Use Driver */
	private static final String DRIVER_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

}
