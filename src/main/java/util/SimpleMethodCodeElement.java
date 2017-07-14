package util;

import java.util.Set;

/**
 * Created by arianna on 29/05/17.
 *
 * Very simplistic representation of a method code element (for testing purposes).
 */

public class SimpleMethodCodeElement{
    private String forMethod;
    private String codeElementName;
    private String returnType;
    private Set<String> codeElementIds;

    public SimpleMethodCodeElement(String forMethod,String codeElementName,  String returnType, Set<String> codeElementIds) {
        this.forMethod = forMethod;
        this.codeElementName = codeElementName;
        this.returnType = returnType;
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

    public String getReturnType() { return returnType; }

    @Override
    public String toString() {
        return "method: "+codeElementName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleMethodCodeElement that = (SimpleMethodCodeElement) o;

        if (forMethod != null ? !forMethod.equals(that.forMethod) : that.forMethod != null) return false;
        if (codeElementName != null ? !codeElementName.equals(that.codeElementName) : that.codeElementName != null)
            return false;
        if (returnType != null ? !returnType.equals(that.returnType) : that.returnType != null) return false;
        return codeElementIds != null ? codeElementIds.equals(that.codeElementIds) : that.codeElementIds == null;
    }

    @Override
    public int hashCode() {
        int result = forMethod != null ? forMethod.hashCode() : 0;
        result = 31 * result + (codeElementName != null ? codeElementName.hashCode() : 0);
        result = 31 * result + (returnType != null ? returnType.hashCode() : 0);
        result = 31 * result + (codeElementIds != null ? codeElementIds.hashCode() : 0);
        return result;
    }
}
