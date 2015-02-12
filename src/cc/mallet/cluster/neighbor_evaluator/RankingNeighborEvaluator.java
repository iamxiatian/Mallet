package cc.mallet.cluster.neighbor_evaluator;


import cc.mallet.classify.Classifier;
import cc.mallet.types.Instance;
import cc.mallet.types.LabelVector;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Uses a {@link Classifier} that scores an array of {@link
 * Neighbor}s. The Classifier expects {@link Instance}s with data
 * equal to an array of {@link Neighbor}s. The labeling of each
 * Instance is a set of {@link Integer}s, with labeling i
 * corresponding the likelihood that {@link Neighbor} i is the "best"
 * {@link Neighbor}.
 *
 * @author "Aron Culotta" <culotta@degas.cs.umass.edu>
 * @version 1.0
 * @see ClassifyingNeighborEvaluator
 * @since 1.0
 */
public class RankingNeighborEvaluator extends ClassifyingNeighborEvaluator {

    private static final long serialVersionUID = 1;
    private static final int CURRENT_SERIAL_VERSION = 1;

    /**
     * @param classifier The Classifier used to assign a score to a {@link Neighbor}.
     * @return
     */
    public RankingNeighborEvaluator(Classifier classifier) {
        super(classifier, null);
    }

    public double evaluate(Neighbor neighbor) {
        throw new UnsupportedOperationException("This class expects an array of Neighbors to choose from");
    }

    /**
     * @param neighbors
     * @return An array containing a score for each of the elements of <code>neighbors</code>.
     */
    public double[] evaluate(Neighbor[] neighbors) {
        double[] scores = new double[neighbors.length];
        LabelVector ranks = classifier.classify(neighbors).getLabelVector();
        for (int i = 0; i < ranks.numLocations(); i++) {
            int idx = ((Integer) ranks.getLabelAtRank(i).getEntry()).intValue();
            scores[idx] = ranks.getValueAtRank(i);
        }
        return scores;
    }

    // SERIALIZATION

    public void reset() {
    }

    public String toString() {
        return "class=" + this.getClass().getName() +
                " classifier=" + classifier.getClass().getName();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(CURRENT_SERIAL_VERSION);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int version = in.readInt();
    }
}
