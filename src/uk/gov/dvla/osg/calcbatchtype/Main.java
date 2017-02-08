package uk.gov.dvla.osg.calcbatchtype;

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

import uk.gov.dvla.osg.common.classes.ProductionConfiguration;
import uk.gov.dvla.osg.common.classes.SelectorLookup;

public class Main {

	private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());
	private static final Properties CONFIG = new Properties();
	
	public static void main(String[] args) {
		LOGGER.info("calcBatchType started");
		String input = "";
		String output = "";
		if(args.length != 3){
			LOGGER.fatal("Incorrect number of args parsed {} expected 3",args.length);
			System.exit(1);
		}else{
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

			List<String> reqFields = new ArrayList<String>();
			LOGGER.debug(heads);
			
			String lookupFile = CONFIG.getProperty("lookupFile");
			reqFields.add(lookupFile + ",lookupFile,N");
			String selectorRef = CONFIG.getProperty("lookupReferenceFieldName");
			reqFields.add(selectorRef + ",lookupReferenceFieldName,Y");
			String ottField = CONFIG.getProperty("ottField");
			reqFields.add(ottField + ",ottField,Y");
			String appField = CONFIG.getProperty("appNameField");
			reqFields.add(appField + ",appNameField,Y");
			String fleetField = CONFIG.getProperty("fleetField");
			reqFields.add(fleetField + ",fleetField,Y");
			String titleField = CONFIG.getProperty("titleField");
			reqFields.add(titleField + ",titleField,Y");
			String name1Field = CONFIG.getProperty("name1Field");
			reqFields.add(name1Field + ",name1Field,Y");
			String name2Field = CONFIG.getProperty("name2Field");
			reqFields.add(name2Field + ",name2Field,Y");
			String add1Field = CONFIG.getProperty("address1Field");
			reqFields.add(add1Field + ",address1Field,Y");
			String add2Field = CONFIG.getProperty("address2Field");
			reqFields.add(add2Field + ",address2Field,Y");
			String add3Field = CONFIG.getProperty("address3Field");
			reqFields.add(add3Field + ",address3Field,Y");
			String add4Field = CONFIG.getProperty("address4Field");
			reqFields.add(add4Field + ",address4Field,Y");
			String add5Field = CONFIG.getProperty("address5Field");
			reqFields.add(add5Field + ",address5Field,Y");
			String pcField = CONFIG.getProperty("postcodeField");
			reqFields.add(pcField + ",postcodeField,Y");
			String mscField = CONFIG.getProperty("mscField");
			reqFields.add(mscField + ",mscField,Y");
			String batchType = CONFIG.getProperty("batchType");
			reqFields.add(batchType + ",batchType,Y");
			String docRef = CONFIG.getProperty("documentReference");
			reqFields.add(docRef + ",documentReference,Y");
			String groupIdField = CONFIG.getProperty("groupIdField");
			reqFields.add(groupIdField + ",groupIdField,N");
			String langField = CONFIG.getProperty("langField");
			reqFields.add(langField + ",langField,Y");
			int maxMulti = Integer.parseInt(CONFIG.getProperty("maxMulti"));
			reqFields.add("" +maxMulti + ",maxMulti,N");
			String presentationPriorityConfigPath = CONFIG.getProperty("presentationPriorityConfigPath");
			reqFields.add(presentationPriorityConfigPath + ",presentationPriorityConfigPath,N");
			String presentationPriorityFileSuffix = CONFIG.getProperty("presentationPriorityFileSuffix");
			reqFields.add(presentationPriorityFileSuffix + ",presentationPriorityFileSuffix,N");
			String productionConfigPath = CONFIG.getProperty("productionConfigPath");
			reqFields.add(productionConfigPath + ",productionConfigPath,N");
			String productionFileSuffix = CONFIG.getProperty("productionFileSuffix");
			reqFields.add(productionFileSuffix + ",productionFileSuffix,N");
			String postageConfigPath = CONFIG.getProperty("postageConfigPath");
			reqFields.add(postageConfigPath + ",postageConfigPath,N");
			String postageFileSuffix = CONFIG.getProperty("postageFileSuffix");
			reqFields.add(postageFileSuffix + ",postageFileSuffix,N");
			
			for(String str : reqFields){
				String[] split = str.split(",");
				if ( "null".equals(split[0])){
					LOGGER.fatal("Field '{}' not in properties file {}.",split[1],args[2]);
					System.exit(1);
				}else{
					if( !(heads.contains(split[0])) && "Y".equals(split[2]) ){
						LOGGER.fatal("Field '{}' not found in input file {}.",split[0],input);
						System.exit(1);
					}
				}
			}
			
			//Write headers out
			printer.printRecord(docRef,batchType,groupIdField);
			
			SelectorLookup lookup = null;
			ProductionConfiguration pc = null;
			Iterable<CSVRecord> records = csvFileParser.getRecords();
			boolean firstCustomer = true;
			for (CSVRecord record : records) {
				if(firstCustomer){
					if( new File(lookupFile).exists() ){
						
						lookup = new SelectorLookup(lookupFile, CONFIG);
						pc = new ProductionConfiguration(productionConfigPath + lookup.get(record.get(selectorRef)).getProductionConfig() + productionFileSuffix);
					}else{
						LOGGER.fatal("File '{}' doesn't exist.",lookupFile);
						System.exit(1);
					}
					firstCustomer=false;
				}
				DocumentProperties dp = new DocumentProperties(
						record.get(selectorRef),
						record.get(docRef),
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
						record.get(mscField),
						record.get(langField));
				
				if( !(record.get(batchType).isEmpty()) ){
					dp.setBatchType(record.get(batchType));
				}
				
				docProps.add(dp);
			}
			inputSize = docProps.size();

	        LOGGER.info("{} record(s) added to array", docProps.size());
			
			CalculateBatchTypes cbt = new CalculateBatchTypes(docProps,maxMulti, lookup,pc);
			
			ArrayList<DocumentProperties> results = cbt.getResults();
			
			if(results.size() != inputSize){
				LOGGER.fatal("Calc batch type output volume '{}', doesn't match input volume '{}'",results.size(),inputSize);
				System.exit(1);
			}
			
			int i = 0;
			for (CSVRecord record : records) {
				printer.printRecord(record.get(docRef),results.get(i).getBatchType(), results.get(i).getGroupId());
			
				LOGGER.debug("BT='{}' GROUP='{}'",results.get(i).getBatchType(),results.get(i).getGroupId());

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
