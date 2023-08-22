package spark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import spark.util.SparkTestUtil;

import static spark.Spark.get;
import static org.junit.Assert.*;
import static spark.util.SparkTestUtil.*;

/**
 * @since 2022/07/26.
 */
public class RequestSlashTest {
    public static final int PORT = 4567;

    private static final SparkTestUtil http = new SparkTestUtil(PORT);

    static List<Checker> checks;

    static class Checker {
        final String serverPath;
        // User inputs which should match
        final String match;
        // User inputs which should not match
        final String noMatch;
        Checker(String serverPath, String match, String noMatch) {
            this.serverPath = serverPath;
            this.match = match;
            this.noMatch = noMatch;
        }
        String getPath() {
            return (serverPath.equals("") || serverPath.equals("/?")) ? "/" : serverPath;
        }
    }

    @BeforeClass
    public static void setup() throws IOException {
        checks = new ArrayList<Checker>() {{
            /*
            // Root Match: Slash should be optional in the server:
            add(new Checker("/","/","/user/"));
            // File match: Opening slash should be optional (server).
            add(new Checker("file1","/file1","/file1/"));
            add(new Checker("/file2","/file2","/file2/"));
            // Directory match: Opening slash should be optional (server).
            add(new Checker("dir1/","/dir1/","/dir1"));
            add(new Checker("/dir2/","/dir2/","/dir2"));
            // Trailing slash optional:
            add(new Checker("dir3/?","/dir3","/dir3/dir"));
            add(new Checker("dir4/?","/dir4/","/dir4/dir/"));
            add(new Checker("/dir5/?","/dir5","/dir"));
            add(new Checker("/dir6/?","/dir6/","/dir/"));
             */
            // Trailing slash optional with parameter:
            add(new Checker("dir7/:id/?","/dir7/1234","/dir7/dir/1111"));
            add(new Checker("dir8/:id/?","/dir8/1234/","/dir8/dir/2222/"));
            add(new Checker("/dir9/:id/?","/dir9/1234","/dir9"));
            add(new Checker("/dir10/:id/?","/dir10/1234/","/dir10/"));
            //add(new Checker("","",""));
        }};
        for(Checker c : checks) {
            get(c.serverPath, (request, response) -> {
                System.out.printf("Requested: %s, Matched: %s %n", request.pathInfo(), request.matchedPath());
                assertEquals(c.getPath(), request.matchedPath());
                return "ok";
            });
        }
        Spark.awaitInitialization();
    }

    @AfterClass
    public static void tearDown() {
        Spark.stop();
    }

    @Test
    public void pathShouldMatch() {
        for(Checker c : checks) {
            try {
                UrlResponse res = http.get(c.match);
                assertEquals(200, res.status);
                assertEquals("ok", res.body);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void pathShouldNotMatch() {
        for(Checker c : checks) {
            try {
                UrlResponse res = http.get(c.noMatch);
                assertEquals(404, res.status);
                assertNotEquals("ok", res.body);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
