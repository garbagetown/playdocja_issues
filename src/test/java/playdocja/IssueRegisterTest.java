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

    @Test
    public void hasDiffs() {
        List<Issue> issues = getDiffsAsIssues("test2");
        assertThat(issues.size(), is(2));
        assertIssue(issues, 0, IssueRegister.LABEL_UPDATE, "Home.md");
        assertIssue(issues, 1, IssueRegister.LABEL_UPDATE, "about/Philosophy.md");
    }
    
    @Test
    public void hasNewFile() {
        List<Issue> issues = getDiffsAsIssues("test3");
        assertThat(issues.size(), is(4));
        assertIssue(issues, 0, IssueRegister.LABEL_NEW, "Highlights23.md");
        assertIssue(issues, 1, IssueRegister.LABEL_UPDATE, "Home.md");
        assertIssue(issues, 2, IssueRegister.LABEL_UPDATE, "about/Philosophy.md");
        assertIssue(issues, 3, IssueRegister.LABEL_NEW, "about/PlayUserGroups.md");
    }
    
    @Test
    public void hasNewDir() {
        List<Issue> issues = getDiffsAsIssues("test4");
        assertThat(issues.size(), is(6));
        assertIssue(issues, 0, IssueRegister.LABEL_NEW, "Highlights23.md");
        assertIssue(issues, 1, IssueRegister.LABEL_UPDATE, "Home.md");
        assertIssue(issues, 2, IssueRegister.LABEL_UPDATE, "about/Philosophy.md");
        assertIssue(issues, 3, IssueRegister.LABEL_NEW, "about/PlayUserGroups.md");
        assertIssue(issues, 4, IssueRegister.LABEL_NEW, "scalaGuide/_Sidebar.md");
        assertIssue(issues, 5, IssueRegister.LABEL_NEW, "scalaGuide/ScalaHome.md");
    }
    
    /**
     * 
     * @param issues
     * @param index
     * @param label
     * @param path
     */
    private void assertIssue(List<Issue> issues, int index, String label, String path) {
        assertThat(issues.get(index).label, is(label));
        assertThat(issues.get(index).path, is(path));
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
