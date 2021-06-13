package it.polito.tdp.crimes.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.polito.tdp.crimes.model.Couple;
import it.polito.tdp.crimes.model.Event;


public class EventsDao 
{	
	public List<Event> listAllEvents()
	{
		String sql = "SELECT * FROM events" ;
		try 
		{
			Connection conn = DBConnect.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			
			List<Event> list = new ArrayList<>() ;
			
			ResultSet res = st.executeQuery() ;
			
			while(res.next()) 
			{
				try 
				{
					list.add(new Event(res.getLong("incident_id"),
							res.getInt("offense_code"),
							res.getInt("offense_code_extension"), 
							res.getString("offense_type_id"), 
							res.getString("offense_category_id"),
							res.getTimestamp("reported_date").toLocalDateTime(),
							res.getString("incident_address"),
							res.getDouble("geo_lon"),
							res.getDouble("geo_lat"),
							res.getInt("district_id"),
							res.getInt("precinct_id"), 
							res.getString("neighborhood_id"),
							res.getInt("is_crime"),
							res.getInt("is_traffic")));
				} 
				catch (Throwable t) 
				{
					t.printStackTrace();
					System.out.println(res.getInt("id"));
				}
			}
			
			res.close();
			st.close();
			conn.close();
			return list ;
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			return null ;
		}
	}
	
	public List<String> getAllOffenseCategories()
	{
		final String sqlQuery = String.format("%s %s %s",
							"SELECT DISTINCT offense_category_id",
							"FROM events",
							"ORDER BY offense_category_id ASC");
		
		List<String> result = new ArrayList<>();
		
		try
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			ResultSet queryResult = statement.executeQuery();
			
			while(queryResult.next())
			{
				String offenseCategory = queryResult.getString("offense_category_id");
				result.add(offenseCategory);
			}
			
			queryResult.close();
			statement.close();
			connection.close();
			return result;
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getAllOffenseCategories()", sqle);
		}
	}
	
	public List<LocalDate> getAllDates()
	{
		final String sqlQuery = String.format("%s %s %s",
				"SELECT DISTINCT DATE(reported_date) AS reportedDate",
				"FROM events",
				"ORDER BY reportedDate ASC");

		List<LocalDate> result = new ArrayList<>();
		
		try
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			ResultSet queryResult = statement.executeQuery();
			
			while(queryResult.next())
			{
				LocalDate reportedDate = queryResult.getDate("reportedDate").toLocalDate();
				result.add(reportedDate);
			}
			
			queryResult.close();
			statement.close();
			connection.close();
			return result;
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getAllDates()", sqle);
		}
	}

	public List<String> getOffenseTypesOf(String selectedCategory, LocalDate selectedDate)
	{
		final String sqlQuery = String.format("%s %s %s %s",
				"SELECT DISTINCT offense_type_id",
				"FROM events",
				"WHERE offense_category_id = ? AND DATE(reported_date) = ?",
				"ORDER BY offense_type_id ASC");

		List<String> result = new ArrayList<>();
		
		try
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			statement.setString(1, selectedCategory);
			statement.setDate(2, Date.valueOf(selectedDate));
			ResultSet queryResult = statement.executeQuery();
			
			while(queryResult.next())
			{
				String offenseType = queryResult.getString("offense_type_id");
				result.add(offenseType);
			}
			
			queryResult.close();
			statement.close();
			connection.close();
			return result;
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getOffenseTypesOf()", sqle);
		}
	}

	public Collection<Couple<String>> getOffenseTypesCouplesOf(
			String selectedCategory, LocalDate selectedDate)
	{
		final String sqlQuery = String.format("%s %s %s %s %s %s",
				"SELECT e1.offense_type_id AS type1, e2.offense_type_id AS type2, COUNT(DISTINCT e1.precinct_id) AS num",
				"FROM events e1, events e2",
				"WHERE e1.offense_category_id = ? AND e1.offense_category_id = e2.offense_category_id",
						"AND DATE(e1.reported_date) = ? AND DATE(e1.reported_date) = DATE(e2.reported_date)",
						"AND e1.precinct_id = e2.precinct_id AND e1.offense_type_id < e2.offense_type_id",
				"GROUP BY type1, type2");

		Collection<Couple<String>> result = new ArrayList<>();
		
		try
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			statement.setString(1, selectedCategory);
			statement.setDate(2, Date.valueOf(selectedDate));
			ResultSet queryResult = statement.executeQuery();
			
			while(queryResult.next())
			{
				String offenseType1 = queryResult.getString("type1");
				String offenseType2 = queryResult.getString("type2");
				int weight = queryResult.getInt("num");
				
				Couple<String> newTypesCouple = new Couple<>(offenseType1, offenseType2, weight);

				result.add(newTypesCouple);
			}
			
			queryResult.close();
			statement.close();
			connection.close();
			return result;
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getOffenseTypesCouplesOf()", sqle);
		}
	}
}
