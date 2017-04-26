package tutorial.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Classes in processmanager should not call classes in opc.
 */
public class CheckProcessManagerCallsOpc extends CustomCheck {

    public static final String CHECK_VIOLATION_MESSAGE = "Classes in processmanager should not call classes in opc.";

    private boolean inProcessManager;

    @Override
    public int[] getDefaultTokens() {
        return new int[] {TokenTypes.PACKAGE_DEF, TokenTypes.IMPORT};
    }

    @Override
    public void visitToken(DetailAST aAST) {

        if (aAST.getType() == TokenTypes.PACKAGE_DEF) {
            String packageName = fullyQualifiedPackage(aAST);
            if (packageName != null && packageName.startsWith("com.sun.j2ee.blueprints.processmanager")) {
                inProcessManager = true;
            } else {
                inProcessManager = false;
            }
            // IMPORTANT: must set inProcessManager to either true or false because it's an instance
            // variable and *this* check object can analyze several source files in a row
        } else if (aAST.getType() == TokenTypes.IMPORT && inProcessManager) {
            String importName = fullyQualifiedPackage(aAST);
            if (importName.startsWith("com.sun.j2ee.blueprints.opc")) {
                log(aAST.getLineNo(), CHECK_VIOLATION_MESSAGE);
            }
        }
    }

}
