package openoffice;

import com.sun.star.beans.PropertyValue;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import ooo.connector.BootstrapSocketConnector;

import java.io.File;
import java.net.MalformedURLException;

public class OpenOfficeUtil {
    public static String officeDirectory = "";

    public static void convert(String wordFile, String pdfFile)
            throws BootstrapException, Exception, MalformedURLException {
        try {
            XDesktop xDesktop = initialize();
            XComponent xComp = loadDocument(wordFile, xDesktop);
            saveToPDF(pdfFile, xComp);

            xDesktop.terminate();
        } catch (BootstrapException | Exception | MalformedURLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void saveToPDF(String pdfFile, XComponent xComp) throws IOException, MalformedURLException {
        PropertyValue[] propertyValues;

        XStorable xStorable = UnoRuntime.queryInterface(XStorable.class, xComp);

        PropertyValue[] aMediaDescriptor = new PropertyValue[2];

        aMediaDescriptor[0] = new PropertyValue();
        aMediaDescriptor[0].Name = "FilterName";
        aMediaDescriptor[0].Value = "writer_pdf_Export";

        propertyValues = new PropertyValue[3];
        propertyValues[0] = new PropertyValue();
        propertyValues[0].Name = "Overwrite";
        propertyValues[0].Value = Boolean.TRUE;
        propertyValues[1] = new PropertyValue();
        propertyValues[1].Name = "SelectPdfVersion";
        propertyValues[1].Value = 1;
        propertyValues[2] = new PropertyValue();
        propertyValues[2].Name = "UseTaggedPDF";
        propertyValues[2].Value = Boolean.TRUE;

        aMediaDescriptor[1] = new PropertyValue();
        aMediaDescriptor[1].Name = "FilterData";
        aMediaDescriptor[1].Value = propertyValues;

        // Appending the favoured extension to the origin document name
        xStorable.storeToURL(new File(pdfFile).toURI().toURL().toString(), aMediaDescriptor);

        System.out.println("Saved " + pdfFile);
    }

    private static XComponent loadDocument(String wordFile, XDesktop xDesktop)
            throws IOException, IllegalArgumentException {
        if (!new File(wordFile).canRead()) {
            throw new RuntimeException("Cannot load template:" + new File(wordFile));
        }

        XComponentLoader xCompLoader = UnoRuntime.queryInterface(XComponentLoader.class, xDesktop);

        String sUrl = "file:///" + wordFile;

        PropertyValue[] propertyValues = new PropertyValue[1];
        propertyValues[0] = new PropertyValue();
        propertyValues[0].Name = "Hidden";
        propertyValues[0].Value = Boolean.TRUE;

        return xCompLoader.loadComponentFromURL(sUrl, "_blank", 0, propertyValues);
    }

    private static XDesktop initialize() throws BootstrapException, Exception {
        XComponentContext xContext;
        xContext = BootstrapSocketConnector.bootstrap(officeDirectory);
        XMultiComponentFactory xMCF = xContext.getServiceManager();
        Object oDesktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
        return UnoRuntime.queryInterface(XDesktop.class, oDesktop);
    }
}
