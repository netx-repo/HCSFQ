package ch.ethz.systems.netbench.xpt.sppifo.ports.HCSFQ;

import ch.ethz.systems.netbench.core.Simulator;

public class FlowState {
    private final double K;
    private long lastArrTime;
    private double r;

    public FlowState(double K) {
        this.K = K;
        this.lastArrTime =  - 1;
        this.r = 0;
    }

    public double getEstArrRate(long newArrTime, long l) {
        // Only update estimate if coming from a different batch
        if (1==1) {
            long T = newArrTime - lastArrTime;
            double _inc = 20;
            // System.out.println(T);

            lastArrTime = newArrTime;
            r = (1 - Math.exp(-(T+_inc) / K)) * (l * 1.0 / (T+_inc)) + Math.exp(-(T+_inc) / K) * r;
        }

        return r;
    }

    public double getEstRate() {
        return r;
    }
}
