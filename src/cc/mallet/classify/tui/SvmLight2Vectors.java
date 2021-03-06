/* Copyright (C) 2010 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */

package cc.mallet.classify.tui;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.PrintInputAndTarget;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.SvmLight2FeatureVectorAndLabel;
import cc.mallet.pipe.iterator.SelectiveFileLineIterator;
import cc.mallet.types.InstanceList;
import cc.mallet.util.CommandOption;
import cc.mallet.util.MalletLogger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Command line import tool for loading a sequence of
 * instances from an SVMLight feature-value pair file, with one instance
 * per line of the input file.
 * <p>
 * <p>
 * The expected format is
 * <p>
 * target feature:value feature:value ...
 * <p>
 * targets and features can be indices, as in
 * SVMLight, or Strings.
 * <p>
 * Note that if targets and features are indices,
 * their indices in the data and target Alphabets
 * may be different, though the data will be
 * equivalent.
 * <p>
 * Note that the input and output args can take multiple files.
 *
 * @author Gregory Druck
 */

public class SvmLight2Vectors {

    static CommandOption.SpacedStrings inputFiles = new CommandOption.SpacedStrings
            (SvmLight2Vectors.class, "input", "FILE", true, null,
                    "The files containing data to be classified, one instance per line", null);
    static CommandOption.SpacedStrings outputFiles = new CommandOption.SpacedStrings
            (SvmLight2Vectors.class, "output", "FILE", true, null,
                    "Write the instance list to this file; Using - indicates stdout.", null);
    static CommandOption.File usePipeFromVectorsFile = new CommandOption.File
            (SvmLight2Vectors.class, "use-pipe-from", "FILE", true, new File("text.vectors"),
                    "Use the pipe and alphabets from a previously created vectors file.\n" +
                            "   Allows the creation, for example, of a test set of vectors that are\n" +
                            "   compatible with a previously created set of training vectors", null);
    static CommandOption.Boolean printOutput = new CommandOption.Boolean
            (SvmLight2Vectors.class, "print-output", "[TRUE|FALSE]", false, false,
                    "If true, print a representation of the processed data\n" +
                            "   to standard output. This option is intended for debugging.", null);
    static CommandOption.String encoding = new CommandOption.String
            (SvmLight2Vectors.class, "encoding", "STRING", true, Charset.defaultCharset().displayName(),
                    "Character encoding for input file", null);
    private static Logger logger = MalletLogger.getLogger(SvmLight2Vectors.class.getName());

    public static void main(String[] args) throws FileNotFoundException, IOException {
        // Process the command-line options
        CommandOption.setSummary(SvmLight2Vectors.class,
                "A tool for creating instance lists of feature vectors from comma-separated-values");
        CommandOption.process(SvmLight2Vectors.class, args);

        // Print some helpful messages for error cases
        if (args.length == 0) {
            CommandOption.getList(SvmLight2Vectors.class).printUsage(false);
            System.exit(-1);
        }
        if (inputFiles == null) {
            throw new IllegalArgumentException("You must include `--input FILE FILE ...' in order to specify " +
                    "files containing the instances, one per line.");
        }

        Pipe instancePipe;
        InstanceList previousInstanceList = null;

        if (usePipeFromVectorsFile.wasInvoked()) {

            // Ignore all options, use a previously created pipe

            previousInstanceList = InstanceList.load(usePipeFromVectorsFile.value);
            instancePipe = previousInstanceList.getPipe();
        } else {
            // Build a new pipe
            ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
            pipeList.add(new SvmLight2FeatureVectorAndLabel());
            if (printOutput.value) {
                pipeList.add(new PrintInputAndTarget());
            }
            instancePipe = new SerialPipes(pipeList);
        }

        if (inputFiles.value.length != outputFiles.value.length) {
            throw new RuntimeException("Number of input and output files must be the same.");
        }

        InstanceList[] instances = new InstanceList[inputFiles.value.length];
        for (int fileIndex = 0; fileIndex < inputFiles.value.length; fileIndex++) {
            // Create the instance list and open the input file
            instances[fileIndex] = new InstanceList(instancePipe);
            Reader fileReader;
            if (inputFiles.value[fileIndex].equals("-")) {
                fileReader = new InputStreamReader(System.in);
            } else {
                fileReader = new InputStreamReader(new FileInputStream(inputFiles.value[fileIndex]), encoding.value);
            }

            // Read instances from the file
            instances[fileIndex].addThruPipe(new SelectiveFileLineIterator(fileReader, "^\\s*#.+"));
        }

        // gdruck@cs.umass.edu
        // If we have multiple files, the data or target alphabet may have new
        // elements added to it with each new file. If we save each InstanceList
        // immediately after processing each file, then Alphabets won't be the
        // same.  Instead, process all files before writing the InstanceLists.
        for (int fileIndex = 0; fileIndex < inputFiles.value.length; fileIndex++) {
            // Save instances to output file
            instances[fileIndex].save(new File(outputFiles.value[fileIndex]));
        }

        //  If we are reusing a pipe from an instance list
        //  created earlier, we may have extended the label
        //  or feature alphabets. To maintain compatibility,
        //  we now save that original instance list back to disk
        //  with the new alphabet.
        if (usePipeFromVectorsFile.wasInvoked()) {
            logger.info(" Rewriting extended pipe from " + usePipeFromVectorsFile.value);
            logger.info("  Instance ID = " + previousInstanceList.getPipe().getInstanceId());
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(usePipeFromVectorsFile.value));
            oos.writeObject(previousInstanceList);
            oos.close();
        }
    }
}

	

