/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */


/**
 @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

package cc.mallet.types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class LabelSequence extends FeatureSequence implements AlphabetCarrying, Serializable {
    private static final long serialVersionUID = 1;
    private static final int CURRENT_SERIAL_VERSION = 0;

    public LabelSequence(LabelAlphabet dict, int[] features) {
        super(dict, features);
    }

    public LabelSequence(LabelAlphabet dict, int capacity) {
        super(dict, capacity);
    }

    public LabelSequence(Label[] labels) {
        super(labels[0].getLabelAlphabet(), getFeaturesFromLabels(labels));
    }

    public LabelSequence(Alphabet dict) {
        super(dict);
    }

    private static int[] getFeaturesFromLabels(Label[] labels) {
        int[] features = new int[labels.length];
        for (int i = 0; i < labels.length; i++)
            features[i] = labels[i].getIndex();
        return features;
    }

    public LabelAlphabet getLabelAlphabet() {
        return (LabelAlphabet) dictionary;
    }

    public Label getLabelAtPosition(int pos) {
        return ((LabelAlphabet) dictionary).lookupLabel(features[pos]);
    }


    // ???
    //public Object get (int pos) {	return getLabelAtPosition (pos); }


    // Serialization

    public Iterator iterator() {
        return new Iterator();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int version = in.readInt();
    }

    public class Iterator implements java.util.Iterator {
        int pos;

        public Iterator() {
            pos = 0;
        }

        public Object next() {
            return getLabelAtPosition(pos++);
        }

        public int getIndex() {
            return pos;
        }

        public boolean hasNext() {
            return pos < features.length;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


}
