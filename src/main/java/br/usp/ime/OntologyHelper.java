package br.usp.ime;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class OntologyHelper {

    // region attributes
    private static Logger logger = LoggerFactory.getLogger(OntologyHelper.class);

    private static OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();

    // endregion

    // region public methods

    /**
     * Save ontology in RDF/XML format at the specified path
     * @param ontology OWLOntology instance to be saved
     * @param path Path where the ontology file should be saved
     * @throws OWLOntologyStorageException If ontology cannot be saved
     */
    public static void save(OWLOntology ontology, String path) throws OWLOntologyStorageException {
        ontologyManager.setOntologyFormat(ontology, new RDFXMLDocumentFormat());
        ontologyManager.saveOntology(ontology, IRI.create(new File(path).toURI()));
        logger.info("Ontology saved at: " + path);
    }

    /**
     * Parse the specified file that should contain Manchester Syntax axioms
     * @param ontology OWL default ontology
     * @param file File to be parsed
     * @return A set of the parsed axioms
     * @throws FileNotFoundException If file is not found
     */
    public static Set<OWLAxiom> parseManchesterSyntax(OWLOntology ontology, File file) throws FileNotFoundException {
        Set<OWLOntology> importsClosure = ontology.getImportsClosure();
        var providerAdapter = new BidirectionalShortFormProviderAdapter(ontologyManager, importsClosure, new SimpleShortFormProvider());
        OWLEntityChecker entityChecker = new ShortFormEntityChecker(providerAdapter);

        ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();
        parser.setDefaultOntology(ontology);
        parser.setOWLEntityChecker(entityChecker);

        Set<OWLAxiom> parsedAxioms = new HashSet<>();

        try(Scanner scanner = new Scanner(file)) {
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                parser.setStringToParse(line);

                parsedAxioms.add(parser.parseAxiom());
            }
        }

        return parsedAxioms;
    }

    // endregion
}
