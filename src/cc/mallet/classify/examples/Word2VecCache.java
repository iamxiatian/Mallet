package cc.mallet.classify.examples;

import cc.mallet.util.CommandOption;
import cc.mallet.util.Word2Vec;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * Cache word2vec calculation results
 *
 * @author <a href="mailto:xiat@ruc.edu.cn">XiaTian</a>
 * @date Feb 05, 2015 11:45 PM
 */
public class Word2VecCache {
    private Jedis jedis = null;

    public Word2VecCache(String host) {
        this.jedis = new Jedis(host);
    }

    public void cacheAll(Word2Vec word2Vec) {
        double total = word2Vec.getWords();
        int count = 0;
        for (String word : word2Vec.getWordMap().keySet()) {
            Set<Word2Vec.WordEntry> entries = word2Vec.distance(word);
            for (Word2Vec.WordEntry entry : entries) {
                jedis.zadd("wv:" + word, entry.score, entry.name);
            }
            count++;
            if (count % 1000 == 0) {
                System.out.printf("%d\t%.2f%%", count, count * 100.0 / total);
            }
        }
    }

    public void cacheSimilarWords(Word2Vec word2Vec, int topN, float
            minScore, int from) {
        double total = word2Vec.getWords();
        int count = 0;
        for (String word : word2Vec.getWordMap().keySet()) {
            count++;

            if (count < from) {
                continue;
            }

            if (jedis.exists(word)) {
                continue;
            }
            List<String> entries = word2Vec.getSimilarWords(word, topN, minScore);
            StringBuilder sb = new StringBuilder();
            for (String s : entries) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(s);
            }
            jedis.set("s:" + word, sb.toString());

            if (count % 1000 == 0) {
                System.out.printf("%d\t%.2f%%", count, count * 100.0 / total);
            }
        }
    }

    public String[] getSimilarWords(String word) {
        if (!jedis.exists("s:" + word)) {
            return new String[0];
        }

        String values = jedis.get("s:" + word);
        return StringUtils.split(values, " ");
    }

    public static void main(String[] args) throws IOException, ParseException {
        HelpFormatter helpFormatter = new HelpFormatter();
        CommandLineParser parser = new PosixParser();
        Options options = new Options();

        String formatString = "Word2VecCache -f model_file sim|all";

        options.addOption(new Option("f", true, "model file"));
        options.addOption(new Option("all", false, "cache all results"));
        options.addOption(new Option("sim", false, "cache similar words"));
        options.addOption(new Option("from", true, "from position"));
        options.addOption("h", "help", false, "print help for the command.");

        CommandLine cmdLine = parser.parse(options, args);
        if (cmdLine.hasOption("h")) {
            helpFormatter.printHelp(formatString, options);
            return;
        }

        if(!cmdLine.hasOption("f")){
            helpFormatter.printHelp(formatString, options);
            return;
        }
        String modelFile = cmdLine.getOptionValue("f");
        if (!new File(modelFile).exists()) {
            System.out.println("Model file " + modelFile + " does not exist.");
            return;
        }

        if (cmdLine.hasOption("all") || cmdLine.hasOption("sim")) {
            Word2Vec vec = new Word2Vec();
            vec.loadModel(modelFile);
            System.out.println("size==>" + vec.getSize());
            int from = 0;
            if (cmdLine.hasOption("from")) {
                from = Integer.parseInt(cmdLine.getOptionValue("from"));
            }

            Word2VecCache cache = new Word2VecCache("localhost");
            if (cmdLine.hasOption("all")) {
                cache.cacheAll(vec);
                System.out.println("I'm DONE for cache all results.");
            }

            if (cmdLine.hasOption("sim")) {
                cache.cacheSimilarWords(vec, 5, 0.7f, from);
                System.out.println("I'm DONE for cache simlar words.");
            }
        }
    }
}