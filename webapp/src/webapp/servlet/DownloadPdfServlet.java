package webapp.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static webapp.SessionNameConstants.PDF_FILE;
import static webapp.SessionNameConstants.STORAGE_DIRECTORY;

public class DownloadPdfServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        ServletContext context = getServletContext();

        final HttpSession session = req.getSession();

        String storage = (String) session.getAttribute(STORAGE_DIRECTORY);
        String pdfFile = (String) session.getAttribute(PDF_FILE);

        if (pdfFile == null) {
            response.sendRedirect("./home");
            return;
        }

        writeFile(response, context, session, new File(storage + pdfFile));
    }

    private void writeFile(HttpServletResponse response, ServletContext context, HttpSession session, File file)
            throws IOException {
        InputStream fis = new FileInputStream(file);
        String mimeType = context.getMimeType(file.getAbsolutePath());
        response.setContentType(mimeType != null ? mimeType : "application/pdf");
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", "attachment; filename=\""
                                                  + session.getAttribute(PDF_FILE) + "\"");

        ServletOutputStream os = response.getOutputStream();
        byte[] bufferData = new byte[1024];
        int read = 0;
        while ((read = fis.read(bufferData)) != -1) {
            os.write(bufferData, 0, read);
        }
        os.flush();
        os.close();
        fis.close();
    }

}
