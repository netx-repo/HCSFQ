package ch.ethz.systems.netbench.xpt.simple.simpletcp;

import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.xpt.newreno.newrenotcp.NewRenoTcpSocket;
import ch.ethz.systems.netbench.xpt.simple.simpleudp.SimpleUdpSocket;

public class SimpleTcpTransportLayer extends TransportLayer {

    /**
     * Create the TCP transport layer with the given network device identifier.
     * The network device identifier is used to create unique flow identifiers.
     *
     * @param identifier        Parent network device identifier
     */
    public SimpleTcpTransportLayer(int identifier) {
        super(identifier);
    }

    @Override
    protected Socket createSocket(long flowId, int destinationId, long flowSizeByte) {
        return new SimpleTcpSocket(this, flowId, this.identifier, destinationId, flowSizeByte);
    }

    @java.lang.Override
    protected Socket createSocketWithRealFlowSize(long flowId, int destinationId, long flowSizeByte, long realFlowSizeByte) {
//        return null;
        if (flowSizeByte == -1) {
            return new SimpleUdpSocket(this, flowId, destinationId, identifier, flowSizeByte, realFlowSizeByte);
        }
        else {
            return new SimpleUdpSocket(this, flowId, identifier, destinationId, flowSizeByte, realFlowSizeByte);
        }
    }

    @Override
    protected Socket createSocketWithPort(long flowId, int destinationId, long flowSizeByte, int sourcePort, int destinationPort) {
        return new SimpleTcpSocket(this, flowId, this.identifier, destinationId, flowSizeByte);
//        return new DemoSocket(this, flowId, identifier, destinationId, flowSizeByte, sourcePort, destinationPort);
    }

    @Override
    protected Socket createSocketWithPortAndRealFlowSize(long flowId, int destinationId, long flowSizeByte, long realFlowSizeByte, int sourcePort, int destinationPort) {
        if (flowSizeByte == -1) {
            return new SimpleUdpSocket(this, flowId, destinationId, identifier, flowSizeByte, realFlowSizeByte, sourcePort, destinationPort);
        }
        else {
            return new SimpleUdpSocket(this, flowId, identifier, destinationId, flowSizeByte, realFlowSizeByte, sourcePort, destinationPort);
        }
    }
}
