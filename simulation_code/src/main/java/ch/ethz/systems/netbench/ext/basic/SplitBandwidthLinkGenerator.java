package ch.ethz.systems.netbench.ext.basic;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.infrastructure.LinkGenerator;

/*Class by Albert Gran to describe different bandwidth links in Leaf-Spine Topology*/

/* Access links are of 10Gbps and leaf-spine links are 40Gbps. Remember that the difference between servers and switches
* is that switches don't have transport layer.*/

public class SplitBandwidthLinkGenerator extends LinkGenerator {

    private final long delayNs;
    private final long bandwidthBitPerNs;

    public SplitBandwidthLinkGenerator(long delayNs, long bandwidthBitPerNs) {
        this.delayNs = delayNs;
        this.bandwidthBitPerNs = bandwidthBitPerNs;
        SimulationLogger.logInfo("Link", "PERFECT_SIMPLE_LINK(delayNs=" + delayNs + ", bandwidthBitPerNs=" + bandwidthBitPerNs + ")");
    }

    @Override
    public Link generate(NetworkDevice fromNetworkDevice, NetworkDevice toNetworkDevice) {
        long delayNs_rtt = delayNs;
        // if (((fromNetworkDevice.getIdentifier() == 0) || (fromNetworkDevice.getIdentifier() == 1)) && (toNetworkDevice.getIdentifier() == 4)) {
        //     delayNs_rtt = delayNs + 10 * 1000;
        // }
        // else if (((fromNetworkDevice.getIdentifier() == 2) || (fromNetworkDevice.getIdentifier() == 3)) && (toNetworkDevice.getIdentifier() == 4)) {
        //     delayNs_rtt = delayNs + 300 * 1000;
        // }

        // delayNs_rtt = delayNs_rtt + 2500;

        // if (((fromNetworkDevice.getIdentifier() == 0) || (fromNetworkDevice.getIdentifier() == 1) || (fromNetworkDevice.getIdentifier() == 2)) && (toNetworkDevice.getIdentifier() == 4)) {
        //     delayNs_rtt = delayNs + 10 * 1000;
        // }
        // else if ((fromNetworkDevice.getIdentifier() == 3) && (toNetworkDevice.getIdentifier() == 4)) {
        //     delayNs_rtt = delayNs + 300 * 1000;
        // }
        if (fromNetworkDevice.isServer() || toNetworkDevice.isServer()) {
            return new PerfectSimpleLink(delayNs_rtt, 40);
            // TODO: CHANGE BACK to 1&4
            // return new PerfectSimpleLink(delayNs, 1);
        } else {
            return new PerfectSimpleLink(delayNs_rtt, 40);
            // TODO: CHANGE BACK to 1&4
            // return new PerfectSimpleLink(delayNs, 4);
        }

    }

}
