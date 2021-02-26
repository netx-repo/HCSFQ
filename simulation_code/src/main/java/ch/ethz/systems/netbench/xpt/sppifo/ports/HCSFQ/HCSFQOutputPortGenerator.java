package ch.ethz.systems.netbench.xpt.sppifo.ports.HCSFQ;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.run.infrastructure.OutputPortGenerator;
import ch.ethz.systems.netbench.xpt.sppifo.ports.HCSFQ.HCSFQOutputPort;

public class HCSFQOutputPortGenerator extends OutputPortGenerator {
    private final long maxQueueSize;
    private double Kc;

    public HCSFQOutputPortGenerator(long maxQueueSize, double Kc) {
        this.maxQueueSize = maxQueueSize;
        this.Kc = Kc;
        SimulationLogger.logInfo("Port", "HCSFQ(maxQueueSize=" + maxQueueSize + ")");
    }

    @Override
    public OutputPort generate(NetworkDevice ownNetworkDevice, NetworkDevice towardsNetworkDevice,
                               Link link) {
        return new HCSFQOutputPort(ownNetworkDevice, towardsNetworkDevice, link, maxQueueSize, Kc);
//        return new CSFQOutputPort(ownNetworkDevice, towardsNetworkDevice, link, maxQueueSize, Kc, maxQueueSizeBytes);
    }
}