package tutorial.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Abstract class with utility methods for custom checks
 * 
 * @author Paulo Merson
 */
public abstract class CustomCheck extends Check {

    /**
     * Regular expression of the packages to ignore in all checks
     */
    private static final Pattern PACKAGES_TO_IGNORE = Pattern.compile("(br\\.gov\\.tcu\\.contas|br\\.gov\\.tcu\\.econtas" +
            "|br\\.gov\\.tcu\\.vpost|br\\.gov\\.tcu\\.intmavenflip|br\\.gov\\.tcu\\.vista|br\\.gov\\.tcu\\.tce)");

    /** Variable that is set when the package should be ignored for every check */
    private boolean ignoreThisPackage = false;

    /**
     * Returns the fully qualified package in a package definition or import statement.
     * 
     * @param packageDefOrImportAST an AST of type PACKAGE_DEF or IMPORT
     * @return the fully qualified package name (e.g., "javax.jws.soap") that is in a
     *         package definition statement or an import statement.
     */
    protected String fullyQualifiedPackage(DetailAST packageDefOrImportAST) {
        if (packageDefOrImportAST == null || (packageDefOrImportAST.getType() != TokenTypes.PACKAGE_DEF &&
                packageDefOrImportAST.getType() != TokenTypes.IMPORT)) {
            throw new IllegalArgumentException("Parameter packageDefOrImportAST must be a PACKAGE_DEF or IMPORT AST");
        }
        DetailAST dot = findFirstAstOfType(packageDefOrImportAST, TokenTypes.DOT);
        if (dot == null) {
            // package name is a single word
            DetailAST packageName = findFirstAstOfType(packageDefOrImportAST, TokenTypes.IDENT);
            return packageName.getText();
        } else {
            // package name has at least one dot
            StringBuilder fullName = new StringBuilder("");
            fullyQualifiedPackageAux(fullName, dot);
            return fullName.toString();
        }
    }

    /**
     * @param classDefToken must be a CLASS_DEF AST.
     * @return the name of the superclass or null if the class definition does not contain an extends clause
     */
    protected String getSuperClassName(DetailAST classDefToken) {
        DetailAST extendsClause = findFirstAstOfType(classDefToken, TokenTypes.EXTENDS_CLAUSE);
        if (extendsClause == null) {
            return null;
        }
        return extendsClause.getFirstChild().getText();
    }

    /**
     * Recursively traverse an expression tree and return all ASTs matching a specific token type.
     * 
     * @return list of DetailAST objects found; returns empty List if none is found.
     */
    protected List<DetailAST> findAllAstsOfType(DetailAST aAST, int type) {
        if (aAST == null) {
            throw new IllegalArgumentException("Parameter aAST must not be null");
        }
        List<DetailAST> children = new ArrayList<DetailAST>();
        DetailAST child = aAST.getFirstChild();
        while (child != null) {
            if (child.getType() == type) {
                children.add(child);
            }
            children.addAll(findAllAstsOfType(child, type));
            child = child.getNextSibling();
        }
        return children;
    }

    /**
     * Recursively traverse a given AST and return the first AST node matching a specific token type within the given AST.
     * This method differs from {@link DetailAST#findFirstToken(int)} in that it searches for the given type
     * in the specified node itself, all children, and indirect descendants (the whole tree), whereas
     * {@link DetailAST#findFirstToken(int)} only searches the direct children.
     * 
     * @return first DetailAST found or null if no node of the given type is found
     * 
     * @see DetailAST#findFirstToken(int)
     */
    protected DetailAST findFirstAstOfType(DetailAST aAST, int type) {
        DetailAST firstAst = null;
        if (aAST == null) {
            throw new IllegalArgumentException("Parameter aAST must not be null");
        }
        if (aAST.getType() == type) {
            firstAst = aAST;
        } else {
            for (DetailAST chld = aAST.getFirstChild(); chld != null && firstAst == null; chld = chld.getNextSibling()) {
                firstAst = findFirstAstOfType(chld, type);
            }
        }
        return firstAst;
    }

    /**
     * Recursive method to traverse the IDENT-DOT-IDENT-DOT-...-IDENT-DOT-IDENT segment of a
     * PACKAGE_DEF or IMPORT token.
     * It builds the fully qualified package name in the StringBuilder parameter.
     */
    private void fullyQualifiedPackageAux(StringBuilder fullName, DetailAST aAST) {
        if (aAST.getType() == TokenTypes.IDENT) {
            // end of the recursion.
            fullName.append(aAST.getText());
        } else {
            // it's a DOT that contains DOTorIDENT followed by IDENT
            DetailAST child = aAST.getFirstChild();
            fullyQualifiedPackageAux(fullName, child);
            fullName.append("." + child.getNextSibling().getText());
        }
    }

    /**
     * @param aAST must be a TokenTypes.CLASS_DEF.
     * @param annotation annotation identifier without '@'
     */
    public boolean containsAnnotation(DetailAST aAST, String annotation) {
        List<DetailAST> annotations = findAllAstsOfType(aAST, TokenTypes.ANNOTATION);
        for (DetailAST an : annotations) {
            DetailAST annotationName = an.findFirstToken(TokenTypes.IDENT);
            if (annotationName != null && annotationName.getText().equals(annotation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given an ASSIGN token, returns the name of the variable being assigned. The position of the variable IDENT token differs
     * whether the ASSIGN is part of a variable definition (declaration) or not.
     */
    protected String getVarNameOfAssign(DetailAST assignToken) {
        if (assignToken.getType() != TokenTypes.ASSIGN) {
            throw new IllegalArgumentException("The type of parameter aAST must be TokenTypes.ASSIGN");
        }
        DetailAST identToken = null;
        DetailAST exprToken = assignToken.findFirstToken(TokenTypes.EXPR);
        if (exprToken != null) {
            // ASSIGN in var declaration. Example: int x = 10;
            // VARIABLE_DEF (MODIFIERS, TYPE, IDENT, ASSIGN (EXPR (right-side value tokens)))
            identToken = assignToken.getPreviousSibling();
        } else if (TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR == assignToken.getParent().getType()) {
            // ASSIGN in annotation. Example: @Bean(name = "daoCategoria")
            // ANNOTATION_MEMBER_VALUE_PAIR (IDENT, ASSIGN, EXPR (right-side value tokens))
            identToken = assignToken.getPreviousSibling();
        } else if (TokenTypes.INDEX_OP == assignToken.getFirstChild().getType()) {
            // ASSIGN of array variable. Example: result[0] = MSG_AO_SEPAS + "\n" + errOut;
            // EXPR (ASSIGN (INDEX_OP (IDENT, EXPR(...), RBRACK), right-side value tokens))
            identToken = assignToken.getFirstChild().getFirstChild();
        } else if (TokenTypes.DOT == assignToken.getFirstChild().getType()) {
            // ASSIGN of member variable. Example: this.login = user.getLogin();
            // EXPR (ASSIGN (DOT (LITERAL_THIS, IDENT), right-side value tokens))
            identToken = assignToken.getFirstChild().findFirstToken(TokenTypes.IDENT);
        } else if (TokenTypes.ARRAY_INIT == assignToken.getFirstChild().getType()) {
            // ASSIGN of array with initialization. Example: String[] headers = {"", "", "", "Distribution, "", "", "x", ""};
            // VARIABLE_DEF (MODIFIERS, TYPE, IDENT, ASSIGN (ARRAY_INIT (EXPR (val tokens), COMMA, EXPR (val tokens), COMMA, ...))
            identToken = assignToken.getPreviousSibling();
        } else if (TokenTypes.LAMBDA == assignToken.getFirstChild().getType()) {
            // ASSIGN of a valute that contians a lambda expression.
            // VARIABLE_DEF (MODIFIERS, TYPE, IDENT, ASSIGN (LAMBDA (IDENT, EXPR)))
            identToken = assignToken.getPreviousSibling();
        } else {
            // ASSIGN of variable previously declared. Example: x = 10;
            // EXPR (ASSIGN (IDENT, right-side value tokens))
            identToken = assignToken.findFirstToken(TokenTypes.IDENT);
        }
        if (identToken != null) {
            return identToken.getText();
        }
        return null;
    }

    /**
     * @return the token (VARIABLE_DEF ou PARAMETER_DEF) where the specified variable (IDENT) was defined.
     * 
     * @param identToken the variable's IDENT
     * 
     * @author Rafael Costa
     */
    protected DetailAST getVariableOrParameterDefForIdent(DetailAST identToken) {
        if (identToken.getType() != TokenTypes.IDENT) {
            throw new IllegalArgumentException("The type of parameter identToken must be TokenTypes.IDENT");
        }
        DetailAST varDefToken = null;
        DetailAST parDefToken = null;
        boolean hasThisPrefix =
                identToken.getPreviousSibling() != null && identToken.getPreviousSibling().getType() == TokenTypes.LITERAL_THIS;
        int closestLine = 0;
        varDefToken = findVariableDefForIdent(identToken, closestLine, hasThisPrefix);

        if (varDefToken != null) {
            closestLine = varDefToken.getLineNo();
        }
        // Walks through PARAMETER_DEF list searching for the var definition
        if (!hasThisPrefix) {
            parDefToken = findParameterDefForIdent(identToken, closestLine);
            if (parDefToken != null) {
                return parDefToken;
            }
        }
        return varDefToken;
    }

    /**
     * Este método busca um token do tipo VARIABLE_DEF que
     * corresponde ao IDENT de chamada de método passado.
     * 
     * @param identToken Token do tipo IDENT da chamada de um método
     * @param closestLine Número da linha de início
     * @param isThisCall Boolean que define se a chamada ao método teve um 'this.'
     * 
     * @author x05119695116 Rafael Costa
     */

    private DetailAST findVariableDefForIdent(DetailAST identToken, int closestLine, boolean isThisCall) {
        DetailAST root = getClassToken(identToken);
        DetailAST classDefToken = null;
        DetailAST defToken = null;
        List<DetailAST> variableDefs = findAllAstsOfType(root, TokenTypes.VARIABLE_DEF);
        // Percorre a lista de VARIABLE_DEF procurando a definição da variável que chamou o método
        // (que esteja definida antes da chamada)
        for (DetailAST current : variableDefs) {
            if (getScopeOfDef(current).getType() == TokenTypes.CLASS_DEF) {
                classDefToken = current;
            }
            if (getVarNameInVariableOrParameterDef(current).equals(identToken.getText()) &&
                    current.getLineNo() <= identToken.getLineNo()) {
                if (!isThisCall && current.getLineNo() > closestLine) {
                    if (getScopeOfDef(current).getLineNo() == getScopeOfDef(identToken).getLineNo() ||
                            getScopeOfDef(current).getLineNo() == root.getLineNo()) {
                        defToken = current;
                        closestLine = current.getLineNo();
                    }
                    // No caso de chamada com 'this.', retorna a primeira definição sob um CLASS_DEF
                } else {
                    return classDefToken;
                }
            }
        }

        return defToken;
    }

    /**
     * Este método busca um token do tipo PARAMETER_DEF que
     * corresponde ao IDENT de chamada de método passado.
     * 
     * @param identToken Token do tipo IDENT da chamada de um método
     * @param closestLine Número da linha de início
     * 
     * @author x05119695116 Rafael Costa
     */
    private DetailAST findParameterDefForIdent(DetailAST identToken, int closestLine) {
        DetailAST root = getClassToken(identToken);
        DetailAST defToken = null;
        List<DetailAST> parameterDefs = findAllAstsOfType(root, TokenTypes.PARAMETER_DEF);
        for (DetailAST current : parameterDefs) {
            if (getVarNameInVariableOrParameterDef(current).equals(identToken.getText()) &&
                    current.getLineNo() <= identToken.getLineNo() &&
                    current.getLineNo() > closestLine &&
                    getScopeOfDef(current).getLineNo() == getScopeOfDef(identToken).getLineNo()) {
                defToken = current;
                closestLine = current.getLineNo();
            }
        }

        return defToken;
    }

    /**
     * Este método retorna o token de escopo da variável
     * passada por parâmetro.
     * 
     * @param defToken Token da definição da variável
     * 
     * @author x05119695116 Rafael Costa
     */
    protected DetailAST getScopeOfDef(DetailAST defToken) {
        DetailAST parent = defToken;
        while (parent.getType() != TokenTypes.CLASS_DEF && parent.getType() != TokenTypes.CTOR_DEF &&
                parent.getType() != TokenTypes.METHOD_DEF) {
            parent = parent.getParent();
        }
        return (parent);
    }

    /**
     * Este método verifica se a classe classDefToken está
     * dentro de uma outra classe.
     * 
     * @param defToken Token de definição da classe (CLASS_DEF)
     * 
     * @author x05119695116 Rafael Costa
     */
    protected boolean isInnerClass(DetailAST classDefToken) {
        DetailAST token = getClassToken(classDefToken);
        if (token != null && token.getType() == TokenTypes.CLASS_DEF) {
            return true;
        }
        return false;
    }

    /**
     * Este método retorna o nome literal da variável definida
     * pelo token passado.
     * 
     * @param defToken Token VARIABLE_DEF ou PARAMETER_DEF
     * @author x05119695116 Rafael Costa
     */
    protected String getVarNameInVariableOrParameterDef(DetailAST defToken) {
        if (defToken.getType() != TokenTypes.VARIABLE_DEF && defToken.getType() != TokenTypes.PARAMETER_DEF) {
            throw new IllegalArgumentException("Parameter defToken must be a VARIABLE_DEF or PARAMETER_DEF");
        }
        // Example:
        // int varName
        // Parses as:
        //
        // +--VARIABLE_DEF
        // |
        // |+--MODIFIERS
        // |+--TYPE
        // ||-+--LITERAL_INT (int)
        // |+--IDENT (varName) <- Returning token
        // |+--SEMI (;)
        //
        DetailAST identToken = defToken.getFirstChild();
        identToken = identToken.getNextSibling().getNextSibling();
        return (identToken.getText());
    }

    /**
     * Este método retorna o nome literal da variável definida
     * pelo token passado.
     * 
     * @param defToken Token de definição
     * @author x05119695116 Rafael Costa
     */
    protected DetailAST getClassToken(DetailAST defToken) {
        DetailAST parent = defToken.getParent();
        while (parent != null && parent.getType() != TokenTypes.CLASS_DEF) {
            parent = parent.getParent();
        }
        return parent;
    }

}
