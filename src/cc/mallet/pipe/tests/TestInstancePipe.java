/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */


/**
 @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

package cc.mallet.pipe.tests;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.pipe.iterator.StringArrayIterator;
import cc.mallet.pipe.tsf.RegexMatches;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.tests.TestSerializable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.util.regex.Pattern;

public class TestInstancePipe extends TestCase {
    String[] data = new String[]{
            "This is the first test string",
            "The second test string is here",
            "And this is the third test string",
    };

    public TestInstancePipe(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(TestInstancePipe.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public Pipe createPipe() {
        return new SerialPipes(new Pipe[]{
                new CharSequence2TokenSequence(),
                new TokenSequenceLowercase(),
                new TokenSequence2FeatureSequence(),
                new FeatureSequence2FeatureVector()});
    }

    public void testOne() {
        Pipe p = createPipe();
        InstanceList ilist = new InstanceList(p);
        ilist.addThruPipe(new StringArrayIterator(data));
        assertTrue(ilist.size() == 3);
    }

    public void testTwo() {
        Pipe p = new SerialPipes(new Pipe[]{
                new CharSequence2TokenSequence(),
                new TokenSequenceLowercase(),
                new RegexMatches("vowel", Pattern.compile("[aeiou]")),
                new RegexMatches("firsthalf", Pattern.compile("[a-m]")),
                new RegexMatches("secondhalf", Pattern.compile("[n-z]")),
                new RegexMatches("length2", Pattern.compile("..")),
                new RegexMatches("length3", Pattern.compile("...")),
                new PrintInput(),
                new TokenSequence2TokenInstances()});
        InstanceList ilist = new InstanceList(p);
        ilist.addThruPipe(new StringArrayIterator(data));
        assert (ilist.size() == 19) : "list size = " + ilist.size();
        assertTrue(ilist.size() == 19);
    }

    public void testOneFromSerialized() throws IOException, ClassNotFoundException {
        Pipe p = createPipe();
        Pipe clone = (Pipe) TestSerializable.cloneViaSerialization(p);
        InstanceList ilist = new InstanceList(clone);
        ilist.addThruPipe(new StringArrayIterator(data));
        assertTrue(ilist.size() == 3);
    }

    protected void setUp() {
    }

    public static class Array2ArrayIterator extends Pipe {
        public Instance pipe(Instance carrier) {
            carrier.setData(new ArrayIterator((Object[]) carrier.getData()));
            return carrier;
        }
    }

}
