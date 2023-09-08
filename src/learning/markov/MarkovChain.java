package learning.markov;

import learning.core.Histogram;

import java.util.*;

public class MarkovChain<L,S> {
    private LinkedHashMap<L, HashMap<Optional<S>, Histogram<S>>> label2symbol2symbol = new LinkedHashMap<>();

    public Set<L> allLabels() {return label2symbol2symbol.keySet();}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (L language: label2symbol2symbol.keySet()) {
            sb.append(language);
            sb.append('\n');
            for (Map.Entry<Optional<S>, Histogram<S>> entry: label2symbol2symbol.get(language).entrySet()) {
                sb.append("    ");
                sb.append(entry.getKey());
                sb.append(":");
                sb.append(entry.getValue().toString());
                sb.append('\n');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    // Increase the count for the transition from prev to next.
    // Should pass SimpleMarkovTest.testCreateChains().
    public void count(Optional<S> prev, L label, S next) {
        // a comment
        if (!label2symbol2symbol.containsKey(label)) {
            label2symbol2symbol.put(label, new HashMap<>());
        }
        HashMap<Optional<S>, Histogram<S>> labelMap = label2symbol2symbol.get(label);
        if (!labelMap.containsKey(prev)) {
            labelMap.put(prev, new Histogram<>());
        }
        labelMap.get(prev).bumpBy(next, 1);
    }

    // Returns P(sequence | label)
    // Should pass SimpleMarkovTest.testSourceProbabilities() and MajorMarkovTest.phraseTest()
    //
    // HINT: Be sure to add 1 to both the numerator and denominator when finding the probability of a
    // transition. This helps avoid sending the probability to zero.
    public double probability(ArrayList<S> sequence, L label) {
        HashMap<Optional<S>, Histogram<S>> labelMap = label2symbol2symbol.get(label);
        double PV = 1.0;
        Optional<S> Prev = Optional.empty();

        for (S item : sequence) {
            Histogram<S> histogram = labelMap.getOrDefault(Prev, new Histogram<>());
            double numerator = histogram.getCountFor(item) + 1;
            double denominator = histogram.getTotalCounts() + histogram.size() + 1;

            PV *= (numerator / denominator);
            Prev = Optional.of(item);
        }
        return PV;
    }


    // Return a map from each label to P(label | sequence).
    // Should pass MajorMarkovTest.testSentenceDistributions()
    public LinkedHashMap<L,Double> labelDistribution(ArrayList<S> sequence) {
        LinkedHashMap<L, Double> sendThrough = new LinkedHashMap<>();
        double totalProb = 0.0;
        for (L label : label2symbol2symbol.keySet()) {
            double calcProb = probability(sequence, label);
            sendThrough.put(label, calcProb);
            totalProb += calcProb;
        }
        for (L label : sendThrough.keySet()) {
            double labelProb = sendThrough.get(label);
            double langDivProb = labelProb / totalProb;
            sendThrough.put(label, langDivProb);
        }

        return sendThrough;
    }


    // Calls labelDistribution(). Returns the label with highest probability.
    // Should pass MajorMarkovTest.bestChainTest()
    public L bestMatchingChain(ArrayList<S> sequence) {
        LinkedHashMap<L, Double> labelProb = labelDistribution(sequence);

        double maxProb = -1.0;
        L maxProbLabel = null;

        for (L label : labelProb.keySet()) {
            double probability = labelProb.get(label);

            if (probability > maxProb) {
                maxProb = probability;
                maxProbLabel = label;
            }
        }

        return maxProbLabel;
    }
}
