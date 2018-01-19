package util.verapdf;

import org.apache.commons.io.IOUtils;
import org.verapdf.apps.utils.ApplicationUtils;
import org.verapdf.component.AuditDuration;
import org.verapdf.core.VeraPDFException;
import org.verapdf.features.FeatureFactory;
import org.verapdf.metadata.fixer.FixerFactory;
import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.validation.validators.ValidatorConfig;
import org.verapdf.pdfa.validation.validators.ValidatorFactory;
import org.verapdf.processor.*;
import org.verapdf.processor.plugins.PluginsCollectionConfig;
import org.verapdf.processor.reports.BatchSummary;
import org.verapdf.processor.reports.FeaturesBatchSummary;
import org.verapdf.processor.reports.MetadataRepairBatchSummary;
import org.verapdf.processor.reports.ValidationBatchSummary;
import org.verapdf.report.HTMLReport;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class VeraPDFUtil {
    private static final String WIKI_URL_BASE = "https://github.com/veraPDF/veraPDF-validation-profiles/wiki/";

    public static String validate(String pdfFile) throws FileNotFoundException, VeraPDFException {
    	synchronized (VeraPDFUtil.class) {
            File file = new File(pdfFile);
            if (!file.canRead()) {
				throw new RuntimeException("Cannot load template: " + file);
			}

            String htmlresult = new String(getHtmlBytes(pdfFile));
            if (pdfFile.endsWith(".pdf")) {
                htmlresult = htmlresult.replaceFirst("<td.*<b>File.*</td>", "<h3 style=\"margin: 5 0 5 0\">veraPDF validation report</h3>");
            } else {
                int firstInd = htmlresult.indexOf("<h2>Build Information</h2>");
                int lastInd = htmlresult.indexOf("<h2>Batch Summary</h2>");
                htmlresult = htmlresult.substring(0, firstInd) +
                        "<h2 style=\"margin: 5 0 5 0\">veraPDF validation report</h2>" +
                        htmlresult.substring(lastInd);
                htmlresult = htmlresult.replaceAll(pdfFile + "/", "");
            }
            return htmlresult;
		}
    }

    private static byte[] getHtmlBytes(String pdfFile) throws VeraPDFException, FileNotFoundException {
        ByteArrayInputStream bais = validateHtml(pdfFile);

        byte[] bytes = new byte[bais.available()];
        bais.read(bytes, 0, bais.available());
        return bytes;
    }

    private static ByteArrayInputStream validateHtml(String pdfFile) throws VeraPDFException {
        VeraGreenfieldFoundryProvider.initialise();
        PDFAFlavour flavour = PDFAFlavour.PDFA_1_A;
        ValidatorConfig validConf = ValidatorFactory.createConfig(flavour, false, 100);
        ProcessorConfig config = createValidateConfig(validConf);

        byte[] htmlBytes;
        try (ByteArrayOutputStream xmlBos = new ByteArrayOutputStream()) {
            BatchSummary summary = processFile(new File(pdfFile), config, xmlBos);
            htmlBytes = getHtmlBytes(xmlBos.toByteArray(), summary);
        } catch (IOException | TransformerException excep) {
            throw new VeraPDFException("Some Java Exception while validating", excep); //$NON-NLS-1$
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
            List<File> list = ApplicationUtils.filterPdfFiles(files, true);
            summary = processor.process(list,
                    ProcessorFactory.getHandler(FormatOption.MRR, false, mrrStream, 100, false));
        } catch (IOException excep) {
        }
        return summary;
    }

//    private static String veraPDFPath;
//
//    public static void initialize(String veraPDFPath) {
//        VeraPDFUtil.veraPDFPath = veraPDFPath;
//    }
//
//    public static String validate(String toValidate) throws InterruptedException, TransformerException, IOException {
//        File file = new File(toValidate);
//        if (!file.canRead()) {
//            throw new RuntimeException("Cannot load template: " + file);
//        }
//        boolean isMulty = file.isDirectory();
//
//        File veraPDFReport = null;
//        try {
//             veraPDFReport = getVeraPDFReport(toValidate);
//            try (InputStream xmlBis = new FileInputStream(veraPDFReport);
//                 ByteArrayOutputStream htmlBos = new ByteArrayOutputStream()) {
//                HTMLReport.writeHTMLReport(xmlBis, htmlBos, new HTMLBatchSummary(isMulty), WIKI_URL_BASE, false);
//                return new String(htmlBos.toByteArray());//.replaceFirst("<td.*<b>File.*</td>", "<h3 style=\"margin: 5 0 5 0\">veraPDF validation report</h3>");
//            }
//        } catch (IOException | InterruptedException | TransformerException e) {
//            e.printStackTrace();
//            throw e;
//        } finally {
//            if (veraPDFReport != null) {
//                veraPDFReport.delete();
//            }
//        }
//    }
//
//    private static File getVeraPDFReport(String filename) throws IOException, InterruptedException {
//        String[] cmd = {veraPDFPath, "--format", "mrr", "-f", "1a", filename};
//        ProcessBuilder pb = new ProcessBuilder();
//        Path outputPath = Files.createTempFile("veraPDFReport", ".xml");
//        File file = outputPath.toFile();
//        pb.redirectOutput(file);
//        pb.command(cmd);
//        Process process = pb.start();
//        process.waitFor();
//        return file;
//    }
//
//    private static class HTMLBatchSummary implements BatchSummary {
//
//        private final boolean isMulty;
//
//        public HTMLBatchSummary(boolean isMulty) {
//            this.isMulty = isMulty;
//        }
//
//        @Override
//        public AuditDuration getDuration() {
//            return null;
//        }
//
//        @Override
//        public ValidationBatchSummary getValidationSummary() {
//            return null;
//        }
//
//        @Override
//        public FeaturesBatchSummary getFeaturesSummary() {
//            return null;
//        }
//
//        @Override
//        public MetadataRepairBatchSummary getRepairSummary() {
//            return null;
//        }
//
//        @Override
//        public boolean isMultiJob() {
//            return this.isMulty;
//        }
//
//        @Override
//        public int getTotalJobs() {
//            return 0;
//        }
//
//        @Override
//        public int getFailedParsingJobs() {
//            return 0;
//        }
//
//        @Override
//        public int getFailedEncryptedJobs() {
//            return 0;
//        }
//    }
}
