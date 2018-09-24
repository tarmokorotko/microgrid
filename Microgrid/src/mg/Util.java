package mg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;



public final class Util {
	// System constants
	// Logging
	final static String logFilePath = "C:/Users/Tarmo/Documents/Microgrid_sim/log.txt";
	final static Boolean appendLog = true;
	final static Boolean logToFile = true;
	// Prosumer
	public final static String[] prosumerRoles = {"Island", "Vendor", "Purchaser", "Distributor"};
	
	// Experiment
	public final static int auctionCycle = 5000; // Auction cycle time in ms
	public final static String experimentDataFilePath = "C:/Users/Tarmo/OneDrive - TTU 2/Doktoritöö/SimulationInput/Experiment_case_4.xlsx";
	public final static int firstDataRow = 5;
	public final static String firstDataCol = "V";
	public final static int nrOfDataRows = 24;
	public final static int maxBidsInSet = 8;
	
	
	/**
	 * Method for program initialization
	 */
	public static void initialize() {
		/**
		 * Logger initialization
		 */
		// Set up logger
		Logger logger = Logger.getLogger("microgridLogger");
		logger.setLevel(Level.ALL);
		System.setProperty("java.util.logging.SimpleFormatter.format", "(%4$-6s)	[%1$tQ]	[%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL]	%5$s%6$s.%n");
		
		try {
			// Log to existing file or create new log file
			String logPath;
			if (appendLog) {
				logPath = logFilePath;
			} else {
			    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
				Date date = new Date();
				logPath = logFilePath.replace("log", String.format("%s", dateFormat.format(date)));
			}
			
			// Initialize handlers and add to logger
			FileHandler fh = new FileHandler(logPath, appendLog); 
			fh.setLevel(Level.ALL);
	        logger.addHandler(fh);
	        SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);  
	        
	    } catch (SecurityException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }  
	} //end method initialize
	
	/**
	 * Method for output logging
	 * @param s - Event text to log
	 * @param lev - logging level (40 = SEVERE, 30 = WARNING, 20 = INFO, 10 = CONFIG)
	 * @param logToConsole - Output text to java console
	 */
	public static void logString(String s, Object... varArg) {
		Integer lev = 20;
		Boolean logToConsole = true;
		
		Logger logger = Logger.getLogger("microgridLogger");
		
		
		
		// Handle inputs
		if (varArg.length > 0) {
	      if (!(varArg[0] instanceof Integer)) { 
	          throw new IllegalArgumentException("Passed argument 2 not type of Integer");
	      }
	      lev = (Integer)varArg[0];
	    }
	    if (varArg.length > 1) {
	        if (!(varArg[1] instanceof Boolean)) { 
	            throw new IllegalArgumentException("Passed argument 3 not type of Boolean");
	        }
	        logToConsole = (Boolean)varArg[1];
	    }
	    
	    // Log to console
	    logger.setUseParentHandlers(logToConsole);
		
		// Set log level
		Level logLevel;	
		
		switch(lev) {
		case 40:
			logLevel = Level.SEVERE;
			break;
		case 30:
			logLevel = Level.WARNING;
			break;
		case 10:
			logLevel = Level.CONFIG;
			break;
		default:
			logLevel = Level.INFO;	
			break;
		}
		
		// Log entry		
	    logger.log(logLevel, s);			
					
	} // end method logString

	/**
	 * Read data from excel file
	 * @param filePath - full path to excel file
	 * @param sheetName - name of sheet
	 * @param cells - List of cell values
	 * @return - returns list of strings containing read data
	 * @throws IOException
	 */
	public static List<String> readFromExcel(String filePath, String sheetName, List<int[]> cells) throws IOException {
		List<String> outData = new ArrayList<String>();
		File excelFile = new File(filePath);
		FileInputStream fis = new FileInputStream(excelFile);		
		XSSFWorkbook wb = new XSSFWorkbook(fis);
		XSSFSheet sheet = wb.getSheet(sheetName);
		
		for (int i=0;i<cells.size();i++) {
			int[] indexes = cells.get(i);
			int rowIndex = indexes[0];
			int colIndex = indexes[1];
			
			XSSFRow row = sheet.getRow(rowIndex);
			XSSFCell cell = row.getCell(colIndex);
			CellType inCellType = cell.getCellType();
			String s = "N/A";
			
			if (inCellType == CellType.STRING) {
				s = cell.getStringCellValue();
			} else if (inCellType == CellType.NUMERIC) {
				Double a = cell.getNumericCellValue();
				s = String.valueOf(a);
			} else if (inCellType == CellType.FORMULA) {
				Double a = cell.getNumericCellValue();
				s = String.valueOf(a);
			} else if (inCellType == CellType.BLANK) {
				s = "";
			}			
			outData.add(s);					
		}		
		wb.close();
		fis.close();		
		return outData;
	}
	
	public static int getCharPosition(char inC) {
		int chCode = (int)inC;
		int pos = 0;
		if (Character.isUpperCase(inC)) {
			int start = 64; //for upper case
			pos = chCode-start;
		} else {
			int start = 96; //for lower case
			pos = chCode-start;
		}
		
		return pos;
	}
}