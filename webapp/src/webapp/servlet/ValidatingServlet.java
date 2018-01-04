package webapp.servlet;

import org.verapdf.core.VeraPDFException;
import verapdf.VeraPDFUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static webapp.SessionNameConstants.PDF_FILE;
import static webapp.SessionNameConstants.SERVICE_MESSAGE;
import static webapp.SessionNameConstants.STORAGE_DIRECTORY;

public class ValidatingServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String documentName = (String) session.getAttribute(PDF_FILE);
        String storage = (String) session.getAttribute(STORAGE_DIRECTORY);
        if (storage == null || documentName == null) {
            resp.sendRedirect("./home");
            return;
        }

        String pdfFile = storage + documentName;
        if (!new File(pdfFile).exists()) {
            session.setAttribute(SERVICE_MESSAGE,
                                 "Something was going wrong during converting.");
        } else {
            try {
                try (PrintWriter pw = resp.getWriter()) {
                    final String xmlReport = VeraPDFUtil.validate(pdfFile);
                    pw.print(xmlReport);
                }
            } catch (VeraPDFException e) {
                e.printStackTrace();
            }
        }
    }
}
