@pragma pa_container_size ingress ig_intr_md.ingress_mac_tstamp 8 32 8

@pragma stateful_field_slice ig_intr_md.ingress_mac_tstamp 39 8
blackbox stateful_alu get_time_stamp_alu {
    reg: timestamp_reg;
    update_lo_1_value: ig_intr_md.ingress_mac_tstamp;

    output_value: alu_lo;
    output_dst: meta.tsp;
}

// @pragma stateful_field_slice ig_intr_md.ingress_mac_tstamp 39 8
blackbox stateful_alu estimate_per_flow_rate_alu {
    reg: per_flow_rate_reg;
    // lo: timestamp; hi: count;

    condition_lo: meta.tsp - register_lo < EPOCH_THRESHOLD;

    update_lo_1_predicate:  condition_lo;
    update_lo_1_value:      register_lo;
    update_lo_2_predicate:  not condition_lo;
    update_lo_2_value:      meta.tsp;

    update_hi_1_predicate:  condition_lo;
#ifdef BPS
    update_hi_1_value:      register_hi + meta.weight_len;
#else     // ** PPS
    update_hi_1_value:      register_hi + 1;
#endif
    update_hi_2_predicate:  not condition_lo;
    update_hi_2_value:      0;

#ifdef TWO_SLOTS
    output_predicate:       not condition_lo;
    output_value:           register_hi;
#else
    output_predicate:       not condition_lo;
    output_value:           register_hi;
#endif
    output_dst:             meta.label;
}

// @pragma stateful_field_slice ig_intr_md.ingress_mac_tstamp 39 8
blackbox stateful_alu get_per_flow_rate_alu {
    reg: stored_per_flow_rate_reg;
    // lo: timestamp; hi: last_count;
    // condition_lo: info_hdr.label = 0;
    // update_hi_1_value: register_lo + info_hdr.label;

    output_value: register_hi;
    output_dst: info_hdr.label;
}

blackbox stateful_alu get_per_flow_rate_times_7_alu {
    reg: stored_per_flow_rate_reg;
    // lo: timestamp; hi: last_count;
    // condition_lo: info_hdr.label = 0;
    // update_hi_1_value: register_lo + info_hdr.label;

    update_lo_1_value: register_lo + meta.label;

    output_value: alu_lo;
    output_dst: info_hdr.label;
}

blackbox stateful_alu estimate_aggregate_arrival_rate_alu {
    reg: aggregate_arrival_rate_reg;
    // lo: timestamp; hi: count;

    condition_lo: meta.tsp - register_lo < EPOCH_THRESHOLD_FOR_AF;

    update_lo_1_predicate:  condition_lo;
    update_lo_1_value:      register_lo;
    update_lo_2_predicate:  not condition_lo;
    update_lo_2_value:      meta.tsp;

    update_hi_1_predicate:  condition_lo;
#ifdef BPS
    update_hi_1_value:      register_hi + meta.halflen;
#else     // ** PPS
    update_hi_1_value:      register_hi + 1;
#endif
    update_hi_2_predicate:  not condition_lo;
    update_hi_2_value:      0;

#ifdef TWO_SLOTS
    output_predicate:       not condition_lo;
    output_value:           register_hi;
#else
    output_predicate:       not condition_lo;
    output_value:           register_hi;
#endif
    output_dst:             meta.per_tenant_A;
    // output_dst:             meta.alpha_times_15;
}

blackbox stateful_alu estimate_total_aggregate_arrival_rate_alu {
    reg: total_aggregate_arrival_rate_reg;
    // lo: timestamp; hi: count;

    condition_lo: meta.tsp - register_lo < EPOCH_THRESHOLD_FOR_AF;

    update_lo_1_predicate:  condition_lo;
    update_lo_1_value:      register_lo;
    update_lo_2_predicate:  not condition_lo;
    update_lo_2_value:      meta.tsp;

    update_hi_1_predicate:  condition_lo;
#ifdef BPS
    update_hi_1_value:      register_hi + meta.halflen;
#else     // ** PPS
    update_hi_1_value:      register_hi + 1;
#endif
    update_hi_2_predicate:  not condition_lo;
    update_hi_2_value:      0;

#ifdef TWO_SLOTS
    output_predicate:       not condition_lo;
    output_value:           register_hi;
#else
    output_predicate:       not condition_lo;
    output_value:           register_hi;
#endif
    output_dst:             meta.total_A;
    // output_dst:             meta.label_times_randv;
}

blackbox stateful_alu get_aggregate_arrival_rate_alu {
    reg: stored_aggregate_arrival_rate_reg;

    // update_hi_1_value: register_lo + info_hdr.alpha_times_15;
    output_value: register_hi;
    // output_dst: info_hdr.alpha_times_15;
    output_dst: info_hdr.per_tenant_A;
}

blackbox stateful_alu get_aggregate_arrival_rate_times_7_alu {
    reg: stored_aggregate_arrival_rate_reg;

    update_lo_1_value: register_lo + meta.per_tenant_A;

    output_value: alu_lo;
    output_dst: info_hdr.per_tenant_A;
}

blackbox stateful_alu get_total_aggregate_arrival_rate_alu {
    reg: total_stored_aggregate_arrival_rate_reg;

    // update_hi_1_value: register_lo + info_hdr.label_times_randv;
    output_value: register_hi;
    // output_dst: info_hdr.label_times_randv;
    output_dst: info_hdr.total_A;
}

blackbox stateful_alu get_total_aggregate_arrival_rate_times_7_alu {
    reg: total_stored_aggregate_arrival_rate_reg;

    update_lo_1_value: register_lo + meta.total_A;

    output_value: alu_lo;
    output_dst: info_hdr.total_A;
}

blackbox stateful_alu get_alpha_alu {
    reg: alpha_reg;

    output_value: register_lo;
    output_dst: recirculate_hdr.per_tenant_alpha;
}

blackbox stateful_alu get_total_alpha_alu {
    reg: total_alpha_reg;

    condition_lo: register_lo > C;

    update_lo_1_predicate:  condition_lo;
    update_lo_1_value:      C;

    output_value: alu_lo;
    output_dst: recirculate_hdr.total_alpha;
}


blackbox stateful_alu estimate_accepted_rate_alu {
    reg: accepted_rate_reg;
    // lo: timestamp; hi: count

    condition_lo: meta.tsp - register_lo < EPOCH_THRESHOLD_FOR_AF;

    update_lo_1_predicate:  condition_lo;
    update_lo_1_value:      register_lo;
    update_lo_2_predicate:  not condition_lo;
    update_lo_2_value:      meta.tsp;

    update_hi_1_predicate:  condition_lo;
#ifdef BPS
    update_hi_1_value:      register_hi + meta.halflen;
#else     // ** PPS
    update_hi_1_value:      register_hi + 1;
#endif
    update_hi_2_predicate:  not condition_lo;
    update_hi_2_value:      0;

#ifdef TWO_SLOTS
    output_predicate:       not condition_lo;
    output_value:           register_hi;
#else
    output_predicate:       not condition_lo;
    output_value:           register_hi;
#endif
    // output_dst:             meta.per_tenant_F;
    output_dst:             meta.per_tenant_F;
}

blackbox stateful_alu estimate_total_accepted_rate_alu {
    reg: total_accepted_rate_reg;
    // lo: timestamp; hi: count

    condition_lo: meta.tsp - register_lo < EPOCH_THRESHOLD_FOR_AF;

    update_lo_1_predicate:  condition_lo;
    update_lo_1_value:      register_lo;
    update_lo_2_predicate:  not condition_lo;
    update_lo_2_value:      meta.tsp;

    update_hi_1_predicate:  condition_lo;
#ifdef BPS
    update_hi_1_value:      register_hi + meta.halflen;
#else     // ** PPS
    update_hi_1_value:      register_hi + 1;
#endif
    update_hi_2_predicate:  not condition_lo;
    update_hi_2_value:      0;

#ifdef TWO_SLOTS
    output_predicate:       not condition_lo;
    output_value:           register_hi;
#else
    output_predicate:       not condition_lo;
    output_value:           register_hi;
#endif
    output_dst:             meta.total_F;
}

blackbox stateful_alu estimate_accepted_rate_2_alu {
    reg: accepted_rate_reg;
    // lo: timestamp; hi: count

    condition_lo: meta.tsp - register_lo < EPOCH_THRESHOLD_FOR_AF;

    update_lo_1_predicate:  condition_lo;
    update_lo_1_value:      register_lo;
    update_lo_2_predicate:  not condition_lo;
    update_lo_2_value:      meta.tsp;

    update_hi_1_predicate:  condition_lo;
    update_hi_1_value:      register_hi;
    update_hi_2_predicate:  not condition_lo;
    update_hi_2_value:      0;

#ifdef TWO_SLOTS
    output_predicate:       not condition_lo;
    output_value:           register_hi;
#else
    output_predicate:       not condition_lo;
    output_value:           register_hi;
#endif
    output_dst:             meta.per_tenant_F;
}

blackbox stateful_alu estimate_total_accepted_rate_2_alu {
    reg: total_accepted_rate_reg;
    // lo: timestamp; hi: count

    condition_lo: meta.tsp - register_lo < EPOCH_THRESHOLD_FOR_AF;

    update_lo_1_predicate:  condition_lo;
    update_lo_1_value:      register_lo;
    update_lo_2_predicate:  not condition_lo;
    update_lo_2_value:      meta.tsp;

    update_hi_1_predicate:  condition_lo;
    update_hi_1_value:      register_hi;
    update_hi_2_predicate:  not condition_lo;
    update_hi_2_value:      0;

#ifdef TWO_SLOTS
    output_predicate:       not condition_lo;
    output_value:           register_hi;
#else
    output_predicate:       not condition_lo;
    output_value:           register_hi;
#endif
    output_dst:             meta.total_F;
}

blackbox stateful_alu get_accepted_rate_alu {
    reg: stored_accepted_rate_reg;
    // lo: speed; hi: speed+cur_win_speed
    // update_hi_1_value: register_lo + recirculate_hdr.per_tenant_F;
    output_value: register_hi;
    output_dst: recirculate_hdr.per_tenant_F;
}

blackbox stateful_alu get_accepted_rate_times_7_alu {
    reg: stored_accepted_rate_reg;
    // lo: speed; hi: speed+cur_win_speed
    // update_hi_1_value: register_lo + recirculate_hdr.per_tenant_F;

    update_lo_1_value: register_lo + meta.per_tenant_F;

    output_value: alu_lo;
    output_dst: recirculate_hdr.per_tenant_F;
}

blackbox stateful_alu get_total_accepted_rate_alu {
    reg: total_stored_accepted_rate_reg;
    // lo: timestamp; hi: last_count
    // update_hi_1_value: register_lo + recirculate_hdr.total_F;
    output_value: register_hi;
    output_dst: recirculate_hdr.total_F;
}

blackbox stateful_alu get_total_accepted_rate_times_7_alu {
    reg: total_stored_accepted_rate_reg;

    update_lo_1_value: register_lo + meta.total_F;

    output_value: alu_lo;
    output_dst: recirculate_hdr.total_F;
}

blackbox stateful_alu maintain_congest_state_alu {
    reg: congest_state_reg;
    // lo: timestamp; hi: congested

    condition_lo: meta.tsp - register_lo < WINDOW_SIZE;
    condition_hi: register_hi == 0;
    // condition_hi: register_hi < info_hdr.per_tenant_F_greater_than_alpha;

    update_lo_1_predicate: (not condition_lo) or condition_hi;
    update_lo_1_value: meta.tsp;

    update_hi_1_predicate: condition_hi;
    update_hi_1_value: 1;
    // update_hi_2_predicate: (not condition_lo) and (not condition_hi);
    // update_hi_2_value: 2;

    output_predicate: (not condition_lo) and (not condition_hi);
    output_value: register_hi;
    output_dst: meta.to_resubmit;
}

blackbox stateful_alu maintain_total_congest_state_alu {
    reg: total_congest_state_reg;
    // lo: timestamp; hi: congested

    condition_lo: meta.tsp - register_lo < WINDOW_SIZE;
    condition_hi: register_hi == 0;
    // condition_hi: register_hi < info_hdr.F_greater_than_C;

    update_lo_1_predicate: (not condition_lo) or condition_hi;
    update_lo_1_value: meta.tsp;

    update_hi_1_predicate: condition_hi;
    update_hi_1_value: 1;
    // update_hi_2_predicate: (not condition_lo) and (not condition_hi);
    // update_hi_2_value: 2;

    output_predicate: (not condition_lo) and (not condition_hi);
    output_value: register_hi;
    output_dst: meta.to_resubmit_2;
}


blackbox stateful_alu allow_mod_congest_state_alu {
    reg: congest_state_reg;

    condition_lo: register_hi == 2;

    update_hi_1_predicate: condition_lo;
    update_hi_1_value: 1;
}

blackbox stateful_alu maintain_uncongest_state_alu {
    reg: congest_state_reg;
    // lo: timestamp; hi: congested

    condition_lo: meta.tsp - register_lo < WINDOW_SIZE;
    condition_hi: register_hi > 0;
    // condition_hi: register_hi > info_hdr.per_tenant_F_greater_than_alpha;

    update_lo_1_predicate: (not condition_lo) or condition_hi;
    update_lo_1_value: meta.tsp;

    update_hi_1_predicate: condition_hi;
    update_hi_1_value: 0;

    // output_predicate: condition_lo or condition_hi;
    output_value: predicate;
    output_dst: meta.uncongest_state_predicate;
}

blackbox stateful_alu maintain_total_uncongest_state_alu {
    reg: total_congest_state_reg;
    // lo: timestamp; hi: congested

    condition_lo: meta.tsp - register_lo < WINDOW_SIZE;
    condition_hi: register_hi > 0;
    // condition_hi: register_hi > info_hdr.F_greater_than_C;

    update_lo_1_predicate: (not condition_lo) or condition_hi;
    update_lo_1_value: meta.tsp;

    update_hi_1_predicate: condition_hi;
    update_hi_1_value: 0;

    // output_predicate: condition_lo or condition_hi;
    output_value: predicate;
    output_dst: meta.total_uncongest_state_predicate;
}

blackbox stateful_alu check_uncongest_state_0_alu {
    reg: tmp_alpha_reg;

    update_lo_1_value: INIT_C;

    // output_value: register_lo;
    // output_dst: recirculate_hdr.per_tenant_alpha;
}

blackbox stateful_alu check_total_uncongest_state_0_alu {
    reg: tmp_total_alpha_reg;

    update_lo_1_value: INIT_C;

    // output_value: register_lo;
    // output_dst: recirculate_hdr.total_alpha;
}

blackbox stateful_alu check_uncongest_state_23_alu {
    reg: tmp_alpha_reg;

    update_lo_1_value: INIT_C;
}

blackbox stateful_alu check_total_uncongest_state_23_alu {
    reg: tmp_total_alpha_reg;

    update_lo_1_value: INIT_C;
}

blackbox stateful_alu check_uncongest_state_1_alu {
    reg: tmp_alpha_reg;

    condition_lo: info_hdr.label > register_lo;
    update_lo_1_value: info_hdr.label;
}

blackbox stateful_alu check_total_uncongest_state_1_alu {
    reg: tmp_total_alpha_reg;

    // condition_lo: meta.per_tenant_F > register_lo;
    // update_lo_1_value: meta.per_tenant_F;
    condition_lo: recirculate_hdr.per_tenant_F > register_lo;
    update_lo_1_value: recirculate_hdr.per_tenant_F;
}

blackbox stateful_alu update_per_tenant_alpha_to_maxalpha_alu {
    reg: alpha_reg;

    // update_lo_1_value: recirculate_hdr.per_tenant_alpha + INIT_C;
    condition_lo: register_lo <= C;

    update_lo_1_predicate: condition_lo;
    // update_lo_1_value: register_lo + meta.delta_c;
    update_lo_1_value: register_lo + meta.delta_total_alpha;
    // update_lo_1_value: meta.delta_total_alpha;
    // TODO: CHANGE HERE MAYBE
    // update_lo_1_value: INIT_C;
}

blackbox stateful_alu update_total_alpha_to_maxalpha_alu {
    reg: total_alpha_reg;

    // update_lo_1_value: recirculate_hdr.total_alpha;

    // output_value: alu_lo;
    // output_dst: recirculate_hdr.total_alpha;
    condition_lo: recirculate_hdr.total_F > C;

    update_lo_1_predicate: not condition_lo;
    update_lo_1_value: register_lo + DELTA_C;
    // update_lo_1_value: register_lo + meta.delta_c;

    output_value: alu_lo;
    output_dst: recirculate_hdr.total_alpha;
}

blackbox stateful_alu update_per_tenant_alpha_by_F1_minus_alu {
    reg: alpha_reg;

    // condition_lo: register_lo < recirculate_hdr.total_alpha; 
    // condition_lo: register_lo > meta.delta_total_alpha;

    // update_lo_1_predicate: condition_lo;
    // update_lo_1_value: register_lo - meta.delta_total_alpha;
    update_lo_1_value: recirculate_hdr.per_tenant_alpha;
}

blackbox stateful_alu update_per_tenant_alpha_by_F1_plus_alu {
    reg: alpha_reg;

    // condition_lo: register_lo <= recirculate_hdr.total_alpha;
    condition_lo: register_lo + meta.delta_total_alpha <= C;

    update_lo_1_predicate: condition_lo;
    // update_lo_1_value: register_lo + meta.delta_c;
    update_lo_1_value: register_lo + meta.delta_total_alpha;
    // update_lo_1_value: register_lo + DELTA_C;
    update_lo_2_predicate: not condition_lo;
    update_lo_2_value: C;
}

blackbox stateful_alu update_total_alpha_by_F0_alu {
    reg: total_alpha_reg;

    condition_lo: recirculate_hdr.total_F > C;
    // condition_hi: register_lo > MINUS_meta.delta_c;

    // update_lo_1_predicate: condition_lo and condition_hi;
    update_lo_1_predicate: condition_lo;
    update_lo_1_value: recirculate_hdr.total_alpha;
    update_lo_2_predicate: not condition_lo;
    update_lo_2_value: register_lo + DELTA_C;
    // update_lo_2_value: register_lo + meta.delta_c;
    
    // update_lo_1_value: recirculate_hdr.total_alpha;
    output_value: alu_lo;
    output_dst: recirculate_hdr.total_alpha;
}

blackbox stateful_alu set_average_per_flow_rate_alu {
    reg: stored_per_flow_rate_reg;

    update_lo_1_value: info_hdr.label;
    update_hi_1_value: register_hi;
}

blackbox stateful_alu set_average_accepted_rate_alu {
    reg: stored_accepted_rate_reg;

    update_lo_1_value: recirculate_hdr.per_tenant_F;
    update_hi_1_value: register_hi;
}

blackbox stateful_alu set_average_aggregate_arrival_rate_alu {
    reg: stored_aggregate_arrival_rate_reg;

    // update_lo_1_value: info_hdr.alpha_times_15; // per_tenant_A
    update_lo_1_value: info_hdr.per_tenant_A;
    update_hi_1_value: register_hi;
}

blackbox stateful_alu set_average_total_aggregate_arrival_rate_alu {
    reg: total_stored_aggregate_arrival_rate_reg;

    // update_lo_1_value: info_hdr.label_times_randv; // total_A
    update_lo_1_value: info_hdr.total_A;
    update_hi_1_value: register_hi;
}

blackbox stateful_alu set_average_total_accepted_rate_alu {
    reg: total_stored_accepted_rate_reg;

    update_lo_1_value: recirculate_hdr.total_F; 
    update_hi_1_value: register_hi;
}

blackbox stateful_alu counter_alu {
    reg: counter_reg;

    condition_lo: register_lo == 24;

    update_lo_1_predicate: condition_lo;
    update_lo_1_value: 0;
    update_lo_2_predicate: not condition_lo;
    update_lo_2_value: register_lo + 1;

    output_value: alu_lo;
    output_dst: info_hdr.update_rate;
}

blackbox stateful_alu get_fraction_factor_alu {
    reg: fraction_factor_reg;

    output_value: register_lo;
    output_dst: meta.fraction_factor;
}

blackbox stateful_alu get_delta_c_alu {
    reg: delta_c_reg;

    output_value: register_lo;
    output_dst: meta.delta_c;
}

blackbox stateful_alu label_times_7_alu {
    reg: stored_per_flow_rate_reg;

    update_lo_2_value: math_unit;
    update_hi_1_value: info_hdr.label;

    // output_value: alu_lo;
    // output_dst: info_hdr.label;

    math_unit_input: info_hdr.label;
    math_unit_exponent_shift: 0;
    math_unit_exponent_invert: false;
    math_unit_output_scale: -3;
    math_unit_lookup_table: 111 104 96 89 81 74 66 59 52 44 37 29 22 14 7 0;
    // math_unit_output_scale: -4;
    // math_unit_lookup_table: 217 210 203 196 189 182 175 168 161 154 147 140 133 126 119 112 105 98 91 84 77 70 63 56 49 42 35 28 21 14 7 0;
}

blackbox stateful_alu aggregate_arrival_rate_times_7_alu {
    reg: stored_aggregate_arrival_rate_reg;

    update_lo_2_value: math_unit;
    update_hi_1_value: info_hdr.per_tenant_A;

    // output_value: alu_lo;
    // output_dst: info_hdr.per_tenant_A;

    math_unit_input: info_hdr.per_tenant_A;
    math_unit_exponent_shift: 0;
    math_unit_exponent_invert: false;
    math_unit_output_scale: -3;
    // math_unit_lookup_table: 105 98 91 84 77 70 63 56 49 42 35 28 21 14 7 0;
    math_unit_lookup_table: 111 104 96 89 81 74 66 59 52 44 37 29 22 14 7 0;
}

blackbox stateful_alu total_aggregate_arrival_rate_times_7_alu {
    reg: total_stored_aggregate_arrival_rate_reg;

    update_lo_2_value: math_unit;
    update_hi_1_value: info_hdr.total_A;

    // output_value: alu_lo;
    // output_dst: info_hdr.total_A;

    math_unit_input: info_hdr.total_A;
    math_unit_exponent_shift: 0;
    math_unit_exponent_invert: false;
    math_unit_output_scale: -3;
    // math_unit_lookup_table: 105 98 91 84 77 70 63 56 49 42 35 28 21 14 7 0;
    math_unit_lookup_table: 111 104 96 89 81 74 66 59 52 44 37 29 22 14 7 0;
}

blackbox stateful_alu accepted_rate_times_7_alu {
    reg: stored_accepted_rate_reg;

    update_lo_2_value: math_unit;
    update_hi_1_value: recirculate_hdr.per_tenant_F;

    // output_value: alu_lo;
    // output_dst: recirculate_hdr.per_tenant_F;

    math_unit_input: recirculate_hdr.per_tenant_F;
    math_unit_exponent_shift: 0;
    math_unit_exponent_invert: false;
    math_unit_output_scale: -3;
    // math_unit_lookup_table: 105 98 91 84 77 70 63 56 49 42 35 28 21 14 7 0;
    math_unit_lookup_table: 111 104 96 89 81 74 66 59 52 44 37 29 22 14 7 0;
}

blackbox stateful_alu total_accepted_rate_times_7_alu {
    reg: total_stored_accepted_rate_reg;

    update_lo_2_value: math_unit;
    update_hi_1_value: recirculate_hdr.total_F;

    // output_value: alu_lo;
    // output_dst: recirculate_hdr.total_F;

    math_unit_input: recirculate_hdr.total_F;
    math_unit_exponent_shift: 0;
    math_unit_exponent_invert: false;
    math_unit_output_scale: -3;
    // math_unit_lookup_table: 105 98 91 84 77 70 63 56 49 42 35 28 21 14 7 0;
    math_unit_lookup_table: 111 104 96 89 81 74 66 59 52 44 37 29 22 14 7 0;
}