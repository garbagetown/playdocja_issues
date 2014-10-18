package playdocja;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHRepository;

/**
 * 
 * @author garbagetown
 *
 */
public class IssueRegisterTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void noDiffs() {
        List<Issue> issues = getDiffsAsIssues("test1");
        assertThat(issues.size(), is(0));
    }

    /**
     * 
     * @param dir
     * @return
     */
    private List<Issue> getDiffsAsIssues(String dir) {
        Path basepath = Paths.get("src/test/resources/issue/", dir);
        String olddir = "old";
        String newdir = "new";
        return new IssueRegister().getDiffsAsIssues(basepath, olddir, newdir);
    }

    @Test
    public void getRepository() throws IOException {
        Properties prop = new Properties();
        try (InputStream stream = this.getClass().getResourceAsStream("../github.properties")) {
            prop.load(stream);
        }
        String l = prop.getProperty("login");
        String p = prop.getProperty("password");
        String o = prop.getProperty("login");
        String r = prop.getProperty("repository");
        GHRepository repository = new IssueRegister().getRepository(l, p, o, r);
        assertNotNull(repository);
    }

}
