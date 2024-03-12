package br.usp.ime;

import org.apache.commons.io.FilenameUtils;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    // region attributes

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    private static Command command = Command.NoCommand;

    private static File ontologyFile;

    private static String saturatedOntologyPath = System.getProperty("user.home") + "\\Desktop\\";

    private static boolean customOutputPath = false;

    private enum Command {
        NoCommand,
        Help,
        Saturate
    };

    // endregion

    public static void main(String[] args) {
        logger.info("TBox Saturator started.\n");
        System.out.println("TBox Saturator\n");

        // args = new String[2];
        // args[0] = "-o";
        // args[1] = "C:\\Projetos\\tbox-saturator\\src\\main\\resources\\ontologies\\01-ontology-cade-28.owl";
        // args[1] = "C:\\Projetos\\tbox-saturator\\src\\main\\resources\\ontologies\\02-ontology-jelia-23.owl";

        if(!parseArgs(args)) {
            help();
            return ;
        }

        switch (command) {
            case NoCommand, Help -> help();
            case Saturate -> saturate();
        }
    }

    // region private methods

    private static boolean parseArgs(String[] args) {
        logger.info("Parsing arguments...");

        if(args == null) {
            logger.error("Invalid parameters");
            return false;
        }

        if(args.length == 0) {
            return true;
        }

        try {
            int i = 0;
            command = Command.Saturate;

            while(i < args.length) {
                switch (args[i++]) {
                    case "-h", "--help" -> command = Command.Help;
                    case "-o", "--ontology" -> ontologyFile = new File(args[i++]);
                    case "-O", "--saturated-ontology" -> {
                        saturatedOntologyPath = args[i++];
                        customOutputPath = true;
                    }
                }
            }

            if(command == Command.Saturate && ontologyFile == null) {
                System.out.println("Missing arguments.");
                logger.error("Missing arguments.");

                return false;
            }

        } catch (Exception e) {
            System.out.println("Error while parsing arguments.");

            logger.error("Error while parsing arguments.");
            logger.debug("Exception caught: " + e.getMessage());

            return false;
        }

        return true;
    }

    private static void saturate() {
        System.out.println("Running saturator...");
        logger.info("Starting saturation...");

        try {
            Saturator saturator = new Saturator(ontologyFile);
            OWLOntology saturatedOntology = saturator.saturate();

            logger.info("Saving ontology...");

            String outputPath = saturatedOntologyPath;
            if (!customOutputPath) {
                outputPath += FilenameUtils.removeExtension(ontologyFile.getName()) + "-saturated.owl";
            }

            OntologyHelper.save(saturatedOntology, outputPath);
        } catch (Exception e) {
            System.out.println("Error while saturating.");

            logger.error("Error while saturating.");
            logger.debug("Exception caught: " + e.getMessage());
        }
    }

    private static void help() {
        System.out.println("Usage: java -cp tbox-saturator.jar [options]");
        System.out.println();
        System.out.println("where options include:");
        System.out.println("    -h --help");
        System.out.println("                   prints this help message");
        System.out.println("    -o --ontology");
        System.out.println("                   <ontology path to be saturated>");
        System.out.println("    -O --saturated-ontology");
        System.out.println("                   <saturated ontology path>");
        System.out.println("    -a --add-class");
        System.out.println("                   add a named class for each new restriction added during");
        System.out.println("                   the saturation process");
    }

    // endregion
}