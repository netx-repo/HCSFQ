package ch.ethz.systems.netbench.xpt.sppifo.ports.TailDrop;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.IpHeader;

import java.util.concurrent.LinkedBlockingQueue;

public class TailDropOutputPort extends OutputPort {

    private final long maxQueueSizeBits;
    private int id,tid;
    TailDropOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, long maxQueueSizeBytes) {
        super(ownNetworkDevice, targetNetworkDevice, link, new LinkedBlockingQueue<Packet>());
        this.maxQueueSizeBits = maxQueueSizeBytes * 8L;
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

        // Tail-drop enqueue
        
        if (this.queue.size() + 1 <= maxQueueSizeBits / 8 / 1460) {
            // if (this.queue.size() + 1 <= 20) {
            guaranteedEnqueue(packet);
        } else {
            // if ((this.id == 288) && (this.tid == 0))
            //     System.out.println("------occupied bits: " + getBufferOccupiedBits() / 8);
            SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED");
            if (ipHeader.getSourceId() == this.getOwnId()) {
                SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED_AT_SOURCE");
            }
        }

        // if (getBufferOccupiedBits() + ipHeader.getSizeBit() <= maxQueueSizeBits) {
        //     // if ((this.id == 288) && (this.tid == 0)) {
        //     //     System.out.println("occupied bits: " + getBufferOccupiedBits() / 8);
        //     //     // System.out.println("tid: " + this.tid);
        //     // }
        //     guaranteedEnqueue(packet);
        // } else {
        //     // if ((this.id == 288) && (this.tid == 0))
        //     //     System.out.println("------occupied bits: " + getBufferOccupiedBits() / 8);
        //     SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED");
        //     if (ipHeader.getSourceId() == this.getOwnId()) {
        //         SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED_AT_SOURCE");
        //     }
        // }
    }
}
