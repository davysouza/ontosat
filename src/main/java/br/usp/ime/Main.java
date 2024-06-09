package br.usp.ime;

import org.apache.commons.io.FilenameUtils;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    // region attributes

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    private static Command command = Command.NoCommand;

    private static SaturationMode saturationMode = SaturationMode.Assertional;

    private static File ontologyFile;

    private static String saturatedOntologyPath = System.getProperty("user.home") + "\\Desktop\\";

    private static boolean customOutputPath = false;

    private enum Command {
        NoCommand,
        Help,
        Saturate
    };

    private enum SaturationMode {
        Assertional,
        Terminological
    }

    // endregion

    public static void main(String[] args) {
        logger.info("OntoSat started.\n");
        System.out.println("OntoSat\n");

        // args = new String[2];
        // args[0] = "-i";
        // args[1] = "C:\\Projetos\\ontosat\\src\\main\\resources\\ontologies\\01-ontology-cade-28.owl";
        // args[1] = "C:\\Projetos\\ontosat\\src\\main\\resources\\ontologies\\02-ontology-jelia-23.owl";

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
                    case "-i", "--ontology" -> ontologyFile = new File(args[i++]);
                    case "-o", "--saturated-ontology" -> {
                        saturatedOntologyPath = args[i++];
                        customOutputPath = true;
                    }
                    case "-m", "--mode" -> {
                        String mode = args[i++];
                        if (mode.equals("assertional")) {
                            saturationMode = SaturationMode.Assertional;
                        } else if (mode.equals("terminological")) {
                            saturationMode = SaturationMode.Terminological;
                        } else {
                            throw new Exception("Invalid saturation mode");
                        }
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
            System.out.println("Exception caught: " + e.getMessage() + "\n");

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
        System.out.println("Usage: java -cp ontosat.jar [options]");
        System.out.println();
        System.out.println("where options include:");
        System.out.println("    -h --help");
        System.out.println("                   prints the help message");
        System.out.println("    -i --ontology");
        System.out.println("                   <specifies the path to the input ontology>");
        System.out.println("    -o --saturated-ontology");
        System.out.println("                   <specifies the path where the saturated ontology will be stored>");
        System.out.println("    -m --mode");
        System.out.println("                   defines the saturation mode, which can be either \"assertional\" or");
        System.out.println("                   \"terminological\". The \"assertional\" mode is selected by default");
    }

    // endregion
}