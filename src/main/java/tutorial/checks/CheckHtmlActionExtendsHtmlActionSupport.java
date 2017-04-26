package tutorial.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class CheckHtmlActionExtendsHtmlActionSupport extends CustomCheck {

    public static final String CHECK_VIOLATION_MESSAGE = "HTML action classes must extend HTMLActionSupport";

    @Override
    public int[] getDefaultTokens() {
        return new int[] {TokenTypes.CLASS_DEF};
    }

    @Override
    public void visitToken(DetailAST aAST) {
        if (aAST.getType() == TokenTypes.CLASS_DEF) {
            visitClassDef(aAST);
        }
    }

    private void visitClassDef(DetailAST classDefToken) {
        String className = classDefToken.findFirstToken(TokenTypes.IDENT).getText();
        if (className.endsWith("HTMLAction")) {
            boolean extendsOk = extendsHtmlActionSupport(classDefToken);
            if (!extendsOk) {
                log(classDefToken.getLineNo(), CHECK_VIOLATION_MESSAGE);
            }
        }
    }

    private boolean extendsHtmlActionSupport(DetailAST classDefToken) {
        DetailAST implementsToken = classDefToken.findFirstToken(TokenTypes.IMPLEMENTS_CLAUSE);
        String superClassName = getSuperClassName(classDefToken);
        if (superClassName != null && superClassName.equals("HTMLActionSupport")) {
            return true;
        }
        return false;
    }

}
