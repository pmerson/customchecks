package checkstyle;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.Configuration;

public abstract class BaseCheckTestSupport {

    static {
        String testInputsDir = System.getProperty("testinputs.dir");
        if (StringUtils.isEmpty(testInputsDir)) {
            System.setProperty("testinputs.dir", "C:\\sti\\fontes-tcu\\git\\sonar-checkstyle-tcu-checks-plugin\\src\\testinputs");
        }
    }

    /** a brief logger that only display info about errors */
    protected static class BriefLogger extends DefaultLogger {
        public BriefLogger(OutputStream out) {
            super(out, true);
        }

        @Override
        public void auditStarted(AuditEvent evt) {
        }

        @Override
        public void fileFinished(AuditEvent evt) {
        }

        @Override
        public void fileStarted(AuditEvent evt) {
        }
    }

    protected final ByteArrayOutputStream mBAOS = new ByteArrayOutputStream();
    protected final PrintStream mStream = new PrintStream(mBAOS);
    protected final Properties mProps = new Properties();

    public static DefaultConfiguration createCheckConfig(Class<?> aClazz) {
        final DefaultConfiguration checkConfig = new DefaultConfiguration(aClazz.getName());
        return checkConfig;
    }

    protected Checker createChecker(Configuration aCheckConfig) throws Exception {
        final DefaultConfiguration dc = createCheckerConfig(aCheckConfig);
        final Checker c = new Checker();
        // make sure the tests always run with english error messages
        // so the tests don't fail in supported locales like german
        final Locale locale = Locale.ENGLISH;
        c.setLocaleCountry(locale.getCountry());
        c.setLocaleLanguage(locale.getLanguage());
        c.setModuleClassLoader(Thread.currentThread().getContextClassLoader());
        c.configure(dc);
        c.addListener(new BriefLogger(mStream));
        return c;
    }

    protected DefaultConfiguration createCheckerConfig(Configuration aConfig) {
        final DefaultConfiguration dc = new DefaultConfiguration("configuration");
        final DefaultConfiguration twConf = createCheckConfig(TreeWalker.class);
        // make sure that the tests always run with this charset
        dc.addAttribute("charset", "iso-8859-1");
        dc.addChild(twConf);
        twConf.addChild(aConfig);
        return dc;
    }

    /**
     * Especifica arquivo a ser processado pelo checkstyle (input do teste). O arquivo � recuperado
     * da pasta indicada na vari�vel de ambiente "testinputs.dir".
     */
    protected static String getPath(String aFilename) throws IOException {
        String testInputsDir = System.getProperty("testinputs.dir");
        if (testInputsDir == null) {
            throw new IOException("variavel testinputs.dir deve ser configurada para um local v�lido.");
        }
        final File f = new File(testInputsDir, aFilename);
        String path = f.getCanonicalPath();
        return path;
    }

    protected static String getSrcPath(String aFilename) throws IOException {
        final File f = new File(System.getProperty("testsrcs.dir"), aFilename);
        return f.getCanonicalPath();
    }

    protected void verify(Configuration aConfig, String aFileName, String[] aExpected) throws Exception {
        verify(createChecker(aConfig), aFileName, aFileName, aExpected);
    }

    protected void verify(Checker aC, String aFileName, String[] aExpected) throws Exception {
        verify(aC, aFileName, aFileName, aExpected);
    }

    protected void verify(Checker aC, String aProcessedFilename, String aMessageFileName, String[] aExpected) throws Exception {
        verify(aC, new File[] {new File(aProcessedFilename)}, aMessageFileName, aExpected);
    }

    private static final String PREFIX_IN_NEWER_VERSIONS = "[ERROR] ";

    protected void verify(Checker aC, File[] aProcessedFiles, String aMessageFileName, String[] aExpected) throws Exception {
        mStream.flush();
        final List<File> theFiles = Lists.newArrayList();
        Collections.addAll(theFiles, aProcessedFiles);
        final int errs = aC.process(theFiles);

        // process each of the lines
        final ByteArrayInputStream bais = new ByteArrayInputStream(mBAOS.toByteArray());
        final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(bais));

        for (int i = 0; i < aExpected.length; i++) {
            String expected = aMessageFileName + ":" + aExpected[i];
            final String actual = lnr.readLine();
            if (actual.startsWith(PREFIX_IN_NEWER_VERSIONS)) {
                expected = PREFIX_IN_NEWER_VERSIONS + expected;
            }
            assertEquals("error message " + i, expected, actual);
        }

        assertEquals("unexpected output: " + lnr.readLine(), aExpected.length, errs);
        aC.destroy();
    }

    protected void verifyAuditDoneWithZeroErrors(DefaultConfiguration aConfig, String aFileName) throws Exception {
        Checker aC = createChecker(aConfig);
        File[] aProcessedFiles = new File[] {new File(aFileName)};
        mStream.flush();
        final List<File> theFiles = Lists.newArrayList();
        Collections.addAll(theFiles, aProcessedFiles);
        int errs = aC.process(theFiles);
        assertEquals("unexpected positive number of errors", 0, errs);
        aC.destroy();
    }

}
