#include <tofino/constants.p4>
#if __TARGET_TOFINO__ == 2
#include <tofino/intrinsic_metadata.p4>
#else
#include <tofino/intrinsic_metadata.p4>
#endif
#include <tofino/primitives.p4>
#include <tofino/stateful_alu_blackbox.p4>

#define BPS
// #define ECN

// #define TWO_SLOTS
#define TWO_LAYERS

#define MONITOR_QUEUE_LENGTH

#include "includes/hcsfq_defines.p4"
#include "includes/hcsfq_headers.p4"
#include "includes/hcsfq_parser.p4"
#include "hcsfq_routing.p4"
#include "hcsfq_blackboxs.p4"
#include "hcsfq_actions.p4"
#include "hcsfq_tables.p4"

// #include "estimate.p4"
// #### metas

header_type node_meta_t {
    fields {
        clone_md: 8;
        // total_F: 32;
        // total_alpha: 32;
        // label: 32;
        // per_tenant_F: 32;
        // per_tenant_A: 32;
        // total_A: 32;
        // per_tenant_alpha: 32;
    }
}
metadata node_meta_t current_node_meta;

field_list i2e_mirror_info {
    current_node_meta.clone_md;
    // current_node_meta.total_F;
    // current_node_meta.total_alpha;
    // current_node_meta.label;
    // current_node_meta.per_tenant_F;
    // current_node_meta.per_tenant_A;
    // current_node_meta.total_A;
    // current_node_meta.per_tenant_alpha;
}

header_type meta_t {
    fields {
        cc: 32;
        per_tenant_A: 32;
        total_A: 32;
        total_F: 32;
        randv: 4;
        randv2: 4;
        per_tenant_true_flag: 8;
        per_tenant_false_flag: 8;
        total_true_flag: 8;
        total_false_flag: 8;

        // label_shl_0: 32;
        per_tenant_F: 32;
        per_tenant_F_prev: 32;
        total_F_prev: 32;
        halflen: 16;
        weight_len: 16;
        label: 32;
        label_shl_1: 32;
        label_shl_2: 32;
        label_shl_3: 32;
        alpha_shl_4: 32;
        alpha_times_15: 32;
        label_times_randv: 32;
        min_alphatimes15_labeltimesrand: 32;
        uncongest_state_predicate: 4;
        total_uncongest_state_predicate: 4;
        to_drop: 4;
        to_resubmit: 2;
        to_resubmit_2: 2;
        to_resubmit_3: 2;
        tsp: 32;
        min_pertenantF_totalalpha: 32;
        delta_total_alpha: 32;
        pertenantF_leq_totalalpha: 8;
        total_alpha_mini: 32;
        per_tenant_alpha_mini: 32;
        per_tenant_alpha_mini_w2: 32;
        fraction_factor: 16;
        w2: 2;
        delta_c: 32;
    }
}
metadata meta_t meta;

// #### registers
register fraction_factor_reg {
    width: 16;
    instance_count: 1;
}

register delta_c_reg {
    width: 32;
    instance_count: 1;
}

register per_flow_rate_reg {
    width: 64;
    instance_count: NUM_FLOWS;
}

register stored_per_flow_rate_reg {
    width: 64;
    instance_count: NUM_FLOWS;
}

register aggregate_arrival_rate_reg {
    width: 64;
    instance_count: NUM_TENANTS;
}

register stored_aggregate_arrival_rate_reg {
    width: 64;
    instance_count: NUM_TENANTS;
}

register counter_reg {
    width: 8;
    instance_count: NUM_FLOWS;
}

register accepted_rate_reg {
    width: 64;
    instance_count: NUM_TENANTS;
}

register stored_accepted_rate_reg {
    width: 64;
    instance_count: NUM_TENANTS;
}

register alpha_reg {
    width: 32;
    instance_count: NUM_TENANTS;
}

register tmp_alpha_reg {
    width: 32;
    instance_count: NUM_TENANTS;
}

register congest_state_reg {
    width: 64;
    instance_count: NUM_TENANTS;
}


register total_aggregate_arrival_rate_reg {
    width: 64;
    instance_count: 1;
}

register total_stored_aggregate_arrival_rate_reg {
    width: 64;
    instance_count: 1;
}

register total_accepted_rate_reg {
    width: 64;
    instance_count: 1;
}

register total_stored_accepted_rate_reg {
    width: 64;
    instance_count: 1;
}

register total_alpha_reg {
    width: 32;
    instance_count: 1;
}

register tmp_total_alpha_reg {
    width: 32;
    instance_count: 1;
}

register total_congest_state_reg {
    width: 64;
    instance_count: 1;
}

register bundle_1_reg {
    width: 32;
    instance_count: 1;
}

register bundle_2_reg {
    width: 32;
    instance_count: 1;
}

register timestamp_reg {
    width: 32;
    instance_count: 1;
}

register label_times_7_reg {
    width: 32;
    instance_count: 1;
}

register aggregate_arrival_rate_times_7_reg {
    width: 32;
    instance_count: 1;
}

register total_aggregate_arrival_rate_times_7_reg {
    width: 32;
    instance_count: 1;
}

control recirc_pipe {
    // stage 0
#ifdef TWO_LAYERS
// if (info_hdr.update_alpha == UPDATE_ALPHA) {
    apply(update_total_alpha_table);
// }
#endif
    apply(get_delta_c_table);

    //* Math Unit*//
    // apply(label_times_7_table);

    // stage 1

    apply(get_delta_total_alpha_table);
    apply(get_min_of_pertenantF_total_alpha_table);

    // stage 2
    // if (info_hdr.update_rate == UPDATE_RATE) {
    apply(set_average_per_flow_rate_table);
// }
    if (recirculate_hdr.per_tenant_F == meta.min_pertenantF_totalalpha) {
        apply(set_pertenantF_leq_totalalpha_table);
    }

// if (info_hdr.update_rate == UPDATE_RATE) {
    apply(set_average_aggregate_arrival_rate_table);
    apply(set_average_total_aggregate_arrival_rate_table);
// }
    // stage 3
    apply(getmin_delta_total_alpha_table);

// if (info_hdr.update_total_alpha == UPDATE_TOTAL_ALPHA) {
    // stage 4
    apply(update_per_tenant_alpha_table);
// }
// if (info_hdr.update_rate == UPDATE_RATE) {
    apply(set_average_accepted_rate_table);
    apply(set_average_total_accepted_rate_table);
// }
    // if (recirculate_hdr.to_drop == 0) {
    //     apply(ipv4_route_2);
    // }
    apply(drop_packet_2_table);
}

control main_pipe {
// if ((tcp.syn == 1)) {
//     apply(ipv4_route);
// }
// else {
    // stage 0
    // GET: meta.tsp := timestamp;
    // GET: randv
    // ADD: info_hdr, recirculate_hdr, tcp.red
    // GET: recirculate_hdr.total_alpha := total_alpha_reg
    apply(get_time_stamp_table);
    apply(get_random_value_table);
    if (valid(tcp)) {
        apply(add_info_hdr_table);
    }
    else {
        apply(add_info_hdr_udp_table);
    }
    apply(get_total_alpha_table);
    apply(get_half_pktlen_table);

    // stage 1
    // GET: meta.label
    // GET: info_hdr.label
    apply(estimate_per_flow_rate_table);
if (eg_intr_md.deq_qdepth != 0) {
    apply(put_src_up_table);
}
    apply(get_fraction_factor_table);

    // stage 2
    // GET: meta.per_tenant_A
    // GET: meta.total_A
    // GET: info_hdr.per_tenant_A
    // GET: info_hdr.total_A
    // MOD: meta.label = meta.label + info_hdr.label 
    apply(estimate_aggregate_arrival_rate_table);
    apply(estimate_total_aggregate_arrival_rate_table);
    // apply(get_aggregate_arrival_rate_table);
    if (meta.label == 0) {
        apply(get_per_flow_rate_table);
    }
    else {
        apply(get_per_flow_rate_times_7_table);
    }
    // apply(get_total_aggregate_arrival_rate_table);
    // if (meta.label != 0) {
    //     apply(sum_per_flow_rate_table);
    // }

    // stage 3

    // MOD: info_hdr.label = meta.label / 2
    // MOD: meta.per_tenant_A = info_hdr.per_tenant_A + meta.per_tenant_A
    //      meta.total_A = info_hdr.total_A = meta.total_A
    if (meta.label != 0) {
        apply(div_per_flow_rate_table);
    }
    if (meta.per_tenant_A == 0) {
        apply(get_aggregate_arrival_rate_table);
    }
    else {
        apply(get_aggregate_arrival_rate_times_7_table);
    }
    if (meta.total_A == 0) {
        apply(get_total_aggregate_arrival_rate_table);
    }
    else {
        apply(get_total_aggregate_arrival_rate_times_7_table);
    }
    // if (meta.per_tenant_A != 0) {
    //     apply(sum_aggregate_arrival_rate_table);
    // }
    // if (meta.total_A != 0) {
    //     apply(sum_total_aggregate_arrival_rate_table);
    // }

    // stage 4
    // GET: recirculate_hdr.per_tenant_F
    // GET: recirculate_hdr.total_F
    // GET: recirculate_hdr.per_tenant_alpha
    // MOD: meta.label_shl_1 = info_hdr.label * 2
    //      meta.label_shl_2 = info_hdr.label * 4
    //      meta.label_shl_3 = info_hdr.label * 8
    // MOD: info_hdr.per_tenant_A = meta.per_tenant_A / 2
    //      info_hdr.total_A = meta.total_A / 2
    // apply(get_accepted_rate_table);
    // apply(get_total_accepted_rate_table);
    apply(get_alpha_table);
    apply(flowrate_shl_table);
    if (meta.per_tenant_A != 0) {
        apply(div_aggregate_arrival_rate_table);
    }
    if (meta.total_A != 0) {
        apply(div_total_aggregate_arrival_rate_table);
    }
    // stage 5
    // MOD: meta.alpha_shl_4 = recirculate_hdr.per_tenant_alpha * 16
    // MOD: meta.label_shl_1 = info_hdr.label + meta.label_shl_1
    // MOD: meta.label_shl_2 = meta.label_shl_2 + meta.label_shl_3
    // GET: info_hdr.update_rate
    // MIN: meta.min_A_alpha = min(info_hdr.per_tenant_A, recirculate_hdr.total_alpha)
    // MIN: meta.min_A_C = min(info_hdr.total_A, C)
    apply(alpha_shl_4_table);
    apply(flowrate_sum_01_table);
    apply(flowrate_sum_23_table);
    apply(counter_table);
    apply(get_minv_0_table);
    apply(get_minv_0_2_table);

    // stage 6
    // MOD: meta.label_times_randv = meta.label_shl_1 + meta.label_shl_2
    // MOD: meta.alpha_times_15 = meta.alpha_shl_4 - recirculate_hdr.per_tenant_alpha
    // GET: meta.to_resubmit
    //      recirculate_hdr.congested = recirculate_hdr.congested | meta.per_tenant_true_flag
    // GET: meta.uncongest_state_predicate
    apply(flowrate_times_randv_table);
    apply(alpha_times_15_table);
    // put min_A_alpha into meta.label
    if (meta.label == recirculate_hdr.total_alpha) {
        apply(maintain_congest_state_table);
    }
    else {
        apply(maintain_uncongest_state_table);
    }
    
    // stage 7
    // MIN: meta.min_alphatimes15_labeltimesrand = min(meta.alpha_times_15, meta.label_times_randv)
    // GET: meta.to_resubmit_2
    //      recirculate_hdr.congested = recirculate_hdr.congested | meta.total_true_flag
    // GET: meta.total_uncongest_state_predicate
    apply(get_minv_table);
    // put min_A_C into meta.label_shl_3
// #ifdef TWO_LAYERS
    if (meta.label_shl_3 == C) {
        apply(maintain_total_congest_state_table);
    }
    else {
        apply(maintain_total_uncongest_state_table);
    }
// #endif 

    // stage 8
    // GET: meta.per_tenant_F
    // GET: meta.total_F
    // MOD: meta.to_drop = 1
    // GET: meta.per_tenant_F
    // GET: meta.total_F
    // MOD: meta.total_alpha_mini = recirculate_hdr.total_alpha >> 7
    //      meta.per_tenant_alpha_mini = recirculate_hdr.per_tenant_alpha >> 8
    // MOD: meta.to_resubmit = 1
    //      recirculate_hdr.congested = recirculate_hdr.congested | meta.per_tenant_false_flag;
    if (meta.min_alphatimes15_labeltimesrand == meta.label_times_randv) {
        apply(estimate_accepted_rate_table);
        apply(estimate_total_accepted_rate_table);
    }
    else {
        apply(set_drop_table);
        apply(estimate_accepted_rate_2_table);
        apply(estimate_total_accepted_rate_2_table);
    }
    apply(get_14_alpha_table);
    if (meta.label != recirculate_hdr.total_alpha) {
        apply(check_uncongest_state_table);
    }
    
    
    // stage 9
    // MOD: meta.per_tenant_F = recirculate_hdr.per_tenant_F + meta.per_tenant_F
    // MOD: meta.total_F = recirculate_hdr.total_F + meta.total_F
    //      meta.to_resubmit_2 = 1
    // MOD: recirculate_hdr.congested = recirculate_hdr.congested | meta.total_false_flag;
    
    if (meta.per_tenant_F != 0) {
        // only change the resubmit tag here
        apply(sum_accepted_rate_table);
    }
    if (meta.total_F != 0) {
        // only change the resubmit tag here
        apply(sum_total_accepted_rate_table);
    }

// #ifdef TWO_LAYERS
    if (meta.label_shl_3 != C) {
        apply(check_total_uncongest_state_table);
    }
    // if (info_hdr.update_rate == UPDATE_RATE) {
    //     apply(set_to_resubmit_table);
    // }
// #endif

    // stage 10
    // MOD: recirculate_hdr.per_tenant_F = meta.per_tenant_F / 2
    //      recirculate_hdr.total_F = meta.total_F / 2
    // MOD: recirculate_hdr.total_alpha = recirculate_hdr.total_alpha - meta.total_alpha_mini
    //      recirculate_hdr.per_tenant_alpha = recirculate_hdr.per_tenant_alpha - meta.per_tenant_alpha_mini
    // MOD: recirculate_hdr.to_drop = meta.to_drop
    if (meta.per_tenant_F == 0) {
        apply(get_accepted_rate_table);
    }
    else {
        apply(get_accepted_rate_times_7_table);
    }
    if (meta.total_F == 0) {
        apply(get_total_accepted_rate_table);
    }
    else {
        apply(get_total_accepted_rate_times_7_table);
    }
    apply(get_34_alpha_table);
    apply(mod_resubmit_field_table);
    if ((meta.to_resubmit != 0) or (meta.to_resubmit_2 != 0) or (meta.to_resubmit_3 != 0)) {
        apply(set_flag_table);
    }
    apply(ipv4_route_2);

    // stage 11
    // if ((meta.to_resubmit != 0) or (meta.to_resubmit_2 != 0)) {
    //     apply(resubmit_table);
    // }
    // else if (meta.to_drop == 0) {
    //     apply(ipv4_route_3);
    // }
    // **********************
    // *******CHANGE TO CLONE ***********
    if (meta.per_tenant_F != 0) {
        apply(div_accepted_rate_table);
    }
    if (meta.total_F != 0) {
        apply(div_total_accepted_rate_table);
    }



    if ((meta.to_resubmit != 0) or (meta.to_resubmit_2 != 0) or (meta.to_resubmit_3 != 0)) {
#ifdef ECN
        if ((meta.to_drop == 0) or (valid(tcp))) {
            // ** resubmit current packet and drop; mirror a packet to route **
            apply(i2e_mirror_table);
            apply(resubmit_table);
        }
        else {
            // ** resubmit current packet and drop **
            apply(resubmit_2_table);
        }
        // apply(i2e_mirror_table);
        // apply(resubmit_table);
#else
        if (meta.to_drop == 0) {
            // ** resubmit current packet and drop; mirror a packet to route **
            apply(i2e_mirror_table);
            apply(resubmit_table);
        }
        else {
            // ** resubmit current packet and drop **
            apply(resubmit_2_table);
        }
#endif
    }
    else {
#ifdef ECN
        if ((meta.to_drop == 0) or (valid(tcp))) {
            // ** route the current packet
            apply(ipv4_route_3);
        }
        else {
            // ** drop the current packet
            apply(drop_packet_table);
        }
        // apply(set_ecn_table);
        // apply(ipv4_route_3);
#else
        if (meta.to_drop == 0) {
            // ** route the current packet
            apply(ipv4_route_3);
        }
        else {
            // ** drop the current packet
            apply(drop_packet_table);
        }
#endif
    }
// }
}

// action set_ecn_flag_action() {
//     modify_field(ipv4.ecn_flag, eg_intr_md.enq_congest_stat);
// }

// table set_ecn_flag_table {
//     reads {
//         ipv4.protocol: exact;
//     }
//     actions {set_ecn_flag_action;
//              _no_op;}
//     default_action: _no_op;
// }

action set_ecn_congest_action() {
    modify_field(ipv4.ecn_flag, 3);
}
action set_ecn_not_congest_action() {
    modify_field(ipv4.ecn_flag, 2);
}
table set_ecn_table {
    reads {
        meta.to_drop: exact;
        ipv4.protocol: exact;
    }
    actions {
        set_ecn_congest_action;
        set_ecn_not_congest_action;
        _no_op;
    }
    default_action: _no_op;
}

table set_ecn_2_table {
    reads {
        meta.to_drop: exact;
        ipv4.protocol: exact;
    }
    actions {
        set_ecn_congest_action;
        set_ecn_not_congest_action;
        _no_op;
    }
    default_action: _no_op;
}

control ingress {
    if (valid(tcp) or valid(udp)) {
        // if (((valid(tcp)) and (tcp.res == 0)) or (valid(udp) and (udp.dstPort != CSFQ_PORT))) {
        if (not valid(info_hdr)) {
            main_pipe();
        }
        else {
            recirc_pipe();
        }
    }
}

control egress {
    // apply(set_ecn_flag_table);
#ifdef ECN
    apply(set_ecn_2_table);
#endif

    // if ((meta.per_tenant_true_flag == 0) and (current_node_meta.clone_md == 1)) {
    // if (eg_intr_md.enq_congest_stat == 3) {
        // apply(put_dst_up_table);
    // }
    apply(set_ecn_byq_table);
}

blackbox stateful_alu set_ecn_byq_alu {
    reg: dst_up_reg;
    condition_lo: eg_intr_md.deq_qdepth > QDEPTH_THRESHOLD;
    // condition_hi: eg_intr_md.enq_qdepth > QDEPTH_THRESHOLD;

    update_lo_1_predicate:  condition_lo;
    update_lo_1_value:      3;
    update_lo_2_predicate:  not condition_lo;
    update_lo_2_value:      2;

    output_value:           alu_lo;
    output_dst:             ipv4.ecn_flag;
    // update_lo_1_value:      eg_intr_md.deq_qdepth;
}
action set_ecn_byq_action() {
    set_ecn_byq_alu.execute_stateful_alu(0);
}
table set_ecn_byq_table {
    reads {
        ipv4.protocol: exact;
    }
    actions {set_ecn_byq_action;
            _no_op;}
    default_action: _no_op;
}
register dst_up_reg {
    width: 64;
    instance_count: 1;
}
register dst_low_reg {
    width: 32;
    instance_count: 1;
}
register src_up_reg {
    width: 64;
    instance_count: 1;
}
register src_low_reg {
    width: 32;
    instance_count: 1;
}
blackbox stateful_alu put_dst_up_alu {
    reg: dst_up_reg;

    // update_lo_1_value: 1;
    update_lo_1_value: eg_intr_md.enq_qdepth;
    // update_hi_1_value: eg_intr_md.deq_qdepth;
}
blackbox stateful_alu put_dst_low_alu {
    reg: dst_low_reg;
    update_lo_1_value: recirculate_hdr.pertenantF_leq_totalalpha;
}
blackbox stateful_alu put_src_up_alu {
    reg: src_up_reg;
#ifdef MONITOR_QUEUE_LENGTH
    // update_lo_1_value: eg_intr_md.enq_qdepth;
    // update_hi_1_value: eg_intr_md.deq_qdepth;
    update_lo_1_value: meta.halflen;
    update_hi_1_value: ipv4.totalLen;
#else
    update_lo_1_value: meta.halflen;
    update_hi_1_value: ipv4.totalLen;
#endif
}
blackbox stateful_alu put_src_low_alu {
    reg: src_low_reg;
    // update_lo_1_value: meta.per_tenant_A;
    update_lo_1_value: info_hdr.label_smaller_than_alpha;
}
action put_dst_up_action() {
    // remove_header(recirculate_hdr);
    // remove_header(info_hdr);
    // modify_field(tcp.res, 0);
    put_dst_up_alu.execute_stateful_alu(0);
}
action put_dst_low_action() {
    put_dst_low_alu.execute_stateful_alu(0);
}
action put_src_up_action() {
    put_src_up_alu.execute_stateful_alu(0);
}
action put_src_low_action() {
    // put_src_low_alu.execute_stateful_alu(0);
    put_src_low_alu.execute_stateful_alu(0);
}

table put_dst_up_table {
    actions {put_dst_up_action;}
    default_action: put_dst_up_action;
}
table put_dst_low_table {
    actions {put_dst_low_action;}
    default_action: put_dst_low_action;
}
table put_src_up_table {
    actions {put_src_up_action;}
    default_action: put_src_up_action;
}
table put_src_low_table {
    actions {put_src_low_action;}
    default_action: put_src_low_action;
}