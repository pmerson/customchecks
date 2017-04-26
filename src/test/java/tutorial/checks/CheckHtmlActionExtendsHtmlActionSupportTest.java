package tutorial.checks;

import java.net.URI;
import java.nio.file.Paths;

import org.junit.Test;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

import checkstyle.BaseCheckTestSupport;

public class CheckHtmlActionExtendsHtmlActionSupportTest extends BaseCheckTestSupport {

    // @Test
    public void testWhenHtmActionHasNoExtendOrImport() throws Exception {
        Class check = CheckHtmlActionExtendsHtmlActionSupport.class;
        final DefaultConfiguration checkConfig = createCheckConfig(check);
        String expected[] = {
                "13: " + CheckHtmlActionExtendsHtmlActionSupport.CHECK_VIOLATION_MESSAGE + " [" + check.getSimpleName() + "]"
        };
        URI uriFile = this.getClass().getResource("/InputCheckHtmlActionExtendsHtmlActionSupportTest.java").toURI();
        String filename = Paths.get(uriFile).toString();
        verify(checkConfig, filename, expected);
    }

    // @Test
    public void testWhenHtmlActionHasImport() throws Exception {
        Class check = CheckHtmlActionExtendsHtmlActionSupport.class;
        final DefaultConfiguration checkConfig = createCheckConfig(check);
        URI uriFile = this.getClass().getResource("/InputCheckHtmlActionExtendsHtmlActionSupportTest2.java").toURI();
        String filename = Paths.get(uriFile).toString();

        verifyAuditDoneWithZeroErrors(checkConfig, filename);
    }

}
