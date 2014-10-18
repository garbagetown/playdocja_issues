package playdocja;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
    
    @Test
    public void ignoreImagesAndCodes() {
        List<Issue> issues = getDiffsAsIssues("test5");
        assertThat(issues.size(), is(1));
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
        return issueRegister(dir).getDiffsAsIssues();
    }

    /**
     * 
     * @param dir
     * @return
     */
    private IssueRegister issueRegister(String dir) {
        Path basepath = Paths.get("src/test/resources/issue/", dir);
        String olddir = "old";
        String newdir = "new";
        return new IssueRegister(basepath, olddir, newdir);
    }
    
    @Test
    public void getRepository() {
        GHRepository repository = issueRegister("test1").getRepository();
        assertNotNull(repository);
    }

}
