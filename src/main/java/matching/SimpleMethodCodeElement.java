package matching;

import java.util.Set;

/**
 * Created by arianna on 29/05/17.
 *
 * Very simplistic representation of a method code element (for testing purposes).
 */

public class SimpleMethodCodeElement {
    private String forMethod;
    private String codeElementName;
    private Set<String> codeElementIds;

    public SimpleMethodCodeElement(String forMethod, String codeElementName, Set<String> codeElementIds) {
        this.forMethod = forMethod;
        this.codeElementName = codeElementName;
        this.codeElementIds = codeElementIds;
    }

    public String getForMethod() {
        return forMethod;
    }

    public String getCodeElementName() {
        return codeElementName;
    }

    public Set<String> getCodeElementIds() {
        return codeElementIds;
    }

    @Override
    public String toString() {
        return "method: "+codeElementName;
    }
}
