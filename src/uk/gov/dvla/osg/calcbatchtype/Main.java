package uk.gov.dvla.osg.calcbatchtype;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

	private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());
	private static final Properties CONFIG = new Properties();
	
	public static void main(String[] args) {
		LOGGER.info("Composition Engine started");
		String input = "";
		String output = "";
		if(args.length == 0){
			LOGGER.info("Running in DEV mode");
			input = "C:\\Users\\dendlel\\Desktop\\RPD\\batchCalc\\OTHER.TEST.1.DAT";
			output = "C:\\Users\\dendlel\\Desktop\\RPD\\batchCalc\\BATCH_CALC.OUT";
			CONFIG.setProperty("minInputFields", "15");
			CONFIG.setProperty("maxMulti", "24");
			CONFIG.setProperty("maxPerEnv", "5");
			CONFIG.setProperty("createFleet", "Y");
			CONFIG.setProperty("createClerical", "Y");
			CONFIG.setProperty("createMultiple", "Y");
			CONFIG.setProperty("createUncoded", "Y");
			CONFIG.setProperty("ottField", "prop1");
			CONFIG.setProperty("appNameField", "app.field");
			CONFIG.setProperty("fleetField", "fleet.field");
			CONFIG.setProperty("titleField", "title.field");
			CONFIG.setProperty("name1Field", "name1.field");
			CONFIG.setProperty("name2Field", "name2.field");
			CONFIG.setProperty("address1Field", "add1.field");
			CONFIG.setProperty("address2Field", "add2.field");
			CONFIG.setProperty("address3Field", "add3.field");
			CONFIG.setProperty("address4Field", "add4.field");
			CONFIG.setProperty("address5Field", "add5.field");
			CONFIG.setProperty("postcodeField", "Doc.Address.PostalCode");
			CONFIG.setProperty("mscField", "Doc.Address.PostalCode");
			CONFIG.setProperty("resultFieldName", "Doc.Address.DeliveryPoint");
			
		}else{
			LOGGER.info("Running in Production mode");
			input = args[0];
			output = args[1];
			if(new File(args[2]).exists()){
				try {
					CONFIG.load(new FileInputStream(args[2]));
				} catch (IOException e) {
					LOGGER.fatal("Log file didn't load: '{}'",e.getMessage());
					System.exit(1);
				}
			}else{
				LOGGER.fatal("Log file: '{}' doesn't exist",args[2]);
				System.exit(1);
			}
		}
		
		LOGGER.info("Input file is: {}", input);
		ArrayList<DocumentProperties> docProps = new ArrayList<DocumentProperties>();
		int inputSize = 0;
        try {
			String result;
			//Define input csv
			FileReader in = new FileReader(input);
			CSVFormat inputFormat= CSVFormat.RFC4180.withFirstRecordAsHeader();
			
			//Define output csv
			Appendable out = new FileWriter(output);
			CSVFormat outputFormat = CSVFormat.RFC4180.withQuoteMode(QuoteMode.ALL);
			CSVPrinter printer = new CSVPrinter(out, outputFormat);
		
			//Get Headers from csv
			CSVParser csvFileParser = new CSVParser(in, inputFormat);
			Map<String, Integer> headers = csvFileParser.getHeaderMap();
			
			List<String> heads = new ArrayList<String>();
			for(Map.Entry<String,Integer> en : headers.entrySet()){
				heads.add(en.getKey());
			}

			LOGGER.debug(heads);
			String ottField = CONFIG.getProperty("ottField");
			String appField = CONFIG.getProperty("appNameField");
			String fleetField = CONFIG.getProperty("fleetField");
			String titleField = CONFIG.getProperty("titleField");
			String name1Field = CONFIG.getProperty("name1Field");
			String name2Field = CONFIG.getProperty("name2Field");
			String add1Field = CONFIG.getProperty("address1Field");
			String add2Field = CONFIG.getProperty("address2Field");
			String add3Field = CONFIG.getProperty("address3Field");
			String add4Field = CONFIG.getProperty("address4Field");
			String add5Field = CONFIG.getProperty("address5Field");
			String pcField = CONFIG.getProperty("postcodeField");
			String mscField = CONFIG.getProperty("mscField");
			String resultField = CONFIG.getProperty("resultField");
			
			if( !(heads.contains(ottField)) ){
				LOGGER.fatal("'{}' is not a field in input file '{}'",ottField, input);
				System.exit(1);
			}
			if( !(heads.contains(appField)) ){
				LOGGER.fatal("'{}' is not a field in input file '{}'",appField, input);
				System.exit(1);
			}
			if( !(heads.contains(fleetField)) ){
				LOGGER.fatal("'{}' is not a field in input file '{}'",fleetField, input);
				System.exit(1);
			}
			if( !(heads.contains(titleField)) ){
				LOGGER.fatal("'{}' is not a field in input file '{}'",titleField, input);
				System.exit(1);
			}
			if( !(heads.contains(name1Field)) ){
				LOGGER.fatal("'{}' is not a field in input file '{}'",name1Field, input);
				System.exit(1);
			}
			if( !(heads.contains(name2Field)) ){
				LOGGER.fatal("'{}' is not a field in input file '{}'",name2Field, input);
				System.exit(1);
			}
			if( !(heads.contains(add1Field)) ){
				LOGGER.fatal("'{}' is not a field in input file '{}'",add1Field, input);
				System.exit(1);
			}
			if( !(heads.contains(add2Field)) ){
				LOGGER.fatal("'{}' is not a field in input file '{}'",add2Field, input);
				System.exit(1);
			}
			if( !(heads.contains(add3Field)) ){
				LOGGER.fatal("'{}' is not a field in input file '{}'",add3Field, input);
				System.exit(1);
			}
			if( !(heads.contains(add4Field)) ){
				LOGGER.fatal("'{}' is not a field in input file '{}'",add4Field, input);
				System.exit(1);
			}
			if( !(heads.contains(add5Field)) ){
				LOGGER.fatal("'{}' is not a field in input file '{}'",add5Field, input);
				System.exit(1);
			}
			if( !(heads.contains(pcField)) ){
				LOGGER.fatal("'{}' is not a field in input file '{}'",pcField, input);
				System.exit(1);
			}
			if( !(heads.contains(mscField)) ){
				LOGGER.fatal("'{}' is not a field in input file '{}'",mscField, input);
				System.exit(1);
			}
			if( !(heads.contains(resultField)) ){
				LOGGER.fatal("'{}' is not a field in input file '{}'",resultField, input);
				System.exit(1);
			}
			
			//Write headers out
			printer.printRecord(heads);
			
			Iterable<CSVRecord> records = csvFileParser.getRecords();
			for (CSVRecord record : records) {
				DocumentProperties dp = new DocumentProperties(
						record.get(ottField),
						record.get(appField),
						record.get(fleetField),
						record.get(titleField),
						record.get(name1Field),
						record.get(name2Field),
						record.get(add1Field),
						record.get(add2Field),
						record.get(add3Field),
						record.get(add4Field),
						record.get(add5Field),
						record.get(pcField),
						record.get(mscField));
				
				docProps.add(dp);
			}
			inputSize = docProps.size();


	        LOGGER.info("{} record(s) added to array", docProps.size());
			
			CalculateBatchTypes cbt = new CalculateBatchTypes(docProps);
			
			/*
			int noOfFleets = cbt.getFleets().size();
			int noOfMultiples = cbt.getMultiples().size();
			int noOfUncoded = cbt.getUncoded().size();
			int noOfCoded = cbt.getCoded().size();
			int totalBatched = noOfFleets + noOfMultiples + noOfUncoded + noOfCoded;
			
			
			LOGGER.info("Number of {} = {}","FLEETS",noOfFleets);
			LOGGER.info("Number of {} = {}","MULTIS",noOfMultiples);
			LOGGER.info("Number of {} = {}","UNCODED",noOfUncoded);
			LOGGER.info("Number of {} = {}","CODED",noOfCoded);
			LOGGER.info("Total input = {}. Total batched = {}",inputSize, totalBatched); 
			*/
			
			ArrayList<DocumentProperties> results = cbt.getResults();
			
			if(results.size() != inputSize){
				LOGGER.fatal("Calc batch type output volume '{}', doesn't match input volume '{}'",results.size(),inputSize);
				System.exit(1);
			}
			
			int i = 0;
			List<String> resultArray = new ArrayList<String>();
			String fieldVal;
			for (CSVRecord record : records) {
				for(String field : heads){
					if(field.equalsIgnoreCase(resultField)){
					    fieldVal=results.get(i).getBatchType();
					}else{
						fieldVal = record.get(field);
					}
					resultArray.add(fieldVal);
				}
				LOGGER.info("BT='{}'",results.get(i).getBatchType());
			    printer.printRecord(resultArray);
			    resultArray.clear();
			    i ++;
			}
			csvFileParser.close();
			printer.close();
		
        } catch (IOException e) {
			LOGGER.fatal(e.getMessage());
			System.exit(1);
		}
        
	}

}
