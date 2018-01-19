package converter.servlet;

import converter.SessionNameConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

public class ViewInformationServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            final HttpSession session = request.getSession();
            String serviceMessage = (String) session.getAttribute(SessionNameConstants.SERVICE_MESSAGE);
            Exception crashException = (Exception) session.getAttribute(SessionNameConstants.EXCEPTION);

            if (crashException != null) {
                out.print(String.format("<pre class=\"alert-danger\">" +
                                        "<div id='exception' role=\"alert\"><h5>%s</h5></div>" +
                                        "</pre>",
                                        crashException.getMessage()));
            } else if (serviceMessage != null) {
                out.print(String.format("<pre class=\"alert-danger\">" +
                                        "<div id='exception' role=\"alert\"><h5>%s</h5></div>" +
                                        "</pre>",
                                        serviceMessage));
            }
        }
    }
}
