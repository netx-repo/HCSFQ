package ch.ethz.systems.netbench.xpt.sppifo.ports.TOY;

import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.run.infrastructure.OutputPortGenerator;

public class TOYOutputPortGenerator extends OutputPortGenerator {

    private final long maxQueueSize;
    private final int windowSize;
    private final int itemsPerWindow;
    private final int acceptThreshold;

    public TOYOutputPortGenerator(long maxQueueSize, int windowSize, int itemsPerWindow, int acceptThreshold) {
        this.maxQueueSize = maxQueueSize;
        this.windowSize = windowSize;
        this.itemsPerWindow = itemsPerWindow;
        this.acceptThreshold = acceptThreshold;
    }

    @Override
    public OutputPort generate(NetworkDevice ownNetworkDevice, NetworkDevice towardsNetworkDevice, Link link) {
        return new TOYOutputPort(ownNetworkDevice, towardsNetworkDevice, link, maxQueueSize, windowSize, itemsPerWindow, acceptThreshold);
    }
}
