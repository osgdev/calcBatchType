package uk.gov.dvla.osg.calcbatchtype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
		Set<DocumentProperties> multiCustomers = new HashSet<DocumentProperties>();

		for(DocumentProperties prop : docProps){
			if( !(uniqueCustomers.add(prop)) ){
				multiCustomers.add(prop);
			}
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
			} else if ("CLERICAL".equals(dp.getBatchType())){
				dp.setBatchType("CLERICAL");
			} else if( multis.contains(dp) ) {
				dp.setBatchType("MULTI");
			} else if ( dp.getMsc().isEmpty() ){
				dp.setBatchType("UNCODED");
			} else {
				dp.setBatchType("CODED");
			}
			result.add(dp);
		}

		return result;
	}
	
}
