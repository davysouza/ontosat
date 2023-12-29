package br.usp.ime;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Saturates the TBox w.r.t. the role assertions found at the ABox. <br/>
 *
 * For each role assertion <b>r(x,y)</b> we find all classes <b>C</b> such
 * that <b>C(y)</b> is in the ABox. For each class found we saturate the
 * TBox with a new class <b>rC</b> equivalent to: <b>"rC : r some C"</b>
 */
public class Saturator {

    // region attributes
    private static Logger logger = LoggerFactory.getLogger(Saturator.class);

    private OWLOntologyManager ontologyManager;

    private OWLDataFactory owlDataFactory;

    private OWLOntology ontology;

    private OWLOntology saturatedOntology;

    // endregion

    // region public methods

    /**
     * Initializes the saturator with the specified ontology
     * @param ontologyFile A File instance related to the specified ontology
     * @throws OWLOntologyCreationException Could not load the ontology from
     * the specified file.
     */
    public Saturator(File ontologyFile) throws OWLOntologyCreationException {
        logger.info("Initializing saturator");

        owlDataFactory = OWLManager.getOWLDataFactory();
        ontologyManager = OWLManager.createOWLOntologyManager();
        ontology = ontologyManager.loadOntologyFromOntologyDocument(ontologyFile);
    }

    /**
     * Saturates ontology
     * @return An OWLOntology instance of the saturated ontology
     */
    public OWLOntology saturate() {
        try {
            saturatedOntology = ontologyManager.createOntology();
            ontologyManager.addAxioms(saturatedOntology, ontology.getAxioms());

            for(OWLAxiom axiom : saturatedOntology.getAxioms()) {
                if(axiom instanceof OWLObjectPropertyAssertionAxiom) {
                    saturateWithProperty((OWLObjectPropertyAssertionAxiom) axiom);
                }
            }

            return saturatedOntology;

        } catch (OWLOntologyCreationException e) {
            logger.error("Failed to saturate ontology.");
            logger.debug("Exception caught: " + e.getMessage());

            return null;
        }
    }

    // endregion

    // region private methods

    private IRI getClassIRI(OWLObjectPropertyAssertionAxiom property, OWLClass owlClass) {
        String propertyName = property.getProperty().getNamedProperty().getIRI().getRemainder().get();
        String className = owlClass.getIRI().getRemainder().get();
        String namespace = owlClass.getIRI().getNamespace();

        return IRI.create(namespace + propertyName + className);
    }

    private void addClass(OWLClass newClass) {
        OWLAxiom declarationAxiom = owlDataFactory.getOWLDeclarationAxiom(newClass);
        ontologyManager.addAxiom(saturatedOntology, declarationAxiom);
    }

    private void addRestriction(OWLObjectPropertyAssertionAxiom property, OWLClass originalClass, OWLClass newClass) {
        OWLObjectSomeValuesFrom owlObjectSomeValuesFrom = owlDataFactory.getOWLObjectSomeValuesFrom(property.getProperty(), originalClass);
        OWLEquivalentClassesAxiom owlEquivalentClassesAxiom = owlDataFactory.getOWLEquivalentClassesAxiom(newClass, owlObjectSomeValuesFrom);
        ontologyManager.addAxiom(saturatedOntology, owlEquivalentClassesAxiom);
    }

    private void saturateWithProperty(OWLObjectPropertyAssertionAxiom property) {
        OWLIndividual object = property.getObject();

        for(OWLClassAssertionAxiom assertionAxiom : saturatedOntology.getClassAssertionAxioms(object)) {
            for(OWLClass owlClass : assertionAxiom.getClassesInSignature()) {
                OWLClass newClass = owlDataFactory.getOWLClass(getClassIRI(property, owlClass));

                addClass(newClass);
                addRestriction(property, owlClass, newClass);
            }
        }
    }

    // endregion
}
