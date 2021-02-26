package ch.ethz.systems.netbench.xpt.sppifo.ports.CSFQ;

public class FlowState {
    private final double K;
    private long lastArrTime;
    private double r;
    private double last_rate;
    private int count;

    public FlowState(double K) {
        this.K = K;
        this.lastArrTime = -1;
        this.r = 0;
        this.last_rate = 0;
        this.count = 0;
    }

    public double getEstArrRate(long newArrTime, long l, boolean flag) {
        count = (count + 1) % 30000;
        // Only update estimate if coming from a different batch
        if (1 == 1) {
//        if (newArrTime != lastArrTime) {
            long T = newArrTime - lastArrTime;
//            System.out.println("T:" + T);
            // System.out.println(T);

            lastArrTime = newArrTime;
            last_rate = r;
            
            r = (1 - Math.exp(-(T+5) / K)) * (l * 1.0 / (T + 5)) + Math.exp(-(T+5) / K) * r;
            
        }

        return r;
        // return this.last_rate;
    }

    public int getCount() {
        return this.count;
    }
    public double getRate() {
        return this.r;
        // return this.last_rate;
    }

    public double getEstRate() {
        // return last_rate;
        return r;
    }
}
