package it.polito.tdp.crimes.db;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConnect 
{
	private static final String jdbcURL = "jdbc:mariadb://localhost/denver_crimes";
	private static final HikariDataSource dataSource;
	
	static 
	{
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(jdbcURL);
		config.setUsername("root");
		config.setPassword("root");
		
		// configurazione MySQL
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		
		dataSource = new HikariDataSource(config);
	}
	
	public static Connection getConnection() 
	{	
		try 
		{
			return dataSource.getConnection();
		} 
		catch (SQLException e) 
		{
			System.err.println("Errore connessione al DB at " + jdbcURL);
			throw new RuntimeException("Errore connessione al DB at " + jdbcURL, e);
		}
	}

}