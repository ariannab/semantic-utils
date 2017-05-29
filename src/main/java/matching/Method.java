package matching;

import java.util.Set;

/**
 * Created by arianna on 29/05/17.
 */
public class Method {
    private String name;
    private Set<String> ids;

    public Method(String name, Set<String> ids) {
        this.name = name;
        this.ids = ids;
    }

    public String getName() {
        return name;
    }

    public Set<String> getIds() {
        return ids;
    }
}
