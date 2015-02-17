package cc.mallet.pipe;

import cc.mallet.extract.StringTokenization;
import cc.mallet.types.Instance;
import cc.mallet.types.SingleInstanceIterator;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
import org.ansj.domain.Term;
import org.ansj.recognition.NatureRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pipe that tokenizes a Chinese character sequence.  Expects a CharSequence
 * in the Instance data, and converts the sequence into a token
 * sequence using Chinese segmentation
 */
public class ChineseSequence2TokenSequence extends Pipe implements Serializable {
    private Set<String> denydPosSet = new HashSet<String>();

    public ChineseSequence2TokenSequence() {
        for (String pos : new String[]{"r", "w", "q", "m", "p", "f", "d", "t"}) {
            denydPosSet.add(pos);
        }
    }

    public ChineseSequence2TokenSequence(String[] denyPosArray) {
        if (denyPosArray != null) {
            for (String pos : denyPosArray) {
                this.denydPosSet.add(pos);
            }
        }
    }

    public Instance pipe(Instance carrier) {
        CharSequence string = (CharSequence) carrier.getData();
        TokenSequence ts = new StringTokenization(string);

        List<Term> terms = ToAnalysis.parse(string.toString());

        if(denydPosSet.isEmpty()) {
            for (Term term : terms) {
                if(term.getName().length()>=2)
                    ts.add(new Token(term.getName()));
            }
        } else {
            new NatureRecognition(terms).recognition();
            for (Term term : terms) {
                if(term.getName().length()>=2 && !denydPosSet.contains(term.getNatrue().natureStr))
                   ts.add(new Token(term.getName()));
            }
        }
        carrier.setData(ts);
        return carrier;
    }

    public static void main(String[] args) {
        try {
            for (int i = 0; i < args.length; i++) {
                Instance carrier = new Instance(new File(args[i]), null, null, null);
                SerialPipes p = new SerialPipes(new Pipe[]{
                        new Input2CharSequence(),
                        new ChineseSequence2TokenSequence()});
                carrier = p.newIteratorFrom(new SingleInstanceIterator(carrier)).next();
                TokenSequence ts = (TokenSequence) carrier.getData();
                System.out.println("===");
                System.out.println(args[i]);
                System.out.println(ts.toString());
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    // Serialization

    private static final long serialVersionUID = 1;
    private static final int CURRENT_SERIAL_VERSION = 0;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int version = in.readInt();
    }


}
