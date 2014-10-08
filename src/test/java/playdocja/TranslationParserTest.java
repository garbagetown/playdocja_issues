package playdocja;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TranslationParserTest {

    TranslationParser parser;
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void parseRegularCase() throws Exception {
        List<Pair<List<String>, List<String>>> pairs = 
                new TranslationParser(Paths.get("src/test/resources/test1.md")).parse();
        assertThat(pairs.size(), is(4));
    }

    @Test
    public void parseHasTranslatedTag() throws Exception {
        List<Pair<List<String>, List<String>>> pairs = 
                new TranslationParser(Paths.get("src/test/resources/test2.md")).parse();
        assertThat(pairs.size(), is(5));
    }
    
    @Test
    public void parseHasInlineComment() throws Exception {
        List<Pair<List<String>, List<String>>> pairs = 
                new TranslationParser(Paths.get("src/test/resources/test3.md")).parse();
        assertThat(pairs.size(), is(6));
        assertThat(pairs.get(5).getLeft().size(), is(7));
        assertThat(pairs.get(5).getRight().size(), is(7));
    }
    
    @Test
    public void parseHasSourceCode() throws Exception {
        List<Pair<List<String>, List<String>>> pairs = 
                new TranslationParser(Paths.get("src/test/resources/test4.md")).parse();
        assertThat(pairs.size(), is(9));
    }
}
