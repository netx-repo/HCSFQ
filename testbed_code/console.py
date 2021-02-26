import sys, os, time, subprocess, random
import paramiko
import threading
from config import *
from multiprocessing import Process

WPKTS_SEND_LIMIT_MS = 200
thread_per_client = 8
udp_thread_per_client = 8
divs = [0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1]
tenant_group = [0, 1,1,1,1, 1,1,1,1, 1,1,1,1]
def prRed(skk): print("\033[91m {}\033[00m" .format(skk))

dstIP_list = ["None", "10.1.0.6", "10.1.0.6", "10.1.0.6", "10.1.0.6",
            "10.1.0.6", "10.1.0.6", "10.1.0.8", "10.1.0.8", "10.1.0.9", "10.1.0.9", "10.1.0.12", "10.1.0.12"]
dstPort_list = ["0", "8080", "8081", "8082", "8083", 
            "8084", "8085", "8080", "8080", "8080", "8080", "8080", "8080"]
srcPort_list = ["0", "8080", "8080", "8080", "8080", 
            "8080", "8080", "8080", "8080", "8080", "8080", "8080", "8080"]

def dstIP(client_id):
    return dstIP_list[int(client_id)]

def dstPort(client_id):
    return dstPort_list[int(client_id)]

def srcPort(server_id):
    return srcPort_list[int(server_id)]

class CSFQConsole(object):
    def to_hostname(self, client_name):
        return id_to_hostname_dict[client_name]

    def to_username(self, client_name):
        return id_to_username_dict[client_name] 

    def to_passwd(self, client_name):
        return id_to_passwd_dict[client_name]

    def __init__(self, client_names, server_names, udp_client_names, udp_server_names, switch_name):
        
        self.target = ["10.1.0.1", "10.1.0.1", "10.1.0.1", "10.1.0.1"
          , "10.1.0.1", "10.1.0.1", "10.1.0.1", "10.1.0.1", "10.1.0.1"
          , "10.1.0.1", "10.1.0.1", "10.1.0.1", "10.1.0.1"]

        # self.benchmark = MICROBENCHMARK_EXCLUSIVE
        self.wpkts_send_limit_ms_client = WPKTS_SEND_LIMIT_MS
        self.wpkts_send_limit_ms_server = WPKTS_SEND_LIMIT_MS

        self.program_name = "csfq"
        self.simple_switch_name = "simpleswitch"
        self.ecn_switch_name = "ecnswitch"
        self.server_names = server_names
        self.client_names = client_names
        self.udp_client_names = udp_client_names
        self.udp_server_names = udp_server_names
        self.switch_name  = switch_name
        self.diff_weight = 0
        
        
        self.passwd = {}
        for client_name in client_names + udp_client_names:
            self.passwd[client_name] = id_to_passwd_dict[client_name]

        for client_name in server_names + udp_server_names:
            self.passwd[client_name] = id_to_passwd_dict[client_name]


        self.switch_pw = "onl"

        self.clients = []
        for client_name in client_names:
            client = paramiko.SSHClient()
            # client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
            client.load_system_host_keys()
            client.connect(hostname = self.to_hostname(client_name), username = "user", password = self.passwd[client_name])
            self.clients.append((client_name, client))

        self.udp_clients = []
        for client_name in udp_client_names:
            client = paramiko.SSHClient()
            # client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
            client.load_system_host_keys()
            client.connect(hostname = self.to_hostname(client_name), username = "user", password = self.passwd[client_name])
            self.udp_clients.append((client_name, client))

        self.servers = []
        for server_name in server_names:
            client = paramiko.SSHClient()
            client.load_system_host_keys()
            client.connect(hostname = self.to_hostname(server_name), username = "user", password = self.passwd[server_name])
            self.servers.append((server_name, client))
            
        self.udp_servers = []
        for server_name in udp_server_names:
            client = paramiko.SSHClient()
            client.load_system_host_keys()
            client.connect(hostname = self.to_hostname(server_name), username = "user", password = self.passwd[server_name])
            self.udp_servers.append((server_name, client))

        switch = paramiko.SSHClient()
        switch.load_system_host_keys()
        switch.connect(hostname = self.to_hostname(self.switch_name), username = "root", password = self.switch_pw)
        self.switch = (self.switch_name, switch)

        self.local_home_dir = "/Users/zyu/Dropbox/code/p4/"
        self.local_main_dir = self.local_home_dir + "csfq/"
        self.local_p4_dir   = self.local_home_dir + "csfq/p4src/"
        self.local_ptf_dir  = self.local_home_dir + "csfq/controller_init/"
        self.local_hcsfq_dir = self.local_home_dir + "csfq/hcsfq/"
        self.local_res_dir  = self.local_home_dir + "csfq/results/"
        self.local_client_dir = self.local_home_dir + "csfq/incast_dpdk/client/"
        self.local_server_dir = self.local_home_dir + "csfq/incast_dpdk/server/"
        self.local_simple_switch_dir = self.local_home_dir + "csfq/simple_switch/"
        self.local_simple_switch_p4_dir = self.local_home_dir + "csfq/simple_switch/p4src/"
        self.local_mmtcp_dir = "/Users/zyu/Projects/mmtcp/"
        self.local_simpletcp_dir = self.local_home_dir + "csfq/simple_tcp/"

        self.remote_server_home_dir = "/home/user/zhuolong/exp/"
        self.remote_server_main_dir = "/home/user/zhuolong/exp/csfq/"        
        self.remote_server_client_dir = "/home/user/zhuolong/exp/csfq/incast_dpdk/client/"
        self.remote_server_server_dir = "/home/user/zhuolong/exp/csfq/incast_dpdk/server/"
        self.remote_server_log_dir  = "/home/user/zhuolong/exp/csfq/logs/"
        self.remote_server_res_dir  = "/home/user/zhuolong/exp/csfq/results/"
        self.remote_server_ptf_dir = "/home/user/zhuolong/exp/csfq/controller_init/"
        self.remote_server_dpdk_dir = "/home/user/zhuolong/exp/csfq/incast_dpdk/"
        self.remote_server_mmtcp_dir = "/home/user/zhuolong/exp/csfq/mmtcp/"
        self.remote_server_simpletcp_dir = "/home/user/zhuolong/exp/csfq/simple_tcp/"
        self.remote_server_mmtcp_app_example_dir = "/home/user/zhuolong/exp/csfq/mmtcp/apps/example/"

        

        self.remote_switch_home_dir = "/home/zhuolong/exp/"
        self.remote_switch_main_dir = "/home/zhuolong/exp/csfq/"
        self.remote_switch_p4_dir   = "/home/zhuolong/exp/csfq/p4src/"
        self.remote_switch_ptf_dir  = "/home/zhuolong/exp/csfq/controller_init/"
        
        # self.remote_switch_sde_dir  = "/home/zhuolong/bf-sde-8.2.2/"
        self.remote_switch_sde_dir = "/home/hz/bf-sde-8.9.1-pg/bf-sde-8.9.1/"
        
        self.remote_switch_log_dir  = "/home/zhuolong/exp/csfq/logs/"
        self.remote_switch_hcsfq_dir = "/home/zhuolong/exp/csfq/hcsfq/"
        self.remote_switch_hcsfq_p4_dir = "/home/zhuolong/exp/csfq/hcsfq/p4src/"
        self.remote_switch_hcsfq_ptf_dir = "/home/zhuolong/exp/csfq/hcsfq/controller_init/"

        self.remote_switch_simple_switch_p4_dir = "/home/zhuolong/exp/csfq/simple_switch/p4src/"
        self.remote_switch_simple_switch_ptf_dir = "/home/zhuolong/exp/csfq/simple_switch/controller_init/"

        self.remote_switch_ecn_switch_p4_dir = "/home/zhuolong/exp/csfq/ecn_switch/p4src/"
        self.remote_switch_ecn_switch_ptf_dir = "/home/zhuolong/exp/csfq/ecn_switch/controller_init/"
        
        print "========init completed========"


    # ********************************
    # fundamental functions
    # ********************************

    def exe(self, client, cmd, with_print=False):
        (client_name, client_shell) = client
        stdin, stdout, stderr = client_shell.exec_command(cmd)
        if with_print:
            print client_name, ":", stdout.read(), stderr.read()

    def sudo_exe(self, client, cmd, with_print=False):
        (client_name, client_shell) = client
        cmdheader = "echo '%s' | sudo -S " %(self.passwd[client_name])
        cmd = cmdheader + cmd
        stdin, stdout, stderr = client_shell.exec_command(cmd)
        if with_print:
            print client_name, ":", stdout.read(), stderr.read()
            stdout.flush()
            stderr.flush()

    def kill_host(self):
        for client in self.clients + self.udp_clients:
            self.sudo_exe(client, "pkill client")
            self.sudo_exe(client, "pkill server")
            self.sudo_exe(client, "pkill epwget")
            self.sudo_exe(client, "pkill epserver")
            self.sudo_exe(client, "pkill python")
            self.sudo_exe(client, "pkill iperf3")
            self.sudo_exe(client, "pkill tcpdump")
        # time.sleep(2)
        for server in self.servers + self.udp_servers:
            self.sudo_exe(server, "pkill client")
            self.sudo_exe(server, "pkill server")
            self.sudo_exe(server, "pkill epwget")
            self.sudo_exe(server, "pkill epserver")
            self.sudo_exe(server, "pkill python")
            self.sudo_exe(server, "pkill iperf3")
            self.sudo_exe(server, "pkill tcpdump")

    def kill_switch(self):
        self.exe(self.switch, "ps -ef | grep switchd | grep -v grep | " \
            "awk '{print $2}' | xargs kill -9")
        self.exe(self.switch, "ps -ef | grep run_p4_test | grep -v grep | " \
            "awk '{print $2}' | xargs kill -9")
        self.exe(self.switch, "ps -ef | grep tofino | grep -v grep | " \
            "awk '{print $2}' | xargs kill -9")

    def kill_all(self):
        self.kill_host()
        self.kill_switch()
    
    def sync_host(self):
        for client in self.client_names + self.udp_client_names:
            cmd = "rsync -r %s user@%s:%s" % (self.local_client_dir, self.to_hostname(client), self.remote_server_client_dir)
            print cmd
            subprocess.call(cmd, shell = True)
            cmd = "rsync -r %s user@%s:%s" % (self.local_ptf_dir, self.to_hostname(client), self.remote_server_ptf_dir)
            print cmd
            subprocess.call(cmd, shell = True)
            # cmd = "rsync -r %s user@%s:%s" % (self.local_mmtcp_dir, self.to_hostname(client), self.remote_server_mmtcp_dir)
            # print cmd
            # subprocess.call(cmd, shell = True)
            cmd = "rsync -r %s user@%s:%s" % (self.local_simpletcp_dir, self.to_hostname(client), self.remote_server_simpletcp_dir)
            print cmd
            subprocess.call(cmd, shell = True)

        for server in self.server_names + self.udp_server_names:
            cmd = "rsync -r %s user@%s:%s" % (self.local_server_dir, self.to_hostname(server), self.remote_server_server_dir)
            print cmd
            subprocess.call(cmd, shell = True)
            cmd = "rsync -r %s user@%s:%s" % (self.local_ptf_dir, self.to_hostname(server), self.remote_server_ptf_dir)
            print cmd
            subprocess.call(cmd, shell = True)
            # cmd = "rsync -r %s user@%s:%s" % (self.local_mmtcp_dir, self.to_hostname(server), self.remote_server_mmtcp_dir)
            # print cmd
            # subprocess.call(cmd, shell = True)
            cmd = "rsync -r %s user@%s:%s" % (self.local_simpletcp_dir, self.to_hostname(server), self.remote_server_simpletcp_dir)
            print cmd
            subprocess.call(cmd, shell = True)



        return

    def sync_switch(self):
        cmd = "scp -r %s root@%s:%s" % (self.local_p4_dir, self.to_hostname(self.switch_name), self.remote_switch_main_dir)
        print cmd
        subprocess.call(cmd, shell = True)
        cmd = "scp -r %s root@%s:%s" % (self.local_ptf_dir, self.to_hostname(self.switch_name), self.remote_switch_main_dir)
        print cmd
        subprocess.call(cmd, shell = True)

        cmd = "scp -r %s root@%s:%s" % (self.local_hcsfq_dir, self.to_hostname(self.switch_name), self.remote_switch_main_dir)
        print cmd
        subprocess.call(cmd, shell = True)
        return

    def sync_all(self):
        self.sync_switch()
        self.sync_host()
        return

    def compile_host(self):
        for client in self.udp_clients:
            dpdk_dir = self.remote_server_client_dir
            cmd = "source /home/user/.bash_profile;cd %s; make > %s/dpdk_client_compile.log 2>&1 &" % (dpdk_dir, self.remote_server_log_dir)
            print "%s compile client_dpdk: %s" % (client[0], cmd)
            self.exe(client, cmd, True)
        
        for server in self.udp_servers:
            dpdk_dir = self.remote_server_server_dir
            cmd = "source /home/user/.bash_profile;cd %s; make > %s/dpdk_server_compile.log 2>&1 &" % (dpdk_dir, self.remote_server_log_dir)
            print "%s compile server_dpdk: %s" % (server[0], cmd)
            self.exe(server, cmd, True)
        return

    def compile_ep(self):
        for client in self.clients:
            # cmd = "cd %s;make clean > %s/epwget_compile.log 2>&1 &" % (self.remote_server_mmtcp_dir, self.remote_server_log_dir)
            cmd = "source /home/user/.bash_profile;cd %s;autoreconf -ivf > /dev/null;./configure --with-dpdk-lib=$RTE_SDK/$RTE_TARGET --disable-hwcsum CFLAGS='-DMAX_CPUS=32' > /dev/null;make clean > /dev/null ; make > %s/epwget_compile.log 2>&1 &" % (self.remote_server_mmtcp_dir, self.remote_server_log_dir)
            print "%s compile epwget: %s" % (client[0], cmd)
            self.exe(client, cmd, True)
        for server in self.servers:
            # cmd = "cd %s;make clean > %s/epwget_compile.log 2>&1 &" % (self.remote_server_mmtcp_dir, self.remote_server_log_dir)
            cmd = "source /home/user/.bash_profile;cd %s;autoreconf -ivf > /dev/null;./configure --with-dpdk-lib=$RTE_SDK/$RTE_TARGET --disable-hwcsum CFLAGS='-DMAX_CPUS=32' > /dev/null;make clean > /dev/null ; make > %s/epwget_compile.log 2>&1 &" % (self.remote_server_mmtcp_dir, self.remote_server_log_dir)
            print "%s compile epserver: %s" % (server[0], cmd)
            self.exe(server, cmd, True)

    def compile_csfq(self):
        sde_dir = self.remote_switch_sde_dir
        p4_build = sde_dir + "p4_build.sh"
        p4_program = self.remote_switch_p4_dir + self.program_name + ".p4"
        cmd = "cd %s;source ./set_sde.bash;%s %s > %s/csfq_compile.log 2>&1 &" % (sde_dir, p4_build,
            p4_program, self.remote_switch_log_dir)
        print cmd
        self.exe(self.switch, cmd, True)
        return

    def compile_hcsfq(self):
        sde_dir = self.remote_switch_sde_dir
        p4_build = sde_dir + "p4_build.sh"
        p4_program = self.remote_switch_hcsfq_p4_dir + "hcsfq" + ".p4"
        cmd = "cd %s;source ./set_sde.bash;%s %s > %s/hcsfq_compile.log 2>&1 &" % (sde_dir, p4_build,
            p4_program, self.remote_switch_log_dir)
        print cmd
        self.exe(self.switch, cmd, True)
        return

    def replace_macro(self, suffix):
        macro_filename = "hcsfq_defines_" + suffix + ".p4" 
        cmd = "cd %s/includes; cat %s > hcsfq_defines.p4 &" % (self.remote_switch_hcsfq_p4_dir, macro_filename)
        print cmd
        self.exe(self.switch, cmd, True)
        return

    def compile_all(self):
        self.compile_host()
        self.compile_switch()
        return

    def run_client(self):
        dpdk_dir = self.remote_server_client_dir
        port_st = 9000
        time_to_run = 64
        for client in self.udp_clients:
            client_id = client[0].strip("netx")
            tenant_id = tenant_group[int(client_id)]
            server_id = self.udp_servers[0][0].strip("netx")
            cmd = "cd %s;source /home/user/.bash_profile; echo '%s' | sudo -S %s/build/client --lcores 0@0,1@1,2@2,3@3,4@4 -- -n%s -t%s -r%s -s%s -p%d -T%d" %\
                 (self.remote_server_client_dir, self.passwd[client[0]], dpdk_dir, client_id, tenant_id, server_id, int(self.wpkts_send_limit_ms_client * float(divs[int(client_id)])), port_st, time_to_run) + " > %s/client_run_even_%s.log 2>&1 &" % (self.remote_server_res_dir, client_id)
            port_st += 4
            print "%s run client_dpdk: %s" % (client[0], cmd)
            self.exe(client, cmd, True)
            time_to_run = time_to_run - 16
            time.sleep(8)
        return

    def run_server(self):
        dpdk_dir = self.remote_server_server_dir
        for server in self.udp_servers:
            server_id = server[0].strip("netx")
            cmd = "cd %s;source /home/user/.bash_profile; echo '%s' | sudo -S %s/build/server --lcores 0@0,1@1,2@2,3@3,4@4,5@5,6@6,7@7 > %s/server_run.log 2>&1 &" %\
                (self.remote_server_server_dir, self.passwd[server[0]], dpdk_dir, self.remote_server_res_dir)
            print "%s run server_dpdk: %s" % (server[0], cmd)
            self.exe(server, cmd, True)
        return

    def run_epwget(self):
        for client in self.clients:
            client_id = client[0].strip("netx")
            cmd = "cd %s; echo '%s' | sudo -S ./epwget %s/trace_9.csv 1 -o output.tmp -f config/mtcp.conf -N 1 -c 1 > %s/epwget_run_%s.log 2>&1 &" % (self.remote_server_mmtcp_app_example_dir, self.passwd[client[0]], self.target[int(client_id)], self.remote_server_log_dir, client_id)
            print "%s run client epwget: %s" % (client[0], cmd)
            self.exe(client, cmd, True)
            time.sleep(4)
        return 

    def run_epserver(self):
        for server in self.servers:
            cmd = "cd %s;echo '%s' | sudo -S ./epserver -p ./data/ -f config/mtcp.conf -N 1 > %s/epserver_run.log 2>&1 &" % (self.remote_server_mmtcp_app_example_dir, self.passwd[server[0]], self.remote_server_log_dir)
            print "%s run server epserver: %s" % (server[0], cmd)
            self.exe(server, cmd, True)
        return

    def setup_alg(self):
        if len(sys.argv)<=3:
            print_usage()
            sys.exit(0)
        target_id = sys.argv[2]
        alg = sys.argv[3]
        print(target_id, alg)
        for client in self.clients:
            client_id = client[0].strip("netx")
            if (client_id == target_id):
                cmd = "sysctl net.ipv4.tcp_congestion_control=%s" % (alg)
                print "%s set tc: %s" % (client[0], cmd)
                self.sudo_exe(client, cmd, True)
                break
        return

    def setup_tc(self):
        if len(sys.argv)<=3:
            print_usage()
            sys.exit(0)
        target_id = sys.argv[2]
        rtt = sys.argv[3]
        print(target_id, rtt)
        for client in self.clients:
            client_id = client[0].strip("netx")
            if (client_id == target_id):
                cmd = "tc qdisc del dev enp5s0f0 root"
                print "%s clear tc: %s" % (client[0], cmd)
                self.sudo_exe(client, cmd, True)
                cmd = "tc qdisc add dev enp5s0f0 root netem delay %sms" % (rtt)
                print "%s set tc: %s" % (client[0], cmd)
                self.sudo_exe(client, cmd, True)
                break
        return

    def clear_tc(self):
        if len(sys.argv)<=2:
            print_usage()
            sys.exit(0)
        target_id = sys.argv[2]
        print(target_id)
        for client in self.clients:
            client_id = client[0].strip("netx")
            if (client_id == target_id):
                cmd = "tc qdisc del dev enp5s0f0 root"
                print "%s clear tc: %s" % (client[0], cmd)
                self.sudo_exe(client, cmd, True)
                break
        return

    def setup_arp(self):
        for client in self.clients + self.udp_clients:
            client_id = client[0].strip("netx")
            cmd = "cd /home/user/zhuolong/exp/csfq/arp_conf/; sh loop.sh > /dev/null 2>&1 &"
            print "%s set arp" % (client[0])
            self.exe(client, cmd, True)
        for server in self.servers + self.udp_servers:
            server_id = server[0].strip("netx")
            cmd = "cd /home/user/zhuolong/exp/csfq/arp_conf/; sh loop.sh > /dev/null 2>&1 &"
            print "%s set arp" % (server[0])
            self.exe(server, cmd, True)
        return

    def kill_arp(self):
        for client in self.clients + self.udp_clients:
            client_id = client[0].strip("netx")
            cmd = "ps -ef | grep loop.sh | grep -v grep | awk '{print $2}' | xargs kill -9"
            print "%s kill arp" % (client[0])
            self.sudo_exe(client, cmd, True)
        for server in self.servers + self.udp_servers:
            server_id = server[0].strip("netx")
            cmd = "ps -ef | grep loop.sh | grep -v grep | awk '{print $2}' | xargs kill -9"
            print "%s kill arp" % (server[0])
            self.sudo_exe(server, cmd, True)
        return

    def run_tcp_client(self):        
        port_st = 8080
        index = 0
        for client in self.clients:
            server = self.servers[index]
            server_id = server[0].strip("netx")
            client_id = client[0].strip("netx")
            # cmd = "cd %s; cd simple_tcp; python throughput.py -c 6553500 10.1.0.%s %s > %s/run_tcp_client.log 2>&1 &" % (self.remote_server_main_dir, server_id, str(port_st), self.remote_server_log_dir)
            log_file = self.remote_server_res_dir + "/%s.log" % (client_id)
            cmd = "cd %s; cd simple_tcp; python nstat.py %s > /dev/null 2>&1 &" % (self.remote_server_main_dir, log_file)
            print "%s run client stats: %s" % (client[0], cmd)
            self.exe(client, cmd, True)
                
        time_to_run = 3200
        time_to_sleep = 0
        i=0
        cc = 0
        for client in self.clients:
            for i in range(thread_per_client):
                # print(self.servers)
                server = self.servers[index]
                server_id = server[0].strip("netx")
                client_id = client[0].strip("netx")
                cmd = "cd %s; cd simple_tcp; python throughput.py -c 65535000 10.1.0.%s %s > %s/run_tcp_client.log 2>&1 &" % (self.remote_server_main_dir, server_id, str(port_st), self.remote_server_log_dir)
                print "%s run tcp client: %s" % (client[0], cmd)
                self.exe(client, cmd, True)
                port_st = port_st + 1
                cc = cc + 1
                cc = cc % 8
        return

    def run_tcp_server(self):
        port_st = 8080
        cc = 0
        for client in self.clients:
            for server in self.servers:
                for i in range(thread_per_client):
                    client_id = client[0].strip("netx")
                    server_id = server[0].strip("netx")
                    cmd = "cd %s; cd simple_tcp; python throughput.py -s %s > %s/run_tcp_server_%s_%d.log 2>&1 &" % (self.remote_server_main_dir, str(port_st), self.remote_server_res_dir, client_id, i)
                    # cmd = "cd %s; cd simple_tcp; taskset -c %d python throughput.py -s %s > %s/run_tcp_server_%s_%d.log 2>&1 &" % (self.remote_server_main_dir, cc, str(port_st), self.remote_server_res_dir, client_id, i)
                    # cmd = "iperf3 -s -p %s -i 0 -D -A %d" % (str(port_st), port_st%8)
                    print "%s run tcp server: %s" % (server[0], cmd)
                    self.exe(server, cmd, True)
                    port_st = port_st + 1
                    cc = cc + 1
                    cc = cc % 8
                    # time.sleep(8)
        return

    def run_udp_client(self):
        port_st = 9049
        index = 0
        cc = 0
        for client in self.udp_clients:
            for i in range(udp_thread_per_client):
                server = self.udp_servers[index]
                server_id = server[0].strip("netx")
                client_id = client[0].strip("netx")
                cmd = "cd %s; cd simple_tcp; python simple_udp.py -c 655350000 10.1.0.%s %s > %s/run_udp_client.log 2>&1 &" % (self.remote_server_main_dir, server_id, str(port_st), self.remote_server_log_dir)
                # cmd = "cd %s; cd simple_tcp; taskset -c %d python simple_udp.py -c 655350000 10.1.0.%s %s > %s/run_udp_client.log 2>&1 &" % (self.remote_server_main_dir, cc, server_id, str(port_st), self.remote_server_log_dir)
                print "%s run udp client: %s" % (client[0], cmd)
                self.exe(client, cmd, True)
                port_st = port_st + 1
                cc = cc + 1
                cc = cc % 8
        return

    def run_udp_server(self):
        port_st = 9049
        cc = 0
        log_index = 0
        for client in self.udp_clients:
            for server in self.udp_servers:
                for i in range(udp_thread_per_client):
                    server_id = server[0].strip("netx")
                    # cmd = "cd %s; cd simple_tcp; python simple_udp.py -s %s > %s/run_udp_server_%d.log 2>&1 &" % (self.remote_server_main_dir,str(port_st), self.remote_server_res_dir, i)
                    cmd = "cd %s; cd simple_tcp; python simple_udp.py -s %s > %s/run_udp_server_%d.log 2>&1 &" % (self.remote_server_main_dir, str(port_st), self.remote_server_res_dir, log_index)
                    # cmd = "cd %s; cd simple_tcp; taskset -c %d python simple_udp.py -s %s > %s/run_udp_server_%d.log 2>&1 &" % (self.remote_server_main_dir, cc,str(port_st), self.remote_server_res_dir, i)
                    print "%s run udp server: %s" % (server[0], cmd)
                    self.exe(server, cmd, True)
                    port_st = port_st + 1
                    log_index = log_index + 1
                    cc = cc + 1
                    cc = cc % 8
        return

    def run_host(self):
        self.run_server()
        self.run_client()

    def run_csfq(self):
        sde_dir = self.remote_switch_sde_dir
        ## run switch
        run_csfqd = sde_dir + "run_switchd.sh"
        cmd = "cd %s;source ./set_sde.bash;%s -p %s > %s/run_switchd.log 2>&1 &" % (sde_dir, run_csfqd,
            self.program_name, self.remote_switch_log_dir)
        print cmd
        self.exe(self.switch, cmd, True)

        ## run ptf_test 
        run_ptf_test = sde_dir + "run_p4_tests.sh"
        ports_map = self.remote_switch_ptf_dir + "ports.json"
        target_mode = "hw"
        cmd = "cd %s;source ./set_sde.bash;%s -t %s -p %s -f %s --target %s > %s/run_ptf_test.log 2>&1 &" % (sde_dir, run_ptf_test,
            self.remote_switch_ptf_dir, self.program_name, ports_map, target_mode, self.remote_switch_log_dir)
        print cmd
        self.exe(self.switch, cmd, True)
        return

    def run_hcsfq(self):
        sde_dir = self.remote_switch_sde_dir
        ## run switch
        run_hcsfqd = sde_dir + "run_switchd.sh"
        cmd = "cd %s;source ./set_sde.bash;%s -p hcsfq > %s/run_switchd.log 2>&1 &" % (sde_dir, run_hcsfqd, self.remote_switch_log_dir)
        print cmd
        self.exe(self.switch, cmd, True)

        ## run ptf_test
        run_ptf_test = sde_dir + "run_p4_tests.sh"
        ports_map = self.remote_switch_hcsfq_ptf_dir + "ports.json"
        target_mode = "hw"
        cmd = "cd %s;source ./set_sde.bash;%s -t %s -p hcsfq -f %s --target %s > %s/run_ptf_test.log 2>&1 &" % (sde_dir, run_ptf_test, self.remote_switch_hcsfq_ptf_dir, ports_map, target_mode, self.remote_switch_log_dir)
        print cmd
        self.exe(self.switch, cmd, True)
        return

    def run_simple_switch(self):
        sde_dir = self.remote_switch_sde_dir
        ## run switch
        run_netlockd = sde_dir + "run_switchd.sh"
        cmd = "cd %s;source ./set_sde.bash;%s -p %s > %s/run_simple_switch_switchd.log 2>&1 &" % (sde_dir, run_netlockd,
            self.simple_switch_name, self.remote_switch_log_dir)
        print cmd
        self.exe(self.switch, cmd, True)

        ## run ptf_test 
        run_ptf_test = sde_dir + "run_p4_tests.sh"
        ports_map = self.remote_switch_simple_switch_ptf_dir + "ports.json"
        target_mode = "hw"
        cmd = "cd %s;source ./set_sde.bash;%s -t %s -p %s -f %s --target %s > %s/run_simple_switch_ptf_test.log 2>&1 &" % (sde_dir, run_ptf_test,
            self.remote_switch_simple_switch_ptf_dir, self.simple_switch_name, ports_map, target_mode, self.remote_switch_log_dir)
        print cmd
        self.exe(self.switch, cmd, True)
        return

    def run_ecn_switch(self):
        sde_dir = self.remote_switch_sde_dir
        ## run switch
        run_netlockd = sde_dir + "run_switchd.sh"
        cmd = "cd %s;source ./set_sde.bash;%s -p %s > %s/run_ecn_switch_switchd.log 2>&1 &" % (sde_dir, run_netlockd,
            self.ecn_switch_name, self.remote_switch_log_dir)
        print cmd
        self.exe(self.switch, cmd, True)

        ## run ptf_test 
        run_ptf_test = sde_dir + "run_p4_tests.sh"
        ports_map = self.remote_switch_ecn_switch_ptf_dir + "ports.json"
        target_mode = "hw"
        cmd = "cd %s;source ./set_sde.bash;%s -t %s -p %s -f %s --target %s > %s/run_ecn_switch_ptf_test.log 2>&1 &" % (sde_dir, run_ptf_test,
            self.remote_switch_ecn_switch_ptf_dir, self.ecn_switch_name, ports_map, target_mode, self.remote_switch_log_dir)
        print cmd
        self.exe(self.switch, cmd, True)
        return

    def run_udp(self):
        self.kill_all()
        time.sleep(10)
        self.run_hcsfq()
        time.sleep(80)
        self.run_server()
        time.sleep(10)
        self.run_client()
        time.sleep(30)
        self.grab_result()
        return

    def run_udp_sw(self):
        self.kill_all()
        time.sleep(10)
        self.run_simple_switch()
        time.sleep(60)
        self.run_server()
        time.sleep(10)
        self.run_client()
        time.sleep(30)
        self.grab_result()
        return

    def run_tcp(self):
        self.kill_all()
        time.sleep(10)
        self.run_hcsfq()
        time.sleep(60)
        self.run_tcp_server()
        self.run_udp_server()
        time.sleep(1)
        self.run_tcp_client()
        self.run_udp_client()
        print("sleep for 40s...")
        time.sleep(100)
        self.kill_all()
        self.grab_result()
        return

    def run_tcp_ecn(self):
        self.kill_all()
        time.sleep(10)
        self.run_ecn_switch()
        time.sleep(50)
        self.run_tcp_server()
        self.run_udp_server()
        time.sleep(1)
        self.run_udp_client()
        self.run_tcp_client()
        print("sleep for 40s...")
        time.sleep(160)
        self.kill_all()
        self.grab_result()
        return

    def run_tcp_sw(self):
        self.kill_all()
        time.sleep(10)
        self.run_simple_switch()
        time.sleep(50)
        self.run_tcp_server()
        self.run_udp_server()
        time.sleep(1)
        self.run_udp_client()
        self.run_tcp_client()
        print("sleep for 40s...")
        time.sleep(160)
        self.kill_all()
        self.grab_result()
        return

    def setup_dpdk(self):
        dpdk_dir = self.remote_server_dpdk_dir
        cmd_eth_down = "ifconfig enp5s0f0 down > %s/eth_down.log 2>&1 &" % (self.remote_server_log_dir)
        cmd_setup_dpdk = "export passwd=%s;source /home/user/.bash_profile;echo $RTE_SDK;sh %s/tools.sh setup_dpdk > %s/setup_dpdk.log 2>&1 &" % (self.passwd['netx2'], dpdk_dir, self.remote_server_log_dir)
        if "netx10" in self.passwd:
            cmd_setup_dpdk_2 = "export passwd=%s;source /home/user/.bash_profile;echo $RTE_SDK;sh %s/tools.sh setup_dpdk > %s/setup_dpdk.log 2>&1 &" % (self.passwd['netx10'], dpdk_dir, self.remote_server_log_dir)
        for client in self.udp_clients:
            print "%s run eth_down: %s" % (client[0], cmd_eth_down)
            self.sudo_exe(client, cmd_eth_down, True)
            print "%s run setup_dpdk: %s" % (client[0], cmd_setup_dpdk)
            if (client[0] == "netx10"):
                self.exe(client, cmd_setup_dpdk_2, True)
            else:
                self.exe(client, cmd_setup_dpdk, True)
        
        for server in self.udp_servers:
            print "%s run eth_down: %s" % (server[0], cmd_eth_down)
            self.sudo_exe(server, cmd_eth_down, True)
            print "%s run setup_dpdk: %s" % (server[0], cmd_setup_dpdk)
            if (server[0] == "netx10"):
                self.exe(server, cmd_setup_dpdk_2, True)
            else:
                self.exe(server, cmd_setup_dpdk, True)
        return

    def unbind_dpdk(self):
        dpdk_dir = self.remote_server_dpdk_dir
        cmd_eth_up = "ifconfig enp5s0f0 up > %s/eth_up.log 2>&1 &" % (self.remote_server_log_dir)
        cmd_unbind_dpdk = "export passwd=%s;source /home/user/.bash_profile;echo $RTE_SDK;sh %s/tools.sh unbind_dpdk > %s/unbind_dpdk.log 2>&1 &" % (self.passwd['netx2'], dpdk_dir, self.remote_server_log_dir)
        if "netx10" in self.passwd:
            cmd_unbind_dpdk_2 = "export passwd=%s;source /home/user/.bash_profile;echo $RTE_SDK;sh %s/tools.sh unbind_dpdk > %s/unbind_dpdk.log 2>&1 &" % (self.passwd['netx10'], dpdk_dir, self.remote_server_log_dir)
        for client in self.clients + self.udp_clients:
            print "%s run unbind_dpdk: %s" % (client[0], cmd_unbind_dpdk)
            if (client[0] == "netx10"):
                self.exe(client, cmd_unbind_dpdk_2, True)
            else:
                self.exe(client, cmd_unbind_dpdk, True)

            print "%s run eth_up: %s" % (client[0], cmd_eth_up)
            self.sudo_exe(client, cmd_eth_up, True)
        
        for server in self.servers + self.udp_servers:
            print "%s run unbind_dpdk: %s" % (server[0], cmd_unbind_dpdk)
            if (server[0] == "netx10"):
                self.exe(server, cmd_unbind_dpdk_2, True)
            else:
                self.exe(server, cmd_unbind_dpdk, True)

            print "%s run eth_up: %s" % (server[0], cmd_eth_up)
            self.sudo_exe(server, cmd_eth_up, True)
        return

    def get_tput(self):
        for client in self.clients: 
            for i in range(thread_per_client):
                client_id = client[0].strip("netx")
                # cmd = "sed -n '20,$p' ~/zhuolong/exp/csfq/results/%d.log | grep -v - | awk -F [' ']+ 'BEGIN{a=0;count=0}{a+=$3;count+=1}END{print a/count}'" % (int(client_id))
                cmd = "cat ~/zhuolong/exp/csfq/logs/run_tcp_client_%s_%d.log | grep sender" % (client[0], i)
                print "%s get tput" % (client[0])
                self.exe(client, cmd, True)
        return

    def get_kernel(self):
        for client in self.clients: 
            client_id = client[0].strip("netx")
            cmd = "uname -r"
            print "%s get kernel version " % (client[0])
            self.exe(client, cmd, True)
        for server in self.servers: 
            server_id = server[0].strip("netx")
            cmd = "uname -r"
            print "%s get kernel version " % (server[0])
            self.exe(server, cmd, True)
        return

    def set_mtu(self):
        for client in self.clients:
            client_id = client[0].strip("netx")
            cmd = "sysctl net.ipv4.tcp_congestion_control=cubic"
            print "%s set mtu" % (client[0])
            self.sudo_exe(client, cmd, True)
        for server in self.servers:
            server_id = server[0].strip("netx")
            cmd = "sysctl net.ipv4.tcp_congestion_control=cubic"
            print "%s set mtu" % (server[0])
            self.sudo_exe(server, cmd, True)
        return

    def reboot_host(self):
        cmd = "reboot > %s/reboot.log 2>&1 &" % (self.remote_server_log_dir)
        for client in self.clients + self.udp_clients:
            print "%s run eth_down: %s" % (client[0], cmd)
            self.sudo_exe(client, cmd, True)
        
        for server in self.servers + self.udp_servers:
            print "%s run eth_down: %s" % (server[0], cmd)
            self.sudo_exe(server, cmd, True)

    def grab_result(self):
        for client in self.client_names + self.udp_client_names:
            cmd = "rsync -r user@%s:%s %s" % (self.to_hostname(client), self.remote_server_res_dir, self.local_res_dir)
            print cmd
            subprocess.call(cmd, shell = True)
        
        for server in self.server_names + self.udp_server_names:
            cmd = "rsync -r user@%s:%s %s" % (self.to_hostname(server), self.remote_server_res_dir, self.local_res_dir)
            print cmd
            subprocess.call(cmd, shell = True)
        return

    def clean_result(self):
        if (self.remote_server_res_dir == "/home/user/zhuolong/exp/csfq/results/"):
            cmd = "rm -rf  %s/* > /dev/null  2>&1 & " % (self.remote_server_res_dir)
            for client in self.clients:
                print "%s deleting the result files: %s" % (client[0], cmd)
                self.sudo_exe(client, cmd, True)
            for server in self.servers:
                print "%s deleting the result files: %s" % (server[0], cmd)
                self.sudo_exe(server, cmd, True)
        return

def print_usage():
    prRed("Usage")
    prRed("  console.py sync_(host, trace, switch, all)")
    prRed("  console.py compile_(host, switch, all)")
    prRed("  console.py run_(client, server, csfq, host, all)")
    prRed("  console.py setup_dpdk")
    prRed("  console.py reboot_host")
    prRed("  console.py grab_result")
    prRed("  console.py clean_result")
    prRed("  console.py draw_figure fig")
    prRed("  console.py benchmark (e.g. micro_bm_s, micro_bm_x, micro_bm_cont, mem_man, mem_size, think_time, run_tpcc, run_tpcc_ms)")
    prRed("  console.py failover")
    sys.exit()

if __name__ == "__main__":
    if len(sys.argv) <= 1:
        print_usage()
        sys.exit(0)

    serverNum = 1
    if sys.argv[1] == "draw_figure":
        draw_figure(sys.argv[2])
        sys.exit()

    nl_console = CSFQConsole(["netx1", "netx4", "netx8"], 
                              ["netx2"],
                              ["netx9"], 
                              ["netx2"],
                              "netxy")
    
    if sys.argv[1] == "sync_host":
        nl_console.sync_host()
    elif sys.argv[1] == "sync_switch":
        nl_console.sync_switch()
    elif sys.argv[1] == "sync_all":
        nl_console.sync_all()
    elif sys.argv[1] == "compile_host":
        nl_console.compile_host()
    elif sys.argv[1] == "compile_csfq":
        nl_console.compile_csfq()
    elif sys.argv[1] == "compile_hcsfq":
        nl_console.compile_hcsfq()
    elif sys.argv[1] == "compile_all":
        nl_console.compile_all()
    elif sys.argv[1] == "run_client":
        nl_console.run_client()
    elif sys.argv[1] == "run_server":
        nl_console.run_server()
    elif sys.argv[1] == "run_csfq":
        nl_console.run_csfq()
    elif sys.argv[1] == "run_hcsfq":
        nl_console.run_hcsfq()
    elif sys.argv[1] == "run_all":
        nl_console.run_all()
    elif sys.argv[1] == "kill_host":
        nl_console.kill_host()
    elif sys.argv[1] == "kill_switch":
        nl_console.kill_switch()
    elif sys.argv[1] == "kill_all":
        nl_console.kill_all()
    elif sys.argv[1] == "setup_dpdk":
        nl_console.setup_dpdk()
    elif sys.argv[1] == "unbind_dpdk":
        nl_console.unbind_dpdk()
    elif sys.argv[1] == "reboot_host":
        nl_console.reboot_host()
    elif sys.argv[1] == "grab_result":
        nl_console.grab_result()
    elif sys.argv[1] == "clean_result":
        nl_console.clean_result()
    elif sys.argv[1] == "run_host":
        nl_console.run_host()
    elif sys.argv[1] == "run_simple_switch":
        nl_console.run_simple_switch()
    elif sys.argv[1] == "run_epwget":
        nl_console.run_epwget()
    elif sys.argv[1] == "run_epserver":
        nl_console.run_epserver()
    elif sys.argv[1] == "run_tcp_client":
        nl_console.run_tcp_client()
    elif sys.argv[1] == "run_tcp_server":
        nl_console.run_tcp_server()
    elif sys.argv[1] == "set_arp":
        nl_console.setup_arp()
    elif sys.argv[1] == "kill_arp":
        nl_console.kill_arp()
    elif sys.argv[1] == "set_mtu":
        nl_console.set_mtu()
    elif sys.argv[1] == "compile_ep":
        nl_console.compile_ep()
    elif sys.argv[1] == "get_kernel":
        nl_console.get_kernel()
    elif sys.argv[1] == "get_tput":
        nl_console.get_tput()
    elif sys.argv[1] == "run_udp_server":
        nl_console.run_udp_server()
    elif sys.argv[1] == "run_udp_client":
        nl_console.run_udp_client()
    elif sys.argv[1] == "run_udp":
        nl_console.run_udp()
    elif sys.argv[1] == "run_udp_sw":
        nl_console.run_udp_sw()
    elif sys.argv[1] == "run_tcp":
        nl_console.run_tcp()
    elif sys.argv[1] == "run_tcp_sw":
        nl_console.run_tcp_sw()
    elif sys.argv[1] == "run_tcp_ecn":
        nl_console.run_tcp_ecn()
    elif sys.argv[1] == "set_tc":
        nl_console.setup_tc()
    elif sys.argv[1] == "clear_tc":
        nl_console.clear_tc()
    else:
        print_usage()
