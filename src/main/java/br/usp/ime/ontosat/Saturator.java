package br.usp.ime.ontosat;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Saturates the ontology w.r.t. the existent assertions. <br/>
 *
 * For each role assertion <b>r(x,y)</b> we find all classes <b>C</b> such
 * that <b>C(y)</b> is in the ABox. For each class found we saturate the
 * TBox with a new restriction equivalent to: <b>"r some C"</b>.
 * On the other hand, for each role assertion in the format <b>s(y,z)<b/>,
 * we add the restriction r some (s some X) where X is an assertion.
 */
public class Saturator {

    // region public attributes

    /**
     * Saturation modes
     */
    public enum SaturationMode {
        /// Assertional mode saturates the ontology with new assertions
        /// based on the role assertions found during the process.
        Assertional,

        /// Terminological mode saturates the ontology adding classes
        /// equivalent to axioms inferred during the saturation process.
        Terminological
    }
    // endregion public attributes

    // region private attributes

    private static Logger logger = LoggerFactory.getLogger(Saturator.class);
    private SaturationMode saturationMode = SaturationMode.Assertional;

    // region ontology
    private OWLOntologyManager ontologyManager;
    private OWLDataFactory owlDataFactory;
    private OWLOntology ontology;
    private OWLOntology saturatedOntology;
    // endregion ontology

    // region graph
    private enum NodeStatus {
        UNVISITED,
        EXPLORED,
        VISITED
    }

    /**
     * The adjacency map representing the graph of relations. Each node (individual)
     * is mapped to another map that represent the pairs of connected individuals
     * with the properties defining their relationships.
     */
    private Map<OWLIndividual, Map<OWLIndividual, Set<OWLObjectProperty>>> graph;

    /**
     * A map of each node and its status
     */
    private Map<OWLIndividual, NodeStatus> nodes;

    /**
     * A map of partial responses of DFS
     */
    private Map<OWLIndividual, Set<OWLAxiom>> responses;
    // endregion graph

    // endregion private attributes

    // region constructors

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

        createGraph();
    }

    /**
     * Initializes the saturator with the specified ontology
     * @param ontologyFile A File instance related to the specified ontology
     * @param saturationMode Saturation mode. Assertional mode selected by default.
     * @throws OWLOntologyCreationException Could not load the ontology from
     * the specified file.
     */
    public Saturator(File ontologyFile, SaturationMode saturationMode) throws OWLOntologyCreationException {
        this(ontologyFile);
        this.saturationMode = saturationMode;
    }

    // endregion constructors

    // region public methods

    /**
     * Saturates the loaded ontology through a DFS on the relation's graph.
     * @return An {@link OWLOntology} object of the saturated ontology.
     */
    public OWLOntology saturate() {
        try {
            saturatedOntology = ontologyManager.createOntology();
            ontologyManager.addAxioms(saturatedOntology, ontology.getAxioms());

            responses = new HashMap<>();

            // calls the adapted DFS for each unvisited node
            for (OWLNamedIndividual individual : ontology.getIndividualsInSignature()) {
                if (nodes.get(individual) == NodeStatus.UNVISITED) {
                    ontologyManager.addAxioms(saturatedOntology, DFS(individual));
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

    private void createGraph() {
        logger.info("Creating graph of relations");

        graph = new HashMap<>();
        nodes = new HashMap<>();

        // initializes the adjacency map as empty and all nodes as unvisited
        for (OWLNamedIndividual individual : ontology.getIndividualsInSignature()) {
            addNode(individual);
        }

        // for each property assertion axiom (role assertion) an edge
        // is added to the graph
        for (OWLAxiom axiom : ontology.getABoxAxioms(Imports.EXCLUDED)) {
            if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
                addEdge((OWLObjectPropertyAssertionAxiom) axiom);
            }
        }
    }

    private void addNode(OWLIndividual individual) {
        graph.put(individual, new HashMap<>());
        nodes.put(individual, NodeStatus.UNVISITED);
    }

    private void addEdge(OWLObjectPropertyAssertionAxiom axiom) {
        OWLObjectProperty property = axiom.getProperty().getNamedProperty();

        OWLIndividual subject = axiom.getSubject();
        OWLIndividual object = axiom.getObject();

        // if the edge (subject, object) already exists, the saturator
        // will just add the new property found
        if(graph.get(subject).containsKey(object)) {
            graph.get(subject).get(object).add(property);
        } else {
            Set<OWLObjectProperty> properties = new HashSet<>();
            properties.add(property);

            graph.get(subject).put(object, properties);
        }
    }

    /**
     * An adapted DFS that for each generate new axioms for the ontology
     * based on the graph of relations.
     * @param node An {@link OWLOntology} object of the current node
     * @return A set of the generated axioms
     */
    private Set<OWLAxiom> DFS(OWLIndividual node) {
        if(responses.containsKey(node)) {
            nodes.put(node, NodeStatus.VISITED);
            return responses.get(node);
        }

        nodes.put(node, NodeStatus.EXPLORED);

        Set<OWLAxiom> axioms = new HashSet<>();

        graph.get(node).forEach((u, properties) -> {
            axioms.addAll(createAxiomsFromObjectClasses(node, u, properties));

            if (nodes.get(u) == NodeStatus.UNVISITED) {
                axioms.addAll(createAxiomsFromChain(DFS(u), node, properties, u));
            } else if (nodes.get(u) == NodeStatus.VISITED) {
                axioms.addAll(createAxiomsFromChain(responses.get(u),node, properties, u));
            }
        });

        nodes.put(node, NodeStatus.VISITED);

        responses.put(node, axioms);

        return axioms;
    }

    /**
     * Create a chain of new axioms based on the given axioms.
     * @param axioms An {@link ArrayList<OWLAxiom>} of axioms to be used in the chain
     * @param subject An {@link OWLOntology} representing the subject individual
     * @param properties A {@link Set<OWLObjectProperty>} of the properties that
     *                   connect subject to the object.
     * @return An {@link Set<OWLAxiom>} of the new axioms.
     */
    private Set<OWLAxiom> createAxiomsFromChain(Set<OWLAxiom> axioms,
                                                OWLIndividual subject,
                                                Set<OWLObjectProperty> properties,
                                                OWLIndividual object) {
        Set<OWLAxiom> newAxioms = new HashSet<>();

        for (OWLAxiom axiom : axioms) {
            OWLClassAssertionAxiom classAssertionAxiom = (OWLClassAssertionAxiom) axiom;

            if (classAssertionAxiom.getIndividual() == object) {
                for (OWLObjectProperty property : properties) {
                    if (saturationMode == SaturationMode.Assertional) {
                        OWLObjectSomeValuesFrom owlObjectSomeValuesFrom =
                                owlDataFactory.getOWLObjectSomeValuesFrom(property, classAssertionAxiom.getClassExpression());

                        newAxioms.add(owlDataFactory.getOWLClassAssertionAxiom(owlObjectSomeValuesFrom, subject));
                    } else {
                        // TODO: add axioms when Terminological mode is selected
                    }
                }
            }
        }

        axioms.addAll(newAxioms);

        return axioms;
    }

    /**
     * Create new axioms based on the classes of the object individual. For each property,
     * given the axiom "property(subject, object)", we check all classes of the object and
     * add the axiom "\exists property.Class(subject)" for each class found. If Terminological
     * mode is selected, then it creates a new class "propertyClass" equivalent to the
     * "\exists property.Class(subject)" axiom.
     * @param subject An {@link OWLOntology} representing the subject individual
     * @param object An {@link OWLOntology} representing the object individual
     * @param properties A {@link Set<OWLObjectProperty>} of the properties that
     *                   connect subject to the object.
     * @return An {@link ArrayList<OWLAxiom>} of the new axioms.
     */
    private ArrayList<OWLAxiom> createAxiomsFromObjectClasses(OWLIndividual subject,
                                                              OWLIndividual object,
                                                              Set<OWLObjectProperty> properties) {
        ArrayList<OWLAxiom> axioms = new ArrayList<>();

        if (saturatedOntology.getClassAssertionAxioms(object).isEmpty()) {
            OWLClass thingClass = owlDataFactory.getOWLThing();

            for (OWLObjectProperty property : properties) {
                if (saturationMode == SaturationMode.Assertional) {
                    axioms.add(createClassAssertionAxiom(property, thingClass, subject));
                } else {
                    axioms.addAll(createEquivalentClassesAxiom(property, thingClass));
                }
            }
        } else {
            for (OWLClassAssertionAxiom assertionAxiom : saturatedOntology.getClassAssertionAxioms(object)) {
                if (assertionAxiom.getClassExpression() instanceof OWLClass) {
                    for (OWLClass owlClass : assertionAxiom.getClassesInSignature()) {
                        for (OWLObjectProperty property : properties) {
                            if (saturationMode == SaturationMode.Assertional) {
                                axioms.add(createClassAssertionAxiom(property, owlClass, subject));
                            } else {
                                axioms.addAll(createEquivalentClassesAxiom(property, owlClass));
                            }
                        }
                    }
                }
            }
        }
        return axioms;
    }

    private OWLClassAssertionAxiom createClassAssertionAxiom(OWLObjectProperty property,
                                                             OWLClass owlClass,
                                                             OWLIndividual individual) {
        OWLObjectSomeValuesFrom owlObjectSomeValuesFrom =
                owlDataFactory.getOWLObjectSomeValuesFrom(property, owlClass);

        return owlDataFactory.getOWLClassAssertionAxiom(owlObjectSomeValuesFrom, individual);
    }

    private ArrayList<OWLAxiom> createEquivalentClassesAxiom(OWLObjectProperty property,
                                                             OWLClass owlClass) {

        OWLClass newClass = owlDataFactory.getOWLClass(getClassIRI(property, owlClass));

        var axioms = new ArrayList<OWLAxiom>();
        axioms.add(addClassDeclaration(newClass));
        axioms.add(addClassEquivalence(property, owlClass, newClass));

        return axioms;
    }

    private IRI getClassIRI(OWLObjectProperty property, OWLClass owlClass) {
        String propertyName = property.getNamedProperty().getIRI().getRemainder().get();
        String className = owlClass.getIRI().getRemainder().get();
        String namespace = owlClass.getIRI().getNamespace();

        return IRI.create(namespace + propertyName + className);
    }

    private OWLAxiom addClassDeclaration(OWLClass newClass) {
        OWLAxiom declarationAxiom = owlDataFactory.getOWLDeclarationAxiom(newClass);
        return declarationAxiom;
    }

    private OWLEquivalentClassesAxiom addClassEquivalence(OWLObjectProperty property,
                                                          OWLClass originalClass,
                                                          OWLClass newClass) {

        OWLObjectSomeValuesFrom owlObjectSomeValuesFrom =
                owlDataFactory.getOWLObjectSomeValuesFrom(property, originalClass);

        return owlDataFactory.getOWLEquivalentClassesAxiom(newClass, owlObjectSomeValuesFrom);
    }

    // endregion
}