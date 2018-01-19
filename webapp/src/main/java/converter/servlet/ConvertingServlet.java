package converter.servlet;

import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.uno.Exception;
import converter.SessionNameConstants;
import util.ZipManagerUtil;
import util.openoffice.OpenOfficeUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;

public class ConvertingServlet extends HttpServlet {

    public static final String PDF_DIR_NAME = "pdfs";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();
        String fileName = (String) session.getAttribute(SessionNameConstants.UPLOADED_FILE);
        String storagePath = (String) session.getAttribute(SessionNameConstants.STORAGE_DIRECTORY);
        if (storagePath == null || fileName == null) {
            resp.sendRedirect("./home");
            return;
        }
        File storage = new File(storagePath);
        File uploaded = new File(fileName);

        if (fileName.endsWith(".zip")) {
            File unzippedDir = ZipManagerUtil.unzipFile(uploaded, "office");
            File pdfDir = new File(storage, PDF_DIR_NAME);
            pdfDir.mkdirs();
            processDir(unzippedDir, pdfDir, session);
            String uploadedName = uploaded.getName();
            File resultZip = ZipManagerUtil.zipDirectory(pdfDir,
                    uploadedName.substring(0, uploadedName.lastIndexOf(".")) + "_pdf");
            session.setAttribute(SessionNameConstants.DOWNLOAD_FILE, resultZip.getAbsolutePath());
        } else {
            File pdf = convertFile(uploaded, storage, session);
            if (pdf != null) {
                session.setAttribute(SessionNameConstants.DOWNLOAD_FILE, pdf.getAbsolutePath());
            }
        }
    }

    private void processDir(File dirToProcess, File pdfDir, HttpSession session) {
        if (!dirToProcess.isDirectory()) {
            throw new IllegalArgumentException("Argument dirToProcess has to be a directory");
        }
        for (File f : dirToProcess.listFiles()) {
            if (f.isDirectory()) {
                File similarPDFDir = new File(pdfDir, f.getName());
                similarPDFDir.mkdirs();
                processDir(f, similarPDFDir, session);
            } else {
                String extension = OpenOfficeUtil.getExtension(f.getAbsolutePath());
                if (Arrays.asList(OpenOfficeUtil.EXTENSIONS).contains(extension)) {
                    convertFile(f, pdfDir, session);
                }
            }
        }
    }

    private File convertFile(File document, File destination, HttpSession session) {
        final String pdfFilename = getPdfFilename(document.getName());
        File pdfFile = new File(destination, pdfFilename);
        try {
            OpenOfficeUtil.convert(document, pdfFile);
            if (pdfFile.exists()) {
                return pdfFile;
            }
        } catch (Exception | BootstrapException | MalformedURLException | InterruptedException e) {
            e.printStackTrace();
        }
        session.setAttribute(SessionNameConstants.SERVICE_MESSAGE,
                "Something was going wrong during converting.");
        return null;
    }

    private String getPdfFilename(String documentName) {
        return documentName.substring(0, documentName.lastIndexOf(".")) + ".pdf";
    }
}
