package uk.gov.dvla.osg.calcbatchtype;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class AppConfig {
	private Properties prop;
	private FileInputStream input;
	private OutputStream output;
	private String propPath ="config.properties";
	
	public AppConfig(){
		prop = new Properties();
	}
	
	public void writeProperty(String property, String val){
		try {
			output = new FileOutputStream(propPath);
			prop.setProperty(property, val);
			prop.store(output, null);
			output.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public String getProperty(String property){
		String result = "";
		try {
			input = new FileInputStream(propPath);
			prop.load(input);
			if(prop.getProperty(property) != null){
				result = prop.getProperty(property);
			}
			input.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		return result;
	}
}