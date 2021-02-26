package ch.ethz.systems.netbench.xpt.sppifo.ports.CSFQ;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.run.infrastructure.OutputPortGenerator;

public class CSFQOutputPortGenerator extends OutputPortGenerator {
    private final long maxQueueSize;
    private double Kc;

    public CSFQOutputPortGenerator(long maxQueueSize, double Kc) {
        this.maxQueueSize = maxQueueSize;
        this.Kc = Kc;
        SimulationLogger.logInfo("Port", "CSFQ(maxQueueSize=" + maxQueueSize + ")");
    }

    @Override
    public OutputPort generate(NetworkDevice ownNetworkDevice, NetworkDevice towardsNetworkDevice,
                               Link link) {
        return new CSFQOutputPort(ownNetworkDevice, towardsNetworkDevice, link, maxQueueSize, Kc);
//        return new CSFQOutputPort(ownNetworkDevice, towardsNetworkDevice, link, maxQueueSize, Kc, maxQueueSizeBytes);
    }
}