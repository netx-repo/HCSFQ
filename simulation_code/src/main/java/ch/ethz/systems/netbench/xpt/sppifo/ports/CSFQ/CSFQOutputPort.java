package ch.ethz.systems.netbench.xpt.sppifo.ports.CSFQ;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.IpHeader;
import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;
import ch.ethz.systems.netbench.core.Simulator;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class CSFQOutputPort extends OutputPort {
    private final ArrayBlockingQueue<Packet> queue;
    private double K = 100000; // Pre-defined constant: many Ts are 32560, others are larger
    private double Ka = 60000; // Pre-defined constant: many Ts are 32560
    private double Kc = 20000; // Pre-defined window size constant
    private final double Khb = 0.99; // Heuristic for decreasing alpha in buffer overflow: 1%
    private final double Khd = 0.75; // Heuristic for not decreasing alpha by no more than: 25%
    private final double Khc = 0.5; // Heuristic for congested threshold: 50%
    private final boolean edgeRouter; // Type of the current device
    private final Map<Long, FlowState> flowMap; // Key: flowId, Val: flowState
    private final EstFairShareRate estFairShareRate;

    private double alpha;
    private long pktArrTime;

    private long maxQueueSizeBits;
    private int id;
    private int targetId;

    private boolean isServer;
//    protected CSFQOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice,
//                             Link link, long maxQueueSize, double Kc, long maxQueueSizeBytes) {
//        // super(ownNetworkDevice, targetNetworkDevice, link, new CSFQueue(maxQueueSize));
//        // super(ownNetworkDevice, targetNetworkDevice, link,
//        // new ArrayBlockingQueue<Packet>((int) maxQueueSize, true));
//        this(ownNetworkDevice, targetNetworkDevice, link,
//                new ArrayBlockingQueue<Packet>((int) maxQueueSize, true));
//        this.Kc = Kc;
//        this.maxQueueSizeBits = maxQueueSizeBytes * 8L;
//    }

    protected CSFQOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice,
                            Link link, long maxQueueSize, double Kc) {
        // super(ownNetworkDevice, targetNetworkDevice, link, new CSFQueue(maxQueueSize));
        // super(ownNetworkDevice, targetNetworkDevice, link,
        // new ArrayBlockingQueue<Packet>((int) maxQueueSize, true));
        this(ownNetworkDevice, targetNetworkDevice, link,
                new ArrayBlockingQueue<Packet>((int) maxQueueSize, true));
        // this.Kc = 3000;

        this.Kc = Simulator.getConfiguration().getDoublePropertyOrFail("Kc");
        this.K = Simulator.getConfiguration().getDoublePropertyOrFail("K");
        this.Ka = Simulator.getConfiguration().getDoublePropertyOrFail("Ka");

        // this.maxQueueSizeBits = maxQueueSize * 8L;
        this.maxQueueSizeBits = maxQueueSize;
    }

    protected CSFQOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice,
                            Link link, ArrayBlockingQueue<Packet> queue) {
        super(ownNetworkDevice, targetNetworkDevice, link, queue);
        this.queue = queue;
        this.edgeRouter = checkIsEdgeRouter(ownNetworkDevice);
        this.isServer = ownNetworkDevice.isServer();
        this.flowMap = new HashMap<>();
        // this.Kc = 3000;

        this.Kc = Simulator.getConfiguration().getDoublePropertyOrFail("Kc");
        this.K = Simulator.getConfiguration().getDoublePropertyOrFail("K");
        this.Ka = Simulator.getConfiguration().getDoublePropertyOrFail("Ka");

        this.estFairShareRate = new EstFairShareRate(Ka, Kc, link.getBandwidthBitPerNs());
        this.alpha = link.getBandwidthBitPerNs();
        this.pktArrTime = 0;
        this.id = ownNetworkDevice.getIdentifier();
        this.targetId = targetNetworkDevice.getIdentifier();
    }

    @Override
    public void enqueue(Packet pkt) {
        FullExtTcpPacket packet = (FullExtTcpPacket) pkt;
        IpHeader ipHeader = (IpHeader) packet;
        pktArrTime = Simulator.getCurrentTime();

        // System.out.println(packet.getDepartureTime());
        // System.out.println(packet.getEnqueueTime());
        // System.out.println(Simulator.getCurrentTime());
        // System.out.println();

        // if (getBufferOccupiedBits() >= 8L*10000) {
        //     ipHeader.markCongestionEncountered();
        // }
        if (this.isServer) {
            // if (getBufferOccupiedBits() + ipHeader.getSizeBit() <= maxQueueSizeBits) {
            if (getQueueSize() + 1 <= maxQueueSizeBits) {
                guaranteedEnqueue(packet);
            } else {
//                System.out.println("PACKET DROPP");
                SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED");
                if (ipHeader.getSourceId() == this.getOwnId()) {
                    SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED_AT_SOURCE");
                }
            }
            return;
        }

        
        // TODO: check is sending?
        // if (edgeRouter) {
            long flowId = packet.getFlowId();
//            long flowId = packet.getSourceId();
            if (!flowMap.containsKey(flowId)) {
                flowMap.put(flowId, new FlowState(K));
            }

            FlowState flowState = flowMap.get(flowId);

        // if ((this.id == 288) && (targetId == 297)) { 
        //     if (flowId == 1001486) {
        //         System.out.println("old flowID: " + flowId);
        //         System.out.println("old time: " + pktArrTime);
        //         System.out.println("old rate: " + flowState.getRate());
        //         System.out.println("old count: " + flowState.getCount());
        //     }
        //     else {
    
        //     }
        // }
            packet.setRateEst(flowState.getEstArrRate(pktArrTime, packet.getSizeBit(), (this.id == 288) && (targetId == 297) && (flowId == 10)));
        // }
    // if ((this.id == 288) && (targetId == 297)) { 
    //     if (flowId == 10) {
    //         System.out.println("flowID: " + flowId);
    //         System.out.println("time: " + pktArrTime);
    //         System.out.println("rate: " + packet.getRateEst());
    //         System.out.println("alpha: " + alpha);
    //     }
    //     else {

    //     }
    // }
        double dropProb;
        if (1 - alpha / packet.getRateEst() > 0) {
            dropProb = 1 - alpha / packet.getRateEst();
        }
        else {
            dropProb = 0;
        }
        // if (dropProb > 0) {
        //    System.out.println("alpha:" + alpha);
        //    System.out.println("rate:" + packet.getRateEst());
        //    System.out.println("prob:" + dropProb);
        // }
        // boolean exdBufThre = getBufferOccupiedBits() >= maxQueueSizeBits * Khc;
        boolean exdBufThre = getQueueSize() >= maxQueueSizeBits * Khc;

        if (dropProb > Math.random()) {

            double newAlpha = estFairShareRate.getEstAlpha(packet, true, pktArrTime, exdBufThre, (this.id == 288) && (targetId == 297));

            // Heuristic
            if (newAlpha < alpha * Khd) {
                newAlpha = alpha * Khd;
                estFairShareRate.setAlpha(newAlpha);
            }

            alpha = newAlpha;
            
//             ipHeader.markCongestionEncountered();
//             if (getQueueSize() + 1 <= maxQueueSizeBits) {
//                 guaranteedEnqueue(packet);
//             } else {
// //                System.out.println("PACKET DROPP");
//                 SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED");
//                 if (ipHeader.getSourceId() == this.getOwnId()) {
//                     SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED_AT_SOURCE");
//                 }
//             }
            // TODO: log drop pkt
        } else {
            double newAlpha = estFairShareRate.getEstAlpha(packet, false, pktArrTime, exdBufThre, (this.id == 288) && (targetId == 297));

            // Heuristic
//            if (newAlpha < alpha * Khd) {
//                newAlpha = alpha * Khd;
//                estFairShareRate.setAlpha(newAlpha);
//            }
            estFairShareRate.setAlpha(newAlpha);
            alpha = newAlpha;

            // Relabel the packet
//            if (dropProb > 0) {
//                packet.setRateEst(alpha);
//            }

//            guaranteedEnqueue(packet);
            if (packet.getRateEst() >= 0.99 * alpha)
                ipHeader.markCongestionEncountered();
            // if (getBufferOccupiedBits() + ipHeader.getSizeBit() <= maxQueueSizeBits) {
            if (getQueueSize() + 1 <= maxQueueSizeBits) {
                guaranteedEnqueue(packet);
            } else {
//                System.out.println("PACKET DROPP");
                SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED");
                if (ipHeader.getSourceId() == this.getOwnId()) {
                    SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED_AT_SOURCE");
                }
            }

//            // Buffer overflow heuristic
//            boolean overflowed = potentialEnqueue(packet);
//
//            if (overflowed) {
//                alpha = alpha * Khb;
//                estFairShareRate.setAlpha(alpha);
//            }
        }
    }

    private boolean checkIsEdgeRouter(NetworkDevice ownNetworkDevice) {
        // TODO: check if it is edge router => may look at topology
        return true;
    }
}
