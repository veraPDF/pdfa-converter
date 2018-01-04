package webapp.servlet;

import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.uno.Exception;
import openoffice.OpenOfficeUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static webapp.SessionNameConstants.DOC_FILE;
import static webapp.SessionNameConstants.PDF_FILE;
import static webapp.SessionNameConstants.SERVICE_MESSAGE;
import static webapp.SessionNameConstants.STORAGE_DIRECTORY;

public class ConvertingServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String documentName = (String) session.getAttribute(DOC_FILE);
        String storage = (String) session.getAttribute(STORAGE_DIRECTORY);
        if (storage == null || documentName == null) {
            resp.sendRedirect("./home");
            return;
        }

        final String pdfFilename = getPdfFilename(documentName);
        String pdfFile = storage + pdfFilename;

        Properties props = PropertyServletContextListener.getWebProperties();
        String officeDirectory = props.getProperty("officeDir");
        if (officeDirectory == null) {
            session.setAttribute(SERVICE_MESSAGE,
                                 "OpenOffice directory wasn't found.");
        } else {
            OpenOfficeUtil.officeDirectory = officeDirectory;
            try {
                OpenOfficeUtil.convert(storage + documentName, pdfFile);
                if (!new File(pdfFile).exists()) {
                    session.setAttribute(SERVICE_MESSAGE,
                                         "Something was going wrong during converting.");
                } else {
                    session.setAttribute(PDF_FILE, pdfFilename);
                }
            } catch (Exception | BootstrapException e) {
                session.setAttribute(SERVICE_MESSAGE,
                                     "Something was going wrong during converting.");
            }
        }
    }

    private String getPdfFilename(String documentName) {
        String[] parts = documentName.split("\\.");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            builder.append(parts[i]).append(".");
        }
        builder.append("pdf");
        return builder.toString();
    }
}
