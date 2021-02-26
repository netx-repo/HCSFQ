package ch.ethz.systems.netbench.xpt.sppifo.ports.TOY;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.IpHeader;
import ch.ethz.systems.netbench.xpt.tcpbase.PriorityHeader;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.util.LinkedList; 
import java.util.Queue; 
import java.util.concurrent.PriorityBlockingQueue;

import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;

public class TOYOutputPort extends OutputPort {
    private final long maxQueueSize;
    private final int windowSize;
    private Integer[] currentWindow;
    private Integer[] lastWindow;
    private long tagTime;
    private long nsPerWindow;
    private int itemsPerWindow;
    private int currentWindowLen;
    private int lastWindowLen;
    private int acceptThreshold;
    private int acceptableRank;
    private Lock reentrantLock;

    private float k;
    private int maxRank;
    
    private Queue qWindow;

    private int count;
    private double avgqlen;
    private double avgcount;

    TOYOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, long maxQueueSize, int windowSize, int itemsPerWindow,
                  int acceptThreshold) {
        super(ownNetworkDevice, targetNetworkDevice, link, new LinkedBlockingQueue<Packet>());
        this.maxQueueSize = maxQueueSize;
        this.windowSize = windowSize;
        this.currentWindow = new Integer[this.windowSize];
        this.lastWindow = new Integer[this.windowSize];
        int i;
        for (i=0; i<this.windowSize; i++) {
            this.currentWindow[i] = 0;
            this.lastWindow[i] = 0;
        }
        this.tagTime = Simulator.getCurrentTime();
        this.nsPerWindow = 1000000;
        this.itemsPerWindow = itemsPerWindow;
        this.currentWindowLen = 0;
        this.lastWindowLen = 0;
        this.reentrantLock = new ReentrantLock();
        this.acceptThreshold = acceptThreshold;
        this.acceptableRank = 0;

        this.k = 0.1f;
        maxRank = 0;

        // this.qWindow = new PriorityBlockingQueue();
        this.qWindow = new LinkedList(); 

        this.count = 0;
        this.avgqlen = 0;
        this.avgcount = 0;
    }

    @Override
    public void enqueue(Packet packet) {
        // Convert to IP packet
        long curTime;
        PriorityHeader header = (PriorityHeader) packet;

        this.reentrantLock.lock();
        try {
            Integer rank = (int) header.getSourcePort();
            // Integer rank = (int) header.getPriority();

            // Maintain the current window and the last window
            curTime = Simulator.getCurrentTime();



            float C = 8*480000.0f;
            Integer acceptableRank;
            float qlen = 20.0f;
            float qlen2 = 0f;
            // int qWindowLen = 500;
            int qWindowLen = 20;

            

            this.count = 1;
            this.avgcount = this.avgcount + 1;
            this.avgqlen = this.avgqlen + buffQueue.size();
            if (this.count == 2) {
                this.count = 1;

                // System.out.println(this.avgqlen / this.avgcount);
            }
            // this.count = 1;
            // if ((getBufferOccupiedBits() <= k*C) || (computeQuantile(rank) <= 1.0/(1-k) * (C - getBufferOccupiedBits()) / C)) {
            // if ((getBufferOccupiedBits() <= k*C) || (compareQuantile(rank, 1.0/(1-k) * (C - getBufferOccupiedBits()) / C))) {
            
            if ((buffQueue.size() <= k*qlen) || (compareQuantile(rank, 1.0/(1-k) * (qlen - buffQueue.size()) / qlen))) {
            // if ((buffQueue.size() <= k*qlen) || (compareQuantile(rank, 1.0/(1-k) * (qWindowLen - qWindow.size()) / qWindowLen))) {
                // System.out.println("buffsize:" + getBufferOccupiedBits() + "; bufflen:" + buffQueue.size());
                IpHeader ipHeader = (IpHeader) packet;
                // if (getBufferOccupiedBits() + ipHeader.getSizeBit() <= 8L*1460 * qlen) {
                
                if (buffQueue.size() + 1 <= qlen) {
                    // increaseA(rank);
                    this.enqueueMode = 1;
                    guaranteedEnqueue(packet);
                    if (this.count == 1) {
                        if (qWindow.size() < qWindowLen) {
                            qWindow.add(packet);
                        }
                        else {
                            qWindow.poll();
                            qWindow.add(packet);  
                        }
                    }
                }
            }
            else {
                SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED");
                IpHeader ipHeader = (IpHeader) packet;
                if (ipHeader.getSourceId() == this.getOwnId()) {
                    SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED_AT_SOURCE");
                }

                if (this.count == 1) {
                    if (qWindow.size() < qWindowLen) {
                        qWindow.add(packet);
                    }
                    else {
                        qWindow.poll();
                        qWindow.add(packet);  
                    }
                }

                if (buffQueue2.size() + 1 <= qlen2) {
                    // increaseA(rank);
                    this.enqueueMode = 2;
                    guaranteedEnqueue(packet);
                    
                }
            }

            // if (this.currentWindowLen > 0) {
            //     int acceptableIndex = (int) (this.currentWindowLen *1.0/(1-k) * (C - getBufferOccupiedBits()) / C);
            //     if (acceptableIndex >= this.currentWindowLen) {
            //         acceptableIndex = this.currentWindowLen - 1;
            //     }
            //     // System.out.println("acc_rank:" + acceptableIndex + "; win_len:" + this.currentWindowLen + "; buff:" + getBufferOccupiedBits());
            //     acceptableRank = this.lastWindow[acceptableIndex];
            // }
            // else 
            //     acceptableRank = 65535;
            // if ((rank <= acceptableRank) || (this.currentWindowLen <= this.acceptThreshold)) {
            //     // Enqueue
            //     //            Object[] contentTOYQ = super.getQueue().toArray();
            //     IpHeader ipHeader = (IpHeader) packet;
            //     if (getBufferOccupiedBits() + ipHeader.getSizeBit() <= 8L*480000) {
            //         increaseA(rank);
            //         guaranteedEnqueue(packet);
            //     }
            // } else {
            //     // Drop
            //     SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED");
            //     IpHeader ipHeader = (IpHeader) packet;
            //     if (ipHeader.getSourceId() == this.getOwnId()) {
            //         SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED_AT_SOURCE");
            //     }
            // }
        } finally {
            this.reentrantLock.unlock();
        }
    }

    public double computeQuantile(int priority) {
        Object[] contentPIFO = buffQueue.toArray();
        Arrays.sort(contentPIFO);
        // packet = (FullExtTcpPacket) contentPIFO[buffQueue.size()-1];
        return 0.0;
    }

    public boolean compareQuantile(int priority, double quantile) {
        Object[] contentPIFO = qWindow.toArray();
        Arrays.sort(contentPIFO);

        // for (int i=0; i<qWindow.size(); i++)
        //     System.out.print(((FullExtTcpPacket)contentPIFO[i]).getPriority() + ",");
        // System.out.println(" u["+ quantile + "] ");

        // packet = (FullExtTcpPacket) contentPIFO[this.size()-1];
        // System.out.println("queue size:" + buffQueue.size() + "; quantile:" + quantile);
        FullExtTcpPacket packet = (FullExtTcpPacket) contentPIFO[(int) (qWindow.size() * quantile)];
        // System.out.println("queue size:" + buffQueue.size() + "; quantile:" + quantile);
        // System.out.println("---- priority:" + priority + "; quantilePri:" + packet.getPriority());
        if (priority < packet.getPriority())
            return true;
        return false;
    }

    public boolean compareQuantile_v2(int priority, double quantile) {
        Object[] contentPIFO = qWindow.toArray();
        Arrays.sort(contentPIFO);

        // for (int i=0; i<qWindow.size(); i++)
        //     System.out.print(((FullExtTcpPacket)contentPIFO[i]).getPriority() + ",");
        // System.out.println(" u["+ quantile + "] ");

        // packet = (FullExtTcpPacket) contentPIFO[this.size()-1];
        // System.out.println("queue size:" + buffQueue.size() + "; quantile:" + quantile);
        FullExtTcpPacket packet = (FullExtTcpPacket) contentPIFO[(int) (qWindow.size() * quantile)];
        // System.out.println("queue size:" + buffQueue.size() + "; quantile:" + quantile);
        // System.out.println("---- priority:" + priority + "; quantilePri:" + packet.getPriority());
        if (priority < packet.getPriority())
            return true;
        return false;
    }
}
