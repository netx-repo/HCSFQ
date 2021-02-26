package ch.ethz.systems.netbench.xpt.sppifo.ports.HCSFQ;
import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;
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
    private long T; // Arrival interval
    private long l; // Packet length
    private boolean congested;
    private long lastFTime;
    private long[] lastATime;
    private long deviceId;
    private long targetDeviceId;
    private int TCount = 1;

    // * for hierarchy * //
    private double[] alpha_perTenant;
    private double[] tmpAlpha_perTenant;
    private double[] A_perTenant;
    private double[] F_perTenant;
    private long[] startTime_perTenant;
    private long[] lastArrTime_perTenant;
    private long[] T_perTenant;
    private long[] l_perTenant;
    private boolean[] congested_perTenant;
    private double[] C_perTenant;
    private long[] lastFTime_perTenant;

    private double tenant_initial = 0.1;
    private double flow_initial = 0.1;

    private int numTenants = 100;
    private double Kf;

    private double count1;
    private double count2;
    public EstFairShareRate(double Ka, double Kc, double C, double Kf, long deviceId, long targetDeviceId) {
        C = C * 985 / 1000.0;
        this.deviceId = deviceId;
        this.targetDeviceId = targetDeviceId;
        this.Ka = Ka;
        this.Kc = Kc;
        this.C = C;
        this.Kf = Kf;
        this.alpha = C;
        this.tmpAlpha = 0;
        this.A = 0;
        this.F = 0;
        this.lastArrTime =  - 1;
        this.lastFTime =  - 1;
        this.lastATime =  new long[numTenants];
        for (int i=0;i<numTenants;i++) {
            this.lastATime[i] = -1;
        }
        this.startTime = Simulator.getCurrentTime();
        this.T = 0;
        this.l = 0;
        this.congested = false;

        

        this.alpha_perTenant = new double[numTenants];
        this.tmpAlpha_perTenant = new double[numTenants];
        this.A_perTenant = new double[numTenants];
        this.F_perTenant = new double[numTenants];
        this.lastArrTime_perTenant = new long[numTenants];
        this.startTime_perTenant = new long[numTenants];
        this.T_perTenant = new long[numTenants];
        this.l_perTenant = new long[numTenants];
        this.congested_perTenant = new boolean[numTenants];
        this.C_perTenant = new double[numTenants];
        this.lastFTime_perTenant = new long[numTenants];
//        System.out.println("C:" + C);
        int i;
        for (i=0;i<numTenants;i++) {
            this.alpha_perTenant[i] = C / 32;
            this.tmpAlpha_perTenant[i] = 0;
            this.A_perTenant[i] = 0;
            this.F_perTenant[i] = 0;
            this.lastArrTime_perTenant[i] =  - 1;
            this.startTime_perTenant[i] = Simulator.getCurrentTime();
            this.T_perTenant[i] = 0;
            this.l_perTenant[i] = 0;
            this.congested_perTenant[i] = false;
            this.C_perTenant[i] = C;
            this.lastFTime_perTenant[i] =  - 1;
        }

        this.tenant_initial = Simulator.getConfiguration().getDoublePropertyOrFail("tenant_initial");
        this.flow_initial = Simulator.getConfiguration().getDoublePropertyOrFail("flow_initial");
    }

    public double getEstAlpha(FullExtTcpPacket packet, boolean dropped, long newArrTime,
                              boolean exdBufThre, int tenantId) {
        // Only update estimate if coming from a different batch
        if (1==1) {
            T = Simulator.getCurrentTime() - lastArrTime;
            if (T != 0)
                lastArrTime = newArrTime;
            l = packet.getSizeBit();

            // System.out.println(T);

            A = getEstRate_A(A, T, l);

            T_perTenant[tenantId] = Simulator.getCurrentTime() - lastArrTime_perTenant[tenantId];
            if (T_perTenant[tenantId] != 0)
                lastArrTime_perTenant[tenantId] = newArrTime;
            l_perTenant[tenantId] = packet.getSizeBit();

            A_perTenant[tenantId] = getEstRate(A_perTenant[tenantId], T_perTenant[tenantId], l_perTenant[tenantId]);
            lastATime[tenantId] = Simulator.getCurrentTime();
            
            
            

            if (dropped == false) {
                long FT = Simulator.getCurrentTime() - lastFTime;
                F = getEstRate(F, FT, l);
                lastFTime = Simulator.getCurrentTime();
            }
            else {
                if (Simulator.getCurrentTime() - lastFTime > Kf) {
                    long FT = Simulator.getCurrentTime() - lastFTime;
                    F = getEstRate(F, FT, 0);
                    lastFTime = Simulator.getCurrentTime();
                }
            }

            if (dropped == false) {
                long FT = Simulator.getCurrentTime() - lastFTime_perTenant[tenantId];
                F_perTenant[tenantId] = getEstRate(F_perTenant[tenantId], FT, l_perTenant[tenantId]);
                lastFTime_perTenant[tenantId] = Simulator.getCurrentTime();
            }
            else {
                if (Simulator.getCurrentTime() - lastFTime_perTenant[tenantId] > Kf) {
                    long FT = Simulator.getCurrentTime() - lastFTime_perTenant[tenantId];
                    F_perTenant[tenantId] = getEstRate(F_perTenant[tenantId], FT, 0);
                    lastFTime_perTenant[tenantId] = Simulator.getCurrentTime();
                }
            }
            
            // if (Simulator.getCurrentTime() - lastFTime_perTenant[1-tenantId] > Kf) {
            //     long FT = Simulator.getCurrentTime() - lastFTime_perTenant[1-tenantId];
            //     F_perTenant[1-tenantId] = getEstRate(F_perTenant[1-tenantId], FT, 0);
            //     lastFTime_perTenant[1-tenantId] = Simulator.getCurrentTime();
            // }

            // if (Simulator.getCurrentTime() - lastATime[1-tenantId] > Kf) {
            //     long AT = Simulator.getCurrentTime() - lastATime[1-tenantId];
            //     A_perTenant[1-tenantId] = getEstRate(A_perTenant[1-tenantId], AT, 0);
            //     lastATime[1-tenantId] = Simulator.getCurrentTime();
            //     C_perTenant[1-tenantId] = alpha;
            //     if (A_perTenant[1-tenantId] > C_perTenant[1-tenantId]) {
            //         if ((alpha_perTenant[1-tenantId] == 0) || (alpha_perTenant[1-tenantId] > F_perTenant[1-tenantId])) {
            //             alpha_perTenant[1-tenantId] = C_perTenant[1-tenantId];
            //         }
            //         else {
            //             alpha_perTenant[1-tenantId] *= (C_perTenant[1-tenantId] / F_perTenant[1-tenantId]);
            //         }
            //     }
            //     else {
            //         tmpAlpha_perTenant[1-tenantId] = flow_initial * C_perTenant[1-tenantId];
            //     }
            // }


//            else {
//                F = getEstRate(F, T, 0);
//            }
//            System.out.println("A:" + A);
//            System.out.println("rate:" + packet.getRateEst());

            // if (A > C) {
            //     System.out.println("exceed C! . A:" + A);
            //     count1 += 1;
            //     System.out.println("ratio:" + count1 / count2);
            // }
            // else {
            //     count2 += 1;
            // }
            
            if (A >= C) {
//                if ((congested == false) && (exdBufThre)) {
//                    if (exdBufThre) {
                if ((congested == false)) {
                        // Heuristic: we only assume the link is congested after
                        // buffer capacity exceeds some threshold
                        congested = true;
                        startTime = Simulator.getCurrentTime();
//                    }
                } else {
                    if (Simulator.getCurrentTime() > startTime + Kc) {
                        double old_alpha = alpha;
                        if ((alpha == 0) || (alpha > F)) {
//                            System.out.println("Update 1");
                            alpha = C;
                        }
                        else {
//                            System.out.println("Update 2");
                            alpha *= (C / F);
                        }
                        // if (alpha < 0.5) {
                        //     System.out.println("alpha=" + alpha);
                        //     System.out.println("---old_alpha=" + old_alpha);
                        //     System.out.println("---F=" + F + "; A=" + A);
                        //     System.out.println("---A0:" + A_perTenant[0] + "; C0:" + C_perTenant[0]);
                        //     System.out.println("---F0:" + F_perTenant[0] + "; alpha0:" + alpha_perTenant[0]);
                        //     System.out.println("---A1:" + A_perTenant[1] + "; C1:" + C_perTenant[1]);
                        //     System.out.println("---F1:" + F_perTenant[1] + "; alpha1:" + alpha_perTenant[1]);
                        // }
                        startTime = Simulator.getCurrentTime();
                    }
//                    System.out.println("cur_time:" + Simulator.getCurrentTime() + "; str_time:" + startTime + "; congested:" + congested);
                }
            } else {
                if (congested == true) {
                    congested = false;
                    startTime = Simulator.getCurrentTime();
                    tmpAlpha = tenant_initial * C;
                } else {
                    if (Simulator.getCurrentTime() < startTime + Kc) {
                        // tmpAlpha = Math.max(tmpAlpha, packet.getRateEst());
                        tmpAlpha = Math.max(tmpAlpha, A_perTenant[packet.getTenantId()]);
                    } else {
                        alpha = tmpAlpha;
                        startTime = Simulator.getCurrentTime();
//                        tmpAlpha = tmpAlpha * 0.99;
//                        tmpAlpha = 0;
                        // tmpAlpha = 0.2 * C;
                        // if (packet.getSizeBit() >= 5000)
                        // ** before auto
                        // tmpAlpha = Math.max(0.3 * C, A_perTenant[packet.getTenantId()]);
                        // ** auto
                        // tmpAlpha = Math.max(tenant_initial * C, A_perTenant[packet.getTenantId()]);
                        // ** for udp
                        tmpAlpha = tenant_initial * C;

                    }
                }
            }
        //    System.out.println("C:" + C + "; alpha:" + alpha);
        
            // Update last arrival time
            
        }

        return alpha;
    }

    public double getEstAlpha_perTenant(FullExtTcpPacket packet, boolean dropped, long newArrTime,
                              boolean exdBufThre, int tenantId) {
        // Only update estimate if coming from a different batch
        if (1==1) {
            C_perTenant[tenantId] = alpha;
            // System.out.println("tid:"+tenantId + "; alpha:" + alpha);
            // if (A_perTenant[tenantId] >= 0.999 * A)  { 
            //     C_perTenant[tenantId] = C;
            // }
            // else {
            //     C_perTenant[tenantId] = alpha;
            // }

        //     if (C_perTenant[tenantId] != C) {
        //         // System.out.println("!!!!!");
        //     if ((tenantId == 0)&& (deviceId == 144) && (targetDeviceId == 0)) {
        //         System.out.println("----------A0:" + A_perTenant[0] + "; F0:" + F_perTenant[0]);
        //     }
        //     if ((A_perTenant[0] > 0) && (deviceId == 144) && (targetDeviceId == 0)) {
                // System.out.println("ID:" + deviceId);
                // System.out.println("time:" + Simulator.getCurrentTime());
                // System.out.println("flowId:" + packet.getFlowId());
                // System.out.println("C:" + C + "; alpha:" + alpha);
                // System.out.println("A:" + A + "; F:" + F);
                // System.out.println("packet.getRateEst():" + packet.getRateEst());
                // System.out.println("C0:" + C_perTenant[80] + "; alpha0:" + alpha_perTenant[80]);
                // System.out.println("A0:" + A_perTenant[80] + "; F0:" + F_perTenant[80]);
                // System.out.println("C1:" + C_perTenant[1] + "; alpha1:" + alpha_perTenant[1]);
                // System.out.println("A1:" + A_perTenant[1] + "; F1:" + F_perTenant[1]);
                // System.out.println("------------------------------------------------------");
        //     }
        // }

            // if (A_perTenant[0] > C_perTenant[0]) {
            //     System.out.println("A:" + A_perTenant[0] + "; C:" + C_perTenant[0]);
            //     System.out.println("A_total:" + A);
            //     System.out.println("F_total:" + F);
            //     if (C_perTenant[0] < 0.5) {
            //         System.out.println("--alpha:" + alpha);
            //         System.out.println("------A:" + A_perTenant[1] + "; C:" + C_perTenant[1]);
            //     }
            // }
            // if (A_perTenant[1] > C_perTenant[1]) {
            //     System.out.println("------A:" + A_perTenant[1] + "; C:" + C_perTenant[1]);
            //     System.out.println("------A_total:" + A);
            // }
           
            
//            else {
//                F_perTenant[tenantId] = getEstRate(F_perTenant[tenantId], T_perTenant[tenantId], 0);
//            }

            int flag = 0;
            if ((A_perTenant[tenantId] >= C_perTenant[tenantId])) {
                if ((congested_perTenant[tenantId] == false)) {
//                if ((congested_perTenant[tenantId] == false)  && (exdBufThre)) {
                    // TODO: decide whether to use heuristic or not...
//                    if (exdBufThre) {
                        // Heuristic: we only assume the link is congested after
                        // buffer capacity exceeds some threshold
                        congested_perTenant[tenantId] = true;
                        startTime_perTenant[tenantId] = Simulator.getCurrentTime();
//                    }
                } else {
                    if (Simulator.getCurrentTime() > startTime_perTenant[tenantId] + Kc) {
                        if ((alpha_perTenant[tenantId] == 0) || (alpha_perTenant[tenantId] > F_perTenant[tenantId])) {
                            alpha_perTenant[tenantId] = C_perTenant[tenantId];
                        }
                        else {
                            alpha_perTenant[tenantId] *= (C_perTenant[tenantId] / F_perTenant[tenantId]);
                        }
                        startTime_perTenant[tenantId] = Simulator.getCurrentTime();
                    }
                }
            } else {
                if (congested_perTenant[tenantId] == true) {
                    congested_perTenant[tenantId] = false;
                    
                    // if (tenantId == 1) 
                    //     tmpAlpha_perTenant[tenantId] = 0.15 * C_perTenant[tenantId];
                    // else
                        tmpAlpha_perTenant[tenantId] = flow_initial * C_perTenant[tenantId];
                    flag = 0;
                    startTime_perTenant[tenantId] = Simulator.getCurrentTime();
                } else {
                    if (Simulator.getCurrentTime() < startTime_perTenant[tenantId] + Kc) {
                        if (packet.getRateEst() > tmpAlpha_perTenant[tenantId]) 
                            flag = 1;
                        tmpAlpha_perTenant[tenantId] = Math.max(tmpAlpha_perTenant[tenantId], packet.getRateEst());
                        
                    } else  if (packet.getSizeBit() >= 1000){
                        alpha_perTenant[tenantId] = tmpAlpha_perTenant[tenantId];
                        
                    //    tmpAlpha = tmpAlpha * 0.999;
                        // tmpAlpha_perTenant[tenantId] = Math.max(0.3 * C_perTenant[tenantId], packet.getRateEst());
                        
                        // ** before auto
                        // tmpAlpha_perTenant[tenantId] = Math.max(0.3 * C_perTenant[tenantId], packet.getRateEst());
                        // ** auto
                        // tmpAlpha_perTenant[tenantId] = Math.max(flow_initial * C_perTenant[tenantId], packet.getRateEst());
                        // ** for udp
                        // if (tenantId == 1) 
                        //     tmpAlpha_perTenant[tenantId] = 0.15 * C_perTenant[tenantId];
                        // else
                            tmpAlpha_perTenant[tenantId] = flow_initial * C_perTenant[tenantId];
                        flag = 0;
                        startTime_perTenant[tenantId] = Simulator.getCurrentTime();

                    }
                }
            }
//            if (tenantId == 0) {
//                System.out.println("C_pertenant:" + C_perTenant[tenantId] + "alpha_pt:" + alpha_perTenant[tenantId]);
//                System.out.println("A_pertenant:" + A_perTenant[tenantId] + "F_perTenant:" + F_perTenant[tenantId]);
//            }
            // Update last arrival time
            
        }
        // alpha_perTenant[tenantId] = C / 32;
        return alpha_perTenant[tenantId];
    }

    private double getEstRate(double oldRate, long T, long l) {
        double _inc = 20;
        double newRate = (1 - Math.exp(-(T + _inc) / Ka)) * (l * 1.0 / (T + _inc)) + Math.exp(-(T + _inc) / Ka) * oldRate;
        return newRate;
    }

    private double getEstRate_A(double oldRate, long T, long l) {
        double _inc = 20;
        double newRate = (1 - Math.exp(-(T + _inc) / (Ka))) * (l * 1.0 / (T + _inc)) + Math.exp(-(T + _inc) / (Ka)) * oldRate;
        return newRate;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public void setAlpha_perTenant(double alpha, int tenantId) {
        this.alpha_perTenant[tenantId] = alpha;
    }

    public double getAlpha() {
        return this.alpha;
    }

    public double getAlpha_perTenant(int tenantId) {
        return this.alpha_perTenant[tenantId];
    }
}
