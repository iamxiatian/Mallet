/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */


package cc.mallet.pipe;

import cc.mallet.classify.examples.Word2VecCache;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
import cc.mallet.util.Word2Vec;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Expand the token to add related word2vec tokens.
 *
 * @author Tian Xia <a href="mailto:xiat@ruc.edu.cn">xiat@ruc.edu.cn</a>
 */

public class TokenSequenceWord2Vec extends Pipe implements Serializable {

    private static final long serialVersionUID = 1;

    // Serialization
    private static final int CURRENT_SERIAL_VERSION = 1;

    private Word2VecCache cache = null;

    public TokenSequenceWord2Vec() {
        this.cache = new Word2VecCache("127.0.0.1");
    }

    public Instance pipe(Instance carrier) {
        TokenSequence ts = (TokenSequence) carrier.getData();

        List<Token> expandedWords = new ArrayList<>();
        for (int i = 0; i < ts.size(); i++) {
            String current = ts.get(i).getText();

            for (String w : cache.getSimilarWords(current)) {
                expandedWords.add(new Token(w));
            }
        }
        ts.addAll(expandedWords);

        return carrier;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        int version = in.readInt();
    }

    public static void load(String model) throws IOException {
        word2Vec.loadModel(model);
    }

    private static final Word2Vec word2Vec = new Word2Vec();
}
