header_type ethernet_t {
    fields {
        // ** 00:00:00:00:00:0a > 00:00:00:57:bf:36
        // ** Saocaozuo TODO check correctness
        // ** store flow rate in dstAddr_upper
        // ** store recirc_flag in dstAddr_lower
        dstAddr_lower: 16;
        dstAddr_upper: 32;
        // dstAddr:    48;
        // ** store flow_id in srcAddr_upper
        // ** store tenant_id in srcAddr_lower
        srcAddr_lower: 16;
        srcAddr_upper: 32;
        // srcAddr:    48;
        etherType:  16;
    }
}
header ethernet_t ethernet;

header_type ipv4_t {
    fields {
        version:        4;
        ihl:            4;
        diffserv:       6;
        ecn_flag:       2;
        totalLen:       16;
        identification: 16;
        flags:          3;
        fragOffset:     13;
        ttl:            8;
        protocol:       8;
        hdrChecksum:    16;
        srcAddr:        32;
        dstAddr:        32;
    }
}
header ipv4_t ipv4;

header_type tcp_t {
    fields {
        srcPort:    16;
        dstPort:    16;
        seqNo:      32;
        ackNo:      32;
        dataOffset: 4;
        res:        3;
        ecn:        3;
        // ctrl:       6;
        urg:        1;
        ack:        1;
        psh:        1;
        rst:        1;
        syn:        1;
        fin:        1;
        window:     16;
        checksum:   16;
        urgentPtr:  16;
    }
}
header tcp_t tcp;

header_type udp_t {
    fields {
        srcPort:    16;
        dstPort:    16;
        // dstPort_1: 1;
        // dstPort_2: 15;
        pkt_length: 16;
        checksum:   16;
    }
}
header udp_t udp;

header_type recirculate_hdr_t {
    fields {
        congested: 8;
        to_drop: 8;
        pertenantF_leq_totalalpha: 8;
        per_tenant_F: 32;
        total_F: 32;
        per_tenant_alpha: 32;
        total_alpha: 32;
    }
}
header recirculate_hdr_t recirculate_hdr;

header_type info_hdr_t {
    fields {
        tsp: 32;
        label: 32;
        per_tenant_A: 32;
        total_A: 32;
        flow_id: 16;
        tenant_id: 16;
        recirc_flag: 1;
        update_alpha: 1;
        update_total_alpha: 1;
        update_rate: 8;
        label_smaller_than_alpha: 1;
    }
}
header info_hdr_t info_hdr;