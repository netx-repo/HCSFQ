@pragma stage 0
table get_time_stamp_table {
    actions {
        get_time_stamp_action;
    }
    default_action: get_time_stamp_action;
}
@pragma stage 0
table get_random_value_table {
    actions {
        get_random_value_action;
    }
    default_action: get_random_value_action;
}
@pragma stage 0
table add_info_hdr_table {
    reads {
        ipv4.srcAddr : exact;
        tcp.dstPort: exact;
    }
    actions {
        add_info_hdr_action;
        add_info_hdr_default_action;
    }
    default_action: add_info_hdr_default_action;
}
@pragma stage 0
table add_info_hdr_udp_table {
    reads {
        ipv4.srcAddr : exact;
        udp.dstPort: exact;
    }
    actions {
        add_info_hdr_udp_action;
        add_info_hdr_udp_default_action;
    }
    default_action: add_info_hdr_udp_default_action;
}
@pragma stage 0
table get_total_alpha_table {
    actions {
        get_total_alpha_action;
    }
    default_action: get_total_alpha_action;
}
@pragma stage 0
table get_half_pktlen_table {
    reads {
        ipv4.srcAddr : exact;
    }
    actions {
        get_half_pktlen_action;
        get_half_pktlen_w2_action;
        get_half_pktlen_w4_action;
    }
    default_action: get_half_pktlen_action;
}
@pragma stage 0
table get_delta_c_table {
    actions {
        get_delta_c_action;
    }
    default_action: get_delta_c_action;
}

@pragma stage 1
table estimate_per_flow_rate_table {
    actions {
        estimate_per_flow_rate_action;
    }
    default_action: estimate_per_flow_rate_action;
}
@pragma stage 2
table get_per_flow_rate_table {
    // reads {
    //     meta.label: exact;
    // }
    actions {
        // get_per_flow_rate_times_7_action;
        get_per_flow_rate_action;
    }
    // if meta.label == 0: get_per_flow_rate_action; else: get_per_flow_rate_times_7_action
    default_action: get_per_flow_rate_action;
}

@pragma stage 2
table get_per_flow_rate_times_7_table {
    actions {
        get_per_flow_rate_times_7_action;
    }
    // if meta.label == 0: get_per_flow_rate_action; else: get_per_flow_rate_times_7_action
    default_action: get_per_flow_rate_times_7_action;
}

@pragma stage 1
table get_fraction_factor_table {
    actions {
        get_fraction_factor_action;
    }
    default_action: get_fraction_factor_action;
}

@pragma stage 2
table estimate_aggregate_arrival_rate_table {
    actions {
        estimate_aggregate_arrival_rate_action;
    }
    default_action: estimate_aggregate_arrival_rate_action;
}
@pragma stage 2
table estimate_total_aggregate_arrival_rate_table {
    actions {
        estimate_total_aggregate_arrival_rate_action;
    }
    default_action: estimate_total_aggregate_arrival_rate_action;
}
@pragma stage 2
table sum_per_flow_rate_table {
    actions {sum_per_flow_rate_action;}
    default_action: sum_per_flow_rate_action;
}
@pragma stage 3
table get_aggregate_arrival_rate_table {
    // reads {
    //     meta.per_tenant_A: exact;
    // }
    actions {
        get_aggregate_arrival_rate_action;
        // get_aggregate_arrival_rate_times_7_action;
    }
    default_action: get_aggregate_arrival_rate_action;
}
@pragma stage 3
table get_aggregate_arrival_rate_times_7_table {
    actions {
        get_aggregate_arrival_rate_times_7_action;
    }
    default_action: get_aggregate_arrival_rate_times_7_action;
}


@pragma stage 3
table get_total_aggregate_arrival_rate_table {
    // reads {
    //     meta.total_A: exact;
    // }
    actions {
        get_total_aggregate_arrival_rate_action;
        // get_total_aggregate_arrival_rate_times_7_action;
    }
    default_action: get_total_aggregate_arrival_rate_action;
}
@pragma stage 3
table get_total_aggregate_arrival_rate_times_7_table {
    actions {
        get_total_aggregate_arrival_rate_times_7_action;
    }
    default_action: get_total_aggregate_arrival_rate_times_7_action;
}


@pragma stage 3
table div_per_flow_rate_table {
    actions {div_per_flow_rate_action;}
    default_action: div_per_flow_rate_action;
}
@pragma stage 3
table sum_aggregate_arrival_rate_table {
    actions {sum_aggregate_arrival_rate_action;}
    default_action: sum_aggregate_arrival_rate_action;
}
@pragma stage 3
table sum_total_aggregate_arrival_rate_table {
    actions {sum_total_aggregate_arrival_rate_action;}
    default_action: sum_total_aggregate_arrival_rate_action;
}

@pragma stage 10
table get_accepted_rate_table {
    // reads {
    //     meta.per_tenant_F: exact;
    // }
    actions {
        get_accepted_rate_action;
        // get_accepted_rate_times_7_action;
    }
    default_action: get_accepted_rate_action;
}
@pragma stage 10
table get_accepted_rate_times_7_table {
    actions {
        get_accepted_rate_times_7_action;
    }
    default_action: get_accepted_rate_times_7_action;
}

@pragma stage 10
table get_total_accepted_rate_table {
    // reads {
    //     meta.total_F: exact;
    // }
    actions {
        get_total_accepted_rate_action;
        // get_total_accepted_rate_times_7_action;
    }
    default_action: get_total_accepted_rate_action;
}
@pragma stage 10
table get_total_accepted_rate_times_7_table {
    actions {
        get_total_accepted_rate_times_7_action;
    }
    default_action: get_total_accepted_rate_times_7_action;
}

@pragma stage 4
table get_alpha_table {
    actions {
        get_alpha_action;
    }
    default_action: get_alpha_action;
}
@pragma stage 4
table flowrate_shl_table {
    actions {flowrate_shl_action;}
    default_action: flowrate_shl_action;
}
@pragma stage 4
table div_aggregate_arrival_rate_table {
    actions {div_aggregate_arrival_rate_action;}
    default_action: div_aggregate_arrival_rate_action;
}
@pragma stage 4
table div_total_aggregate_arrival_rate_table {
    actions {div_total_aggregate_arrival_rate_action; }
    default_action: div_total_aggregate_arrival_rate_action;
}

@pragma stage 5
table alpha_shl_4_table {
    actions {
        alpha_shl_4_action;
    }
    default_action: alpha_shl_4_action;
}
@pragma stage 5
table flowrate_sum_01_table {
    reads {
        meta.randv: ternary;
    }
    actions {
        flowrate_sum_01_01_action;
        flowrate_sum_01_0_action;
        flowrate_sum_01_1_action;
        flowrate_sum_01_none_action;
    }
    default_action: flowrate_sum_01_none_action;
}

@pragma stage 5
table flowrate_sum_23_table {
    reads {
        meta.randv: ternary;
    }
    actions {
        flowrate_sum_23_23_action;
        flowrate_sum_23_2_action;
        flowrate_sum_23_3_action;
        flowrate_sum_23_none_action;
    }
    default_action: flowrate_sum_23_none_action;
}
@pragma stage 5
table counter_table {
    actions {counter_action;}
    default_action: counter_action;
}
@pragma stage 5
table get_minv_0_table {
    actions {
        get_minv_0_action;
    }
    default_action: get_minv_0_action;
}
@pragma stage 5
table get_minv_0_2_table {
    actions {get_minv_0_2_action;}
    default_action: get_minv_0_2_action;
}

@pragma stage 6
table flowrate_times_randv_table {
    actions {
        flowrate_times_randv_action;
    }
    default_action: flowrate_times_randv_action;
}
@pragma stage 6
table alpha_times_15_table {
    actions {
        alpha_times_15_action;
    }
    default_action: alpha_times_15_action;
}
@pragma stage 6
table maintain_congest_state_table {
    actions {
        maintain_congest_state_action;
    }
    default_action: maintain_congest_state_action;
}
@pragma stage 6
table maintain_uncongest_state_table {
    actions {
        maintain_uncongest_state_action;
    }
    default_action: maintain_uncongest_state_action;
}

@pragma stage 7
table get_minv_table {
    actions {
        get_minv_action;
    }
    default_action: get_minv_action;
}
@pragma stage 7
table maintain_total_congest_state_table {
    actions {
        maintain_total_congest_state_action;
    }
    default_action: maintain_total_congest_state_action;
}
@pragma stage 7
table maintain_total_uncongest_state_table {
    actions {
        maintain_total_uncongest_state_action;
    }
    default_action: maintain_total_uncongest_state_action;
}

@pragma stage 8
table estimate_accepted_rate_table {
    actions {
        estimate_accepted_rate_action;
    }
    default_action: estimate_accepted_rate_action;
}
@pragma stage 8
table estimate_total_accepted_rate_table {
    actions {
        estimate_total_accepted_rate_action;
    }
    default_action: estimate_total_accepted_rate_action;
}
@pragma stage 8
table estimate_accepted_rate_2_table {
    actions {
        estimate_accepted_rate_2_action;
    }
    default_action: estimate_accepted_rate_2_action;
}
@pragma stage 8
table estimate_total_accepted_rate_2_table {
    actions {
        estimate_total_accepted_rate_2_action;
    }
    default_action: estimate_total_accepted_rate_2_action;
}
@pragma stage 8
table set_drop_table {
    actions {
        set_drop_action;
    }
    default_action: set_drop_action;
}
@pragma stage 8
table get_14_alpha_table {
    reads {
        meta.fraction_factor: exact;
    }
    actions {
        get_14_alpha_action;
        get_14_alpha_2_action;
        get_14_alpha_3_action;
        get_14_alpha_4_action;
        get_14_alpha_5_action;
        get_14_alpha_6_action;
        get_14_alpha_7_action;
        get_14_alpha_8_action;
        get_14_alpha_9_action;
        get_14_alpha_10_action;
        get_14_alpha_11_action;
        get_14_alpha_12_action;
        get_14_alpha_13_action;
        get_14_alpha_14_action;
        get_14_alpha_15_action;
        get_14_alpha_16_action;
        get_14_alpha_17_action;
        get_14_alpha_18_action;
        get_14_alpha_19_action;
        get_14_alpha_20_action;
    }
    default_action: get_14_alpha_action;
}
@pragma stage 8
table check_uncongest_state_table {
    reads {
        meta.uncongest_state_predicate: exact;
    }
    actions {
        check_uncongest_state_0_action;
        check_uncongest_state_23_action;
        check_uncongest_state_1_action;
    }
}

@pragma stage 10
table mod_resubmit_field_table {
    actions {
        mod_resubmit_field_action;
    }
    default_action: mod_resubmit_field_action;
}
@pragma stage 9
table sum_accepted_rate_table {
    actions {sum_accepted_rate_action; }
    default_action: sum_accepted_rate_action;
}
@pragma stage 9
table sum_total_accepted_rate_table {
    actions {sum_total_accepted_rate_action; }
    default_action: sum_total_accepted_rate_action;
}
@pragma stage 9
table check_total_uncongest_state_table {
    reads {
        meta.total_uncongest_state_predicate: exact;
    }
    actions {
        check_total_uncongest_state_0_action;
        check_total_uncongest_state_23_action;
        check_total_uncongest_state_1_action;
    }
}
// @pragma stage 9
// table set_to_resubmit_table {
//     actions {set_to_resubmit_action; }
//     default_action: set_to_resubmit_action;
// }

@pragma stage 11
table div_accepted_rate_table {
    actions {div_accepted_rate_action; }
    default_action: div_accepted_rate_action;
}
@pragma stage 11
table div_total_accepted_rate_table {
    actions {div_total_accepted_rate_action; }
    default_action: div_total_accepted_rate_action;
}
@pragma stage 10
table get_34_alpha_table {
    reads {
        meta.w2: exact;
    }
    actions {
        get_34_alpha_action;
        get_34_alpha_w2_action;
    }
    default_action: get_34_alpha_action;
}


@pragma stage 11
table resubmit_table {
    actions {
        resubmit_action;
    }
    default_action: resubmit_action;
}








//  ** recirc_pipe
@pragma stage 0
table update_total_alpha_table {
    reads { 
        recirculate_hdr.congested: exact;
    }
    actions { 
        update_total_alpha_to_maxalpha_action;
        update_total_alpha_by_F0_action;
        _no_op;
    }
    default_action: _no_op;
}

@pragma stage 2
table set_average_per_flow_rate_table {
    actions {set_average_per_flow_rate_action;}
    default_action: set_average_per_flow_rate_action;
}
@pragma stage 1
table get_delta_total_alpha_table {
    actions {get_delta_total_alpha_action;}
    default_action: get_delta_total_alpha_action;
}
@pragma stage 1
table get_min_of_pertenantF_total_alpha_table {
    actions {
        get_min_of_pertenantF_total_alpha_action;
    }
    default_action: get_min_of_pertenantF_total_alpha_action;
}

@pragma stage 2
table set_pertenantF_leq_totalalpha_table {
    actions {set_pertenantF_leq_totalalpha_action;}
    default_action: set_pertenantF_leq_totalalpha_action;
}
@pragma stage 3
table set_average_aggregate_arrival_rate_table {
    actions {set_average_aggregate_arrival_rate_action;}
    default_action: set_average_aggregate_arrival_rate_action;
}
@pragma stage 3
table set_average_total_aggregate_arrival_rate_table {
    actions {set_average_total_aggregate_arrival_rate_action;}
    default_action: set_average_total_aggregate_arrival_rate_action;
}

@pragma stage 3
table getmin_delta_total_alpha_table {
    actions {getmin_delta_total_alpha_action; }
    default_action: getmin_delta_total_alpha_action;
}

@pragma stage 4
table update_per_tenant_alpha_table {
    reads {
        recirculate_hdr.congested: exact;
        recirculate_hdr.pertenantF_leq_totalalpha: exact;
    }
    actions { 
        update_per_tenant_alpha_to_maxalpha_action;
        update_per_tenant_alpha_by_F1_plus_action;
        update_per_tenant_alpha_by_F1_minus_action;
        _no_op;
    }
    default_action: _no_op;
}
@pragma stage 10
table set_average_accepted_rate_table {
    actions {set_average_accepted_rate_action;}
    default_action: set_average_accepted_rate_action;
}
@pragma stage 10
table set_average_total_accepted_rate_table {
    actions {set_average_total_accepted_rate_action;}
    default_action: set_average_total_accepted_rate_action;
}






table i2e_mirror_table {
    reads {
        ipv4.dstAddr: exact;
    }
    actions {
        i2e_mirror_action;
    }
}

table resubmit_2_table {
    actions {
        resubmit_action;
    }
    default_action: resubmit_action;
}

table drop_packet_table {
    actions {
        _drop;
    }
    default_action: _drop;
}

table drop_packet_2_table {
    actions {
        _drop;
    }
    default_action: _drop;
}
table drop_packet_3_table {
    actions {
        _drop;
    }
    default_action: _drop;
}
table drop_packet_4_table {
    actions {
        _drop;
    }
    default_action: _drop;
}

@pragma stage 10
table set_flag_table {
    reads {
        ipv4.protocol: exact;
    }
    actions {
        set_tcp_flag_action;
        set_udp_flag_action;
    }
    default_action: set_tcp_flag_action;
}

// table label_times_7_table {
//     actions {
//         label_times_7_action;
//     }
//     default_action: label_times_7_action;
// }