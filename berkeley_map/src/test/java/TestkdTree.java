import org.junit.Test;
import org.junit.Before;
import org.junit.Test;

public class TestkdTree {
    private static GraphDB graphSmall;
    private static GraphDB graphTiny;
    private static final String OSM_DB_PATH_TINY = "../library-su18/bearmaps/tiny-clean.osm.xml";
    private static final String OSM_DB_PATH_SMALL = "../library-su18/bearmaps/berkeley-2018-small.osm.xml";

    private static boolean initialized = false;

    @Before
    public void setUp() throws Exception {
        if (initialized) {
            return;
        }
        graphTiny = new GraphDB(OSM_DB_PATH_TINY);
        graphSmall = new GraphDB(OSM_DB_PATH_SMALL);
        initialized = true;
    }

    @Test
    public void kdtConstructor() {
        System.out.println(graphTiny.closest( -122.22, 37.89));
        Node n = new Node("1200", "100000", "10000000");
    }
}
