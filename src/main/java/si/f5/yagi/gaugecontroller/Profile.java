package si.f5.yagi.gaugecontroller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import si.f5.yagi.gaugecontroller.gauge.Unit;


public class Profile {

    private static final Path PATH = Paths.get("gauge.properties");
    private static final Properties properties;
    
    private static final String PORT_KEY = "port";
    private static final String UNIT_KEY = "unit";

    private Profile() throws Exception {
    }

    static {
        properties = new Properties();
        try {
        	
        	if (Files.notExists(PATH)) {
        		Files.createFile(PATH);
        	}
        	
            properties.load(Files.newBufferedReader(PATH, StandardCharsets.UTF_8));
            
        } catch (IOException e) {
			e.printStackTrace();
            System.out.println("failed to read property: " + PATH);
        }
    }
    
    public static void store() {
    	try {
			properties.store(Files.newBufferedWriter(PATH, StandardCharsets.UTF_8), null);
		} catch (IOException e) {
			e.printStackTrace();
            System.out.println("failed to write property: " + PATH);
		}
    }

    private static void setProperty(final String key, final String value) {
    	properties.put(key, value);
    }

    private static String getProperty(final String key, final String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    
    public static void setPort(String portName) {
    	setProperty(PORT_KEY, portName);
    	store();
    }
    
    public static String getPort() {
    	return getProperty(PORT_KEY, null);
    }
    
    public static void setUnit(Unit unit) {
    	setProperty(UNIT_KEY, unit.name());
    	store();
    }
    
    public static Unit getUnit() {
    	return Enum.valueOf(Unit.class, getProperty(UNIT_KEY, Unit.PASCAL.name()));
    }
    
    
}