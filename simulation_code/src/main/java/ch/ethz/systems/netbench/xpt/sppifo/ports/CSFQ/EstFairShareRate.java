package ch.ethz.systems.netbench.xpt.sppifo.ports.CSFQ;

import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;

import java.time.temporal.TemporalAccessor;

import ch.ethz.systems.netbench.core.Simulator;

public class EstFairShareRate {
    private final double Ka; // Constant
    private final double Kc; // Constant
    private final double C; // Output link speed

    private double alpha; // Estimated fair share rate
    private double tmpAlpha; // Temp fair share rate
    private double A; // Estimated aggregate arrival rate
    private double F; // Estimated rate of accepted traffic
    private long startTime;
    private long lastArrTime;
    private long lastDiffArrTime;
    private long T; // Arrival interval
    private long l; // Packet length
    private boolean congested;

    private long lastFTime;

    private double flow_initial;

    public EstFairShareRate(double Ka, double Kc, double C) {
        this.Ka = Ka;
        this.Kc = Kc;
        this.C = C;
        this.alpha = C;
        this.tmpAlpha = C ;
        this.A = 0;
        this.F = 1;
        this.lastArrTime = -1;
        this.lastDiffArrTime = -1;
        this.lastFTime = -1;
        this.startTime = 0;
        this.T = 0;
        this.l = 0;
        this.congested = false;

        this.flow_initial = Simulator.getConfiguration().getDoublePropertyOrFail("flow_initial");
    }

    public double getEstAlpha(FullExtTcpPacket packet, boolean dropped, long newArrTime,
                              boolean exdBufThre, boolean flag) {
        // Only update estimate if coming from a different batch
//        if (newArrTime != lastArrTime) {
        if (1==1) {
            T = Simulator.getCurrentTime() - lastArrTime;
            l = packet.getSizeBit();

            // System.out.println(T);
            A = getEstRate(A, T, l);

            if (dropped == false) {
                long FT = Simulator.getCurrentTime() - lastFTime;
                F = getEstRate(F, FT, l);
                lastFTime = Simulator.getCurrentTime();
            }
            else {
                if (Simulator.getCurrentTime() - lastFTime > 100000) {
                    long FT = Simulator.getCurrentTime() - lastFTime;
                    F = getEstRate(F, FT, 0);
                    lastFTime = Simulator.getCurrentTime();
                }
            }
//            else {
//                F = getEstRate(F, T, 0);
//            }
            // if (flag == true) {
            //     System.out.println("A: " + A);
            //     System.out.println("alpha: " + alpha);
            //     System.out.println("F: " + F);
            // }
            if (A >= C) {
//                System.out.println("max A:" + A);
                if (congested == false) {
//                    if (exdBufThre) {
                        // Heuristic: we only assume the link is congested after
                        // buffer capacity exceeds some threshold
                        congested = true;
                        startTime = Simulator.getCurrentTime();
//                    }
                } else {
                    if (Simulator.getCurrentTime() > startTime + Kc) {
                        if ((alpha == 0) || (alpha > F)) {
                            alpha = C;
                        }
                        else {
                            alpha *= (C / F);
                        }
                        startTime = Simulator.getCurrentTime();
                    }
                }
            } else {
                if (congested == true) {
                    congested = false;
                    startTime = Simulator.getCurrentTime();
                    tmpAlpha = 0;
                    // tmpAlpha = Math.max(0.2*C, packet.getRateEst());
                } else {
                    if (Simulator.getCurrentTime() < startTime + Kc) {

                        tmpAlpha = Math.max(tmpAlpha, packet.getRateEst());
                        // System.out.println("--curTime:" + Simulator.getCurrentTime());
                        // System.out.println("--startTime:" + startTime);
                    } else {
                        if (tmpAlpha == 0) {
                            tmpAlpha = 0.07 * C;
                        }
                        alpha = tmpAlpha;
                        startTime = Simulator.getCurrentTime();

                        // ** fixed para
                        // tmpAlpha = Math.max(0.07*C, packet.getRateEst());

                        // ** using script
                        tmpAlpha = Math.max(flow_initial * C, packet.getRateEst());
//                        tmpAlpha = tmpAlpha * 0.99;
//                        tmpAlpha = C;
                    }
                }
            }
            // if (flag == true) {
            //     System.out.println("new A: " + A);
            //     System.out.println("new alpha: " + alpha);
            //     System.out.println("new F: " + F);
            // }
            // Update last arrival time
            lastDiffArrTime = lastArrTime;
            lastArrTime = newArrTime;

        }
        else {
            T = Simulator.getCurrentTime() - lastDiffArrTime;
            l = packet.getSizeBit();
            A = getEstRate_sameTime(A, T, l);

            if (dropped == false) {
                F = getEstRate_sameTime(F, T, l);
            }
            else {
                F = getEstRate_sameTime(F, T, 0);
            }
//            System.out.println("same time A:" + A);
//            System.out.println("same time rate:" + packet.getRateEst());
            if (A >= C) {
                if (congested == false) {
                    if (exdBufThre) {
                        // Heuristic: we only assume the link is congested after
                        // buffer capacity exceeds some threshold
                        congested = true;
                        startTime = Simulator.getCurrentTime();
                    }
                } else {
                    if (Simulator.getCurrentTime() > startTime + Kc) {
                        alpha *= (C / F);
                        startTime = Simulator.getCurrentTime();
                    }
                }
            } else {
                if (congested == true) {
                    congested = false;
                    startTime = Simulator.getCurrentTime();
                    tmpAlpha = 0;
                } else {
                    if (Simulator.getCurrentTime() < startTime + Kc) {
                        tmpAlpha = Math.max(tmpAlpha, packet.getRateEst());
                    } else {
                        alpha = tmpAlpha;
                        startTime = Simulator.getCurrentTime();
//                        tmpAlpha = tmpAlpha * 0.99;
                        tmpAlpha = C / 10;
                    }
                }
            }
        }

        return alpha;
    }

    private double getEstRate(double oldRate, long T, long l) {
        double newRate = (1 - Math.exp(-(T+5) / Ka)) * (l * 1.0 / (T + 5)) + Math.exp(-(T+5) / Ka) * oldRate;
//        System.out.println("oldRate:" + oldRate + "; newRate:" + newRate);
//        System.out.println("l:" + l + "; T:" + T);
        return newRate;
    }

    private double getEstRate_sameTime(double oldRate, long T, long l) {
        double newRate = (1 - Math.exp(-T / Ka)) * (l * 1.0 / T) + oldRate;
        return newRate;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }
}