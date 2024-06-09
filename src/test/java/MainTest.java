import br.usp.ime.Main;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
            "Usage: java -cp ontosat.jar [options]\r\n\r\n" +
            "where options include:\r\n" +
            "    -h --help\r\n" +
            "                   prints the help message\r\n" +
            "    -i --ontology\r\n" +
            "                   <specifies the path to the input ontology>\r\n" +
            "    -o --saturated-ontology\r\n" +
            "                   <specifies the path where the saturated ontology will be stored>\r\n" +
            "    -m --mode\r\n" +
            "                   defines the saturation mode, which can be either \"assertional\" or\r\n" +
            "                   \"terminological\". The \"assertional\" mode is selected by default\r\n";

    private static final String headerContent = "OntoSat\n\r\n";

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
        Main.main(null);

        String expected = headerContent + helpContent;
        Assertions.assertEquals(expected, outContent.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-h", "--help"})
    public void testHelper(String argument) {
        String[] args = { argument };
        Main.main(args);

        String expected = headerContent + helpContent;
        Assertions.assertEquals(expected, outContent.toString());
    }

    @ParameterizedTest
    @CsvSource({"-m,invalid", "--mode,invalid"})
    public void testInvalidMode(String command, String mode) {
        String[] args = { command, mode };
        Main.main(args);

        String error = "Error while parsing arguments.\r\n"
                + "Exception caught: Invalid saturation mode\n\r\n";
        String expected = headerContent + error + helpContent;
        Assertions.assertEquals(expected, outContent.toString());
    }

    @ParameterizedTest
    @CsvSource({"-m,terminological", "--mode,terminological"})
    public void testMissingArguments(String command, String mode) {
        String[] args = { command, mode };
        Main.main(args);

        String error = "Missing arguments.\r\n";
        String expected = headerContent + error + helpContent;
        Assertions.assertEquals(expected, outContent.toString());
    }

    @Test
    public void testSaturation() {
        String[] args = {
                "-o",
                "C:\\Projetos\\ontosat\\src\\main\\resources\\ontologies\\ontology.owl"
        };

        // Assertions.assertEquals(0, Main.main(args));
    }
    // endregion
}
