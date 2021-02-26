import pd_base_tests
import pdb
import time
import sys

from collections import OrderedDict
from ptf import config
from ptf.testutils import *
from ptf.thriftutils import *

import os

from pal_rpc.ttypes import *

from hcsfq.p4_pd_rpc.ttypes import *
from mirror_pd_rpc.ttypes import *
from res_pd_rpc.ttypes import *

from tm_api_rpc.ttypes import *

from pkt_pd_rpc.ttypes import *

use_ecn = False

FRACTION_FACTOR = 11
# C=118000 / 32 * 100 * 10
C = 115200 * 100 * 10
# C = 100000 * 100 * 20
PER_TENANT_C = C / 32
DELTA_C= C / 500

# FRACTION_FACTOR = 12
# C=1151456
# DELTA_C= 6000

TCP_PORT = 8080
UDP_PORT = 9000
CSFQ_PORT = 8888
UDP_DSTPORT = 8888

# server_ips = [0x0a010002, 0x0a010001, 0x0a01000a, 0x0a01000b]
server_ips = [0x0a010002]

port_ip_dic = {188: 0x0a010001 , 184: 0x0a010002 , 180: 0x0a010003 , 176: 0x0a010004 ,
               172: 0x0a010005 , 168: 0x0a010006 , 164: 0x0a010007 , 160: 0x0a010008 ,
               156: 0x0a010009 , 152: 0x0a01000a , 148: 0x0a01000b , 144: 0x0a01000c}
# port_ip_dic = {188: 0x0a010001 , 184: 0x0a010002 , 180: 0x0a010003 , 176: 0x0a010004 ,
#                172: 0x0a010005 , 168: 0x0a010006 , 164: 0x0a010007 , 160: 0x0a010008 ,
#                156: 0x0a010009 , 152: 0x0a01000a , 148: 0x0a01000b}

dev_id = 0
if (test_param_get("arch") == "tofino") or (test_param_get("arch") == "Tofino"):
  print "TYPE Tofino"
  sys.stdout.flush()
  MIR_SESS_COUNT = 1024
  MAX_SID_NORM = 1015
  MAX_SID_COAL = 1023
  BASE_SID_NORM = 1
  BASE_SID_COAL = 1016
elif (test_param_get("arch") == "tofino2") or (test_param_get("arch") == "Tofino2"):
  print "TYPE Tofino2"
  sys.stdout.flush()
  MIR_SESS_COUNT = 256
  MAX_SID_NORM = 255
  MAX_SID_COAL = 255
  BASE_SID_NORM = 0
  BASE_SID_COAL = 0
else:
  print "TYPE NONE"
  print test_param_get("arch")
  sys.stdout.flush()

ports = [188]

mirror_ids = []

dev_tgt = DevTarget_t(0, hex_to_i16(0xFFFF))

def setup_random(seed_val=0):
    if 0 == seed_val:
        seed_val = int(time.time())
    print
    print "Seed is:", seed_val
    sys.stdout.flush()
    random.seed(seed_val)

def make_port(pipe, local_port):
    assert(pipe >= 0 and pipe < 4)
    assert(local_port >= 0 and local_port < 72)
    return (pipe << 7) | local_port

def port_to_pipe(port):
    local_port = port & 0x7F
    assert(local_port < 72)
    pipe = (port >> 7) & 0x3
    assert(port == ((pipe << 7) | local_port))
    return pipe

def port_to_pipe_local_port(port):
    return port & 0x7F

swports = []
swports_by_pipe = {}
for device, port, ifname in config["interfaces"]:
    if port == 0: continue
    if port == 64: continue
    pipe = port_to_pipe(port)
    print device, port, pipe, ifname
    print int(test_param_get('num_pipes'))
    if pipe not in swports_by_pipe:
        swports_by_pipe[pipe] = []
    if pipe in range(int(test_param_get('num_pipes'))):
        swports.append(port)
        swports.sort()
        swports_by_pipe[pipe].append(port)
        swports_by_pipe[pipe].sort()

if swports == []:
    for pipe in range(int(test_param_get('num_pipes'))):
        for port in range(1):
            swports.append( make_port(pipe,port) )
cpu_port = 64
#cpu_port = 192
print "Using ports:", swports
sys.stdout.flush()

def mirror_session(mir_type, mir_dir, sid, egr_port=0, egr_port_v=False,
                   egr_port_queue=0, packet_color=0, mcast_grp_a=0,
                   mcast_grp_a_v=False, mcast_grp_b=0, mcast_grp_b_v=False,
                   max_pkt_len=9216, level1_mcast_hash=0, level2_mcast_hash=0,
                   mcast_l1_xid=0, mcast_l2_xid=0, mcast_rid=0, cos=0, c2c=0, extract_len=0, timeout=0,
                   int_hdr=[], hdr_len=0):
    return MirrorSessionInfo_t(mir_type,
                             mir_dir,
                             sid,
                             egr_port,
                             egr_port_v,
                             egr_port_queue,
                             packet_color,
                             mcast_grp_a,
                             mcast_grp_a_v,
                             mcast_grp_b,
                             mcast_grp_b_v,
                             max_pkt_len,
                             level1_mcast_hash,
                             level2_mcast_hash,
                             mcast_l1_xid,
                             mcast_l2_xid,
                             mcast_rid,
                             cos,
                             c2c,
                             extract_len,
                             timeout,
                             int_hdr,
                             hdr_len)

class CSFQ_HDR(Packet):
    name = "CSFQ_HDR"
    fields_desc = [
        XByteField("recirc_flag", 0),
        XIntField("flow_id", 0),
        XIntField("label", 0)
    ]

def hcsfq_packet(pktlen=0,
            eth_dst='00:11:11:11:11:11',
            eth_src='00:22:22:22:22:22',
            ip_src='0.0.0.2',
            ip_dst='0.0.0.1',
            udp_sport=8000,
            udp_dport=CSFQ_PORT,
            recirc_flag=0,
            flow_id=0,
            label=0):
    udp_pkt = simple_udp_packet(pktlen=0,
                                eth_dst=eth_dst,
                                eth_src=eth_src,
                                ip_dst=ip_dst,
                                ip_src=ip_src,
                                udp_sport=udp_sport,
                                udp_dport=udp_dport)

    return udp_pkt / CSFQ_HDR(recirc_flag=recirc_flag, flow_id=flow_id, label=label)

def scapy_hcsfq_bindings():
    bind_layers(UDP, CSFQ_HDR, dport=CSFQ_PORT)

def receive_packet(test, port_id, template):
    dev, port = port_to_tuple(port_id)
    (rcv_device, rcv_port, rcv_pkt, pkt_time) = dp_poll(test, dev, port, timeout=2)
    nrcv = template.__class__(rcv_pkt)
    return nrcv

def print_packet(test, port_id, template):
    receive_packet(test, port_id, template).show2()

def addPorts(test):
    test.pal.pal_port_add_all(dev_id, pal_port_speed_t.BF_SPEED_40G, pal_fec_type_t.BF_FEC_TYP_NONE)
    test.pal.pal_port_enable_all(dev_id)
    ports_not_up = True
    print("Waiting for ports to come up...")
    sys.stdout.flush()
    num_tries = 12
    i = 0
    while ports_not_up:
        ports_not_up = False
        for p in swports:
            x = test.pal.pal_port_oper_status_get(dev_id, p)
            if x == pal_oper_status_t.BF_PORT_DOWN:
                ports_not_up = True
                print("  port", p, "is down")
                sys.stdout.flush()
                time.sleep(3)
                break
        i = i + 1
        if i >= num_tries:
            break
    assert ports_not_up == False
    print("All ports up.")
    sys.stdout.flush()
    return



def init_tables(test, sess_hdl, dev_tgt):
    test.entry_hdls_ipv4 = []
    test.entry_hdls_ipv4_2 = []
    test.entry_flowrate_shl_0_table = []
    test.entry_flowrate_shl_1_table = []
    test.entry_flowrate_shl_2_table = []
    test.entry_flowrate_shl_3_table = []
    test.entry_check_uncongest_state_table = []

    ipv4_table_address_list = [0x0a010001, 0x0a010002, 0x0a010003, 0x0a010004, 0x0a010005,
        0x0a010006, 0x0a010007, 0x0a010008, 0x0a010009, 0x0a01000a, 0x0a01000b, 0x0a01000c, 0x01010101]
    ipv4_table_port_list = [188, 184, 180, 176, 172, 168, 164, 160, 156, 152, 148, 144, 320]
    ethernet_set_mac_src = ["\xa8\x2b\xb5\xde\x92\x2e", 
                            "\xa8\x2b\xb5\xde\x92\x32",
                            "\xa8\x2b\xb5\xde\x92\x36",
                            "\xa8\x2b\xb5\xde\x92\x3a",
                            "\xa8\x2b\xb5\xde\x92\x3e",
                            "\xa8\x2b\xb5\xde\x92\x42",
                            "\xa8\x2b\xb5\xde\x92\x46",
                            "\xa8\x2b\xb5\xde\x92\x4a",
                            "\xa8\x2b\xb5\xde\x92\x4e",
                            "\xa8\x2b\xb5\xde\x92\x52",
                            "\xa8\x2b\xb5\xde\x92\x56",
                            "\xa8\x2b\xb5\xde\x92\x5a"]
    ethernet_set_mac_dst = ["\x3c\xfd\xfe\xab\xde\xd8",
                            "\x3c\xfd\xfe\xa6\xeb\x10",
                            "\x3c\xfd\xfe\xaa\x5d\x00",
                            "\x3c\xfd\xfe\xaa\x46\x68",
                            "\x3c\xfd\xfe\xab\xde\xf0",
                            "\x3c\xfd\xfe\xab\xdf\x90",
                            "\x3c\xfd\xfe\xab\xe0\x50",
                            "\x3c\xfd\xfe\xab\xd9\xf0",
                            "\xd0\x94\x66\x3b\x12\x37",
                            "\xd0\x94\x66\x84\x9f\x19",
                            "\xd0\x94\x66\x84\x9f\xa9",
                            "\xd0\x94\x66\x84\x54\x81"]
    fix_src_port = []
    for i in range(128):
        fix_src_port.append(9000 + i)
    udp_src_port_list = []
    for i in range(128):
        udp_src_port_list.append(UDP_DSTPORT + i)

    flow_id = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
    tenant_id = [1] * 100
    # tenant_id = [0, 1, 1, 2, 2, 2, 2, 2, 2, 9, 10, 11, 12, 13, 14, 15]

    # for i in range(len(ipv4_table_address_list)):
    #     match_spec = hcsfq_add_info_hdr_table_match_spec_t(ipv4_table_address_list[i], 6)
    #     action_spec = hcsfq_add_info_hdr_action_action_spec_t(flow_id[i+1], tenant_id[i+1])
    #     entry_hdl = test.client.add_info_hdr_table_table_add_with_add_info_hdr_action(sess_hdl, dev_tgt, match_spec, action_spec)
    #     match_spec = hcsfq_add_info_hdr_table_match_spec_t(ipv4_table_address_list[i], 17)
    #     action_spec = hcsfq_add_info_hdr_action_action_spec_t(flow_id[i+1], tenant_id[i+1])
    #     entry_hdl = test.client.add_info_hdr_table_table_add_with_add_info_hdr_action(sess_hdl, dev_tgt, match_spec, action_spec)

    tcp_port_list = []
    tcp_port = TCP_PORT
    for i in range(70):
        tcp_port_list.append(tcp_port)
        tcp_port += 1
    udp_port_list = []
    udp_port = UDP_PORT
    for i in range(70):
        udp_port_list.append(udp_port)
        udp_port += 1

    test.client.add_info_hdr_table_set_default_action_add_info_hdr_default_action(sess_hdl, dev_tgt)
    for i in range(len(ipv4_table_address_list)):
        if ipv4_table_address_list[i] in server_ips:
            continue
        for tcp_port in tcp_port_list:
            match_spec = hcsfq_add_info_hdr_table_match_spec_t(ipv4_table_address_list[i], tcp_port)
            flow_idx = tcp_port + 1 - TCP_PORT
            if flow_idx <= 8:
                tenant_idx = 1
            # elif flow_idx <= 24:
            #     tenant_idx = 2
            # elif flow_idx <= 28:
            #     tenant_idx = 3
            else:
                tenant_idx = 2
            tenant_idx = 1
            action_spec = hcsfq_add_info_hdr_action_action_spec_t(flow_idx, tenant_idx)
            entry_hdl = test.client.add_info_hdr_table_table_add_with_add_info_hdr_action(sess_hdl, dev_tgt, match_spec, action_spec)

    test.client.add_info_hdr_udp_table_set_default_action_add_info_hdr_udp_default_action(sess_hdl, dev_tgt)
    for i in range(len(ipv4_table_address_list)):
        if ipv4_table_address_list[i] in server_ips:
            continue
        for udp_port in udp_port_list:
            match_spec = hcsfq_add_info_hdr_udp_table_match_spec_t(ipv4_table_address_list[i], udp_port)
            flow_idx = udp_port + 1 - UDP_PORT
            # if flow_idx <= 24:
            #     tenant_idx = 1
            # # elif flow_idx <= 24:
            # #     tenant_idx = 2
            # # elif flow_idx <= 28:
            # #     tenant_idx = 3
            # else:
            #     tenant_idx = 2
            tenant_idx = 1
            action_spec = hcsfq_add_info_hdr_udp_action_action_spec_t(flow_idx, tenant_idx)
            entry_hdl = test.client.add_info_hdr_udp_table_table_add_with_add_info_hdr_udp_action(sess_hdl, dev_tgt, match_spec, action_spec)


    weights = [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 13]
    for i in range(len(ipv4_table_address_list)):
        match_spec = hcsfq_get_half_pktlen_table_match_spec_t(ipv4_table_address_list[i])
        if ipv4_table_address_list[i] < 0x0a010001:
            break
        weight_index = ipv4_table_address_list[i] - 0x0a010001
        if weights[weight_index] == 1:
            test.client.get_half_pktlen_table_table_add_with_get_half_pktlen_action(sess_hdl, dev_tgt, match_spec)
        elif weights[weight_index] == 2:
            test.client.get_half_pktlen_table_table_add_with_get_half_pktlen_w2_action(sess_hdl, dev_tgt, match_spec)
        elif weights[weight_index] == 4:
            test.client.get_half_pktlen_table_table_add_with_get_half_pktlen_w4_action(sess_hdl, dev_tgt, match_spec)

    # add entries for ipv4 routing
    # test.client.ipv4_route_set_default_action__drop(sess_hdl, dev_tgt)
    # for i in range(len(ipv4_table_address_list)):
    #     match_spec = hcsfq_ipv4_route_match_spec_t(ipv4_table_address_list[i])
    #     action_spec = hcsfq_set_egress_action_spec_t(ipv4_table_port_list[i])
    #     entry_hdl = test.client.ipv4_route_table_add_with_set_egress(
    #         sess_hdl, dev_tgt, match_spec, action_spec)
    #     test.entry_hdls_ipv4.append(entry_hdl)

    match_spec = hcsfq_get_34_alpha_table_match_spec_t(1)
    test.client.get_34_alpha_table_table_add_with_get_34_alpha_w2_action(sess_hdl, dev_tgt, match_spec)
    match_spec = hcsfq_get_34_alpha_table_match_spec_t(0)
    test.client.get_34_alpha_table_table_add_with_get_34_alpha_action(sess_hdl, dev_tgt, match_spec)


    test.client.ipv4_route_2_set_default_action__drop(sess_hdl, dev_tgt)
    for i in range(len(ipv4_table_address_list)):
        match_spec = hcsfq_ipv4_route_2_match_spec_t(ipv4_table_address_list[i])
        action_spec = hcsfq_set_egress_action_spec_t(ipv4_table_port_list[i])
        entry_hdl = test.client.ipv4_route_2_table_add_with_set_egress(
            sess_hdl, dev_tgt, match_spec, action_spec)
        test.entry_hdls_ipv4_2.append(entry_hdl)

    test.client.ipv4_route_3_set_default_action__drop(sess_hdl, dev_tgt)
    for i in range(len(ipv4_table_address_list)):
        match_spec = hcsfq_ipv4_route_3_match_spec_t(ipv4_table_address_list[i], 6)
        action_spec = hcsfq_set_egress_3_action_spec_t(ipv4_table_port_list[i])
        entry_hdl = test.client.ipv4_route_3_table_add_with_set_egress_3(
            sess_hdl, dev_tgt, match_spec, action_spec)
        test.entry_hdls_ipv4_2.append(entry_hdl)

        match_spec = hcsfq_ipv4_route_3_match_spec_t(ipv4_table_address_list[i], 17)
        action_spec = hcsfq_set_egress_3_udp_action_spec_t(ipv4_table_port_list[i])
        entry_hdl = test.client.ipv4_route_3_table_add_with_set_egress_3_udp(
            sess_hdl, dev_tgt, match_spec, action_spec)
        test.entry_hdls_ipv4_2.append(entry_hdl)

    match_spec = hcsfq_set_flag_table_match_spec_t(6)
    entry_hdl = test.client.set_flag_table_table_add_with_set_tcp_flag_action(
            sess_hdl, dev_tgt, match_spec)
    match_spec = hcsfq_set_flag_table_match_spec_t(17)
    entry_hdl = test.client.set_flag_table_table_add_with_set_udp_flag_action(
            sess_hdl, dev_tgt, match_spec)


    priority_0 = 1
    # match_spec = hcsfq_<tablename>_match_spec_t(1, 1)
    # entry_hdl = test.client.<table_name>_table_add_with_<action_name>(sess_hdl, dev_tgt, match_spec, priority_0)

    ## flowrate_sum_01_table
    match_spec = hcsfq_flowrate_sum_01_table_match_spec_t(0, 3)
    entry_hdl = test.client.flowrate_sum_01_table_table_add_with_flowrate_sum_01_none_action(sess_hdl, dev_tgt, match_spec, priority_0)
    match_spec = hcsfq_flowrate_sum_01_table_match_spec_t(1, 3)
    entry_hdl = test.client.flowrate_sum_01_table_table_add_with_flowrate_sum_01_0_action(sess_hdl, dev_tgt, match_spec, priority_0)
    match_spec = hcsfq_flowrate_sum_01_table_match_spec_t(2, 3)
    entry_hdl = test.client.flowrate_sum_01_table_table_add_with_flowrate_sum_01_1_action(sess_hdl, dev_tgt, match_spec, priority_0)
    match_spec = hcsfq_flowrate_sum_01_table_match_spec_t(3, 3)
    entry_hdl = test.client.flowrate_sum_01_table_table_add_with_flowrate_sum_01_01_action(sess_hdl, dev_tgt, match_spec, priority_0)

    ## flowrate_sum_23_table
    match_spec = hcsfq_flowrate_sum_23_table_match_spec_t(0, 12)
    entry_hdl = test.client.flowrate_sum_23_table_table_add_with_flowrate_sum_23_none_action(sess_hdl, dev_tgt, match_spec, priority_0)
    match_spec = hcsfq_flowrate_sum_23_table_match_spec_t(4, 12)
    entry_hdl = test.client.flowrate_sum_23_table_table_add_with_flowrate_sum_23_2_action(sess_hdl, dev_tgt, match_spec, priority_0)
    match_spec = hcsfq_flowrate_sum_23_table_match_spec_t(8, 12)
    entry_hdl = test.client.flowrate_sum_23_table_table_add_with_flowrate_sum_23_3_action(sess_hdl, dev_tgt, match_spec, priority_0)
    match_spec = hcsfq_flowrate_sum_23_table_match_spec_t(12, 12)
    entry_hdl = test.client.flowrate_sum_23_table_table_add_with_flowrate_sum_23_23_action(sess_hdl, dev_tgt, match_spec, priority_0)

    ## check_uncongest_state_table
    match_spec_1 = hcsfq_check_uncongest_state_table_match_spec_t(1)
    match_spec_2 = hcsfq_check_uncongest_state_table_match_spec_t(2)
    match_spec_4 = hcsfq_check_uncongest_state_table_match_spec_t(4)
    match_spec_8 = hcsfq_check_uncongest_state_table_match_spec_t(8)
    entry_hdl_1 = test.client.check_uncongest_state_table_table_add_with_check_uncongest_state_0_action(sess_hdl, dev_tgt, match_spec_1)
    entry_hdl_2 = test.client.check_uncongest_state_table_table_add_with_check_uncongest_state_23_action(sess_hdl, dev_tgt, match_spec_4)
    entry_hdl_4 = test.client.check_uncongest_state_table_table_add_with_check_uncongest_state_1_action(sess_hdl, dev_tgt, match_spec_2)
    entry_hdl_8 = test.client.check_uncongest_state_table_table_add_with_check_uncongest_state_23_action(sess_hdl, dev_tgt, match_spec_8)

    test.client.get_14_alpha_table_set_default_action_get_14_alpha_action(sess_hdl, dev_tgt)
    match_spec_0 = hcsfq_get_14_alpha_table_match_spec_t(0)
    match_spec_2 = hcsfq_get_14_alpha_table_match_spec_t(2)
    match_spec_3 = hcsfq_get_14_alpha_table_match_spec_t(3)
    match_spec_4 = hcsfq_get_14_alpha_table_match_spec_t(4)
    match_spec_5 = hcsfq_get_14_alpha_table_match_spec_t(5)
    match_spec_6 = hcsfq_get_14_alpha_table_match_spec_t(6)
    match_spec_7 = hcsfq_get_14_alpha_table_match_spec_t(7)
    match_spec_8 = hcsfq_get_14_alpha_table_match_spec_t(8)
    match_spec_9 = hcsfq_get_14_alpha_table_match_spec_t(9)
    match_spec_10 = hcsfq_get_14_alpha_table_match_spec_t(10)
    match_spec_11 = hcsfq_get_14_alpha_table_match_spec_t(11)
    match_spec_12 = hcsfq_get_14_alpha_table_match_spec_t(12)
    match_spec_13 = hcsfq_get_14_alpha_table_match_spec_t(13)
    match_spec_14 = hcsfq_get_14_alpha_table_match_spec_t(14)
    match_spec_15 = hcsfq_get_14_alpha_table_match_spec_t(15)
    match_spec_16 = hcsfq_get_14_alpha_table_match_spec_t(16)
    match_spec_17 = hcsfq_get_14_alpha_table_match_spec_t(17)
    match_spec_18 = hcsfq_get_14_alpha_table_match_spec_t(18)
    match_spec_19 = hcsfq_get_14_alpha_table_match_spec_t(19)
    match_spec_20 = hcsfq_get_14_alpha_table_match_spec_t(20)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_action(sess_hdl, dev_tgt, match_spec_0)

    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_2_action(sess_hdl, dev_tgt, match_spec_2)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_3_action(sess_hdl, dev_tgt, match_spec_3)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_4_action(sess_hdl, dev_tgt, match_spec_4)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_5_action(sess_hdl, dev_tgt, match_spec_5)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_6_action(sess_hdl, dev_tgt, match_spec_6)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_7_action(sess_hdl, dev_tgt, match_spec_7)

    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_8_action(sess_hdl, dev_tgt, match_spec_8)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_9_action(sess_hdl, dev_tgt, match_spec_9)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_10_action(sess_hdl, dev_tgt, match_spec_10)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_11_action(sess_hdl, dev_tgt, match_spec_11)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_12_action(sess_hdl, dev_tgt, match_spec_12)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_13_action(sess_hdl, dev_tgt, match_spec_13)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_14_action(sess_hdl, dev_tgt, match_spec_14)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_15_action(sess_hdl, dev_tgt, match_spec_15)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_16_action(sess_hdl, dev_tgt, match_spec_16)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_17_action(sess_hdl, dev_tgt, match_spec_17)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_18_action(sess_hdl, dev_tgt, match_spec_18)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_19_action(sess_hdl, dev_tgt, match_spec_19)
    entry_hdl = test.client.get_14_alpha_table_table_add_with_get_14_alpha_20_action(sess_hdl, dev_tgt, match_spec_20)

    ## check_total_uncongest_state_table
    match_spec_1 = hcsfq_check_total_uncongest_state_table_match_spec_t(1)
    match_spec_2 = hcsfq_check_total_uncongest_state_table_match_spec_t(2)
    match_spec_4 = hcsfq_check_total_uncongest_state_table_match_spec_t(4)
    match_spec_8 = hcsfq_check_total_uncongest_state_table_match_spec_t(8)
    entry_hdl_1 = test.client.check_total_uncongest_state_table_table_add_with_check_total_uncongest_state_0_action(sess_hdl, dev_tgt, match_spec_1)
    entry_hdl_2 = test.client.check_total_uncongest_state_table_table_add_with_check_total_uncongest_state_23_action(sess_hdl, dev_tgt, match_spec_4)
    entry_hdl_4 = test.client.check_total_uncongest_state_table_table_add_with_check_total_uncongest_state_1_action(sess_hdl, dev_tgt, match_spec_2)
    entry_hdl_8 = test.client.check_total_uncongest_state_table_table_add_with_check_total_uncongest_state_23_action(sess_hdl, dev_tgt, match_spec_8)

    if use_ecn:
        # match_spec_0 = hcsfq_set_ecn_table_match_spec_t(0, 6)
        # match_spec_1 = hcsfq_set_ecn_table_match_spec_t(1, 6)
        # # test.client.set_ecn_table_table_add_with_set_ecn_not_congest_action(sess_hdl, dev_tgt, match_spec_0)
        # test.client.set_ecn_table_table_add_with__no_op(sess_hdl, dev_tgt, match_spec_0)
        # test.client.set_ecn_table_table_add_with_set_ecn_congest_action(sess_hdl, dev_tgt, match_spec_1)
        
        match_spec_0 = hcsfq_set_ecn_2_table_match_spec_t(0, 6)
        match_spec_1 = hcsfq_set_ecn_2_table_match_spec_t(1, 6)
        # test.client.set_ecn_2_table_table_add_with_set_ecn_not_congest_action(sess_hdl, dev_tgt, match_spec_0)
        test.client.set_ecn_2_table_table_add_with__no_op(sess_hdl, dev_tgt, match_spec_0)
        test.client.set_ecn_2_table_table_add_with_set_ecn_congest_action(sess_hdl, dev_tgt, match_spec_1)

    match_spec_0 = hcsfq_set_ecn_byq_table_match_spec_t(6)
    # match_spec_1 = hcsfq_set_ecn_flag_table_match_spec_t(6)
    test.client.set_ecn_byq_table_table_add_with_set_ecn_byq_action(sess_hdl, dev_tgt, match_spec_0)
    # test.client.set_ecn_2_table_table_add_with_set_ecn_congest_action(sess_hdl, dev_tgt, match_spec_1)
    test.client.set_ecn_byq_table_set_default_action__no_op(sess_hdl, dev_tgt)

    ## update_total_alpha_table
    # test.client.update_total_alpha_table_set_default_action__no_op(sess_hdl, dev_tgt)
    # match_list = [16, 18, 25, 27, 24, 26]
    # for i in match_list:
    #     match_spec_1 = hcsfq_update_total_alpha_table_match_spec_t(i)
    #     entry_hdl_1 = test.client.update_total_alpha_table_table_add_with_update_total_alpha_to_maxalpha_action(sess_hdl, dev_tgt, match_spec_1)
    # match_list = [20, 22, 29, 31, 28, 30]
    # for i in match_list:
    #     match_spec_1 = hcsfq_update_total_alpha_table_match_spec_t(i)
    #     entry_hdl_1 = test.client.update_total_alpha_table_table_add_with_update_total_alpha_by_F0_action(sess_hdl, dev_tgt, match_spec_1)

    ## update_per_tenant_alpha_table
    test.client.update_per_tenant_alpha_table_set_default_action__no_op(sess_hdl, dev_tgt)
    match_list = [8, 10, 28, 30, 24, 26]
    for i in match_list:
        match_spec_1 = hcsfq_update_per_tenant_alpha_table_match_spec_t(i, 0)
        entry_hdl_1 = test.client.update_per_tenant_alpha_table_table_add_with_update_per_tenant_alpha_to_maxalpha_action(sess_hdl, dev_tgt, match_spec_1)
        match_spec_1 = hcsfq_update_per_tenant_alpha_table_match_spec_t(i, 1)
        entry_hdl_1 = test.client.update_per_tenant_alpha_table_table_add_with_update_per_tenant_alpha_to_maxalpha_action(sess_hdl, dev_tgt, match_spec_1)
    match_list = [9, 11, 29, 31, 25, 27]
    for i in match_list:
        match_spec_1 = hcsfq_update_per_tenant_alpha_table_match_spec_t(i, 0)
        entry_hdl_1 = test.client.update_per_tenant_alpha_table_table_add_with_update_per_tenant_alpha_by_F1_minus_action(sess_hdl, dev_tgt, match_spec_1)
        match_spec_2 = hcsfq_update_per_tenant_alpha_table_match_spec_t(i, 1)
        entry_hdl_2 = test.client.update_per_tenant_alpha_table_table_add_with_update_per_tenant_alpha_by_F1_plus_action(sess_hdl, dev_tgt, match_spec_2)

    # test.client.get_time_stamp_table_set_default_action_get_time_stamp_action(sess_hdl, dev_tgt)
    # test.client.get_flow_id_table_set_default_action_get_flow_id_action(sess_hdl, dev_tgt)
    # test.client.get_random_value_table_set_default_action_get_random_value_action(sess_hdl, dev_tgt)
    # # test.client.get_random_value_2_table_set_default_action_get_random_value_action(sess_hdl, dev_tgt)
    # test.client.estimate_aggregate_arrival_rate_table_set_default_action_estimate_aggregate_arrival_rate_action(sess_hdl, dev_tgt)
    # test.client.estimate_total_aggregate_arrival_rate_table_set_default_action_estimate_total_aggregate_arrival_rate_action(sess_hdl, dev_tgt)
    # test.client.get_aggregate_arrival_rate_table_set_default_action_get_aggregate_arrival_rate_action(sess_hdl, dev_tgt)
    # test.client.get_total_aggregate_arrival_rate_table_set_default_action_get_total_aggregate_arrival_rate_action(sess_hdl, dev_tgt)
    # test.client.get_minv_0_table_set_default_action_get_minv_0_action(sess_hdl, dev_tgt)
    # test.client.get_alpha_table_set_default_action_get_alpha_action(sess_hdl, dev_tgt)
    # test.client.get_total_alpha_table_set_default_action_get_total_alpha_action(sess_hdl, dev_tgt)
    # test.client.alpha_shl_4_table_set_default_action_alpha_shl_4_action(sess_hdl, dev_tgt)
    # test.client.alpha_times_15_table_set_default_action_alpha_times_15_action(sess_hdl, dev_tgt)
    # test.client.flowrate_times_randv_table_set_default_action_flowrate_times_randv_action(sess_hdl, dev_tgt)
    # test.client.get_minv_table_set_default_action_get_minv_action(sess_hdl, dev_tgt)
    # test.client.set_drop_table_set_default_action_set_drop_action(sess_hdl, dev_tgt)
    # test.client.estimate_accepted_rate_table_set_default_action_estimate_accepted_rate_action(sess_hdl, dev_tgt)
    # test.client.estimate_accepted_rate_2_table_set_default_action_estimate_accepted_rate_2_action(sess_hdl, dev_tgt)
    # test.client.maintain_congest_state_table_set_default_action_maintain_congest_state_action(sess_hdl, dev_tgt)
    # test.client.maintain_total_congest_state_table_set_default_action_maintain_total_congest_state_action(sess_hdl, dev_tgt)
    # test.client.maintain_uncongest_state_table_set_default_action_maintain_uncongest_state_action(sess_hdl, dev_tgt)
    # test.client.maintain_total_uncongest_state_table_set_default_action_maintain_total_uncongest_state_action(sess_hdl, dev_tgt)
    # test.client.get_accepted_rate_table_set_default_action_get_accepted_rate_action(sess_hdl, dev_tgt)
    # test.client.estimate_total_accepted_rate_table_set_default_action_estimate_total_accepted_rate_action(sess_hdl, dev_tgt)
    # test.client.estimate_total_accepted_rate_2_table_set_default_action_estimate_total_accepted_rate_2_action(sess_hdl, dev_tgt)
    # test.client.get_total_accepted_rate_table_set_default_action_get_total_accepted_rate_action(sess_hdl, dev_tgt)
    # test.client.get_min_of_pertenantF_total_alpha_table_set_default_action_get_min_of_pertenantF_total_alpha_action(sess_hdl, dev_tgt)
    # test.client.set_pertenantF_leq_totalalpha_table_set_default_action_set_pertenantF_leq_totalalpha_action(sess_hdl, dev_tgt)
    # test.client.mod_resubmit_field_table_set_default_action_mod_resubmit_field_action(sess_hdl, dev_tgt)
    # test.client.estimate_per_flow_rate_table_set_default_action_estimate_per_flow_rate_action(sess_hdl, dev_tgt)
    # test.client.get_per_flow_rate_table_set_default_action_get_per_flow_rate_action(sess_hdl, dev_tgt)
    # test.client.put_into_infohdr_table_set_default_action_put_into_infohdr_action(sess_hdl, dev_tgt)
    # test.client.resubmit_2_table_set_default_action_resubmit_2_action(sess_hdl, dev_tgt)
    

    test.client.register_write_fraction_factor_reg(sess_hdl, dev_tgt, 0, FRACTION_FACTOR)
    test.client.register_write_delta_c_reg(sess_hdl, dev_tgt, 0, DELTA_C)

    test.client.register_write_tmp_total_alpha_reg(sess_hdl, dev_tgt, 0, C)
    test.client.register_write_total_alpha_reg(sess_hdl, dev_tgt, 0, C)
    for i in range(11):
        test.client.register_write_alpha_reg(sess_hdl, dev_tgt, i, PER_TENANT_C)
        test.client.register_write_tmp_alpha_reg(sess_hdl, dev_tgt, i, PER_TENANT_C)

    # per_flow_rate = hcsfq_stored_per_flow_rate_reg_value_t(C, C)
    # accepted_rate = hcsfq_stored_accepted_rate_reg_value_t(C, C)
    # aggregate_rate = hcsfq_stored_aggregate_arrival_rate_reg_value_t(C, C)
    # total_aggregate_rate = hcsfq_total_stored_aggregate_arrival_rate_reg_value_t(C, C)
    # total_accepted_rate = hcsfq_total_stored_accepted_rate_reg_value_t(C, C)
    # test.client.register_write_stored_aggregate_arrival_rate_reg(sess_hdl, dev_tgt, 0, total_aggregate_rate)
    # test.client.register_write_stored_accepted_rate_reg(sess_hdl, dev_tgt, 0, total_accepted_rate)
    # for i in range(11):
    #     test.client.register_write_stored_per_flow_rate_reg(sess_hdl, dev_tgt, i, per_flow_rate)
    #     test.client.register_write_stored_accepted_rate_reg(sess_hdl, dev_tgt, i, accepted_rate)
    #     test.client.register_write_stored_aggregate_arrival_rate_reg(sess_hdl, dev_tgt, i, aggregate_rate)

    # test.client.register_write_tmp_total_alpha_reg(sess_hdl, dev_tgt, 0, 114374)
    # test.client.register_write_total_alpha_reg(sess_hdl, dev_tgt, 0, 114374)
    # for i in range(11):
    #     test.client.register_write_alpha_reg(sess_hdl, dev_tgt, i, 214374)
    #     test.client.register_write_tmp_alpha_reg(sess_hdl, dev_tgt, i, 214374)

    # test.client.register_write_tmp_total_alpha_reg(sess_hdl, dev_tgt, 0, 5737 * 2)
    # test.client.register_write_total_alpha_reg(sess_hdl, dev_tgt, 0, 5737 * 2)
    # for i in range(11):
    #     test.client.register_write_alpha_reg(sess_hdl, dev_tgt, i, 11437 * 2 * 2)
    #     test.client.register_write_tmp_alpha_reg(sess_hdl, dev_tgt, i, 11437 * 2 * 2)

def clean_tables(test, sess_hdl, dev_id):
    print "closing session"
    # status = test.conn_mgr.client_cleanup(sess_hdl)


class SimpleTest(pd_base_tests.ThriftInterfaceDataPlane):
    def __init__(self):
        pd_base_tests.ThriftInterfaceDataPlane.__init__(self, ["hcsfq"])
        scapy_hcsfq_bindings()

    def runTest(self):
        print "========== acquire lock test =========="
        sess_hdl = self.conn_mgr.client_init()
        self.sids = []
        print("start")
        # self.tm.tm_port_ingress_drop_limit_set(dev_tgt, 33, 0x1234)
        # qmap = {}
        qmap = tm_q_map_t(md_qid0_to_tm_q=0, md_qid1_to_tm_q=0, md_qid2_to_tm_q=0, md_qid3_to_tm_q=0, md_qid4_to_tm_q=0, md_qid5_to_tm_q=0, md_qid6_to_tm_q=0, md_qid7_to_tm_q=0, md_qid8_to_tm_q=0, md_qid9_to_tm_q=0, md_qid10_to_tm_q=0, md_qid11_to_tm_q=0, md_qid12_to_tm_q=0, md_qid13_to_tm_q=0, md_qid14_to_tm_q=0, md_qid15_to_tm_q=0, md_qid16_to_tm_q=0, md_qid17_to_tm_q=0, md_qid18_to_tm_q=0, md_qid19_to_tm_q=0, md_qid20_to_tm_q=0, md_qid21_to_tm_q=0, md_qid22_to_tm_q=0, md_qid23_to_tm_q=0, md_qid24_to_tm_q=0, md_qid25_to_tm_q=0, md_qid26_to_tm_q=0, md_qid27_to_tm_q=0, md_qid28_to_tm_q=0, md_qid29_to_tm_q=0, md_qid30_to_tm_q=0, md_qid31_to_tm_q=0, q_count=1)
        
        self.tm.tm_set_port_q_mapping(dev_id, 184, 1, qmap)

        print("done")
        try:
            if (test_param_get('target') == 'hw'):
                addPorts(self)
            else:
                print "test_param_get(target):", test_param_get('target')

            sids = random.sample(xrange(BASE_SID_NORM, MAX_SID_NORM), len(swports))
            
            for port,sid in zip(swports[0:len(swports)], sids[0:len(sids)]):
                ip_address = port_ip_dic[port]
                match_spec = hcsfq_i2e_mirror_table_match_spec_t(ip_address)                
                action_spec = hcsfq_i2e_mirror_action_action_spec_t(sid)
                result = self.client.i2e_mirror_table_table_add_with_i2e_mirror_action(sess_hdl,
                 dev_tgt, match_spec, action_spec)
                info = mirror_session(MirrorType_e.PD_MIRROR_TYPE_NORM,
                                  Direction_e.PD_DIR_INGRESS,
                                  sid,
                                  port,
                                  True)
                print(self.tm.tm_get_port_q_mapping(dev_id, port))
                print "port:", port, "; sid:", sid 
                sys.stdout.flush()
                self.mirror.mirror_session_create(sess_hdl, dev_tgt, info)
                self.sids.append(sid)
            
            self.conn_mgr.complete_operations(sess_hdl)
            for sid in self.sids:
                self.mirror.mirror_session_enable(sess_hdl, Direction_e.PD_DIR_INGRESS, dev_tgt, sid)
            self.conn_mgr.complete_operations(sess_hdl)
            init_tables(self, sess_hdl, dev_tgt)
            self.conn_mgr.complete_operations(sess_hdl)
            print "INIT Finished."
            sys.stdout.flush()

            while (True):
                time.sleep(1)
            self.conn_mgr.complete_operations(sess_hdl)
        finally:
            for sid in self.sids:
                self.mirror.mirror_session_disable(sess_hdl, Direction_e.PD_DIR_INGRESS, dev_tgt, sid)
            for sid in self.sids:
                self.mirror.mirror_session_delete(sess_hdl, dev_tgt, sid)
            clean_tables(self, sess_hdl, dev_id)