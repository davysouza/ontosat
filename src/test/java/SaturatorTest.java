import br.usp.ime.ontosat.Saturator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

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
        String resourceName = "ontologies/000-invalid-file.owl";

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourceName).getFile());

        assertThrows(UnparsableOntologyException.class, () -> new Saturator(file));
    }

    @ParameterizedTest
    @CsvSource({
            "ontologies/001-test.owl,responses/001-test-saturated.owl",
            "ontologies/002-test.owl,responses/002-test-saturated.owl",
            "ontologies/003-test.owl,responses/003-test-saturated.owl",
            "ontologies/004-test.owl,responses/004-test-saturated.owl",
            "ontologies/005-test.owl,responses/005-test-saturated.owl",
            "ontologies/006-test.owl,responses/006-test-saturated.owl",
            "ontologies/007-test.owl,responses/007-test-saturated.owl",
            "ontologies/008-test.owl,responses/008-test-saturated.owl",
            "ontologies/009-test.owl,responses/009-test-saturated.owl",
            "ontologies/010-test.owl,responses/010-test-saturated.owl",
            "ontologies/011-test.owl,responses/011-test-saturated.owl",
            "ontologies/012-test.owl,responses/012-test-saturated.owl",
            "ontologies/013-test.owl,responses/013-test-saturated.owl",
            "ontologies/014-test.owl,responses/014-test-saturated.owl",
            "ontologies/015-test.owl,responses/015-test-saturated.owl",
            "ontologies/016-test.owl,responses/016-test-saturated.owl",
            "ontologies/017-test.owl,responses/017-test-saturated.owl",
            "ontologies/018-test.owl,responses/018-test-saturated.owl",
            "ontologies/019-test.owl,responses/019-test-saturated.owl",
            "ontologies/020-test.owl,responses/020-test-saturated.owl",
            "ontologies/021-test.owl,responses/021-test-saturated.owl",
            "ontologies/022-test.owl,responses/022-test-saturated.owl",
            "ontologies/023-test.owl,responses/023-test-saturated.owl",
            "ontologies/024-test.owl,responses/024-test-saturated.owl",
            "ontologies/025-test.owl,responses/025-test-saturated.owl",
            "ontologies/026-test.owl,responses/026-test-saturated.owl",
            "ontologies/100-sample-cade-28.owl,responses/100-sample-cade-28-saturated.owl",
            "ontologies/101-sample-jelia-23.owl,responses/101-sample-jelia-23-saturated.owl",
    })
    public void saturatorTests(String ontologyResourceName, String expectedResourceName)
            throws OWLOntologyCreationException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(ontologyResourceName).getFile());

        Saturator saturator = new Saturator(file);
        OWLOntology saturatedOntology = saturator.saturate();

        File responseFile = new File(classLoader.getResource(expectedResourceName).getFile());
        OWLOntology expected = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(responseFile);

        assertEquals(true, expected.getAxioms().equals(saturatedOntology.getAxioms()));
    }
}
