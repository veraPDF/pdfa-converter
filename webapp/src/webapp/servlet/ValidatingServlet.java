package webapp.servlet;

import org.verapdf.core.VeraPDFException;
import util.verapdf.VeraPDFUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import static webapp.SessionNameConstants.DOWNLOAD_FILE;
import static webapp.SessionNameConstants.SERVICE_MESSAGE;
import static webapp.SessionNameConstants.STORAGE_DIRECTORY;

public class ValidatingServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String documentName = (String) session.getAttribute(DOWNLOAD_FILE);
        String storage = (String) session.getAttribute(STORAGE_DIRECTORY);
        if (storage == null || documentName == null) {
            resp.sendRedirect("./home");
            return;
        }

        File toValidate = documentName.endsWith("zip") ?
                new File(storage, ConvertingServlet.PDF_DIR_NAME) :
                new File(documentName);
        if (!toValidate.exists()) {
            session.setAttribute(SERVICE_MESSAGE,
                    "Something was going wrong during converting.");
        } else {
            try (PrintWriter pw = resp.getWriter()) {
                final String htmlReport = VeraPDFUtil.validate(toValidate.getAbsolutePath());
                pw.print(htmlReport);
            } catch (FileNotFoundException | VeraPDFException e) {
                e.printStackTrace();
            }
		}
    }
}
