package converter.servlet;

import org.apache.commons.io.FileCleaner;
import util.openoffice.OpenOfficeUtil;
import util.verapdf.VeraPDFUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyServletContextListener implements ServletContextListener {
    private static final String PROPERTIES_VARIABLE_NAME = "LOGIUS_PROPERTIES";
    private static Properties webProperties;

    public static Properties getWebProperties() {
        return webProperties;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        webProperties = new Properties();
        String propertiesFile = System.getenv(PROPERTIES_VARIABLE_NAME);
        if (propertiesFile == null) {
            throw new RuntimeException(
                    String.format("Environment variable %s is not defined!", PROPERTIES_VARIABLE_NAME));
        }
        try {
            webProperties.load(new FileInputStream(propertiesFile));
//            VeraPDFUtil.initialize(webProperties.getProperty("veraPDF"));
			OpenOfficeUtil.officeDirectory = webProperties.getProperty("officeDir");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        FileCleaner.exitWhenFinished();
    }
}
