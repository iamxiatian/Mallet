/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */


/**
 Takes a list of directory names as arguments, (each directory
 should contain all the text files for each class), performs a random train/test split,
 trains a classifier, and outputs accuracy on the testing and training sets.

 @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

package cc.mallet.classify.examples;

import cc.mallet.classify.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.FileIterator;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Multinomial;
import org.apache.commons.cli.*;

import java.io.*;

public class DC2 {

    private void saveClassifier(Classifier classifier, File modelFile)
            throws IOException {
        FileOutputStream out = new FileOutputStream(modelFile);
        ObjectOutputStream oos =
                new ObjectOutputStream(out);
        oos.writeObject(classifier);
        oos.flush();
        out.flush();

        oos.close();
        out.close();
    }

    private Classifier loadClassifier(File modelFile)
            throws IOException, ClassNotFoundException {
        Classifier classifier = null;

        ObjectInputStream ois =
                new ObjectInputStream (new FileInputStream (modelFile));
        classifier = (Classifier) ois.readObject();
        ois.close();

        return classifier;
    }

    private Pipe makePipe() {
        String encoding = "GBK";

        // Create the pipeline that will take as input {data = File, target = String for classname}
        // and turn them into {data = FeatureVector, target = Label}
        Pipe instancePipe = new SerialPipes(new Pipe[]{
                new Target2Label(),                              // Target String -> class label
                new Input2CharSequence(encoding),                  // Data File ->
                // String containing contents
                //new CharSubsequence(CharSubsequence.SKIP_HEADER), // Remove
                // UseNet or email header
                //new CharSequence2TokenSequence(),  // Data String -> TokenSequence
                new ChineseSequence2TokenSequence(),  // Data String ->TokenSequence
                new TokenSequenceLowercase(),          // TokenSequence words lowercased
                new TokenSequenceRemoveStopwords(),// Remove stopwords from sequence
                new TokenSequenceWord2Vec(),
                new TokenSequence2FeatureSequence(),// Replace each Token with a feature index
                new FeatureSequence2FeatureVector(),// Collapse word order into a "feature vector"
                new PrintInputAndTarget(false),
        });
        return instancePipe;
    }

    private void classify(InstanceList trainList, InstanceList testList) {
        // Create a classifier trainer, and use it to create a classifier
        ClassifierTrainer naiveBayesTrainer = new NaiveBayesTrainer();
        Classifier classifier = naiveBayesTrainer.train(trainList);

        try {
            saveClassifier(classifier, new File("/home/xiatian/expt/fudan/bayes" +
                    ".bin"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("The training accuracy is "
                + classifier.getAccuracy(trainList));
        System.out.println("The testing accuracy is "
                + classifier.getAccuracy(testList));
    }

    public void classify(File corpusDirs) {
        Pipe instancePipe = makePipe();

        // Create an empty list of the training instances
        InstanceList instanceList = new InstanceList(instancePipe);

        // Add all the files in the directories to the list of instances.
        // The Instance that goes into the beginning of the instancePipe
        // will have a File in the "data" slot, and a string from args[] in the "target" slot.
        instanceList.addThruPipe(new FileIterator(corpusDirs, FileIterator.STARTING_DIRECTORIES));

        // Make a test/train split; ilists[0] will be for training; ilists[1] will be for testing
        InstanceList[] ilists = instanceList.split(new double[]{.5, .5});

        classify(ilists[0], ilists[1]);
    }

    public void classify(File[] trainDirs, File[] testDirs) {
        Pipe instancePipe = makePipe();

        // Create an empty list of the training instances
        InstanceList trainList = new InstanceList(instancePipe);

        // Add all the files in the directories to the list of instances.
        // The Instance that goes into the beginning of the instancePipe
        // will have a File in the "data" slot, and a string from args[] in the "target" slot.
        trainList.addThruPipe(new FileIterator(trainDirs,
                FileIterator.LAST_DIRECTORY));

        InstanceList testList = new InstanceList(instancePipe);

        // Add all the files in the directories to the list of instances.
        // The Instance that goes into the beginning of the instancePipe
        // will have a File in the "data" slot, and a string from args[] in the "target" slot.
        testList.addThruPipe(new FileIterator(testDirs,
                FileIterator.LAST_DIRECTORY));

        classify(trainList, testList);
    }

    public void trainClassifier(File[] trainDirs, File modelFile) throws IOException {
        Pipe instancePipe = makePipe();

        InstanceList trainList = new InstanceList(instancePipe);
        trainList.addThruPipe(new FileIterator(trainDirs,
                FileIterator.LAST_DIRECTORY));

        // Create a classifier trainer, and use it to create a classifier
        ClassifierTrainer<NaiveBayes> naiveBayesTrainer = new
                NaiveBayesTrainer();
        NaiveBayes classifier = naiveBayesTrainer.train(trainList);
        saveClassifier(classifier, modelFile);
        System.out.println("The training accuracy is "
                + classifier.getAccuracy(trainList));
    }

    public void testClassifier(File modelFile, File[] testDirs) throws
            IOException, ClassNotFoundException {
        Pipe instancePipe = makePipe();

        InstanceList testList = new InstanceList(instancePipe);
        testList.addThruPipe(new FileIterator(testDirs,
                FileIterator.LAST_DIRECTORY));

        NaiveBayes classifier = (NaiveBayes)loadClassifier(modelFile);

//        double accuracy = classifier.getAccuracy(testList);
//        System.out.println("Accuracy is " + accuracy);
        Multinomial.Logged priors = classifier.getPriors();
        priors.print();

        Trial trial = new Trial(classifier, testList);

        System.out.println("The test accuracy is " + trial.getAccuracy());
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        DC2 classifier = new DC2();
        HelpFormatter helpFormatter = new HelpFormatter();
        CommandLineParser parser = new PosixParser();
        Options options = new Options();

        String formatString = "DocumentClassifier [-train -f model output " +
                "file] | [-test -f " +
                "model file]  -dir the parent dir that contains sample folder";

        options.addOption(new Option("train", false, "train the classifier"));
        options.addOption(new Option("test", false, "test the classifier"));
        options.addOption(new Option("f", true, "model file"));
        options.addOption(new Option("wvm", true, "word2vc model file"));
        options.addOption(new Option("dir", true, "the directory that " +
                "contains subdir and " + "each subdir represents a " +
                "classification"));
        options.addOption("h", "help", false, "print help for the command.");

        try {
            CommandLine cmdLine = parser.parse(options, args);
            if (cmdLine.hasOption("h")
                    || !cmdLine.hasOption("f")
                    || !cmdLine.hasOption("dir")
                    || !cmdLine.hasOption("wvm")) {
                helpFormatter.printHelp(formatString, options);
                return;
            }

            if ((cmdLine.hasOption("train") && cmdLine.hasOption("test"))
                    || (!cmdLine.hasOption("train") && !cmdLine.hasOption(
                    ("test")))) {
                System.out.println("Must specify train or test parameter.");
                helpFormatter.printHelp(formatString, options);
                return;
            }

            File modelFile = new File(cmdLine.getOptionValue("f"));

            File dir = new File(cmdLine.getOptionValue("dir"));
            if (!dir.exists()) {
                System.out.println(dir.getAbsoluteFile() + " does not exists.");
                return;
            } else if (dir.listFiles().length == 0) {
                System.out.println(dir.getAbsoluteFile() + " has no sub dirs.");
                return;
            }

            File[] subDirs = dir.listFiles();

            TokenSequenceWord2Vec.load(cmdLine.getOptionValue("wvm"));
            if (cmdLine.hasOption("train")) {
                System.out.println("train classifier from " + dir.getAbsolutePath());
                classifier.trainClassifier(subDirs, modelFile);
                System.out.println("Done!");
            } else {
                System.out.println("test classifier from " + dir.getAbsolutePath());
                classifier.testClassifier(modelFile, subDirs);
                System.out.println("Done!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
