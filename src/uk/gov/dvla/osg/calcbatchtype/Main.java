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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.common.classes.ProductionConfiguration;
import uk.gov.dvla.osg.common.classes.DocPropField;
import uk.gov.dvla.osg.common.classes.RpdFileHandler;
import uk.gov.dvla.osg.common.classes.SelectorLookup;

public class Main {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Properties CONFIG = new Properties();
	private static final int EXPECTED_NUMBER_OF_ARGS = 3;
	
	static List<String> headerRecords;
	
	private static String input;
	private static String output;
	private static String propsFile;
	
	private static String lookupFile, selectorRef, ottField, appField, fleetField, titleField, name1Field,
	name2Field, add1Field, add2Field, add3Field, add4Field, add5Field, pcField, mscField, batchType, docRef,
	groupIdField, langField, presentationPriorityConfigPath, presentationPriorityFileSuffix, productionConfigPath,
	productionFileSuffix, postageConfigPath, postageFileSuffix, outputBatchType;
	
	private static int maxMulti;
	
	public static void main(String[] args) {
		LOGGER.info("calcBatchType started");

		validateArgs(args);
		
		input = args[0];
		output = args[1];
		propsFile = args[2];
		loadConfigFile(propsFile);
		assignPropsFromPropsFile();
		RpdFileHandler fh = new RpdFileHandler(input,output);
		headerRecords = fh.getHeaders();
		ensureRequiredPropsAreSet(headerRecords);
		
		HashMap<String,Integer> fileMap = fh.getMapping();
		

		ArrayList<DocumentProperties> docProps = new ArrayList<DocumentProperties>();
		int inputSize = 0;
        try {
			fh.write(headerRecords);
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
			int batchTypeIdx=fileMap.get(outputBatchType);
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
			bu.close();
			
		
        } catch (IOException e) {
			LOGGER.fatal(e.getMessage());
			System.exit(1);
		}
	}
	
	private static void validateArgs(String[] args) {
		if(args.length != 3){
			LOGGER.fatal("Incorrect number of args parsed {} expected 3",args.length);
			System.exit(1);
		}
	}

	private static void loadConfigFile(String filePath) {
		if(new File(filePath).exists()){
			try {
				CONFIG.load(new FileInputStream(filePath));
			} catch (IOException e) {
				LOGGER.fatal("Log file didn't load: '{}'",e.getMessage());
				System.exit(1);
			}
		}else{
			LOGGER.fatal("Log file: '{}' doesn't exist",filePath);
			System.exit(1);
		}
	}
	private static void assignPropsFromPropsFile() {
		lookupFile = CONFIG.getProperty("lookupFile");
		selectorRef = CONFIG.getProperty("lookupReferenceFieldName");
		ottField = CONFIG.getProperty("ottField");
		appField = CONFIG.getProperty("appNameField");
		fleetField = CONFIG.getProperty("fleetField");
		titleField = CONFIG.getProperty("titleField");
		name1Field = CONFIG.getProperty("name1Field");
		name2Field = CONFIG.getProperty("name2Field");
		add1Field = CONFIG.getProperty("address1Field");
		add2Field = CONFIG.getProperty("address2Field");
		add3Field = CONFIG.getProperty("address3Field");
		add4Field = CONFIG.getProperty("address4Field");
		add5Field = CONFIG.getProperty("address5Field");
		pcField = CONFIG.getProperty("postcodeField");
		mscField = CONFIG.getProperty("mscField");
		batchType = CONFIG.getProperty("batchType");
		docRef = CONFIG.getProperty("documentReference");
		groupIdField = CONFIG.getProperty("groupIdField");
		langField = CONFIG.getProperty("langField");
		maxMulti = Integer.parseInt(CONFIG.getProperty("maxMulti"));
		presentationPriorityConfigPath = CONFIG.getProperty("presentationPriorityConfigPath");
		presentationPriorityFileSuffix = CONFIG.getProperty("presentationPriorityFileSuffix");
		productionConfigPath = CONFIG.getProperty("productionConfigPath");
		productionFileSuffix = CONFIG.getProperty("productionFileSuffix");
		postageConfigPath = CONFIG.getProperty("postageConfigPath");
		postageFileSuffix = CONFIG.getProperty("postageFileSuffix");
		outputBatchType = CONFIG.getProperty("outputBatchType");
	}
	
	
	private static void ensureRequiredPropsAreSet(List<String> headers) {
		
		//reqFields is used to validate input, the Y signifies that the field should be present in the input file
		List<DocPropField> reqFields = new ArrayList<DocPropField>();
		reqFields.add(new DocPropField(lookupFile, "lookupFile", false));
		reqFields.add(new DocPropField(selectorRef, "lookupReferenceFieldName", true));
		reqFields.add(new DocPropField(ottField, "ottField", true));
		reqFields.add(new DocPropField(appField, "appNameField", true));
		reqFields.add(new DocPropField(fleetField, "fleetField", true));
		reqFields.add(new DocPropField(titleField, "titleField", true));
		reqFields.add(new DocPropField(name1Field, "name1Field", true));
		reqFields.add(new DocPropField(name2Field, "name2Field", true));
		reqFields.add(new DocPropField(add1Field, "address1Field", true));
		reqFields.add(new DocPropField(add2Field, "address2Field", true));
		reqFields.add(new DocPropField(add3Field, "address3Field", true));
		reqFields.add(new DocPropField(add4Field, "address4Field", true));
		reqFields.add(new DocPropField(add5Field, "address5Field", true));
		reqFields.add(new DocPropField(pcField, "postCodeField", true));
		reqFields.add(new DocPropField(mscField, "mscField", true));
		reqFields.add(new DocPropField(batchType, "batchType", true));
		reqFields.add(new DocPropField(outputBatchType, "outputBatchType", true));
		reqFields.add(new DocPropField(docRef, "documentReference", true));
		reqFields.add(new DocPropField(groupIdField, "groupIdField", false));
		reqFields.add(new DocPropField(langField, "langField", true));
		reqFields.add(new DocPropField("" + maxMulti, "maxMulti", false));
		reqFields.add(new DocPropField(presentationPriorityConfigPath, "presentationPriorityConfigPath", false));
		reqFields.add(new DocPropField(presentationPriorityFileSuffix, "presentationPriorityFileSuffix", false));
		reqFields.add(new DocPropField(productionConfigPath, "productionConfigPath", false));
		reqFields.add(new DocPropField(productionFileSuffix, "productionFileSuffix", false));
		reqFields.add(new DocPropField(postageConfigPath, "postageConfigPath", false));
		reqFields.add(new DocPropField(postageFileSuffix, "postageFileSuffix", false));
		
		for(DocPropField requiredField : reqFields){

			if ( requiredField.getAttibuteValue() == null || "null".equals(requiredField.getAttibuteValue())){
				LOGGER.fatal("Field '{}' not in properties file {}.",requiredField.getAttibuteName(), propsFile);
				System.exit(1);
			}else{
				if( !(headers.contains(requiredField.getAttibuteValue())) && requiredField.isRequiredInInputFile() ){
					LOGGER.fatal("Field '{}' not found in input file {}.",requiredField.getAttibuteValue(),input);
					System.exit(1);
				}
			}
		}
	}
	
}
