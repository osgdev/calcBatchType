package uk.gov.dvla.osg.calcbatchtype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.common.classes.ProductionConfiguration;
import uk.gov.dvla.osg.common.classes.SelectorLookup;

public class CalculateBatchTypes {
	private ArrayList<DocumentProperties> docProps;
	private SelectorLookup lookup;
	private int maxMulti;
	private ProductionConfiguration pc;
	
	private static final Logger LOGGER = LogManager.getLogger(CalculateBatchTypes.class.getName());
	
	public CalculateBatchTypes(ArrayList<DocumentProperties> docProps, int maxMulti, SelectorLookup lookup, ProductionConfiguration pc){
		this.lookup = lookup;
		this.docProps = docProps;
		this.maxMulti = maxMulti;
		this.pc = pc;
		LOGGER.info("CalculateBatchTypes initiated");
	}
	
	public ArrayList<DocumentProperties> getResults(){

		Iterator<DocumentProperties> it = docProps.iterator();
		ArrayList<DocumentProperties> result = new ArrayList<DocumentProperties>();

		Set<DocumentProperties> uniqueCustomers = new HashSet<DocumentProperties>();
		Set<String> uniqueFleets = new HashSet<String>();
		Set<DocumentProperties> multiCustomers = new HashSet<DocumentProperties>();
		Map<DocumentProperties,Integer> multiMap = new HashMap<DocumentProperties,Integer>();
		Map<String,Integer> fleetMap = new HashMap<String,Integer>();

		
		for(DocumentProperties prop : docProps){
			if( !(uniqueCustomers.add(prop)) ){
				multiCustomers.add(prop);
			}
			if(!("".equals(prop.getFleetNo().trim()))){
				uniqueFleets.add(prop.getFleetNo() + prop.getLang());
			}
		}
		int i = 0;
		for(DocumentProperties prop : multiCustomers){
			if("".equals(prop.getFleetNo().trim())){
				multiMap.put(prop, i);
				LOGGER.info("Added {} to map with ID {}. Map size now {}",prop.getDocRef(),i,multiMap.size());
				i ++;
			}
		}
		
		for(String fleet : uniqueFleets){
			fleetMap.put(fleet, i);
			LOGGER.info("Added {} to map with ID {}. Map size now {}",fleet,i,fleetMap.size());
			i ++;
		}
		
		
		int occurrences = 0;
		for(DocumentProperties prop : multiCustomers){
			if((("E".equalsIgnoreCase(prop.getLang()) ) && !("x".equalsIgnoreCase(pc.getEnglishClerical()))) ||
				(("W".equalsIgnoreCase(prop.getLang()) ) && !("x".equalsIgnoreCase(pc.getWelshClerical())))){
				occurrences = Collections.frequency(docProps, prop);
				if(occurrences > maxMulti){
					//Change batch type to CLERICAL
					for (DocumentProperties customer : docProps){
						if (customer.equals(prop)) {
							customer.setBatchType("CLERICAL");
						}
					}
				}
			}
			
		}
		
		ArrayList<DocumentProperties> multis = new ArrayList<DocumentProperties>(multiCustomers);
		
		while (it.hasNext()) {
			DocumentProperties dp = it.next();
			if( (dp.getBatchType() == null) ){
				if( "E".equalsIgnoreCase(dp.getLang()) ){
					if( !("".equals(dp.getFleetNo().trim())) && !("x".equalsIgnoreCase( pc.getEnglishFleet() )) ){
						dp.setBatchType("FLEET");
						dp.setGroupId(fleetMap.get(dp.getFleetNo() + dp.getLang()));
					} else if ( "CLERICAL".equals(dp.getBatchType()) && !("x".equalsIgnoreCase( pc.getEnglishClerical() )) ){
						dp.setBatchType("CLERICAL");
						dp.setGroupId(multiMap.get(dp));
					} else if( multis.contains(dp) && !( (pc.getEnglishMulti().contains("X") || pc.getEnglishMulti().contains("x")) )) {
						dp.setBatchType("MULTI");
						dp.setGroupId(multiMap.get(dp));
					} else if ( multis.contains(dp) && ( "x".equalsIgnoreCase(pc.getEnglishMulti())) ){
						dp.setGroupId(multiMap.get(dp));
					} else if ( dp.getMsc().isEmpty() && !("x".equalsIgnoreCase(pc.getEnglishUnsorted())) ){
						dp.setBatchType("UNSORTED");
					} else if ( !("x".equalsIgnoreCase(pc.getEnglishSorted())) ) {
						dp.setBatchType("SORTED");
					}else{
						dp.setBatchType("UNSORTED");
					}
				}else{
					if( !("".equals(dp.getFleetNo().trim())) && !("x".equalsIgnoreCase( pc.getWelshFleet() )) ){
						dp.setBatchType("FLEET");
						dp.setGroupId(fleetMap.get(dp.getFleetNo() + dp.getLang()));
					} else if ( "CLERICAL".equals(dp.getBatchType()) && !("x".equalsIgnoreCase( pc.getWelshClerical() )) ){
						dp.setBatchType("CLERICAL");
						dp.setGroupId(multiMap.get(dp));
					} else if( multis.contains(dp) && !( (pc.getWelshMulti().contains("X") || pc.getWelshMulti().contains("x")) )) {
						dp.setBatchType("MULTI");
						dp.setGroupId(multiMap.get(dp));
					} else if ( multis.contains(dp) && ( "x".equalsIgnoreCase(pc.getWelshMulti())) ){
						dp.setGroupId(multiMap.get(dp));
					} else if ( dp.getMsc().isEmpty() && !("x".equalsIgnoreCase(pc.getWelshUnsorted())) ){
						dp.setBatchType("UNSORTED");
					} else if ( !("x".equalsIgnoreCase(pc.getWelshSorted())) ) {
						dp.setBatchType("SORTED");
					}else{
						dp.setBatchType("UNSORTED");
					}
				}
			}

			result.add(dp);
		}

		return result;
	}
	
}
