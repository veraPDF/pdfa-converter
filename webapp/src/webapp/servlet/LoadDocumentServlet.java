package webapp.servlet;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static webapp.SessionNameConstants.DOC_FILE;
import static webapp.SessionNameConstants.EXCEPTION;
import static webapp.SessionNameConstants.SERVICE_MESSAGE;
import static webapp.SessionNameConstants.STORAGE_DIRECTORY;

public class LoadDocumentServlet extends HttpServlet {
    private String folder;
    private int maxFileSize = 20 * 1024 * 1024;
    private int maxMemSize = 20 * 1024 * 1024;

    public void init() {
        Properties prop = PropertyServletContextListener.getWebProperties();
        folder = prop.getProperty("dir.temp");
        new File(folder).mkdirs();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");

        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(maxMemSize);
        factory.setRepository(new File(folder + "/temp/"));

        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(maxFileSize);
        upload.setHeaderEncoding("UTF-8");
        String storage = folder + UUID.randomUUID().toString() + "/";
        final HttpSession session = request.getSession();
        try {
            // Parse the request to get file items.
            List<FileItem> fileItems = upload.parseRequest(request);

            cleanSessionParams(request);
            // Process the uploaded file items
            for (FileItem fi : fileItems) {
                if (!fi.isFormField()) {
                    String fileName = fi.getName();
                    String templateFileName;
                    if (fileName.lastIndexOf("\\") >= 0) {
                        templateFileName = fileName.substring(fileName.lastIndexOf("\\"));
                    } else {
                        templateFileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
                    }

                    if (!Arrays.asList("doc", "docx", "xls", "xlsx", "ppt", "pptx",
                                       "ods", "odt", "odp").contains(getExtension(templateFileName))) {
                        throw new FileUploadBase.InvalidContentTypeException();
                    }

                    new File(storage).mkdirs();

                    File file = new File(storage + templateFileName);
                    fi.write(file);

                    session.setAttribute(DOC_FILE, templateFileName);
                    session.setAttribute(STORAGE_DIRECTORY, storage);
                }
            }
            if (fileItems.isEmpty()) {
                session.setAttribute(SERVICE_MESSAGE, "Please choose your document.");
            }
        } catch (FileUploadBase.SizeLimitExceededException e) {
            session.setAttribute(SERVICE_MESSAGE, "Your file is greater than limit size 20MB.");
        } catch (FileUploadBase.InvalidContentTypeException e) {
            session.setAttribute(SERVICE_MESSAGE, "Please upload MS Office or OpenOffice file (supported extensions:" +
                                                  "doc, docx, xls, xlsx, ppt, pptx, ods, odt, odp).");
        } catch (FileUploadException e) {
            e.printStackTrace();
            session.setAttribute(SERVICE_MESSAGE,
                                 "Please update this page and try to upload your file again.");
        }
        catch (Exception | OutOfMemoryError ex) {
            ex.printStackTrace();
            session.setAttribute(EXCEPTION, ex);
        }
    }

    private void cleanSessionParams(HttpServletRequest request) {
        final HttpSession session = request.getSession();
        session.removeAttribute(STORAGE_DIRECTORY);
        session.removeAttribute(SERVICE_MESSAGE);
        session.removeAttribute(EXCEPTION);
    }

    private String getExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i+1);
        }
        return extension;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getSession().removeAttribute(EXCEPTION);
        req.getRequestDispatcher("./load_template.jsp").forward(req, resp);
    }
}
