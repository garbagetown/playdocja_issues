package playdocja;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHMilestone;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

public class IssueRegister {

    private static Logger logger = Logger.getLogger(IssueRegister.class.getName());

    public static final String MARKDOWN_EXT = ".md";

    public static final String LABEL_UPDATE = "update";
    public static final String LABEL_NEW = "new";

    private Path basepath;
    private Path oldpath;
    private Path newpath;
    private Properties prop;

    /**
     *
     * @param basepath
     * @param olddir
     * @param newdir
     */
    public IssueRegister(Path basepath, String olddir, String newdir) {
        this.basepath = basepath;
        this.oldpath = basepath.resolve(olddir).toAbsolutePath();
        this.newpath = basepath.resolve(newdir).toAbsolutePath();
        this.prop = new Properties();
        try {
            InputStream stream = IssueRegister.class.getResourceAsStream("../github.properties");
            prop.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return
     */
    public List<Issue> getDiffsAsIssues() {

        String command = String.format("diff -qr %s %s", oldpath, newpath);
        logger.debug(command);

        List<String> results = execute(command);

        List<Issue> issues = new ArrayList<Issue>();
        for (String result : results) {
            logger.debug(result);

            Pattern p = Pattern.compile(String.format("^Files %s/(.+) and %s/(.+) differ$", oldpath, newpath));
            Matcher m = p.matcher(result);
            if (m.find()) {
                String path = m.group(1);
                if (path.endsWith(MARKDOWN_EXT)) {
                    issues.add(toUpdate(path));
                }
                continue;
            }

            p = Pattern.compile(String.format("^Only in %s/(.+)", newpath));
            m = p.matcher(result.replace(": ", "/"));
            if (m.find()) {
                String path = m.group(1);
                issues.addAll(toNew(path));
                continue;
            }
        }
        return issues;
    }

    /**
     *
     * @param path
     * @return
     */
    private Issue toUpdate(String path) {
        logger.debug(String.format("[%-6s] %s/%s", LABEL_UPDATE, newpath, path));

        String baseurl = prop.getProperty("baseurl");
        String olddir = basepath.toAbsolutePath().relativize(oldpath).toString();
        String newdir = basepath.toAbsolutePath().relativize(newpath).toString();
        String oldtitle = olddir + "/" + path;
        String newtitle = newdir + "/" + path;

        StringBuilder body = new StringBuilder();
        body.append(String.format("- %s/%s\n", baseurl, newtitle));
        body.append(String.format("- diffs between [%s](%s/%s) ", oldtitle, baseurl, oldtitle));
        body.append(String.format("and [%s](%s/%s).\n", newtitle, baseurl, newtitle));

        String command = String.format("diff -u %s/%s /%s/%s", oldpath, path, newpath, path);
        List<String> results = execute(command);

        body.append("```\n");
        for (String result : results) {
            result = result.replace("```", "'''");
            body.append(result);
            body.append("\n");
        }
        body.append("\n```");

        return new Issue(LABEL_UPDATE, path, body.toString());
    }

    /**
     *
     * @param path
     * @return
     */
    private List<Issue> toNew(String path) {
        logger.debug(String.format("[%-6s] %s/%s", LABEL_NEW, newpath, path));

        String baseurl = prop.getProperty("baseurl");
        String newdir = basepath.toAbsolutePath().relativize(newpath).toString();

        List<File> files = findFiles(newpath.resolve(path).toFile());
        List<Issue> issues = new ArrayList<>();
        for (File file : files) {
            path = newpath.relativize(file.toPath()).toString();
            String body = String.format("- %s/%s/%s", baseurl, newdir, path);
            issues.add(new Issue(LABEL_NEW, path, body));
        }
        return issues;
    }

    /**
     *
     * @param file
     * @return
     */
    private List<File> findFiles(File file) {
        List<File> files = new ArrayList<>();
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                files.addAll(findFiles(f));
            }
        } else {
            if (file.getName().endsWith(MARKDOWN_EXT)) {
                files.add(file);
            }
        }
        return files;
    }

    /**
     *
     * @param command
     * @return
     */
    private List<String> execute(String command) {
        List<String> lines = new ArrayList<String>();
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }

    /**
     *
     * @return
     */
    public GHRepository getRepository() {
        String l = prop.getProperty("login");
        String p = prop.getProperty("password");
        String o = prop.getProperty("organization");
        String r = prop.getProperty("repository");
        try {
            GitHub github = GitHub.connectUsingPassword(l, p);
            GHRepository repository = github.getRepository(o + "/" + r);
            return repository;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param issues
     * @throws IOException
     * @throws NumberFormatException
     * @throws InterruptedException
     */
    public void registerIssues(List<Issue> issues) throws NumberFormatException, IOException, InterruptedException {

        GHRepository repository = getRepository();

        String s = prop.getProperty("milestone");
        int n = -1;

        Iterator<GHMilestone> it = repository.listMilestones(GHIssueState.OPEN).iterator();
        while (it.hasNext()) {
            GHMilestone m = it.next();
            logger.debug(m.getNumber() + ":" + m.getTitle());
            if (m.getTitle().equals(s)) {
                n = m.getNumber();
            }
        }
        if (n < 0) {
            throw new RuntimeException();
        }
        GHMilestone milestone = repository.getMilestone(n);

        int count = 0;
        for (Issue issue : issues) {
            count += 1;
            if (count <= 72) {
                continue;
            }
            logger.debug(String.format("create %s issue: %s", issue.label, issue.path));
            GHIssueBuilder builder = repository.createIssue(issue.path);
            builder.milestone(milestone);
            builder.label(issue.label);
            builder.body(issue.body);
            builder.create();
            Thread.sleep(30_000);
        }
    }

    /**
     *
     * @param args
     * @throws IOException
     * @throws InterruptedException
     * @throws NumberFormatException
     */
    public static void main(String[] args) throws IOException, NumberFormatException, InterruptedException {

        Path basepath = Paths.get(args[0]);
        String olddir = args[1];
        String newdir = args[2];

        IssueRegister register = new IssueRegister(basepath, olddir, newdir);

        List<Issue> issues = register.getDiffsAsIssues();

        int u = 0;
        int n = 0;
        for (Issue issue : issues) {
            logger.info(String.format("[%-6s] %s", issue.label, issue.path));
            switch (issue.label) {
            case LABEL_UPDATE:
                u++;
                break;
            case LABEL_NEW:
                n++;
                break;
            default:
                break;
            }
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            logger.warn(String.format("%d update issues and %d new issues will be created: [y/N]", u, n));
            String answer = reader.readLine();
            if (answer.equals("y")) {
                register.registerIssues(issues);
            }
        }
    }
}
