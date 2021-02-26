action set_egress(egress_spec) {
    // remove_header(recirculate_hdr);
    // remove_header(info_hdr);
    // modify_field(tcp.res, 0);
    modify_field(ig_intr_md_for_tm.ucast_egress_port, egress_spec);
    add_to_field(ipv4.ttl, -1);
}

action set_egress_3(egress_spec) {
    remove_header(recirculate_hdr);
    remove_header(info_hdr);
    // modify_field(tcp.res, 0);
    modify_field(ig_intr_md_for_tm.ucast_egress_port, egress_spec);
    add_to_field(ipv4.ttl, -1);
}

action set_egress_3_udp(egress_spec) {
    remove_header(recirculate_hdr);
    remove_header(info_hdr);
    modify_field(ig_intr_md_for_tm.ucast_egress_port, egress_spec);
    add_to_field(ipv4.ttl, -1);
}

// @pragma stage 11
table ipv4_route {
    reads {
        ipv4.dstAddr : exact;
    }
    actions {
        // resubmit_action;
        set_egress;
        _drop;
    }
    size : 8192;
}

@pragma stage 10
table ipv4_route_2 {
    reads {
        ipv4.dstAddr : exact;
    }
    actions {
        // resubmit_action;
        set_egress;
        _drop;
    }
    size : 8192;
}

@pragma stage 11
table ipv4_route_3 {
    reads {
        ipv4.dstAddr : exact;
        ipv4.protocol: exact;
    }
    actions {
        // resubmit_action;
        set_egress_3_udp;
        set_egress_3;
        _drop;
    }
    size : 8192;
}

action ethernet_set_mac_act (smac, dmac) {
    // ** nop here
    _no_op();
    // modify_field (ethernet.srcAddr, smac);
    // modify_field (ethernet.dstAddr, dmac);
}

table ethernet_set_mac {
    reads {
        ig_intr_md_for_tm.ucast_egress_port: exact;
    }
    actions {
        ethernet_set_mac_act;
        _no_op;
    }
}

action _no_op() {
    no_op();
}

action _drop() {
    drop();
}