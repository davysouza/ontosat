import br.usp.ime.Saturator;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.io.UnparsableOntologyException;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SaturatorTest {

    @Test
    public void testConstructorNullParameter() {
        Throwable exception = assertThrows(
                NullPointerException.class,
                () -> new Saturator(null));

        assertEquals("file cannot be null", exception.getMessage());
    }

    @Test
    public void testConstructorInvalidFile() {
        String resourceName = "invalid-ontology-file.owl";

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourceName).getFile());

        assertThrows(UnparsableOntologyException.class, () -> new Saturator(file));
    }
}
