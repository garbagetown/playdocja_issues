package playdocja;

/**
 * 
 * @author garbagetown
 *
 */
public class Issue {
    public String label;
    public String path;
    public String body;
    public Issue(String label, String path, String body) {
        this.label = label;
        this.path = path;
        this.body = body;
    }
}