package ch.ethz.systems.netbench.xpt.voijslav_and_sppifo.tcp.pfabric;


import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.xpt.newreno.newrenotcp.NewRenoTcpSocket;
import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;


public class PfabricSocket extends NewRenoTcpSocket {
	
	private long predictedFlowSize;

    public PfabricSocket(
    	TransportLayer transportLayer,
    	long flowId,
    	int sourceId,
    	int destinationId,
    	long flowSizeByte,
    	long seed
    ) {
		super(transportLayer, flowId, sourceId, destinationId, flowSizeByte);
        //RTO = 3* RTT, with an RTT of 32.12us for the leaf spine of 1-4Gbps
        this.roundTripTimeout = 96384L;
        // this.roundTripTimeout = 32384L;
        this.congestionWindow = this.slowStartThreshold;
        // this.LOSS_WINDOW_SIZE = (long) this.slowStartThreshold;
		predictedFlowSize = flowSizeByte;
    }

    // @Override
    // protected void handleDuplicateAcknowledgment(long ack, long count) {
    //     return;
    // }

    // @Override
    // protected void fastRetransmit(long seq) {
    //     return;

    // }

    // @Override
    // public void handleRetransmissionTimeOut() {
    //     return;

    // }

    @Override
    protected FullExtTcpPacket createPacket(
        long dataSizeByte,
        long sequenceNumber,
        long ackNumber,
        boolean ACK,
        boolean SYN,
        boolean ECE
    ) {

		long priority = Math.max(0,predictedFlowSize - sequenceNumber);

        return new FullExtTcpPacket(
            flowId, dataSizeByte, sourceId, destinationId,
            100, 80, 80, // TTL, source port, destination port
            sequenceNumber, ackNumber, // Seq number, Ack number
            false, false, ECE, // NS, CWR, ECE
            false, ACK, false, // URG, ACK, PSH
            false, SYN, false, // RST, SYN, FIN
            0, // Window size
            priority
        );
    }

}
