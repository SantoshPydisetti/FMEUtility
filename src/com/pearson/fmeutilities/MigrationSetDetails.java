package com.pearson.fmeutilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * Simple Java Program to connect Oracle database by using Oracle JDBC thin
 * driver Make sure you have Oracle JDBC thin driver in your classpath before
 * running this program
 * 
 * @author
 */
public class MigrationSetDetails {

	static Logger logger_info = Logger.getLogger("complete");
	static Logger logger_success = Logger.getLogger("success");
	static Logger logger_error = Logger.getLogger("failure");
	static Properties properties = null;
	static FileInputStream prop_read = null;
	static String migId;
	static String url;
	static String username;
	static String pwd;

	public static void main(String args[]) throws SQLException, IOException {

		Properties properties = null;
		FileInputStream prop_read = null;

		try {
			prop_read = new FileInputStream("db_conf.properties");
			properties = new Properties();
		} catch (Exception e) {
			// System.out.println("Error in reading the properties
			// files:"+e.getMessage());
			logger_info.info("Error in reading the properties files:" + e.getMessage());
			logger_error.error("Error in reading the properties files:" + e.getMessage());
			System.exit(0);
		}
		// create a blank workbook object
		HSSFWorkbook new_workbook = new HSSFWorkbook();
		// create a worksheet with caption score_details
		HSSFSheet sheet = new_workbook.createSheet("MigSet_Details");
		// create a map and define data
		Map<String, Object[]> excel_data = new HashMap<String, Object[]>();
		Map<String, Object[]> excel_data_header = new HashMap<String, Object[]>();

		properties.load(prop_read);
		PropertyConfigurator.configure("log4j.properties");
		// Migration set ID
		migId = properties.getProperty("migrationSetId").trim();
		System.setProperty("migId", migId);

		// URL and credentials of Oracle database server
		url = properties.getProperty("db_url").trim();
		username = properties.getProperty("db_username").trim();
		pwd = properties.getProperty("db_password").trim();

		logger_info.info("FME Utility Started to find the details for " + migId);
		logger_info.info("DB Url" + url);

		// properties for creating connection to Oracle database
		Properties props = new Properties();
		props.setProperty("user", username);
		props.setProperty("password", pwd);

		// creating connection to Oracle database using JDBC
		Connection conn = DriverManager.getConnection(url, props);
		System.out.println("Connection established and getting the data...");
		logger_success.info("Connection established.." + url);

		String sql = "select mig.Name as MigrationSet_Name, sos.NAME as MigraionSet_Status, so.ID_IN_SOURCE_SYSTEM, so.ID_IN_TARGET_SYSTEM, so.SCANNED_DATE, so.IMPORTED_DATE, mc_util.get_source_attributes(so.source_attributes, 'dctm_obj_link'), mc_util.get_source_attributes(so.source_attributes, 'object_name'), mc_util.get_source_attributes(so.source_attributes, 'owner_name'), mc_util.get_source_attributes(so.source_attributes, 'prsn_title'), mc_util.get_target_attributes(so.target_attributes, 'cm:name'), mc_util.get_target_attributes(so.target_attributes, 'folderpath') FROM SOURCE_OBJECTS so inner join SOURCE_OBJECT_STATUSES sos on (so.STATUS_ID = sos.ID) inner join MIGSETS mig on (so.MIGSET_ID = mig.ID) WHERE so.MIGSET_ID="
				+ migId;

		logger_info.info("Query: " + sql);

		try {
			// creating PreparedStatement object to execute query
			PreparedStatement preStatement = conn.prepareStatement(sql);
			ResultSet result = preStatement.executeQuery();
			if (result.next()) {
				logger_info.info("Found the Migration set with " + migId + "Data will process ");
			} else {
				logger_info.info("Not able to find the data " + migId);
				logger_info.info("Please check Properties");
				System.exit(0);
			}
			int row_counter = 0;
			String mgName = null;
			String mgStatus = null;
			String id_In_Sourcesystem = null;
			String id_In_TargetSystem = null;
			String scanned_Date = null;
			String imported_Date = null;
			String dcctm_obj_Link = null;

			String ObjectName = null;
			String ownerName = null;
			String prsn_title = null;
			String alfresco_FileName = null;
			String alfresco_FolderPath = null;

			System.out.println("Data processing  started..");
			logger_info.info("Data processing  started..");
			while (result.next()) {

				row_counter = row_counter + 1;

				mgName = result.getString(1);
				mgStatus = result.getString(2);
				id_In_Sourcesystem = result.getString(3);
				id_In_TargetSystem = result.getString(4);
				scanned_Date = result.getString(5);
				imported_Date = result.getString(6);
				dcctm_obj_Link = result.getString(7);

				ObjectName = result.getString(8);
				ownerName = result.getString(9);
				prsn_title = result.getString(10);
				alfresco_FileName = result.getString(11);
				alfresco_FolderPath = result.getString(12);

				excel_data_header.put(Integer.toString(row_counter),
						new Object[] { "Mig Name", "Mig Status", "id_In_Sourcesystem", "id_In_TargetSystem",
								"Scanned Date", "Imported Date", "Dctm Obj Link", "Object Name", "Owner Name",
								"Prsn Title", "Alfresco FileName", "Alfresco Folder Path" });
				excel_data.put(Integer.toString(row_counter),
						new Object[] { mgName, mgStatus, id_In_Sourcesystem, id_In_TargetSystem, scanned_Date,
								imported_Date, dcctm_obj_Link, ObjectName, ownerName, prsn_title, alfresco_FileName,
								alfresco_FolderPath });

				System.out.println(row_counter + " record processing");

				// System.out.println("_"+mgName+"_"+mgStatus+"_"+id_In_Sourcesystem+"_"+id_In_TargetSystem+"_"+scanned_Date+"_"+imported_Date+"_"+dcctm_obj_Link+"_"+folderPath+"_"+ObjectName+"_"+ownerName+"_"+prsn_title+"_"+alfresco_FileName+"_"+alfresco_FolderPath);

			}
			// System.out.println(row_counter+" Entries found in the data base
			// for "+mgName);
			logger_info.info(row_counter + " Entries found in the data base for " + mgName);

			/* Load data into logical worksheet */
			Set<String> keyset = excel_data.keySet();
			Set<String> header_keyset = excel_data_header.keySet();

			int rownum = 1;
			// loop through the Headers and add them to the cell
			for (String key : header_keyset) {
				Row row = sheet.createRow(0);
				Object[] objArr = excel_data_header.get(key);
				int cellnum = 0;
				for (Object obj : objArr) {
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof Double)
						cell.setCellValue((Double) obj);
					else
						cell.setCellValue((String) obj);
				}
			}

			// System.out.println("Writing to the excel...");
			logger_info.info("Writing to the excel...");

			// loop through the data and add them to the cell
			for (String key : keyset) {
				Row row = sheet.createRow(rownum++);
				Object[] objArr = excel_data.get(key);
				int cellnum = 0;
				for (Object obj : objArr) {
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof Double)
						cell.setCellValue((Double) obj);
					else
						cell.setCellValue((String) obj);
				}
			}

		} catch (Exception e) {
			// System.out.println("Exception in establish the connection " +
			// e.getMessage());
			logger_info.info("Exception in establish the connection " + e.getMessage());
			logger_error.error("Exception in establish the connection " + e.getMessage());
		}

		try {
			// create
			// XLS
			// file
			FileOutputStream output_file = new FileOutputStream(new File(migId + "_Details.csv"));
			new_workbook.write(output_file);// write excel document to output
											// stream
			output_file.close(); // close the file
		} catch (Exception e) {
			// System.out.println("Exception in writing data to excel file " +
			// e.getMessage());
			logger_info.info("Exception in writing data to excel file " + e.getMessage());
			logger_error.error("Exception in writing data to excel file " + e.getMessage());
			System.exit(0);
		}

		// System.out.println("Details retrived successfully.Please check in "+
		// migId + "_Details.csv");
		logger_info.info("Details retrived successfully.Please check in " + migId + "_Details.csv");

	}
}
