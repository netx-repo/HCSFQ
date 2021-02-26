action add_info_hdr_action(flow_id, tenant_id) {
    add_header(info_hdr);
    add_header(recirculate_hdr);
    modify_field(info_hdr.label_smaller_than_alpha, 0);
    modify_field(info_hdr.flow_id, flow_id);
    modify_field(info_hdr.tenant_id, tenant_id);
    modify_field(info_hdr.recirc_flag, 0);
    modify_field(info_hdr.update_alpha, 0);
    modify_field(info_hdr.update_rate, 0);

    modify_field(meta.per_tenant_true_flag, 9);
    modify_field(meta.per_tenant_false_flag, 8);
    modify_field(meta.total_true_flag, 20);
    modify_field(meta.total_false_flag, 16);
}

action add_info_hdr_default_action() {
    add_header(info_hdr);
    add_header(recirculate_hdr);
    modify_field(info_hdr.label_smaller_than_alpha, 0);
    modify_field(info_hdr.flow_id, 99);
    modify_field(info_hdr.tenant_id, 9);
    modify_field(info_hdr.recirc_flag, 0);
    modify_field(info_hdr.update_alpha, 0);
    modify_field(info_hdr.update_rate, 0);

    modify_field(meta.per_tenant_true_flag, 9);
    modify_field(meta.per_tenant_false_flag, 8);
    modify_field(meta.total_true_flag, 20);
    modify_field(meta.total_false_flag, 16);
}

action add_info_hdr_udp_action(flow_id, tenant_id) {
    add_header(info_hdr);
    add_header(recirculate_hdr);
    modify_field(info_hdr.label_smaller_than_alpha, 0);
    modify_field(info_hdr.flow_id, flow_id);
    modify_field(info_hdr.tenant_id, tenant_id);
    modify_field(info_hdr.recirc_flag, 0);
    modify_field(info_hdr.update_alpha, 0);
    modify_field(info_hdr.update_rate, 0);

    modify_field(meta.per_tenant_true_flag, 9);
    modify_field(meta.per_tenant_false_flag, 8);
    modify_field(meta.total_true_flag, 20);
    modify_field(meta.total_false_flag, 16);
}

action add_info_hdr_udp_default_action() {
    add_header(info_hdr);
    add_header(recirculate_hdr);
    modify_field(info_hdr.label_smaller_than_alpha, 0);
    modify_field(info_hdr.flow_id, 0);
    modify_field(info_hdr.tenant_id, 0);
    modify_field(info_hdr.recirc_flag, 0);
    modify_field(info_hdr.update_alpha, 0);
    modify_field(info_hdr.update_rate, 0);

    modify_field(meta.per_tenant_true_flag, 9);
    modify_field(meta.per_tenant_false_flag, 8);
    modify_field(meta.total_true_flag, 20);
    modify_field(meta.total_false_flag, 16);
}

action get_random_value_action() {
    modify_field_rng_uniform(meta.randv, LOWER_BOUND, UPPER_BOUND);
    modify_field_rng_uniform(meta.randv2, LOWER_BOUND, UPPER_BOUND);
}

action estimate_aggregate_arrival_rate_action() {
    estimate_aggregate_arrival_rate_alu.execute_stateful_alu(info_hdr.tenant_id);
}

action estimate_total_aggregate_arrival_rate_action() {
    estimate_total_aggregate_arrival_rate_alu.execute_stateful_alu(0);
}

action get_aggregate_arrival_rate_action() {
    get_aggregate_arrival_rate_alu.execute_stateful_alu(info_hdr.tenant_id);
}

action get_aggregate_arrival_rate_times_7_action() {
    get_aggregate_arrival_rate_times_7_alu.execute_stateful_alu(info_hdr.tenant_id);
    modify_field(meta.to_resubmit_3, 1);
}

action get_total_aggregate_arrival_rate_action() {
    get_total_aggregate_arrival_rate_alu.execute_stateful_alu(0);
}

action get_total_aggregate_arrival_rate_times_7_action() {
    get_total_aggregate_arrival_rate_times_7_alu.execute_stateful_alu(0);
}

action get_alpha_action() {
    // ** set info_hdr.tenant_id = ethernet.srcAddr_lower in the same time
    get_alpha_alu.execute_stateful_alu(info_hdr.tenant_id);
}

action get_total_alpha_action() {
    get_total_alpha_alu.execute_stateful_alu(0);
}

action alpha_shl_4_action() {
    shift_left(meta.alpha_shl_4, recirculate_hdr.per_tenant_alpha, 4);
}

action flowrate_sum_01_01_action() {
    add(meta.label_shl_1, info_hdr.label, meta.label_shl_1);
}

action flowrate_sum_01_0_action() {
    add(meta.label_shl_1, info_hdr.label, 0);
}

action flowrate_sum_01_1_action() {
    add(meta.label_shl_1, meta.label_shl_1, 0);
}

action flowrate_sum_01_none_action() {
    modify_field(meta.label_shl_1, 0);
}

action flowrate_sum_23_23_action() {
    add(meta.label_shl_2, meta.label_shl_2, meta.label_shl_3);
}

action flowrate_sum_23_2_action() {
    add(meta.label_shl_2, meta.label_shl_2, 0);
}

action flowrate_sum_23_3_action() {
    add(meta.label_shl_2, meta.label_shl_3, 0);
}

action flowrate_sum_23_none_action() {
    modify_field(meta.label_shl_2, 0);
}

action alpha_times_15_action() {
    subtract(meta.alpha_times_15, meta.alpha_shl_4, recirculate_hdr.per_tenant_alpha);
}

action flowrate_times_randv_action() {
    add(meta.label_times_randv, meta.label_shl_1, meta.label_shl_2);
}

action get_minv_0_action() {
    // ** put meta.min_A_alpha into meta.label
    min(meta.label, info_hdr.per_tenant_A, recirculate_hdr.total_alpha);
    // modify_field(meta.alpha_shl_4, recirculate_hdr.total_alpha);
    
}

action get_minv_0_2_action() {
    // ** put meta.min_A_C into meta.label_shl_3
    min(meta.label_shl_3, info_hdr.total_A, C);
}

action get_minv_action() {
    min(meta.min_alphatimes15_labeltimesrand, meta.alpha_times_15, meta.label_times_randv);
}

action estimate_accepted_rate_action() {
    estimate_accepted_rate_alu.execute_stateful_alu(info_hdr.tenant_id);
}

action estimate_accepted_rate_2_action() {
    estimate_accepted_rate_2_alu.execute_stateful_alu(info_hdr.tenant_id);
}

action get_accepted_rate_action() {
    get_accepted_rate_alu.execute_stateful_alu(info_hdr.tenant_id);
}

action get_accepted_rate_times_7_action() {
    get_accepted_rate_times_7_alu.execute_stateful_alu(info_hdr.tenant_id);
    // modify_field(meta.to_resubmit_3, 1);
}

action estimate_total_accepted_rate_action() {
    estimate_total_accepted_rate_alu.execute_stateful_alu(0);
}

action estimate_total_accepted_rate_2_action() {
    estimate_total_accepted_rate_2_alu.execute_stateful_alu(0);
}

action get_total_accepted_rate_action() {
    get_total_accepted_rate_alu.execute_stateful_alu(0);
}

action get_total_accepted_rate_times_7_action() {
    get_total_accepted_rate_times_7_alu.execute_stateful_alu(0);
    // modify_field(meta.to_resubmit, 1);
}

action maintain_congest_state_action() {
    maintain_congest_state_alu.execute_stateful_alu(info_hdr.tenant_id);
    bit_or(recirculate_hdr.congested, recirculate_hdr.congested, meta.per_tenant_true_flag);
}

action maintain_total_congest_state_action() {
    maintain_total_congest_state_alu.execute_stateful_alu(0);
    bit_or(recirculate_hdr.congested, recirculate_hdr.congested, meta.total_true_flag);
}


action allow_mod_congest_state_action() {
    allow_mod_congest_state_alu.execute_stateful_alu(info_hdr.tenant_id);
}

action maintain_uncongest_state_action() {
    maintain_uncongest_state_alu.execute_stateful_alu(info_hdr.tenant_id);
}

action maintain_total_uncongest_state_action() {
    maintain_total_uncongest_state_alu.execute_stateful_alu(0);
}

action check_uncongest_state_0_action() {
    check_uncongest_state_0_alu.execute_stateful_alu(info_hdr.tenant_id);
    modify_field(meta.to_resubmit, 1);   
    bit_or(recirculate_hdr.congested, recirculate_hdr.congested, meta.per_tenant_false_flag);
}

action check_total_uncongest_state_0_action() {
    check_total_uncongest_state_0_alu.execute_stateful_alu(0);
    modify_field(meta.to_resubmit_2, 1);   
    bit_or(recirculate_hdr.congested, recirculate_hdr.congested, meta.total_false_flag);
}


action check_uncongest_state_23_action() {
    check_uncongest_state_23_alu.execute_stateful_alu(info_hdr.tenant_id);
}

action check_total_uncongest_state_23_action() {
    check_total_uncongest_state_23_alu.execute_stateful_alu(0);
}

action check_uncongest_state_1_action() {
    check_uncongest_state_1_alu.execute_stateful_alu(info_hdr.tenant_id);
}

action check_total_uncongest_state_1_action() {
    check_total_uncongest_state_1_alu.execute_stateful_alu(0);
}

action set_recirc_field_action() {
}

action set_drop_action() {
    modify_field(meta.to_drop, 1);
    // bit_or(recirculate_hdr.congested, recirculate_hdr.congested, 2); // meta.to_drop = 1
}

action resubmit_action() {
    recirculate(68);
}

action get_time_stamp_action() {
    get_time_stamp_alu.execute_stateful_alu(0);
}

action get_min_of_pertenantF_total_alpha_action() {
    min(meta.min_pertenantF_totalalpha, recirculate_hdr.per_tenant_F, recirculate_hdr.total_alpha);
}

action set_pertenantF_leq_totalalpha_action() {
    modify_field(recirculate_hdr.pertenantF_leq_totalalpha, 1);
}

action update_per_tenant_alpha_to_maxalpha_action() {
    update_per_tenant_alpha_to_maxalpha_alu.execute_stateful_alu(info_hdr.tenant_id);
}
action update_per_tenant_alpha_by_F1_minus_action() {
    update_per_tenant_alpha_by_F1_minus_alu.execute_stateful_alu(info_hdr.tenant_id);
}
action update_per_tenant_alpha_by_F1_plus_action() {
    update_per_tenant_alpha_by_F1_plus_alu.execute_stateful_alu(info_hdr.tenant_id);
}

action update_total_alpha_to_maxalpha_action() {
    update_total_alpha_to_maxalpha_alu.execute_stateful_alu(0);
}
action update_total_alpha_by_F0_action() {
    update_total_alpha_by_F0_alu.execute_stateful_alu(0);
}

action mod_resubmit_field_action() {
    // modify_field(info_hdr.update_alpha, meta.to_resubmit);
    // modify_field(info_hdr.update_total_alpha, meta.to_resubmit_2);
    modify_field(recirculate_hdr.to_drop, meta.to_drop);
}

action estimate_per_flow_rate_action() {
    estimate_per_flow_rate_alu.execute_stateful_alu(info_hdr.flow_id);
}


action get_per_flow_rate_action() {
    get_per_flow_rate_alu.execute_stateful_alu(info_hdr.flow_id);
}

action get_per_flow_rate_times_7_action() {
    get_per_flow_rate_times_7_alu.execute_stateful_alu(info_hdr.flow_id);
    modify_field(meta.to_resubmit_3, 1);
}

action set_average_per_flow_rate_action() {
    // set_average_per_flow_rate_alu.execute_stateful_alu(info_hdr.flow_id);
    label_times_7_alu.execute_stateful_alu(info_hdr.flow_id);
}

action set_average_aggregate_arrival_rate_action() {
    // set_average_aggregate_arrival_rate_alu.execute_stateful_alu(info_hdr.tenant_id);
    aggregate_arrival_rate_times_7_alu.execute_stateful_alu(info_hdr.tenant_id);
}

action set_average_total_aggregate_arrival_rate_action() {
    // set_average_total_aggregate_arrival_rate_alu.execute_stateful_alu(0);
    total_aggregate_arrival_rate_times_7_alu.execute_stateful_alu(0);
}

action set_average_accepted_rate_action() {
    // set_average_accepted_rate_alu.execute_stateful_alu(info_hdr.tenant_id);
    accepted_rate_times_7_alu.execute_stateful_alu(info_hdr.tenant_id);
}

action set_average_total_accepted_rate_action() {
    // set_average_total_accepted_rate_alu.execute_stateful_alu(0);
    total_accepted_rate_times_7_alu.execute_stateful_alu(0);
}

action get_delta_total_alpha_action() {
    // shift_right(meta.delta_total_alpha, recirculate_hdr.total_alpha, 7);
    shift_right(meta.delta_total_alpha, recirculate_hdr.total_alpha, INCREASE_SPEED);
}

action set_label_smaller_than_alpha_action() {
    modify_field(info_hdr.label_smaller_than_alpha, 1);
}

action flowrate_shl_action() {
    shift_left(meta.label_shl_1, info_hdr.label, 1);
    shift_left(meta.label_shl_2, info_hdr.label, 2);
    shift_left(meta.label_shl_3, info_hdr.label, 3);
}

action get_14_alpha_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 9);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 10);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 11);
}

action get_14_alpha_2_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 1);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 2);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 3);
}
action get_14_alpha_3_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 2);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 3);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 4);
}
action get_14_alpha_4_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 3);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 4);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 5);
}
action get_14_alpha_5_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 4);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 5);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 6);
}
action get_14_alpha_6_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 5);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 6);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 7);
}
action get_14_alpha_7_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 6);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 7);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 8);
}

action get_14_alpha_8_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 7);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 8);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 9);
}
action get_14_alpha_9_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 9);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 9);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 10);
}
action get_14_alpha_10_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 10);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 10);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 11);
}
action get_14_alpha_11_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 11);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 11);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 12);
}
action get_14_alpha_12_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 11);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 12);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 13);
}
action get_14_alpha_13_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 13);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 13);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 14);
}
action get_14_alpha_14_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 14);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 14);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 15);
}
action get_14_alpha_15_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 14);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 15);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 16);
}
action get_14_alpha_16_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 15);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 16);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 17);
}
action get_14_alpha_17_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 16);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 17);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 18);
}
action get_14_alpha_18_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 17);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 18);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 19);
}
action get_14_alpha_19_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 18);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 19);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 20);
}
action get_14_alpha_20_action() {
    shift_right(meta.total_alpha_mini, recirculate_hdr.total_alpha, 19);
    shift_right(meta.per_tenant_alpha_mini, recirculate_hdr.per_tenant_alpha, 20);
    shift_right(meta.per_tenant_alpha_mini_w2, recirculate_hdr.per_tenant_alpha, 21);
}

action get_34_alpha_action() {
    subtract(recirculate_hdr.total_alpha, recirculate_hdr.total_alpha, meta.total_alpha_mini);
    subtract(recirculate_hdr.per_tenant_alpha, recirculate_hdr.per_tenant_alpha, meta.per_tenant_alpha_mini);
}

action get_34_alpha_w2_action() {
    subtract(recirculate_hdr.total_alpha, recirculate_hdr.total_alpha, meta.total_alpha_mini);
    subtract(recirculate_hdr.per_tenant_alpha, recirculate_hdr.per_tenant_alpha, meta.per_tenant_alpha_mini_w2);
}

action getmin_delta_total_alpha_action() {
    min(meta.delta_total_alpha, meta.delta_total_alpha, meta.delta_c);
}

action counter_action() {
    counter_alu.execute_stateful_alu(info_hdr.flow_id);
}

action sum_per_flow_rate_action() {
    // add(info_hdr.label, info_hdr.label, meta.label);
    // add(meta.label, info_hdr.label, meta.label);
    // modify_field(meta.to_resubmit_3, 1);
}

action div_per_flow_rate_action() {
    shift_right(info_hdr.label, info_hdr.label, bitwidth);
}

action sum_aggregate_arrival_rate_action() {
    // add(info_hdr.per_tenant_A, info_hdr.per_tenant_A, meta.per_tenant_A);
    // modify_field(meta.to_resubmit_3, 1);
}

action sum_total_aggregate_arrival_rate_action() {
    // add(info_hdr.total_A, info_hdr.total_A, meta.total_A);
}

action div_aggregate_arrival_rate_action() {
    shift_right(info_hdr.per_tenant_A, info_hdr.per_tenant_A, bitwidth);
}

action div_total_aggregate_arrival_rate_action() {
    shift_right(info_hdr.total_A, info_hdr.total_A, bitwidth);
    modify_field(meta.to_resubmit_3, 1);
}

action sum_accepted_rate_action() {
    // add(recirculate_hdr.per_tenant_F, recirculate_hdr.per_tenant_F, meta.per_tenant_F);
    modify_field(meta.to_resubmit_3, 1);
}

action sum_total_accepted_rate_action() {
    // add(recirculate_hdr.total_F, recirculate_hdr.total_F, meta.total_F);
    modify_field(meta.to_resubmit, 1);
}

action div_accepted_rate_action() {
    shift_right(recirculate_hdr.per_tenant_F, recirculate_hdr.per_tenant_F, bitwidth);
}

action div_total_accepted_rate_action() {
    shift_right(recirculate_hdr.total_F, recirculate_hdr.total_F, bitwidth);
}

action set_update_total_alpha_flag_action() {
    modify_field(info_hdr.update_total_alpha, UPDATE_TOTAL_ALPHA);
}

action set_to_resubmit_action() {
    modify_field(meta.to_resubmit_3, 1);
}

action i2e_mirror_action(mirror_id) {
    //  ## notify the client that has obtained the lock  ##
    modify_field(current_node_meta.clone_md, 1);
    
#if __TARGET_TOFINO__ == 2
    modify_field(ig_intr_md_for_mb.mirror_hash, 2);
    modify_field(ig_intr_md_for_mb.mirror_multicast_ctrl, 0);
    modify_field(ig_intr_md_for_mb.mirror_io_select, 0);
#endif
    clone_ingress_pkt_to_egress(mirror_id, i2e_mirror_info);
}


action get_half_pktlen_action() {
    shift_right(meta.weight_len, ipv4.totalLen, 3);
    shift_right(meta.halflen, ipv4.totalLen, 3);
}

action get_half_pktlen_w2_action() {
    shift_right(meta.weight_len, ipv4.totalLen, 4);
    shift_right(meta.halflen, ipv4.totalLen, 3);
    modify_field(meta.w2, 1);
}

action get_half_pktlen_w4_action() {
    shift_right(meta.weight_len, ipv4.totalLen, 5);
    shift_right(meta.halflen, ipv4.totalLen, 3);
}

action get_fraction_factor_action() {
    get_fraction_factor_alu.execute_stateful_alu(0);
}

action set_tcp_flag_action() {
    modify_field(tcp.res, RATE_ESTIMATED);
}

action set_udp_flag_action() {
    modify_field(udp.dstPort, CSFQ_PORT);
}

action get_delta_c_action() {
    get_delta_c_alu.execute_stateful_alu(0);
}

// action label_times_7_action() {
//     label_times_7_alu.execute_stateful_alu(0);
// }