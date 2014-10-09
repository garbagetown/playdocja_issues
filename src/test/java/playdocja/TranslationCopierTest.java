package playdocja;

import static org.apache.commons.lang3.StringUtils.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TranslationCopierTest {

    private static final String TEST_RESOURCES = "src/test/resources/copy/";
    private static final String BACKUP_SUFFIX = "~";
    
    @Before
    public void setUp() throws Exception {
        backupDir(Paths.get(TEST_RESOURCES, "to_dir").toFile());
    }

    @After
    public void tearDown() throws Exception {
        rollbackDir(Paths.get(TEST_RESOURCES, "to_dir").toFile());
    }

    @Test
    public void copyFile() {
        Path from = Paths.get(TEST_RESOURCES, "from_dir", "test1.md");
        Path to = Paths.get(TEST_RESOURCES, "to_dir", "test1.md");
        new TranslationCopier().copy(from, to);
        
        List<Pair<List<String>, List<String>>> pairs = parse(to);
        assertThat(pairs.size(), is(6));
    }

    @Test
    public void copyDir() {
        Path from = Paths.get(TEST_RESOURCES, "from_dir");
        Path to = Paths.get(TEST_RESOURCES, "to_dir");
        new TranslationCopier().copy(from, to);
        
        assertThat(parse(Paths.get(TEST_RESOURCES, "to_dir", "test1.md")).size(), is(6));
        assertThat(parse(Paths.get(TEST_RESOURCES, "to_dir", "dir1", "test2.md")).size(), is(6));
        assertThat(parse(Paths.get(TEST_RESOURCES, "to_dir", "dir2", "test3.md")).size(), is(6));
        assertThat(parse(Paths.get(TEST_RESOURCES, "to_dir", "dir2", "test4.md")).size(), is(6));
    }
        
    private List<Pair<List<String>, List<String>>> parse(Path path) {
        return new TranslationParser(path).parse();
    }

    private void backupDir(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                backupDir(file);
            } else {
                backupFile(file);
            }
        }
    }

    private void backupFile(File file) {
        Path source = file.toPath();
        Path target = Paths.get(source.getParent().toString(), source.getFileName().toString() + BACKUP_SUFFIX);
        try {
            Files.copy(source, target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void rollbackDir(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                rollbackDir(file);
            } else {
                rollbackFile(file);
            }
        }
    }

    private void rollbackFile(File file) {
        
        if (!file.getName().endsWith(BACKUP_SUFFIX)) {
            return;
        }
        
        Path source = file.toPath();
        
        String dir = file.getParent().toString();
        String name = file.getName().replace(BACKUP_SUFFIX, EMPTY);
        Path target = Paths.get(dir, name);
        
        try {
            Files.delete(target);
            Files.move(source, target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
