# Twenty Years After: Hierarchical Core-Stateless Fair Queueing
## 0. Introduction<br>
HCSFQ is to the best of our knowledge, the first scalable practical solution to implement hierarchical fair queueing on commodity hardware at line rate with no per-flow state and no hierarchical queue management. 

The details of the design are available in our NSDI'21 paper "Twenty Years After: Hierarchical Core-Stateless Fair Queueing". [[Paper]](http://cs.jhu.edu/~zhuolong)

This repo contains our code for the hardware prototype and the large-scale simulations.
Below we show how to configure the environment, how to run the system, and how to reproduce the results.

## 1. Content<br>
- simulation_code/: contains the Java-based implementation built on top of NetBench. Check [this](simulation_code/)<br>
- testbed_code/<br>
  - client_dpdk/: dpdk code to run on clients (for UDP experiments).<br>
  - hcsfq/<br>
    - controller_init/: control-plane module for HCSFQ.<br>
    - p4src/: data-plane module (p4 code) for HCSFQ.<br>
  - simple_switch/<br>
    - controller_init/: control-plane module for simple switch.<br>
    - p4src/: data-plane module (p4 code) for simple switch.<br>
  - console.py: A script to help config and run different set of evaluations.<br>
- README.md: This file.<br>

## 2. Testbed environment requirement<br>
- Hardware
  - A Barefoot Tofino switch.<br>
  - Servers with a DPDK-compatible NIC (we used an Intel XL710 for 40GbE QSFP+) and multi-core CPU.<br>
- Software<br>
  The current version of HCSFQ is tested on:<br>
    - Tofino SDK (version after 8.2.2) on the switch.<br>
    - DPDK (16.11.1) on the servers.<br>
      You can either refer to the [official guige](https://doc.dpdk.org/guides/linux_gsg/quick_start.html) or use the tools.sh script in dpdk_code/.
        ```shell
        cd dpdk_code
        ./tools.sh install_dpdk
        ```
  We provide easy-to-use scripts to run the experiments and to analyze the results. To use the scripts, you need:
    - Python2.7, Paramiko at your endhost.<br>
      ```pip install paramiko```

## 3. How to run<br>
Please refer to [testbed_code](testbed_code/) for testbed experiements and [simulation_code](simulation_code) for simulations.
## 4. Contact<br>
You can email us at `zhuolong at cs dot jhu dot edu` if you have any questions.
