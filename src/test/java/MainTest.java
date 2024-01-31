import br.usp.ime.Main;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class MainTest {
    private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private static final PrintStream originalOut = System.out;
    private static final PrintStream originalErr = System.err;

    // region expected values
    private static final String helpContent =
            "Usage: java -cp tbox-saturator.jar [options]\r\n\r\n" +
            "where options include:\r\n" +
            "    -h --help\r\n" +
            "                   prints this help message\r\n" +
            "    -o --ontology\r\n" +
            "                   <ontology path to be saturated>\r\n" +
            "    -s --saturation-request\r\n" +
            "                   <path to the assertions used to saturate the ontology>\r\n" +
            "    -O --saturated-ontology\r\n" +
            "                   <saturated ontology path>\r\n";

    private static final String headerContent = "TBox Saturator\n\r\n";

    // endregion

    // region gear up / tear down

    @BeforeAll
    public static void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterAll
    public static void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @AfterEach
    public void cleanStreams() {
        outContent.reset();
        errContent.reset();
    }

    // endregion

    // region tests
    @Test
    public void testNoParameter() {
        // Assertions.assertEquals(1, Main.main(null));

        // String expected = headerContent + helpContent;
        // Assertions.assertEquals(expected, outContent.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-h", "--help"})
    public void testHelper(String argument) {
        // String[] args = { argument };
        // Assertions.assertEquals(0, Main.main(args));

        // String expected = headerContent + helpContent;
        // Assertions.assertEquals(expected, outContent.toString());
    }

    @Test
    public void testSaturation() {
        String[] args = {
                "-o",
                "C:\\Projetos\\tbox-saturator\\src\\main\\resources\\ontologies\\ontology.owl",
                "-s",
                "C:\\Projetos\\tbox-saturator\\src\\main\\resources\\saturation-request.owl"
        };

        // Assertions.assertEquals(0, Main.main(args));
    }
    // endregion
}
