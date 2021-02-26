package ch.ethz.systems.netbench.ext.basic;

import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Packet;

import java.util.concurrent.LinkedBlockingQueue;

public class EcnTailDropOutputPort extends OutputPort {

    private final long ecnThresholdKBits;
    private final long maxQueueSizeBits;
    private int id, tid;

    EcnTailDropOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, long maxQueueSizeBytes, long ecnThresholdKBytes) {
        super(ownNetworkDevice, targetNetworkDevice, link, new LinkedBlockingQueue<Packet>());
        this.maxQueueSizeBits = maxQueueSizeBytes * 8L;
        this.ecnThresholdKBits = ecnThresholdKBytes * 8L;
        this.id = ownNetworkDevice.getIdentifier();
        this.tid = targetNetworkDevice.getIdentifier();
    }

    /**
     * Enqueue the given packet.
     * Drops it if the queue is full (tail drop).
     *
     * @param packet    Packet instance
     */
    @Override
    public void enqueue(Packet packet) {

        // Convert to IP packet
        IpHeader ipHeader = (IpHeader) packet;

        // Mark congestion flag if size of the queue is too big
        if (getBufferOccupiedBits() >= ecnThresholdKBits) {
            ipHeader.markCongestionEncountered();
        }

        // Tail-drop enqueue
        if (getBufferOccupiedBits() + ipHeader.getSizeBit() <= maxQueueSizeBits) {
            // if ((this.id == 288) && (this.tid == 0))
            // // if (this.tid == 288)
            //     System.out.println("DCTCP occupied bits: " + getBufferOccupiedBits() / 8);
            guaranteedEnqueue(packet);
        } else {
            // if ((this.id == 288) && (this.tid == 0))
            // // if (this.tid == 288)
            //     System.out.println("--------DCTCP occupied bits: " + getBufferOccupiedBits() / 8);
            SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED");
            if (ipHeader.getSourceId() == this.getOwnId()) {
                SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED_AT_SOURCE");
            }
        }

    }

}
