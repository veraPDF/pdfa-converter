package cli;

import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.uno.Exception;
import openoffice.OpenOfficeUtil;
import org.verapdf.core.VeraPDFException;
import verapdf.VeraPDFUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

public class MainCLI {
    public static void main(String[] args) throws FileNotFoundException, VeraPDFException, BootstrapException,
            Exception, MalformedURLException {
        if (args.length < 3) {
            return;
        }
        OpenOfficeUtil.officeDirectory = args[2];
        File office = new File(args[0]), pdf = new File(args[1]);
        OpenOfficeUtil.convert(office.getAbsolutePath(), pdf.getAbsolutePath());
        VeraPDFUtil.validate(pdf.getAbsolutePath());
    }
}
