## How to setup and run HCSFQ<br>
You need to setup dpdk (`python console.py setup_dpdk`) for UDP experiments or make sure ip address and arp correctly setted for TCP experiments.

You can either manually execute programs on the switch and the servers, or use the script we provided (Recommended).
- To use scripts (Recommended)<br>
  - Configure the parameters in the files based on your environment<br>
    - `config.py`: provide the information of your servers (username, passwd, hostname, dir).<br>
    - `hcsfq/controller_init/ports.json`: use the information (actual enabled ports) on your switch.
  - Environment setup<br>
    - Setup the switch<br>
      - Setup the necessary environment variables to point to the appropriate locations.<br>
      - Copy the files to the switch.<br>
        - `python console.py sync_switch`<br>
      - Compile the hcsfq.<br>
        - `python console.py compile_hcsfq`<br>
          This will take **a couple of minutes**. You can check `logs/p4_compile.log` in the switch to see if it's finished.
    - Setup the servers for UDP (enable DPDK)<br>
      - Setup DPDK environment (install dpdk, and set correct environment variables).<br>
      - Copy the files to the servers.<br>
        - `python console.py init_sync_server`<br>
      - Compile the clients and lock servers.<br>
        - `python console.py compile_host`<br>
          It will compile for both lock servers and clients.<br>
      - Bind NIC to DPDK.<br>
        - `python console.py setup_dpdk`<br>
          It will bind NIC to DPDK for both lock servers and clients.<br>
    - Setup the servers for TCP (disable DPDK)<br>
      - Correctly config the ip address and arp according to you network.<br>
      - Set congestion control algorithms.<br>
        - `python console.py setup_alg [client_id] [cc algorithm]`<br>
      - Set RTT values.<br>
        - `python console.py setup_tc [client_id] [rtt]`<br>
  - Run the programs<br>
    - `python console.py run_hcsfq`<br>