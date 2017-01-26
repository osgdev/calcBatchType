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

public class CalculateBatchTypes {
	private ArrayList<DocumentProperties> docProps;
	private int maxMulti;
	
	private static final Logger LOGGER = LogManager.getLogger(CalculateBatchTypes.class.getName());
	
	public CalculateBatchTypes(ArrayList<DocumentProperties> docProps, int maxMulti){
		this.docProps = docProps;
		this.maxMulti = maxMulti;
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
		
		ArrayList<DocumentProperties> multis = new ArrayList<DocumentProperties>(multiCustomers);
		
		while (it.hasNext()) {
			DocumentProperties dp = it.next();
			if(!("".equals(dp.getFleetNo().trim()))){
				dp.setBatchType("FLEET");
				dp.setGroupId(fleetMap.get(dp.getFleetNo() + dp.getLang()));
			} else if ("CLERICAL".equals(dp.getBatchType())){
				dp.setBatchType("CLERICAL");
				dp.setGroupId(multiMap.get(dp));
			} else if( multis.contains(dp) ) {
				dp.setBatchType("MULTI");
				dp.setGroupId(multiMap.get(dp));
			} else if ( dp.getMsc().isEmpty() ){
				dp.setBatchType("UNCODED");
				//dp.setGroupId(i);
				//i ++;
			} else {
				dp.setBatchType("CODED");
				//dp.setGroupId(i);
				//i ++;
			}
			result.add(dp);
		}

		return result;
	}
	
}
