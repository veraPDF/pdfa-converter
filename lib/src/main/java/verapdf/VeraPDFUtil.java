package verapdf;

import org.apache.commons.io.IOUtils;
import org.verapdf.core.VeraPDFException;
import org.verapdf.features.FeatureFactory;
import org.verapdf.metadata.fixer.FixerFactory;
import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.validation.validators.ValidatorConfig;
import org.verapdf.pdfa.validation.validators.ValidatorFactory;
import org.verapdf.processor.BatchProcessor;
import org.verapdf.processor.FormatOption;
import org.verapdf.processor.ProcessorConfig;
import org.verapdf.processor.ProcessorFactory;
import org.verapdf.processor.TaskType;
import org.verapdf.processor.plugins.PluginsCollectionConfig;
import org.verapdf.processor.reports.BatchSummary;
import org.verapdf.report.HTMLReport;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class VeraPDFUtil {
    private static final String WIKI_URL_BASE = "https://github.com/veraPDF/veraPDF-validation-profiles/wiki/";

    public static String validate(String pdfFile) throws FileNotFoundException, VeraPDFException {
        if (!new File(pdfFile).canRead()) {
            throw new RuntimeException("Cannot load template: " + new File(pdfFile));
        }

        VeraGreenfieldFoundryProvider.initialise();

        return new String(getHtmlBytes(pdfFile)).replaceFirst("<td.*<b>File.*</td>", "<h3 style=\"margin: 5 0 5 0\">veraPDF validation report</h3>");
    }

    private static byte[] getHtmlBytes(String pdfFile) throws VeraPDFException, FileNotFoundException {
        ByteArrayInputStream bais = validateHtml(new FileInputStream(pdfFile));

        byte[] bytes = new byte[bais.available()];
        bais.read(bytes, 0, bais.available());
        return bytes;
    }

    private static ByteArrayInputStream validateHtml(InputStream uploadedInputStream) throws VeraPDFException {
        VeraGreenfieldFoundryProvider.initialise();

        File file;
        try {
            file = File.createTempFile("cache", "");
        } catch (IOException excep) {
            throw new VeraPDFException("IOException creating a temp file", excep); //$NON-NLS-1$
        }
        try (OutputStream fos = new FileOutputStream(file)) {
            IOUtils.copy(uploadedInputStream, fos);
            uploadedInputStream.close();
        } catch (IOException excep) {
            throw new VeraPDFException("IOException creating a temp file", excep); //$NON-NLS-1$
        }

        PDFAFlavour flavour = PDFAFlavour.PDFA_1_A;
        ValidatorConfig validConf = ValidatorFactory.createConfig(flavour, false, 100);
        ProcessorConfig config = createValidateConfig(validConf);

        byte[] htmlBytes;
        try (ByteArrayOutputStream xmlBos = new ByteArrayOutputStream()) {
            BatchSummary summary = processFile(file, config, xmlBos);
            htmlBytes = getHtmlBytes(xmlBos.toByteArray(), summary);
        } catch (IOException | TransformerException excep) {
            throw new VeraPDFException("Some Java Exception while validating", excep); //$NON-NLS-1$
            // TODO Auto-generated catch block
        }
        return new ByteArrayInputStream(htmlBytes);
    }

    private static byte[] getHtmlBytes(byte[] xmlBytes, BatchSummary summary) throws IOException, TransformerException {
        try (InputStream xmlBis = new ByteArrayInputStream(xmlBytes);
             ByteArrayOutputStream htmlBos = new ByteArrayOutputStream()) {
            HTMLReport.writeHTMLReport(xmlBis, htmlBos, summary, WIKI_URL_BASE, false);
            return htmlBos.toByteArray();
        }

    }

    private static ProcessorConfig createValidateConfig(ValidatorConfig validConf) {
        return ProcessorFactory.fromValues(validConf, FeatureFactory.defaultConfig(),
                                           PluginsCollectionConfig.defaultConfig(), FixerFactory.defaultConfig(), EnumSet.of(
                        TaskType.VALIDATE));
    }

    private static BatchSummary processFile(File file, ProcessorConfig config, OutputStream mrrStream)
            throws VeraPDFException {
        List<File> files = Arrays.asList(file);
        BatchSummary summary = null;
        try (BatchProcessor processor = ProcessorFactory.fileBatchProcessor(config)) {
            summary = processor.process(files,
                                        ProcessorFactory.getHandler(FormatOption.MRR, false, mrrStream, 100, false));
        } catch (IOException excep) {
        }
        return summary;
    }
}
