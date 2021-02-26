package ch.ethz.systems.netbench.ext.poissontraffic;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.run.traffic.TrafficPlanner;
import ch.ethz.systems.netbench.ext.poissontraffic.flowsize.FlowSizeDistribution;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Random;

public class PoissonArrivalPlanner extends TrafficPlanner {

    public enum PairDistribution {
        ALL_TO_ALL,
        ALL_TO_ALL_FRACTION,
        ALL_TO_ALL_SERVER_FRACTION,
        PARETO_SKEW_DISTRIBUTION,
        PAIRINGS_FRACTION,
        DUAL_ALL_TO_ALL_FRACTION,
        DUAL_ALL_TO_ALL_SERVER_FRACTION
    }

    private double lambdaFlowStartsPerSecond;
    private final FlowSizeDistribution flowSizeDistribution;
    private final Random ownIndependentRng;
    private final RandomCollection<Pair<Integer, Integer>> randomPairGenerator;
//    private final RandomCollection<Pair<Integer, Integer>> randomPairGenerator_tenant2;

    private PoissonArrivalPlanner(Map<Integer, TransportLayer> idToTransportLayerMap, double lambdaFlowStartsPerSecond, FlowSizeDistribution flowSizeDistribution) {
        super(idToTransportLayerMap);
        this.lambdaFlowStartsPerSecond = lambdaFlowStartsPerSecond;
        this.flowSizeDistribution = flowSizeDistribution;
        this.ownIndependentRng = Simulator.selectIndependentRandom("poisson_inter_arrival");
        this.randomPairGenerator = new RandomCollection<>(Simulator.selectIndependentRandom("pair_probabilities_draw"));
//        this.randomPairGenerator_tenant2 = new RandomCollection<>(Simulator.selectIndependentRandom("pair_probabilities_draw"));
    }

    /**
     * Constructor.
     *
     * @param idToTransportLayerMap     Maps a network device identifier to its corresponding transport layer
     * @param lambdaFlowStartsPerSecond Poisson-arrival lambda
     * @param flowSizeDistribution      Flow size distribution
     * @param fileName                  Name of the input file with communication probability densities
     */
    public PoissonArrivalPlanner(Map<Integer, TransportLayer> idToTransportLayerMap, double lambdaFlowStartsPerSecond, FlowSizeDistribution flowSizeDistribution, String fileName) {
        this(idToTransportLayerMap, lambdaFlowStartsPerSecond, flowSizeDistribution);
        this.readPairProbabilitiesFromFile(fileName);
        SimulationLogger.logInfo("Flow planner", "POISSON_ARRIVAL(lambda=" + lambdaFlowStartsPerSecond + ", fileName=" + fileName + ")");
    }
    
    /**
     * Constructor.
     *
     * @param idToTransportLayerMap     Maps a network device identifier to its corresponding transport layer
     * @param lambdaFlowStartsPerSecond Poisson-arrival lambda
     * @param flowSizeDistribution      Flow size distribution
     * @param pairDistribution          Choice of static pair distribution which is valid for any topology
     */
    public PoissonArrivalPlanner(Map<Integer, TransportLayer> idToTransportLayerMap, double lambdaFlowStartsPerSecond, FlowSizeDistribution flowSizeDistribution, PairDistribution pairDistribution) {
        this(idToTransportLayerMap, lambdaFlowStartsPerSecond, flowSizeDistribution);
        switch (pairDistribution) {

            case ALL_TO_ALL:
                // this.setPairProbabilitiesAllToAll();
               this.setPairProbabilitiesAllToAll_withUDP();
            // this.setPairProbabilitiesAllToAll_rackID();
//                this.setPairProbabilitiesAllToAll_TwoTenants();
                break;

            case ALL_TO_ALL_FRACTION:
                this.setPairProbabilitiesAllToAllInFraction();
                break;

            case ALL_TO_ALL_SERVER_FRACTION:
                this.setPairProbabilitiesAllToAllInServerFraction(idToTransportLayerMap);
                break;

            case PAIRINGS_FRACTION:
                this.setPairProbabilitiesPairingsInFraction();
                break;
                
            case PARETO_SKEW_DISTRIBUTION:
                this.setPairProbabilitiesParetoSkew();
                break;

            case DUAL_ALL_TO_ALL_FRACTION:
                this.setPairProbabilitiesDualAllToAllFraction();
                break;

            case DUAL_ALL_TO_ALL_SERVER_FRACTION:
                this.setPairProbabilitiesDualAllToAllServerFraction();
                break;

            default:
                throw new IllegalArgumentException("Invalid pair distribution given: " + pairDistribution + ".");

        }
        SimulationLogger.logInfo("Flow planner", "POISSON_ARRIVAL(lambda=" + lambdaFlowStartsPerSecond + ", pairDistribution=" + pairDistribution + ")");
    }
    
    /**
     * Set the communication pair probabilities to be all-to-all uniform.
     */
    private void setPairProbabilitiesParetoSkew() {

        System.out.print("Generating pareto-skewed pair probabilities between all ToRs...");

        // Get the random generator for this part
        Random gen = Simulator.selectIndependentRandom("skew_pareto_distribution");
        ParetoDistribution pareto = new ParetoDistribution(
                Simulator.getConfiguration().getDoublePropertyOrFail("traffic_pareto_skew_shape"),
                10, // Scale does not matter because of normalization
                gen
        );

        // ToRs, shuffled
        List<Integer> tors = new ArrayList<>(Simulator.getConfiguration().getGraphDetails().getTorNodeIds());
        Collections.shuffle(tors, gen);

        // For every ToR, draw its probability mass
        ArrayList<Double> probRes = new ArrayList<>();
        double sumAll = 0;
        for (int i = 0; i < tors.size(); i++) {
            double curProb = pareto.draw();
            sumAll += curProb;
            probRes.add(curProb);
        }

        // Normalize the "probability mass" by the total sum of "probability mass",
        // such that the results is a normalized Pareto distribution
        for (int i = 0; i < probRes.size(); i++) {
            probRes.set(i, probRes.get(i) / sumAll);
        }
        Collections.sort(probRes);

        // Calculate how much probability is wasted on the diagonal
        double torExcluded = 0.0;
        for (int i = 0; i < tors.size(); i++) {
            torExcluded += probRes.get(i) * probRes.get(i);
        }

        // Write away to random pair generator the pairs and their respective probability
        for (int i = 0; i < tors.size(); i++) {
            for (int j = 0; j < tors.size(); j++) {
                if (i != j) {

                    double torPairProb = probRes.get(i) * probRes.get(j) / (1 - torExcluded); // ToR-pair probability with diagonal waste normalized out
                    List<Integer> srcServers = new ArrayList<>(Simulator.getConfiguration().getGraphDetails().getServersOfTor(tors.get(i)));
                    List<Integer> dstServers = new ArrayList<>(Simulator.getConfiguration().getGraphDetails().getServersOfTor(tors.get(j)));

                    double serverProb = torPairProb / (srcServers.size() * dstServers.size());
                    for (int src : srcServers) {
                        for (int dst : dstServers) {
                            this.randomPairGenerator.add(serverProb, new ImmutablePair<>(src, dst));
                        }
                    }

                }
            }
        }

        System.out.println(" done.");
        System.out.println("Top 20 ToR probabilities:");
        for (int i = tors.size() - 1; i >= Math.max(0, tors.size() - 20); i--) {
            System.out.println("ToR #" + tors.get(i) + " has probability " + probRes.get(i));
        }

    }

    /**
     * Set the communication pair probabilities to be all-to-all uniform.
     */
    private void setPairProbabilitiesAllToAll() {

        System.out.print("Generating all-to-all pair probabilities between all nodes with a transport layer...");

        // Uniform probability for every server pair
        double pdfNumBytes = 1.0 / (this.idToTransportLayerMap.size() * (this.idToTransportLayerMap.size() - 1));

        // Add uniform probability for every pair
        for (Integer src : this.idToTransportLayerMap.keySet()){
            for (Integer dst : this.idToTransportLayerMap.keySet()){
                if(!src.equals(dst)) {
                    this.randomPairGenerator.add(pdfNumBytes, new ImmutablePair<>(src, dst));
                }
            }
        }

        System.out.println(" done.");

    }

    private void setPairProbabilitiesAllToAll_rackID() {
        System.out.print("Generating all-to-all pair probabilities between all nodes with a transport layer (only rack ID)...");

        // Uniform probability for every server pair
        double pdfNumBytes = 1.0 / (9 * (9 - 1));

        // Add uniform probability for every pair
        int i, j;
        for (i=0; i<9; i++)
            for (j=0; j<9; j++)
            if (i != j) {
                Integer src = new Integer(i);
                Integer dst = new Integer(j);
                this.randomPairGenerator.add(pdfNumBytes, new ImmutablePair<>(src, dst));
            }

        System.out.println(" done.");
    }

    private void setPairProbabilitiesAllToAll_withUDP() {

        System.out.print("Generating all-to-all pair probabilities between all nodes with a transport layer...");

        // Uniform probability for every server pair
        double pdfNumBytes = 1.0 / (144 * (144 - 1));

        // Add uniform probability for every pair
        int i, j;
        for (i=0; i<144; i++)
            for (j=0; j<144; j++)
            if (i != j) {
                Integer src = new Integer(i);
                Integer dst = new Integer(j);
                this.randomPairGenerator.add(pdfNumBytes, new ImmutablePair<>(src, dst));
            }

        System.out.println(" done.");

    }

    private void setPairProbabilitiesAllToAll_TwoTenants() {

        System.out.print("Generating all-to-all pair probabilities between all nodes with a transport layer (two tenants)...");

        // Uniform probability for every server pair
//        double pdfNumBytes = 1.0 / (2 * 96 * 80 + 5 * 48 * 32);
        double pdfNumBytes = 1.0 / (64*48 + 32 * 48);

        // Add uniform probability for every pair
        int k,i,j;
        System.out.println("this.idToTransportLayerMap.size()" + this.idToTransportLayerMap.size());

        for (i=0; i<64; i++)
            for (j=0; j<48; j++) {
                Integer src = new Integer(i);
                Integer dst = new Integer(j + 96);
                this.randomPairGenerator.add(pdfNumBytes, new ImmutablePair<>(src, dst));
            }
        for (i=0; i<32; i++)
            for (j=0; j<48; j++) {
                Integer src = new Integer(i + 64);
                Integer dst = new Integer(j + 96);
                this.randomPairGenerator.add(pdfNumBytes, new ImmutablePair<>(src, dst));
            }
//        for (i=0; i<36; i++)
//            for (j=0; j<36; j++) {
//                Integer src = new Integer(i + 72);
//                Integer dst = new Integer(j + 108);
//                this.randomPairGenerator.add(pdfNumBytes, new ImmutablePair<>(src, dst));
//            }

//        for (k=0;k<2;k++)
//            for (i=0;i<(96); i++)
//                for (j=0;j<(96); j++)
//                    if (i / 16!=j / 16) {
//                        Integer src = new Integer(i);
//                        Integer dst = new Integer(j);
//                        this.randomPairGenerator.add(pdfNumBytes, new ImmutablePair<>(src, dst));
//    //                    System.out.println("src:" + src + "; dst" + dst);
//                    }
//
//        for (k=0;k<5;k++)
//            for (i=0;i<(48); i++)
//                for (j=0;j<(48); j++)
//                    if (i / 16!=j / 16) {
//                        Integer src = new Integer(96 + i);
//                        Integer dst = new Integer(96 + j);
//                        this.randomPairGenerator.add(pdfNumBytes, new ImmutablePair<>(src, dst));
//    //                    System.out.println("src:" + src + "; dst" + dst);
//                    }


        System.out.println(" done!");

    }

    private void setPairProbabilitiesAllToAll_TwoTenants_bak() {

        System.out.print("Generating all-to-all pair probabilities between all nodes with a transport layer (two tenants)...");

        // Uniform probability for every server pair
        double pdfNumBytes = 1.0 / (((this.idToTransportLayerMap.size() / 4) * ((this.idToTransportLayerMap.size() / 4) - 1)) +
                ((this.idToTransportLayerMap.size() * 3 / 4) * ((this.idToTransportLayerMap.size() * 3 / 4) - 1)));

        // Add uniform probability for every pair
        int i,j;
        System.out.println("this.idToTransportLayerMap.size()" + this.idToTransportLayerMap.size());
        for (i=0;i<(this.idToTransportLayerMap.size() * 3 / 4); i++)
            for (j=0;j<(this.idToTransportLayerMap.size() * 3 / 4); j++)
                if (i!=j) {
                    Integer src = new Integer(i);
                    Integer dst = new Integer(j);
                    this.randomPairGenerator.add(pdfNumBytes, new ImmutablePair<>(src, dst));
//                    System.out.println("src:" + src + "; dst" + dst);
                }
        for (i=0;i<(this.idToTransportLayerMap.size() / 4); i++)
            for (j=0;j<(this.idToTransportLayerMap.size() / 4); j++)
                if (i!=j) {
                    Integer src = new Integer(this.idToTransportLayerMap.size() * 3 / 4 + i);
                    Integer dst = new Integer(this.idToTransportLayerMap.size() * 3 / 4 + j);
                    this.randomPairGenerator.add(pdfNumBytes, new ImmutablePair<>(src, dst));
//                    System.out.println("src:" + src + "; dst" + dst);
                }


        System.out.println(" done!");

    }
    /**
     * Set the pair probabilities to be all-to-all within a fraction of the servers.
     */
    private void setPairProbabilitiesAllToAllInServerFraction(Map<Integer, TransportLayer> idToTransportLayerMap) {


        // Retrieve necessary parameters from the extension
        double activeFractionX = Simulator.getConfiguration().getDoublePropertyOrFail("traffic_probabilities_active_fraction");
        boolean fractionIsOrdered = Simulator.getConfiguration().getBooleanPropertyOrFail("traffic_probabilities_active_fraction_is_ordered");

        // Shuffle nodes
        List<Integer> servers = new ArrayList<>();
        for (Integer i : idToTransportLayerMap.keySet()) {
            servers.add(i);
        }
        Collections.sort(servers);
        if (!fractionIsOrdered) {
            Collections.shuffle(servers, Simulator.selectIndependentRandom("all_to_all_fraction_shuffle"));
        }
        int numChosenServers = (int) Math.floor(servers.size() * activeFractionX);

        // Probability between each server pair
        double serverPairProb = 1.0 / (numChosenServers * (numChosenServers - 1));

        // Go over every server pair
        List<Integer> chosen = new ArrayList<>();
        for (int i = 0; i < numChosenServers; i++) {
            chosen.add(servers.get(i));
            for (int j = 0; j < numChosenServers; j++) {
                if (i != j) {
                    this.randomPairGenerator.add(serverPairProb, new ImmutablePair<>(servers.get(i), servers.get(j)));
                }

            }
        }

        // Log chosen fraction
        Collections.sort(chosen);
        SimulationLogger.logInfo("A2A_FRACTION_CHOSEN_SERVERS", chosen.toString());

        System.out.println(" done.");

    }

    /**
     * Set the pair probabilities to be all-to-all within a fraction of the nodes.
     */
    private void setPairProbabilitiesAllToAllInFraction() {


        // Retrieve necessary parameters from the extension
        int numTors = Simulator.getConfiguration().getGraphDetails().getNumTors();
        int serversPerNodeToExtendWith = Simulator.getConfiguration().getIntegerPropertyOrFail("scenario_topology_extend_servers_per_tl_node");
        boolean fractionIsOrdered = Simulator.getConfiguration().getBooleanPropertyOrFail("traffic_probabilities_active_fraction_is_ordered");
        double activeFractionX = Simulator.getConfiguration().getDoublePropertyOrFail("traffic_probabilities_active_fraction");

        // Shuffle nodes
        List<Integer> tors = new ArrayList<>();
        for (int i = 0; i < numTors; i++) {
            tors.add(i);
        }
        if (!fractionIsOrdered) {
            Collections.shuffle(tors, Simulator.selectIndependentRandom("all_to_all_fraction_shuffle"));
        }
        int numChosenTors = (int) Math.floor(numTors * activeFractionX);

        // Probability between each server pair
        double serverPairProb = 1.0 / (numChosenTors * (numChosenTors - 1) * serversPerNodeToExtendWith * serversPerNodeToExtendWith);

        System.out.print("Generating all-to-all pair probabilities in " + (activeFractionX * 100) + "% fraction " + tors.size() + " ToRs between their servers...");

        // Go over every ToR pair
        List<Integer> chosen = new ArrayList<>();
        for (int i = 0; i < numChosenTors; i++) {
            chosen.add(tors.get(i));
            for (int j = 0; j < numChosenTors; j++) {
                if (i != j) {

                    int torA = tors.get(i);
                    int torB = tors.get(j);

                    for (Integer svrA : Simulator.getConfiguration().getGraphDetails().getServersOfTor(torA)) {
                        for (Integer svrB : Simulator.getConfiguration().getGraphDetails().getServersOfTor(torB)) {
                            // Add to random pair generator
                            this.randomPairGenerator.add(serverPairProb, new ImmutablePair<>(svrA, svrB));
                        }
                    }

                }

            }
        }

        // Log chosen fraction
        Collections.sort(chosen);
        SimulationLogger.logInfo("A2A_FRACTION_CHOSEN_TORS", chosen.toString());

        System.out.println(" done.");

    }

    /**
     * Set the pair probabilities to be dual all-to-all within two fraction of the nodes.
     */
    private void setPairProbabilitiesDualAllToAllFraction() {

        // Retrieve necessary parameters from the extension
        int numTors = Simulator.getConfiguration().getGraphDetails().getNumTors();
        boolean fractionIsOrdered = Simulator.getConfiguration().getBooleanPropertyOrFail("traffic_probabilities_active_fraction_is_ordered");
        double activeFractionA = Simulator.getConfiguration().getDoublePropertyOrFail("traffic_probabilities_fraction_A");
        double probabilityMassA = Simulator.getConfiguration().getDoublePropertyOrFail("traffic_probabilities_mass_A");


        // Shuffle nodes
        List<Integer> tors = new ArrayList<>();
        for (int i = 0; i < numTors; i++) {
            tors.add(i);
        }
        if (!fractionIsOrdered) {
            Collections.shuffle(tors, Simulator.selectIndependentRandom("all_to_all_fraction_shuffle"));
        }
        int numChosenTorsA = (int) Math.floor(numTors * activeFractionA);
        int numChosenTorsB = numTors - numChosenTorsA;

        if (numChosenTorsA == 0 || numChosenTorsB == 0) {
            throw new RuntimeException("Cannot have an empty fraction on either side for the all-to-all dual fraction probabilities.");
        }

        double probabilityPerA = probabilityMassA / (double) numChosenTorsA;
        double probabilityPerB = (1.0 - probabilityMassA) / (double) numChosenTorsB;

        // Calculate how much probability is wasted on the diagonal
        double wastedProbability = 0.0;
        wastedProbability += numChosenTorsA * probabilityPerA * probabilityPerA;
        wastedProbability += numChosenTorsB * probabilityPerB * probabilityPerB;

        SimulationLogger.logInfo("DUAL_FRACTION_A", "Portion A: (n=" + numChosenTorsA + ", p=" + probabilityPerA + ", tot=" + probabilityMassA + ")");
        SimulationLogger.logInfo("DUAL_FRACTION_B", "Portion B: (n=" + numChosenTorsB + ", p=" + probabilityPerB + ", tot=" + (1-probabilityMassA) + ")");
        System.out.println("Portion A: (n=" + numChosenTorsA + ", p=" + probabilityPerA + ", tot=" + probabilityMassA + ")");
        System.out.println("Portion B: (n=" + numChosenTorsB + ", p=" + probabilityPerB + ", tot=" + (1-probabilityMassA) + ")");
        System.out.print("Generating all-to-all dual fraction probabilities... ");

        // Go over every ToR pair
        for (int i = 0; i < tors.size(); i++) {
            double torProbI = i < numChosenTorsA ? probabilityPerA : probabilityPerB;
            for (int j = 0; j < tors.size(); j++) {
                double torProbJ = j < numChosenTorsA ? probabilityPerA : probabilityPerB;
                if (i != j) {

                    // ToR-pair probability with diagonal waste normalized out
                    double torPairProb = torProbI * torProbJ / (1 - wastedProbability);
                    List<Integer> srcServers = new ArrayList<>(Simulator.getConfiguration().getGraphDetails().getServersOfTor(tors.get(i)));
                    List<Integer> dstServers = new ArrayList<>(Simulator.getConfiguration().getGraphDetails().getServersOfTor(tors.get(j)));

                    double serverProb = torPairProb / (srcServers.size() * dstServers.size());
                    for (int src : srcServers) {
                        for (int dst : dstServers) {
                            this.randomPairGenerator.add(serverProb, new ImmutablePair<>(src, dst));
                        }
                    }

                }

            }
        }

        System.out.println(" done.");

    }

    /**
     * Set the pair probabilities to be dual all-to-all within two fraction of the nodes.
     */
    private void setPairProbabilitiesDualAllToAllServerFraction() {

        // Retrieve necessary parameters from the extension
        int numTors = Simulator.getConfiguration().getGraphDetails().getNumTors();
        boolean fractionIsOrdered = Simulator.getConfiguration().getBooleanPropertyOrFail("traffic_probabilities_active_fraction_is_ordered");
        double activeFractionA = Simulator.getConfiguration().getDoublePropertyOrFail("traffic_probabilities_fraction_A");
        double probabilityMassA = Simulator.getConfiguration().getDoublePropertyOrFail("traffic_probabilities_mass_A");


        // Shuffle nodes
        List<Integer> tors = new ArrayList<>();
        for (int i = 0; i < numTors; i++) {
            tors.add(i);
        }
        if (!fractionIsOrdered) {
            Collections.shuffle(tors, Simulator.selectIndependentRandom("all_to_all_fraction_shuffle"));
        }
        int numChosenTorsA = (int) Math.floor(numTors * activeFractionA);
        int numChosenTorsB = numTors - numChosenTorsA;

        if (numChosenTorsA == 0 || numChosenTorsB == 0) {
            throw new RuntimeException("Cannot have an empty fraction on either side for the all-to-all dual fraction probabilities.");
        }

        double probabilityPerA = probabilityMassA / (double) numChosenTorsA;
        double probabilityPerB = (1.0 - probabilityMassA) / (double) numChosenTorsB;

        // Calculate how much probability is wasted on the diagonal
        double wastedProbability = 0.0;
        wastedProbability += numChosenTorsA * probabilityPerA * probabilityPerA;
        wastedProbability += numChosenTorsB * probabilityPerB * probabilityPerB;

        SimulationLogger.logInfo("SERVER_DUAL_FRACTION_A", "Portion A: (n=" + numChosenTorsA + ", p=" + probabilityPerA + ", tot=" + probabilityMassA + ")");
        SimulationLogger.logInfo("SERVER_DUAL_FRACTION_B", "Portion B: (n=" + numChosenTorsB + ", p=" + probabilityPerB + ", tot=" + (1-probabilityMassA) + ")");
        System.out.println("Portion A: (n=" + numChosenTorsA + ", p=" + probabilityPerA + ", tot=" + probabilityMassA + ")");
        System.out.println("Portion B: (n=" + numChosenTorsB + ", p=" + probabilityPerB + ", tot=" + (1-probabilityMassA) + ")");
        System.out.print("Generating all-to-all dual fraction probabilities... ");

        // Go over every ToR pair
        for (int i = 0; i < tors.size(); i++) {
            double torProbI = i < numChosenTorsA ? probabilityPerA : probabilityPerB;
            for (int j = 0; j < tors.size(); j++) {
                double torProbJ = j < numChosenTorsA ? probabilityPerA : probabilityPerB;
                if (i != j) {
                    // ToR-pair probability with diagonal waste normalized out
                    double torPairProb = torProbI * torProbJ / (1 - wastedProbability);
                    this.randomPairGenerator.add(torPairProb, new ImmutablePair<>(tors.get(i), tors.get(j)));
                }

            }
        }

        System.out.println(" done.");

    }

    /**
     * Set the pair probabilities to be pairings within a fraction of the nodes.
     */
    private void setPairProbabilitiesPairingsInFraction() {


        // Retrieve necessary parameters from the extension
        int numTors = Simulator.getConfiguration().getGraphDetails().getNumTors();
        int serversPerNodeToExtendWith = Simulator.getConfiguration().getIntegerPropertyOrFail("scenario_topology_extend_servers_per_tl_node");
        boolean fractionIsOrdered = Simulator.getConfiguration().getBooleanPropertyOrFail("traffic_probabilities_active_fraction_is_ordered");
        double activeFractionX = Simulator.getConfiguration().getDoublePropertyOrFail("traffic_probabilities_active_fraction");

        // Shuffle nodes
        List<Integer> tors = new ArrayList<>();
        for (int i = 0; i < numTors; i++) {
            tors.add(i);
        }
        if (!fractionIsOrdered) {
            Collections.shuffle(tors, Simulator.selectIndependentRandom("all_to_all_fraction_shuffle"));
        }
        int numChosenTors = (int) Math.floor(numTors * activeFractionX);

        // Probability between each server pair
        double serverPairProb = 1.0 / (Math.floor((double) numChosenTors / 2) * serversPerNodeToExtendWith * serversPerNodeToExtendWith * 2);

        System.out.print("Generating pairings pair probabilities in " + (activeFractionX * 100) + "% fraction " + tors.size() + " ToRs between their servers...");

        List<Integer> remaining = new ArrayList<>();
        for (int i = 0; i < numChosenTors; i++) {
            remaining.add(tors.get(i));
        }

        Random pairingsRandom = Simulator.selectIndependentRandom("pairings_fraction");

        // Go over every ToR pair
        List<org.apache.commons.lang3.tuple.Pair<Integer, Integer>> chosen = new ArrayList<>();
        while (remaining.size() >= 2 ) {

            // Choose two at random
            int idxFirst = Math.abs(pairingsRandom.nextInt()) % remaining.size();
            int first = remaining.get(idxFirst);
            int idxSecond = Math.abs(pairingsRandom.nextInt()) % remaining.size();
            int second = remaining.get(idxSecond);
            while (first == second) {
                idxSecond = Math.abs(pairingsRandom.nextInt()) % remaining.size();
                second = remaining.get(idxSecond);
            }

            // Add to random pair generator
            for (Integer svrA : Simulator.getConfiguration().getGraphDetails().getServersOfTor(first)) {
                for (Integer svrB : Simulator.getConfiguration().getGraphDetails().getServersOfTor(second)) {
                    this.randomPairGenerator.add(serverPairProb, new ImmutablePair<>(svrA, svrB));
                    this.randomPairGenerator.add(serverPairProb, new ImmutablePair<>(svrB, svrA));
                }
            }
            chosen.add(new ImmutablePair<>(first, second));

            // Remove ones already decided for
            remaining.remove(idxFirst);
            if (idxFirst < idxSecond) {
                idxSecond = idxSecond - 1;
            }
            remaining.remove(idxSecond);

        }

        // Log chosen fraction
        Collections.sort(chosen);
        SimulationLogger.logInfo("PAIRING_NUM_CHOSEN_TORS", String.valueOf(numChosenTors));
        SimulationLogger.logInfo("PAIRINGS_FRACTION_CHOSEN_TORS", chosen.toString());

        System.out.println(" done.");

    }
    
    /**
     * Read in the communication pair probabilities from a file.
     *
     * File line structure:
     * # This is a comment line which is ignored
     * [tor_pair_id], [src], [dst], [pdf_num_bytes]
     *
     * @param fileName  File name of the pair probabilities file
     */
    private void readPairProbabilitiesFromFile(String fileName) {

        try {

            System.out.print("Reading in communication pair probabilities...");

            // Open file stream
            FileReader input = new FileReader(fileName);
            BufferedReader br = new BufferedReader(input);

            // Go over all lines
            String line;

            // Read in others
            double totalPdfSum = 0.0;
            while ((line = br.readLine()) != null) {

                // Remove trailing whitespace
                line = line.trim();

                // Skip empty lines
                if (line.equals("") || line.startsWith("#")) {
                    continue;
                }

                String[] spl = line.split(",");

                if (spl.length != 4) {
                    throw new RuntimeException("Communication probability line must have [tor_pair_id, src, dst, pdf_num_bytes]");
                }

                // Convert to correct format
                // int torPairId = Integer.valueOf(spl[0]);
                int src = Integer.valueOf(spl[1]);
                int dst = Integer.valueOf(spl[2]);
                double pdfNumBytes = Double.valueOf(spl[3]);

                // Add random pair generator
                this.randomPairGenerator.add(pdfNumBytes, new ImmutablePair<>(src, dst));

                // Add to total probability sum
                totalPdfSum += pdfNumBytes;

            }

            if (Math.abs(1.0 - totalPdfSum) > 1e-6) {
                throw new RuntimeException("Probability density did not sum up to approximately 1, but was " + totalPdfSum);
            }

            // Close file stream
            br.close();

            System.out.println(" done.");

        } catch (IOException e) {
            throw new RuntimeException("Graph: failed to read file: " + e.getMessage());
        }

    }

//    @Override
    public void createPlan_smallScale(long durationNs) {

        System.out.print("Creating arrival plan...");

        // Statistics tracking
        long time = 0;
        int x = 0;
        long sum = 0;

        // Generate flow start events until the duration
        // of the experiment has lapsed
        long nextProgressLog = durationNs / 10;
        int count = 0;
        long interArrivalTime = (long) (-Math.log(ownIndependentRng.nextDouble()) / (lambdaFlowStartsPerSecond / 1e9));
        long fs1 = 1000000000L;
        long nextUDPStartTime = 50000000;

//        while (time <= durationNs) {
//            interArrivalTime = (long) (-Math.log(ownIndependentRng.nextDouble()) / (11850 / 1e9));
//            sum += interArrivalTime;
//            Pair<Integer, Integer> pair = choosePair();
////            registerFlow(time, (pair.getLeft() % 8), (pair.getLeft() % 8) + 16, flowSizeDistribution.generateFlowSizeByte() * 10);
//            if (pair.getLeft() %4 == 3) {
//                registerFlow(time, 14, 14 + 16, flowSizeDistribution.generateFlowSizeByte() * 10);
//            }
//            else {
//                registerFlow(time, (pair.getLeft() % 3) + 8, (pair.getLeft() % 3) + 8 + 16, flowSizeDistribution.generateFlowSizeByte() * 10);
//            }
//            time += interArrivalTime;
//            x++;
//        }
        this.increaseFlowIdCounter();
        time = 0;


        while (time <= durationNs) {

            // Poisson arrival
            //
            // F(x) = 1 - e^(-lambda * x)
            // <=>
            // ln( 1 - F(x) ) = -lambda * x
            // <=>
            // x = -ln( 1 - F(x) ) / lambda
            // <=>
            // x = -ln( Uniform(x) ) / lambda
            //
            interArrivalTime = (long) (-Math.log(ownIndependentRng.nextDouble()) / (lambdaFlowStartsPerSecond / 1e9));

            // Uniform arrival
            //
            // long interArrivalTime = (long) (1 / (lambdaFlowStartsPerSecond / 1e9));

            // Add to sum for later statistics
            sum += interArrivalTime;

//            System.out.println("Sum:" + sum + ", interval:" + interArrivalTime);

            // Register flow
            Pair<Integer, Integer> pair = choosePair();
            // TODO: change here left,right->1,18


            registerFlow(time, (pair.getLeft() % 8) + 8, (pair.getLeft() % 8) + 8 + 16, flowSizeDistribution.generateFlowSizeByte());

            // Advance time to next arrival
            time += interArrivalTime;
            x++;

            if (time > nextProgressLog) {
                System.out.print(" " + (100 * time / durationNs) + "%...");
                nextProgressLog += durationNs / 10;
            }

            count ++;

        }

        System.out.println(" done.");

        // Log plan created
//        System.out.println("Poisson Arrival plan created.");
//        System.out.println("Number of flows created: " + x + ".");
//        System.out.println("Mean inter-arrival time: " + (sum / x) + " (expectation: "
//                + (1 / (lambdaFlowStartsPerSecond / 1e9)) + ")");
//        SimulationLogger.logInfo("Flow planner number flows", String.valueOf(x));
//        SimulationLogger.logInfo("Flow planner mean inter-arrival time", String.valueOf((sum / x)));
//        SimulationLogger.logInfo("Flow planner expected inter-arrival time", String.valueOf((1 / (lambdaFlowStartsPerSecond / 1e9))));
//        SimulationLogger.logInfo("Flow planner poisson rate lambda (flow starts/s)", String.valueOf(lambdaFlowStartsPerSecond));

    }

    @Override
    public void createPlan(long durationNs) {
        System.out.print("Creating arrival plan...");

        // Statistics tracking
        long time = 0;
        int x = 0;
        long sum = 0;
        int mod_nx;
        // Generate flow start events until the duration
        // of the experiment has lapsed
        long nextProgressLog = durationNs / 10;
        int count = 0;
        long interArrivalTime = (long) (-Math.log(ownIndependentRng.nextDouble()) / (lambdaFlowStartsPerSecond / 1e9));
        long fs1 = 1000000000L;
        long nextUDPStartTime = 50000000;
        int very_big_flow_size = 2000000000;

        // ** inject UDP
         int i,j,k;
//          for (i=0;i<9;i++) {
//              for (j = 0; j < 16; j++) {
//                  time = 0;
//                  for (k = 0; k < 32; k++) {
//                      registerFlowWithPort_udp(time, (i * 16 + j) % 144 + 144, ((i + 1) * 16 + j) % 144 + 144, very_big_flow_size, 80, 80);
//                     //  time = time + 1500 * 2;
//                     time = time + 300;
//                     //  registerFlowWithPort_udp(time, ((i + 1) * 16 + j) % 144 + 144, (i * 16 + j) % 144 + 144,  600000000, 80, 80);
//                  }
                 
// //                 for (k = 0; k < 1; k++) {
// //                     registerFlowWithPort_udp(time, (i * 16 + j) % 144, ((i + 1) * 16 + j) % 144, 600000000, 11, 8000);
// //                 }
//              }
//          }
        // registerFlowWithPort_udp(time, 1, 3, 1200000000, 1, 1);
        // registerFlowWithPort_udp(time, 2, 3, 1200000000, 2, 2);

        this.increaseFlowIdCounter();
        time = 0;

        int experiment = 1;
        // experiment = 1;

        if (experiment == 0){
            //**   incast   */
            Random rand = new Random();
            int random_integer = rand.nextInt(100-10) + 10;
            random_integer = 0;
            int num_of_servers = 90;
            if (lambdaFlowStartsPerSecond <= 3601) 
                num_of_servers = 10;
            else if (lambdaFlowStartsPerSecond <= 5201)
                num_of_servers = 20;
            else if (lambdaFlowStartsPerSecond <= 7001)
                num_of_servers = 30;
            else if (lambdaFlowStartsPerSecond <= 8901)
                num_of_servers = 40;
            else if (lambdaFlowStartsPerSecond <= 11101)
                num_of_servers = 50;
            else
                num_of_servers = 60;
            for (i=32;i<32 + num_of_servers;i++) {
                // random_integer = rand.nextInt(2);
                // System.out.println("register a flow.");
                registerFlowWithPort(time + random_integer, i, 0, 1 * (1 * 1500 * 1000/num_of_servers), 80, 80);
                registerFlowWithPort(time + random_integer, i, 0, 1 * (1 * 1500 * 1000/num_of_servers), 80, 80);
                registerFlowWithPort(time + random_integer, i, 0, 1 * (1 * 1500 * 1000/num_of_servers), 80, 80);
                // registerFlowWithPort(time + random_integer, i, 0, 1 * (1 * 1500 * 1000/num_of_servers), 80, 80);
                // registerFlowWithPort(time + random_integer, i, 0, 1 * (1 * 1500 * 1000/num_of_servers), 80, 80);
                // registerFlowWithPort(time, i,  j, very_big_flow_size, tenant_id, tenant_id);
            }
        }
        else  if (experiment == 2) {
            // ** Web search workload ** //
            while (time <= durationNs) {
                interArrivalTime = (long) (-Math.log(ownIndependentRng.nextDouble()) / (lambdaFlowStartsPerSecond / 1e9));
                // Add to sum for later statistics
                sum += interArrivalTime;

                // Register flow
                Pair<Integer, Integer> pair;
                pair = choosePair();

                Random independentRng = new Random();
                int ratio = 11;
                
                registerFlowWithPort(time, pair.getLeft(), pair.getRight(), flowSizeDistribution.generateFlowSizeByte(),80, 80);
        
                // Advance time to next arrival
                time += interArrivalTime;
                x++;

                if (time > nextProgressLog) {
                    System.out.print(" " + (100 * time / durationNs) + "%...");
                    nextProgressLog += durationNs / 10;
                }

                count ++;
            }
        }
        System.out.println(" done.");
    }

    /**
     * Choose a random pair based on their probability density weight.
     *
     * @return (src, dst) pair
     */
    private Pair<Integer, Integer> choosePair() {
        return this.randomPairGenerator.next();
    }
}
