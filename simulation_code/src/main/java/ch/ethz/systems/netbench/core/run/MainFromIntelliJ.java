package ch.ethz.systems.netbench.core.run;

public class MainFromIntelliJ {


    public static void main(String args[]) {
        /* Figure 9 and 10: Fairness FCT statistics */
        int flag = 1;
        // int flag = 2;
        if (flag == 2) {
            // simulation of rack-scale
            String K_args = args[0];
            String Ka_args = args[1];
            String Kc_args = args[2];
            String tenant_initial_args = args[3];
            String flow_initial_args = args[4];
            String queue_length_args = args[5];
            MainFromProperties.main(new String[]{"projects/sppifo/runs/rackscale/mix/HCSFQ.properties", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args});
            MainFromProperties.main(new String[]{"projects/sppifo/runs/rackscale/mix/TCP.properties", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args});
        }
        else if (flag == 1)
        {
            // simulation of web search workload
            String output_port_max_size_packets = "120";
            String K_args = args[0];
            String Ka_args = args[1];
            String Kc_args = args[2];
            String tenant_initial_args = args[3];
            String flow_initial_args = args[4];
            String queue_length_args = args[5];
            String Kf_args = args[6];

                // MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/3600/CSFQ.properties"
                //         , "window_size=1000", "items_per_window=300", "accept_threshold=200", "output_port_max_size_packets=" + output_port_max_size_packets, "Kc=2000", "traffic_lambda_flow_starts_per_s=3600", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args });
                // MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/5200/CSFQ.properties"
                //         , "window_size=1000", "items_per_window=300", "accept_threshold=200", "output_port_max_size_packets=" + output_port_max_size_packets, "Kc=2000", "traffic_lambda_flow_starts_per_s=5200", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args });
                // MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/7000/CSFQ.properties"
                //         , "window_size=1000", "items_per_window=300", "accept_threshold=200", "output_port_max_size_packets=" + output_port_max_size_packets, "Kc=2000", "traffic_lambda_flow_starts_per_s=7000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args });
                // MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/8900/CSFQ.properties"
                //         , "window_size=1000", "items_per_window=300", "accept_threshold=200", "output_port_max_size_packets=" + output_port_max_size_packets, "Kc=2000", "traffic_lambda_flow_starts_per_s=8900", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args });
                // MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/11100/CSFQ.properties"
                //         , "window_size=1000", "items_per_window=300", "accept_threshold=200", "output_port_max_size_packets=" + output_port_max_size_packets, "Kc=2000", "traffic_lambda_flow_starts_per_s=11100", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args });
                // MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/14150/CSFQ.properties"
                //         , "window_size=1000", "items_per_window=300", "accept_threshold=200", "output_port_max_size_packets=" + output_port_max_size_packets, "Kc=2000", "traffic_lambda_flow_starts_per_s=14150", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args });
                // MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/19000/CSFQ.properties"
                //          , "window_size=1000", "items_per_window=300", "accept_threshold=200", "output_port_max_size_packets=" + output_port_max_size_packets, "Kc=2000", "traffic_lambda_flow_starts_per_s=19000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args });
                // MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/25000/CSFQ.properties"
                //          , "window_size=1000", "items_per_window=300", "accept_threshold=200", "output_port_max_size_packets=" + output_port_max_size_packets, "Kc=2000", "traffic_lambda_flow_starts_per_s=25000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args });
                // MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/32000/CSFQ.properties"
                //          , "window_size=1000", "items_per_window=300", "accept_threshold=200", "output_port_max_size_packets=" + output_port_max_size_packets, "Kc=2000", "traffic_lambda_flow_starts_per_s=32000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args });

            // MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/3600/PIFOWFQ_32.properties", "traffic_lambda_flow_starts_per_s=3600", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp"});
            // MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/5200/PIFOWFQ_32.properties", "traffic_lambda_flow_starts_per_s=5200", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp"});
            // MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/7000/PIFOWFQ_32.properties", "traffic_lambda_flow_starts_per_s=7000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp"});
            // MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/8900/PIFOWFQ_32.properties", "traffic_lambda_flow_starts_per_s=8900", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp"});
            // MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/11100/PIFOWFQ_32.properties", "traffic_lambda_flow_starts_per_s=11100", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp"});
            // MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/14150/PIFOWFQ_32.properties", "traffic_lambda_flow_starts_per_s=14150", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp"});
            // MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/19000/PIFOWFQ_32.properties", "traffic_lambda_flow_starts_per_s=19000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp"});

            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/3600/SPPIFOWFQ_32.properties", "traffic_lambda_flow_starts_per_s=3600", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/5200/SPPIFOWFQ_32.properties", "traffic_lambda_flow_starts_per_s=5200", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/7000/SPPIFOWFQ_32.properties", "traffic_lambda_flow_starts_per_s=7000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/8900/SPPIFOWFQ_32.properties", "traffic_lambda_flow_starts_per_s=8900", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/11100/SPPIFOWFQ_32.properties", "traffic_lambda_flow_starts_per_s=11100", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/14150/SPPIFOWFQ_32.properties", "traffic_lambda_flow_starts_per_s=14150", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/19000/SPPIFOWFQ_32.properties", "traffic_lambda_flow_starts_per_s=19000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", "scenario_topology_file=example/topologies/leaf_spine/pFabric_9leaf_4spine_16servers.topology" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/25000/SPPIFOWFQ_32.properties", "traffic_lambda_flow_starts_per_s=25000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", "scenario_topology_file=example/topologies/leaf_spine/pFabric_9leaf_4spine_16servers.topology" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/32000/SPPIFOWFQ_32.properties", "traffic_lambda_flow_starts_per_s=32000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", "scenario_topology_file=example/topologies/leaf_spine/pFabric_9leaf_4spine_16servers.topology" });

            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/3600/TCP.properties", "traffic_lambda_flow_starts_per_s=3600", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", "output_port_max_queue_size_bytes=120000" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/5200/TCP.properties", "traffic_lambda_flow_starts_per_s=5200", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", "output_port_max_queue_size_bytes=120000" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/7000/TCP.properties", "traffic_lambda_flow_starts_per_s=7000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", "output_port_max_queue_size_bytes=120000" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/8900/TCP.properties", "traffic_lambda_flow_starts_per_s=8900", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", "output_port_max_queue_size_bytes=120000" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/11100/TCP.properties", "traffic_lambda_flow_starts_per_s=11100", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", "output_port_max_queue_size_bytes=120000" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/14150/TCP.properties", "traffic_lambda_flow_starts_per_s=14150", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", "output_port_max_queue_size_bytes=120000" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/19000/TCP.properties", "traffic_lambda_flow_starts_per_s=19000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", "output_port_max_queue_size_bytes=120000" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/25000/TCP.properties", "traffic_lambda_flow_starts_per_s=25000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", "output_port_max_queue_size_bytes=120000" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/32000/TCP.properties", "traffic_lambda_flow_starts_per_s=32000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", "output_port_max_queue_size_bytes=120000" });

            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/3600/DCTCP.properties", "traffic_lambda_flow_starts_per_s=3600", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/5200/DCTCP.properties", "traffic_lambda_flow_starts_per_s=5200", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/7000/DCTCP.properties", "traffic_lambda_flow_starts_per_s=7000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/8900/DCTCP.properties", "traffic_lambda_flow_starts_per_s=8900", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/11100/DCTCP.properties", "traffic_lambda_flow_starts_per_s=11100", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/14150/DCTCP.properties", "traffic_lambda_flow_starts_per_s=14150", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/19000/DCTCP.properties", "traffic_lambda_flow_starts_per_s=19000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/25000/DCTCP.properties", "traffic_lambda_flow_starts_per_s=25000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/32000/DCTCP.properties", "traffic_lambda_flow_starts_per_s=32000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });

            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/3600/AFQ_32.properties", "traffic_lambda_flow_starts_per_s=3600", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/5200/AFQ_32.properties", "traffic_lambda_flow_starts_per_s=5200", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/7000/AFQ_32.properties", "traffic_lambda_flow_starts_per_s=7000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/8900/AFQ_32.properties", "traffic_lambda_flow_starts_per_s=8900", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/11100/AFQ_32.properties", "traffic_lambda_flow_starts_per_s=11100", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/14150/AFQ_32.properties", "traffic_lambda_flow_starts_per_s=14150", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/19000/AFQ_32.properties", "traffic_lambda_flow_starts_per_s=19000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/25000/AFQ_32.properties", "traffic_lambda_flow_starts_per_s=25000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/32000/AFQ_32.properties", "traffic_lambda_flow_starts_per_s=32000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp" });

            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/3600/HCSFQ.properties"
                , "traffic_lambda_flow_starts_per_s=3600", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args, Kf_args });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/5200/HCSFQ.properties"
                , "traffic_lambda_flow_starts_per_s=5200", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args, Kf_args });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/7000/HCSFQ.properties"
                , "traffic_lambda_flow_starts_per_s=7000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args, Kf_args });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/8900/HCSFQ.properties"
                , "traffic_lambda_flow_starts_per_s=8900", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args, Kf_args });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/11100/HCSFQ.properties"
                , "traffic_lambda_flow_starts_per_s=11100", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args, Kf_args });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/14150/HCSFQ.properties"
                , "traffic_lambda_flow_starts_per_s=14150", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args, Kf_args });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/19000/HCSFQ.properties"
                , "traffic_lambda_flow_starts_per_s=19000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args, Kf_args });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/25000/HCSFQ.properties"
                , "traffic_lambda_flow_starts_per_s=25000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args, Kf_args });
            MainFromProperties.main(new String[]{"projects/sppifo/runs/sppifo_evaluation/fairness/web_search_workload/32000/HCSFQ.properties"
                , "traffic_lambda_flow_starts_per_s=32000", "link_bandwidth_bit_per_ns=20", "second_transport_layer=udp", K_args, Ka_args, Kc_args, tenant_initial_args, flow_initial_args, queue_length_args, Kf_args });
        }

        /* Analyze and plot */
        MainFromProperties.runCommand("python projects/sppifo/plots/sppifo_evaluation/fairness/analyze.py", true);
        MainFromProperties.runCommand("gnuplot projects/sppifo/plots/sppifo_evaluation/fairness/plot.gnuplot", true);
    }

}

