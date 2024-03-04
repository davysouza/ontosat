package br.usp.ime;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Saturates the TBox w.r.t. the role assertions found at the ABox. <br/>
 *
 * For each role assertion <b>r(x,y)</b> we find all classes <b>C</b> such
 * that <b>C(y)</b> is in the ABox. For each class found we saturate the
 * TBox with a new restriction equivalent to: <b>"r some C"</b>.
 * On the other hand, for each role assertion in the format <b>s(y,z)<b/>,
 * we add the restriction r some (s some X) where X is an assertion.
 */
public class Saturator {

    // region attributes
    private static Logger logger = LoggerFactory.getLogger(Saturator.class);

    // region ontology
    private OWLOntologyManager ontologyManager;
    private OWLDataFactory owlDataFactory;
    private OWLOntology ontology;
    private OWLOntology saturatedOntology;
    // endregion ontology

    // region graph
    private static final int UNVISITED = -1;
    private static final int EXPLORED = 0;
    private static final int VISITED = 1;
    private Map<OWLIndividual, Map<OWLIndividual, OWLObjectProperty>> graph;
    private HashMap<OWLIndividual, Integer> nodes;
    // endregion graph
    // endregion attributes

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

        createRelationGraph();
    }

    public OWLOntology saturate() {
        try {
            saturatedOntology = ontologyManager.createOntology();
            ontologyManager.addAxioms(saturatedOntology, ontology.getAxioms());

            for (OWLNamedIndividual individual : ontology.getIndividualsInSignature()) {
                if (nodes.get(individual) == UNVISITED) {
                    ontologyManager.addAxioms(saturatedOntology, DFS(null, individual));
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

    private void createRelationGraph() {
        logger.info("Creating graph of relations");

        graph = new HashMap<>();
        nodes = new HashMap<>();

        for (OWLNamedIndividual individual : ontology.getIndividualsInSignature()) {
            graph.put(individual, new HashMap<>());
            nodes.put(individual, UNVISITED);
        }

        for (OWLAxiom axiom : ontology.getAxioms()) {
            if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
                OWLObjectPropertyAssertionAxiom propertyAssertion = (OWLObjectPropertyAssertionAxiom) axiom;
                OWLObjectProperty property = propertyAssertion.getProperty().getNamedProperty();

                OWLIndividual subject = propertyAssertion.getSubject();
                OWLIndividual object = propertyAssertion.getObject();

                graph.get(subject).put(object, property);
            }
        }
    }

    private ArrayList<OWLAxiom> DFS(OWLIndividual parent, OWLIndividual node) {
        nodes.put(node, EXPLORED);

        ArrayList<OWLAxiom> axioms = new ArrayList<>();

        graph.get(node).forEach((u, property) -> {
            if (nodes.get(u) == UNVISITED) {
                axioms.addAll(DFS(node, u));
            } else if (nodes.get(u) == EXPLORED) {
                logger.info("DFS: Cycle found.");

                axioms.addAll(createRoleAssertionAxioms(axioms, node, property));
                axioms.addAll(createClassAssertionAxioms(node, u, property));
            }
        });

        if (parent != null) {
            var property = graph.get(parent).get(node);

            axioms.addAll(createRoleAssertionAxioms(axioms, parent, property));
            axioms.addAll(createClassAssertionAxioms(parent, node, property));
        }

        nodes.put(node, VISITED);

        return axioms;
    }

    private ArrayList<OWLAxiom> createRoleAssertionAxioms(ArrayList<OWLAxiom> axioms,
                                                          OWLIndividual subject,
                                                          OWLObjectProperty property) {
        ArrayList<OWLAxiom> newAxioms = new ArrayList<>();

        for (OWLAxiom axiom : axioms) {
            OWLClassAssertionAxiom classAssertionAxiom = (OWLClassAssertionAxiom) axiom;

            OWLObjectSomeValuesFrom owlObjectSomeValuesFrom =
                    owlDataFactory.getOWLObjectSomeValuesFrom(property, classAssertionAxiom.getClassExpression());

            classAssertionAxiom = owlDataFactory.getOWLClassAssertionAxiom(owlObjectSomeValuesFrom, subject);

            newAxioms.add(classAssertionAxiom);
        }

        axioms.addAll(newAxioms);

        return axioms;
    }

    private ArrayList<OWLAxiom> createClassAssertionAxioms(OWLIndividual subject,
                                                           OWLIndividual object,
                                                           OWLObjectProperty property) {
        ArrayList<OWLAxiom> axioms = new ArrayList<>();

        for(OWLClassAssertionAxiom assertionAxiom : saturatedOntology.getClassAssertionAxioms(object)) {
            for(OWLClass owlClass : assertionAxiom.getClassesInSignature()) {
                OWLObjectSomeValuesFrom owlObjectSomeValuesFrom =
                        owlDataFactory.getOWLObjectSomeValuesFrom(property, owlClass);

                OWLClassAssertionAxiom classAssertionAxiom =
                        owlDataFactory.getOWLClassAssertionAxiom(owlObjectSomeValuesFrom, subject);

                axioms.add(classAssertionAxiom);
            }
        }

        return axioms;
    }
    // endregion
}