package tutorial.checks;

import java.net.URI;
import java.nio.file.Paths;

import org.junit.Test;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

import checkstyle.BaseCheckTestSupport;

public class CheckProcessManagerCallsOpcTest extends BaseCheckTestSupport {

    // @Test
    public void testWhenHtmActionHasNoExtendOrImport() throws Exception {
        Class check = CheckProcessManagerCallsOpc.class;
        final DefaultConfiguration checkConfig = createCheckConfig(check);
        String expected[] = {
                "3: " + CheckProcessManagerCallsOpc.CHECK_VIOLATION_MESSAGE + " [" + check.getSimpleName() + "]",
                "4: " + CheckProcessManagerCallsOpc.CHECK_VIOLATION_MESSAGE + " [" + check.getSimpleName() + "]",
                "5: " + CheckProcessManagerCallsOpc.CHECK_VIOLATION_MESSAGE + " [" + check.getSimpleName() + "]"
        };
        URI uriFile = this.getClass().getResource("/InputCheckProcessManagerCallsOpcTest.java").toURI();
        String filename = Paths.get(uriFile).toString();
        verify(checkConfig, filename, expected);
    }

}
