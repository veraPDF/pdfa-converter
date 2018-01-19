package converter.servlet;

import converter.SessionNameConstants;
import org.verapdf.core.VeraPDFException;
import util.verapdf.VeraPDFUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class ValidatingServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String documentName = (String) session.getAttribute(SessionNameConstants.DOWNLOAD_FILE);
        String storage = (String) session.getAttribute(SessionNameConstants.STORAGE_DIRECTORY);
        if (storage == null || documentName == null) {
            resp.sendRedirect("./home");
            return;
        }

        File toValidate = documentName.endsWith("zip") ?
                new File(storage, ConvertingServlet.PDF_DIR_NAME) :
                new File(documentName);
        if (!toValidate.exists()) {
            session.setAttribute(SessionNameConstants.SERVICE_MESSAGE,
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
