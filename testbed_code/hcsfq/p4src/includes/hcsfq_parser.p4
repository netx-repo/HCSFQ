parser start {
    return parse_ethernet;
}

parser parse_ethernet {
    extract (ethernet);
    return select (latest.etherType) {
        0x0800:     parse_ipv4;
        default:    ingress;
    }
}

parser parse_ipv4 {
    extract (ipv4);
    return select (latest.protocol) {
        6:          parse_tcp;
        17:         parse_udp;
        default:    ingress;
    }
}

field_list ipv4_field_list {
    ipv4.version;
    ipv4.ihl;
    ipv4.diffserv;
    ipv4.ecn_flag;
    ipv4.totalLen;
    ipv4.identification;
    ipv4.flags;
    ipv4.fragOffset;
    ipv4.ttl;
    ipv4.protocol;
    ipv4.srcAddr;
    ipv4.dstAddr;
}

field_list_calculation ipv4_chksum_calc {
    input {
        ipv4_field_list;
    }
    algorithm: csum16;
    output_width: 16;
}

calculated_field ipv4.hdrChecksum {
    update ipv4_chksum_calc;
}

parser parse_tcp {
    extract (tcp);
    return select (latest.res) {
        RATE_ESTIMATED: parse_info_hdr;
        default: ingress;
    }
}

parser parse_udp {
    extract (udp);
    // return select (latest.dstPort_1) {
    //     RECIRCULATED: parse_info_hdr;
    //     default: ingress;
    // }
    return select (latest.dstPort) {
        CSFQ_PORT: parse_info_hdr;
        default: ingress;
    }
}

parser parse_recirculate_hdr {
    extract (recirculate_hdr);
    return ingress;
}

parser parse_info_hdr {
    extract (info_hdr);
    return parse_recirculate_hdr;
}
