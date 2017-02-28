package uk.gov.dvla.osg.calcbatchtype;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.common.classes.ProductionConfiguration;
import uk.gov.dvla.osg.common.classes.RpdFileHandler;
import uk.gov.dvla.osg.common.classes.SelectorLookup;

public class Main {

	private static final Logger LOGGER = LogManager.getLogger();
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
		
		RpdFileHandler fh = new RpdFileHandler(input,output);
		
		HashMap<String,Integer> fileMap = fh.getMapping();
		
		List<String> heads = fh.getHeaders();
		
		
		List<String> reqFields = new ArrayList<String>();
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
		
		ArrayList<DocumentProperties> docProps = new ArrayList<DocumentProperties>();
		int inputSize = 0;
        try {
			//Write headers out
			fh.write(fh.getHeaders());
			SelectorLookup lookup = null;
			ProductionConfiguration pc = null;
			boolean firstCustomer = true;
			String record ="";
			
			
			File f = new File(input);

            BufferedReader b = new BufferedReader(new FileReader(f));

            String readLine = b.readLine();
            
            LOGGER.debug("Read line as header '{}'",readLine);

            while ((readLine = b.readLine()) != null) {
            	String[] split = readLine.split("\\t",-1);
            	DocumentProperties dp = null;
            	
				if(firstCustomer){
					if( new File(lookupFile).exists() ){
						lookup = new SelectorLookup(lookupFile, CONFIG);
						
						pc = new ProductionConfiguration(productionConfigPath + lookup.get(split[fileMap.get(selectorRef)]).getProductionConfig() + productionFileSuffix);
						//pc = new ProductionConfiguration(productionConfigPath + lookup.get(record.get(selectorRef)).getProductionConfig() + productionFileSuffix);
					}else{
						LOGGER.fatal("File '{}' doesn't exist.",lookupFile);
						System.exit(1);
					}
					firstCustomer=false;
				}
				
					dp = new DocumentProperties(
							split[fileMap.get(selectorRef)],
							split[fileMap.get(docRef)],
							split[fileMap.get(ottField)],
							split[fileMap.get(appField)],
							split[fileMap.get(fleetField)],
							split[fileMap.get(titleField)],
							split[fileMap.get(name1Field)],
							split[fileMap.get(name2Field)],
							split[fileMap.get(add1Field)],
							split[fileMap.get(add2Field)],
							split[fileMap.get(add3Field)],
							split[fileMap.get(add4Field)],
							split[fileMap.get(add5Field)],
							split[fileMap.get(pcField)],
							split[fileMap.get(mscField)],
							split[fileMap.get(langField)]);
					
					
					if( !(split[fileMap.get(batchType)].isEmpty()) ){
						dp.setBatchType(split[fileMap.get(batchType)]);
					}
					docProps.add(dp);
			}
            b.close();
			inputSize = docProps.size();

	        LOGGER.info("{} record(s) added to array", docProps.size());
			
			CalculateBatchTypes cbt = new CalculateBatchTypes(docProps,maxMulti, lookup,pc);
			
			ArrayList<DocumentProperties> results = cbt.getResults();
			
			if(results.size() != inputSize){
				LOGGER.fatal("Calc batch type output volume '{}', doesn't match input volume '{}'",results.size(),inputSize);
				System.exit(1);
			}
			
			int i = 0;
			int batchTypeIdx=fileMap.get(batchType);
			int groupIdIdx=fileMap.get(groupIdField);
			
			//Write results
			BufferedReader bu = new BufferedReader(new FileReader(f));
			List<String> list = new ArrayList<String>();
			String result= bu.readLine();
			while ((readLine = bu.readLine()) != null) {
				String[] split = readLine.split("\\t",-1);
				list.clear();
				for( int x = 0; x < split.length; x ++ ){
					if( x == batchTypeIdx ){
						list.add(results.get(i).getBatchType());
					} else if( x == groupIdIdx ){
						if( results.get(i).getGroupId() != null){
							list.add("" + results.get(i).getGroupId());
						}else{
							list.add("");
						}
					} else {
						list.add(split[x]);
					}
				}
				fh.write(list);
				i++;
			}
			fh.closeFile();
			
			
		
        } catch (IOException e) {
			LOGGER.fatal(e.getMessage());
			System.exit(1);
		}
	}
}
