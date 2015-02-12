/* This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */

package cc.mallet.classify;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Label;

import java.util.HashMap;
import java.util.TreeMap;


/**
 * A Classifier that will return the most frequent class label based on a training set.
 * The Classifier needs to be trained using the MostFrequentClassAssignmentTrainer.
 *
 * @author Martin Wunderlich <a href="mailto:martin@wunderlich.com">martin@wunderlich.com</a>
 * @see MostFrequentClassAssignmentTrainer
 */
public class MostFrequentClassifier extends Classifier {

    /**
     *
     */
    private static final long serialVersionUID = -2685212760735255652L;
    TreeMap<String, Integer> sortedLabelMap = new TreeMap<String, Integer>();
    HashMap<String, Label> labels = new HashMap<String, Label>();

    public MostFrequentClassifier(Pipe instancePipe) {
        this.instancePipe = instancePipe;
    }

    /**
     * Classify an instance using the most frequent class label.
     *
     * @param Instance to be classified. Data field must be a FeatureVector.
     * @return Classification containing the labeling of the instance.
     */
    @Override
    public Classification classify(Instance instance) {
        String mostFrequentLabelStr = this.sortedLabelMap.firstEntry().getKey();
        Label mostFrequentLabel = this.labels.get(mostFrequentLabelStr);
        Classification mostFrequentClassification = new Classification(instance, this, mostFrequentLabel);

        return mostFrequentClassification;
    }

    public void addTargetLabel(Label label) {
        String labelEntry = (String) label.getEntry();

        if (!this.labels.containsKey(labelEntry)) {
            this.sortedLabelMap.put(labelEntry, new Integer(1));
            this.labels.put(labelEntry, label);
        } else {
            Integer oldCount = this.sortedLabelMap.get(labelEntry);
            Integer newCount = oldCount + 1;
            this.sortedLabelMap.put(labelEntry, newCount);
            this.labels.put(labelEntry, label);
        }
    }
}
