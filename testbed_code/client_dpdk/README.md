# server (netx8):
go to `/home/user/hz/incast_dpdk/server`

Compile:
`make`

Run:
`sudo ./build/server -l 0`

### Note:
- `-l` denotes the list of thread IDs.


# client (netx1,2,3)
go to `/home/user/hz/incast_dpdk/client`

Compile:
`make`

Run:
`sudo ./build/client -l 0 -- -x 1 -s 100000`

### how to modify the ethernet frame size:
modify the length of the int array on L93 in `util/util.h`

### Note:
- `-l` denotes the list of thread IDs.
- `-x 1`: specify the client_ip: "10.1.0.<x>"
- `-s 100000`: pkts/ms
