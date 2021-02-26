package ch.ethz.systems.netbench.xpt.sppifo.ports.HCSFQ;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.IpHeader;
import ch.ethz.systems.netbench.xpt.sppifo.ports.HCSFQ.EstFairShareRate;
import ch.ethz.systems.netbench.xpt.sppifo.ports.HCSFQ.FlowState;
import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;
import ch.ethz.systems.netbench.core.Simulator;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class HCSFQOutputPort extends OutputPort {
    private final ArrayBlockingQueue<Packet> queue;
    private double K = 10000000.0 / 100; // Pre-defined constant: many Ts are 32560, others are larger
    private double Ka = 6000000.0 / 0.6 / 100; // Pre-defined constant: many Ts are 32560
    private double Kc = 20000; // Pre-defined window size constant
    private final double Khb = 0.99; // Heuristic for decreasing alpha in buffer overflow: 1%
    private final double Khd = 0.75; // Heuristic for not decreasing alpha by no more than: 25%
    private final double Khc = 0.9; // Heuristic for congested threshold: 50%
    // private final double Kf = 60000;
    private double Kf = 20000;
    private final boolean edgeRouter; // Type of the current device
    private final Map<Long, ch.ethz.systems.netbench.xpt.sppifo.ports.HCSFQ.FlowState> flowMap; // Key: flowId, Val: flowState
    private final ch.ethz.systems.netbench.xpt.sppifo.ports.HCSFQ.EstFairShareRate estFairShareRate;

    private double alpha;
    private long pktArrTime;

    private long maxQueueSizeBits;

    private boolean isServer;

    

    protected HCSFQOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice,
                             Link link, long maxQueueSize, double Kc) {
        // super(ownNetworkDevice, targetNetworkDevice, link, new HCSFQueue(maxQueueSize));
        // super(ownNetworkDevice, targetNetworkDevice, link,
        // new ArrayBlockingQueue<Packet>((int) maxQueueSize, true));
        this(ownNetworkDevice, targetNetworkDevice, link,
                new ArrayBlockingQueue<Packet>((int) maxQueueSize, true));
        // this.Kc = 200000;
        this.Kc = Simulator.getConfiguration().getDoublePropertyOrFail("Kc");
        this.K = Simulator.getConfiguration().getDoublePropertyOrFail("K");
        this.Ka = Simulator.getConfiguration().getDoublePropertyOrFail("Ka");
        
        this.Kf = Simulator.getConfiguration().getDoublePropertyOrFail("Kf");

        // this.maxQueueSizeBits = maxQueueSize * 8L;
        this.maxQueueSizeBits = maxQueueSize;
    }

    protected HCSFQOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice,
                             Link link, ArrayBlockingQueue<Packet> queue) {
        super(ownNetworkDevice, targetNetworkDevice, link, queue);
        this.queue = queue;
        this.edgeRouter = checkIsEdgeRouter(ownNetworkDevice);
        this.isServer = ownNetworkDevice.isServer();
        this.flowMap = new HashMap<>();
        // this.Kc = 200000;
        // this.Kc = 50000;
        this.Kc = Simulator.getConfiguration().getDoublePropertyOrFail("Kc");
        this.K = Simulator.getConfiguration().getDoublePropertyOrFail("K");
        this.Ka = Simulator.getConfiguration().getDoublePropertyOrFail("Ka");
        
        this.estFairShareRate = new EstFairShareRate(Ka, Kc, link.getBandwidthBitPerNs(), Kf, this.getOwnId(), this.getTargetId());
        this.alpha = link.getBandwidthBitPerNs();
        this.pktArrTime = 0;
    }

    @Override
    public void enqueue(Packet pkt) {
        FullExtTcpPacket packet = (FullExtTcpPacket) pkt;
        IpHeader ipHeader = (IpHeader) packet;
        pktArrTime = Simulator.getCurrentTime();
        long flowId = packet.getFlowId();
//        long flowId = packet.getSourceId();
        int tenantId = packet.getTenantId();
        float factor = (float)1.0;

        // System.out.println(maxQueueSizeBits);
        if (packet.getSizeBit() < 1000) {
            if (getQueueSize() + 1 <= maxQueueSizeBits) {
                guaranteedEnqueue(packet);
            } else {
                if (tenantId == 0)
                    SimulationLogger.increaseStatisticCounter("tenant 1 PACKETS_DROPPED at queue");
                else
                    SimulationLogger.increaseStatisticCounter("tenant 2 PACKETS_DROPPED at queue");
                if (ipHeader.getSourceId() == this.getOwnId()) {
                    SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED_AT_SOURCE");
                }
//                alpha = estFairShareRate.getAlpha() * Khb;
//                estFairShareRate.setAlpha(alpha);
//                alpha = estFairShareRate.getAlpha_perTenant(tenantId) * Khb;
//                estFairShareRate.setAlpha_perTenant(alpha, tenantId);
            }
            return;
        }
        if (getBufferOccupiedBits() >= 8L*48000L) {
            // System.out.println("mark ECN");
            ipHeader.markCongestionEncountered();
        }
        else {
            // System.out.println("------dont mark ECN");
        }

// if (tenantId < 27) {
//     if (getQueueSize() <= maxQueueSizeBits /5)
//         factor = (float)1.9;
//     else if (getQueueSize() <= maxQueueSizeBits /5 * 2)
//         factor = (float)1.5;
//     else if (getQueueSize() <= maxQueueSizeBits /5 * 3)
//         factor = (float)1.1;
//     else if (getQueueSize() <= maxQueueSizeBits /5 * 4)
//         factor = (float)0.7;
//     else
//         factor = (float)0.5;
// }
// else {
//     // if (getQueueSize() <= maxQueueSizeBits /3)
//     //     factor = (float)0.5;
//     // else if (getQueueSize() <= maxQueueSizeBits /3 * 2)
//     //     factor = (float)0.9;
//     // else
//     //     factor = (float)1.5;
//     if (getQueueSize() <= maxQueueSizeBits /5)
//         factor = (float)0.5;
//     else if (getQueueSize() <= maxQueueSizeBits /5 * 2)
//         factor = (float)0.7;
//     else if (getQueueSize() <= maxQueueSizeBits /5 * 3)
//         factor = (float)1.1;
//     else if (getQueueSize() <= maxQueueSizeBits /5 * 4)
//         factor = (float)1.5;
//     else
//         factor = (float)1.9;
// }
        // factor = (float)1.0;
        if ((this.isServer)) {
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
        if (edgeRouter) {

            if (!flowMap.containsKey(flowId)) {
                flowMap.put(flowId, new ch.ethz.systems.netbench.xpt.sppifo.ports.HCSFQ.FlowState(K));
            }

            FlowState flowState = flowMap.get(flowId);
            packet.setRateEst(flowState.getEstArrRate(pktArrTime, packet.getSizeBit()));
        }
        alpha = estFairShareRate.getAlpha_perTenant(tenantId);

        
//        double dropProb = Math.max(0, 1 - alpha / packet.getRateEst());
        double dropProb;
        if (1 - factor * alpha / packet.getRateEst() > 0) {
            dropProb = 1 - factor * alpha / packet.getRateEst();
        }
        else {
            dropProb = 0;
        }

        // if (dropProb > 0) {
        //     ipHeader.markCongestionEncountered();
        // }
//        if (tenantId == 0)
//        System.out.println("Alpha:" + estFairShareRate.getAlpha_perTenant(tenantId));
//        System.out.println("rate:" + packet.getRateEst());
//        System.out.println("dropProb:" + dropProb);
//        System.out.println("random:" + Math.random());
//        boolean exdBufThre = queue.size() > (queue.size() + queue.remainingCapacity()) * Khc;
        // boolean exdBufThre = (getBufferOccupiedBits() >= maxQueueSizeBits * Khc);
        boolean exdBufThre = (getQueueSize() >= maxQueueSizeBits * Khc);


        if ((dropProb > Math.random())) {
            double oldAlpha = estFairShareRate.getAlpha();
            double newAlpha = estFairShareRate.getEstAlpha(packet, true, pktArrTime, exdBufThre, tenantId);

            // Heuristic
            if ((newAlpha < oldAlpha * Khd)) {
                newAlpha = oldAlpha * Khd;
                estFairShareRate.setAlpha(newAlpha);
            }
//            else if (newAlpha > oldAlpha * (2.0 - Khd)) {
//                newAlpha = oldAlpha * (2.0 - Khd);
//                estFairShareRate.setAlpha(newAlpha);
//            }

            oldAlpha = estFairShareRate.getAlpha_perTenant(tenantId);
            newAlpha = estFairShareRate.getEstAlpha_perTenant(packet, true, pktArrTime, exdBufThre, tenantId);
            // Heuristic
            if (newAlpha < oldAlpha * Khd) {
                newAlpha = oldAlpha * Khd;
                estFairShareRate.setAlpha_perTenant(newAlpha, tenantId);
            }
//            else if (newAlpha > oldAlpha * (2.0 - Khd)) {
//                newAlpha = oldAlpha * (2.0 - Khd);
//                estFairShareRate.setAlpha_perTenant(newAlpha, tenantId);
//            }
if (tenantId == 0)
    SimulationLogger.increaseStatisticCounter("tenant 1 PACKETS_DROPPED at csfq");
else 
SimulationLogger.increaseStatisticCounter("tenant 2 PACKETS_DROPPED at csfq");

        } else {
            double oldAlpha = estFairShareRate.getAlpha();
            double newAlpha = estFairShareRate.getEstAlpha(packet, false, pktArrTime, exdBufThre, tenantId);

            // Heuristic
            if (newAlpha < oldAlpha * Khd) {
                newAlpha = oldAlpha * Khd;
                estFairShareRate.setAlpha(newAlpha);
            }
//            else if (newAlpha > oldAlpha * (2.0 - Khd)) {
//                newAlpha = oldAlpha * (2.0 - Khd);
//                estFairShareRate.setAlpha(newAlpha);
//            }

            oldAlpha = estFairShareRate.getAlpha_perTenant(tenantId);
            newAlpha = estFairShareRate.getEstAlpha_perTenant(packet, false, pktArrTime, exdBufThre, tenantId);
            // Heuristic
            if (newAlpha < oldAlpha * Khd) {
                newAlpha = oldAlpha * Khd;
                estFairShareRate.setAlpha_perTenant(newAlpha, tenantId);
            }
//            else if (newAlpha > oldAlpha * (2.0 - Khd)) {
//                newAlpha = oldAlpha * (2.0 - Khd);
//                estFairShareRate.setAlpha_perTenant(newAlpha, tenantId);
//            }

            // Relabel the packet
            if (dropProb > 0) {
                packet.setRateEst(newAlpha);
            }

            // if (getBufferOccupiedBits() + ipHeader.getSizeBit() <= maxQueueSizeBits) {
            if (getQueueSize() + 1 <= maxQueueSizeBits) {
                guaranteedEnqueue(packet);
            } else {
                if (tenantId == 0)
                    SimulationLogger.increaseStatisticCounter("tenant 1 PACKETS_DROPPED at queue");
                else
                    SimulationLogger.increaseStatisticCounter("tenant 2 PACKETS_DROPPED at queue");
                if (ipHeader.getSourceId() == this.getOwnId()) {
                    SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED_AT_SOURCE");
                }
//                alpha = estFairShareRate.getAlpha() * Khb;
//                estFairShareRate.setAlpha(alpha);
//                alpha = estFairShareRate.getAlpha_perTenant(tenantId) * Khb;
//                estFairShareRate.setAlpha_perTenant(alpha, tenantId);
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
