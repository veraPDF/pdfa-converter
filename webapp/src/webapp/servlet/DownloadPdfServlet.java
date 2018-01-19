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

import static webapp.SessionNameConstants.DOWNLOAD_FILE;
import static webapp.SessionNameConstants.STORAGE_DIRECTORY;

public class DownloadPdfServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {
        ServletContext context = getServletContext();

        final HttpSession session = req.getSession();
        String downloadFile = (String) session.getAttribute(DOWNLOAD_FILE);

        if (downloadFile == null) {
            response.sendRedirect("./home");
            return;
        }

        writeFile(response, context, new File(downloadFile));
    }

    private void writeFile(HttpServletResponse response, ServletContext context, File file)
            throws IOException {
        try (InputStream fis = new FileInputStream(file)) {
            String mimeType = context.getMimeType(file.getAbsolutePath());
            response.setContentType(mimeType != null ? mimeType : "application/pdf");
            response.setContentLength((int) file.length());
            response.setHeader("Content-Disposition", "attachment; filename=\""
                    + file.getName() + "\"");

            ServletOutputStream os = response.getOutputStream();
            byte[] bufferData = new byte[1024];
            int read = 0;
            while ((read = fis.read(bufferData)) != -1) {
                os.write(bufferData, 0, read);
            }
            os.flush();
            os.close();
        }
    }

}
