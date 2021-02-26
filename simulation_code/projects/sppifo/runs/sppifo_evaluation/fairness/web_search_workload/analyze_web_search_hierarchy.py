import numpy as np
import csv
import sys
import os


##################################
# Setup
#

print("NetBench python analysis tool v0.01")

# Usage print
def print_usage():
    print("Usage: python analyze.py /path/to/run/folder")

# Check length of arguments
if len(sys.argv) != 2:
    print("Number of arguments must be exactly two: analyze.py and /path/to/run/folder.")
    print_usage()
    exit()

# Check run folder path given as first argument
run_folder_path = sys.argv[1]
if not os.path.isdir(run_folder_path):
    print("The run folder path does not exist: " + run_folder_path)
    print_usage()
    exit()

# Create analysis folder
analysis_folder_path = run_folder_path + '/analysis'
if not os.path.exists(analysis_folder_path):
    os.makedirs(analysis_folder_path)


##################################
# Analyze flow completion
#
def analyze_flow_completion():
    with open(run_folder_path + '/flow_completion.csv.log') as file:
        reader = csv.reader(file)

        # To enable preliminary read to determine size:
        # data = list(reader)
        # row_count = len(data)

        # Column lists
        flow_ids = []
        source_ids = []
        target_ids = []
        sent_bytes = []
        total_size_bytes = []
        start_time = []
        end_time = []
        duration = []
        completed = []
        source_ports = []
        target_ports = []

#         print("Reading in flow completion log file...")

        # Read in column lists
        for row in reader:
            flow_ids.append(float(row[0]))
            source_ids.append(float(row[1]))
            target_ids.append(float(row[2]))
            sent_bytes.append(float(row[3]))
            total_size_bytes.append(float(row[4]))
            start_time.append(float(row[5]))
            end_time.append(float(row[6]))
            duration.append(float(row[7]))
            completed.append(row[8] == 'TRUE')
            source_ports.append(int(row[9]))
            target_ports.append(int(row[10]))


            if len(row) != 11:
                print("Invalid row: ", row)
                exit()

#         print("Calculating statistics...")

        statistics = {
            'general_num_flows': len(flow_ids),
            'general_num_unique_sources': len(set(source_ids)),
            'general_num_unique_targets': len(set(target_ids)),
            'general_flow_size_bytes_mean': np.mean(total_size_bytes),
            'general_flow_size_bytes_std': np.std(total_size_bytes)
        }

        range_low =                     [-1,            10000,      20000,        30000,      50000,    80000,       200000,      2000000,            -1]
        range_high =                    [-1,            10000,      20000,        30000,      50000,    80000,      1000000,           -1,        100000]
        range_name =                    ["all",         "10KB",     "20KB",      "30KB",     "50KB",   "80KB",  "200KB-1MB",    "geq_2MB",  "less_100KB"]
        range_completed_duration =      [[],            [],            [],           [],         [],       [],           [],           [],            []]
        range_completed_throughput =    [[],            [],            [],           [],         [],       [],           [],           [],            []]
        range_num_finished_flows =      [0,             0,             0,             0,          0,        0,            0,            0,             0]
        range_num_unfinished_flows =    [0,             0,             0,             0,          0,        0,            0,            0,             0]
        range_low_eq =                  [0,             1,             1,             1,          1,        1,            1,            1,             1]
        range_high_eq =                 [0,             1,             1,             1,          1,        1,            1,            0,             1]

        range_completed_duration_tenant_1 =      [[],            [],            [],           [],         [],       [],           [],           [],            []]
        range_completed_throughput_tenant_1 =    [[],            [],            [],           [],         [],       [],           [],           [],            []]
        range_num_finished_flows_tenant_1 =      [0,             0,             0,             0,          0,        0,            0,            0,             0]
        range_num_unfinished_flows_tenant_1 =    [0,             0,             0,             0,          0,        0,            0,            0,             0]

        range_completed_duration_tenant_2 =      [[],            [],            [],           [],         [],       [],           [],           [],            []]
        range_completed_throughput_tenant_2 =    [[],            [],            [],           [],         [],       [],           [],           [],            []]
        range_num_finished_flows_tenant_2 =      [0,             0,             0,             0,          0,        0,            0,            0,             0]
        range_num_unfinished_flows_tenant_2 =    [0,             0,             0,             0,          0,        0,            0,            0,             0]

        bytes_transfered_tenant_1 = 0
        bytes_transfered_tenant_2 = 0

        range_completed_duration_tenant_x = []
        range_completed_throughput_tenant_x = []
        range_num_finished_flows_tenant_x = []
        range_num_unfinished_flows_tenant_x = []
        bytes_transfered_tenant_x = []

        number_of_tenants = 100
        for i in range(number_of_tenants):
            range_completed_duration_tenant_x.append([[],            [],            [],           [],         [],       [],           [],           [],            []])
            range_completed_throughput_tenant_x.append([[],            [],            [],           [],         [],       [],           [],           [],            []])
            range_num_finished_flows_tenant_x.append([0,             0,             0,             0,          0,        0,            0,            0,             0])
            range_num_unfinished_flows_tenant_x.append([0,             0,             0,             0,          0,        0,            0,            0,             0])
            bytes_transfered_tenant_x.append(0)

        

        # Go over all flows
        for i in range(0, len(flow_ids)):
            bytes_transfered_tenant_x[source_ports[i]] += sent_bytes[i]
            # Range-specific
            for j in range(0, len(range_name)):
                if (
                        (range_low[j] == -1 or (range_low_eq[j] == 0 and total_size_bytes[i] > range_low[j]) or (range_low_eq[j] == 1 and total_size_bytes[i] >= range_low[j])) and
                        (range_high[j] == -1 or (range_high_eq[j] == 0 and total_size_bytes[i] < range_high[j]) or (range_high_eq[j] == 1 and total_size_bytes[i] <= range_high[j]))
                ):
                    if completed[i]:
#                         if (source_ids[i] >= 7.9) and (source_ids[i] < 13.9):
#                         print(flow_ids[i])

                        if (flow_ids[i] >= 1000000):
                            range_num_finished_flows_tenant_x[source_ports[i]][j] += 1
                            range_completed_duration_tenant_x[source_ports[i]][j].append(duration[i])
                            range_completed_throughput_tenant_x[source_ports[i]][j].append(total_size_bytes[i] * 8 / (duration[i] + 0.0001))
                        # bytes_transfered_tenant_x[source_ports[i]] += total_size_bytes[i]
#                         if (source_ports[i] == 10):
#                             if (flow_ids[i] >= 1000000):
#                                 range_num_finished_flows_tenant_1[j] += 1
#                                 range_completed_duration_tenant_1[j].append(duration[i])
#                                 range_completed_throughput_tenant_1[j].append(total_size_bytes[i] * 8 / (duration[i] + 0.0001))
#                             bytes_transfered_tenant_1 += total_size_bytes[i]

# #                         elif (source_ids[i] >= 13.9) and (source_ids[i] < 15.9):
#                         else:
#                             if (flow_ids[i] >= 1000000):
#                                 range_num_finished_flows_tenant_2[j] += 1
#                                 range_completed_duration_tenant_2[j].append(duration[i])
#                                 range_completed_throughput_tenant_2[j].append(total_size_bytes[i] * 8 / (duration[i] + 0.0001))
#                             bytes_transfered_tenant_2 += total_size_bytes[i]
                        if (flow_ids[i] >= 1000000):
                            # if (int(source_ids[i]) % 16 != int(target_ids[i]) % 16):
                            range_num_finished_flows[j] += 1
                            range_completed_duration[j].append(duration[i])
                            range_completed_throughput[j].append(total_size_bytes[i] * 8 / (duration[i] + 0.0001))

                    else:
                        if (flow_ids[i] >= 1000000):
                            range_num_unfinished_flows_tenant_x[source_ports[i]][j] += 1
                        
#                         if (source_ports[i] == 10):
# #                         if (int(source_ids[i]) % 16 <12):
#                             if (flow_ids[i] >= 1000000):
#                                 range_num_unfinished_flows_tenant_1[j] += 1
#                                 ### try different option: count durations of flows didn't finish.
# #                                 range_completed_duration_tenant_1[j].append(duration[i])
#                             bytes_transfered_tenant_1 += sent_bytes[i]

# #                         elif (source_ids[i] >= 13.9) and (source_ids[i] < 15.9):
#                         else:
#                             if (flow_ids[i] >= 1000000):
#                                 range_num_unfinished_flows_tenant_2[j] += 1
#                                 ### try different option: count durations of flows didn't finish.
# #                                 range_completed_duration_tenant_2[j].append(duration[i])
#                             bytes_transfered_tenant_2 += sent_bytes[i]
                        if (flow_ids[i] >= 1000000):
                            if (int(source_ids[i]) % 16 != int(target_ids[i]) % 16):
                                range_num_unfinished_flows[j] += 1
                            ### try different option: count durations of flows didn't finish.
#                             range_completed_duration[j].append(duration[i])


        # Ranges statistics
        # statistics['all_throughput_tenant_1'] = bytes_transfered_tenant_1
        # statistics['all_throughput_tenant_2'] = bytes_transfered_tenant_2
        # for x in range(number_of_tenants):
        #     statistics["all_throughput_tenant_" + str(x)] = bytes_transfered_tenant_x[x]
        for j in range(0, len(range_name)):

            # Number of finished flows
            statistics[range_name[j] + '_num_flows'] = range_num_finished_flows[j] + range_num_unfinished_flows[j]
            statistics[range_name[j] + '_num_finished_flows'] = range_num_finished_flows[j]
            statistics[range_name[j] + '_num_unfinished_flows'] = range_num_unfinished_flows[j]

            # statistics[range_name[j] + '_num_flows_tenant_1'] = range_num_finished_flows_tenant_1[j] + range_num_unfinished_flows_tenant_1[j]
            # statistics[range_name[j] + '_num_finished_flows_tenant_1'] = range_num_finished_flows_tenant_1[j]
            # statistics[range_name[j] + '_num_unfinished_flows_tenant_1'] = range_num_unfinished_flows_tenant_1[j]

            # statistics[range_name[j] + '_num_flows_tenant_2'] = range_num_finished_flows_tenant_2[j] + range_num_unfinished_flows_tenant_2[j]
            # statistics[range_name[j] + '_num_finished_flows_tenant_2'] = range_num_finished_flows_tenant_2[j]
            # statistics[range_name[j] + '_num_unfinished_flows_tenant_2'] = range_num_unfinished_flows_tenant_2[j]

            for x in range(number_of_tenants):
                if range_num_finished_flows_tenant_x[x][j] + range_num_unfinished_flows_tenant_x[x][j] != 0:
                    # for x in range(number_of_tenants):
                    statistics["all_throughput_tenant_" + str(x)] = bytes_transfered_tenant_x[x]
                    statistics[range_name[j] + '_num_flows_tenant_' + str(x)] = range_num_finished_flows_tenant_x[x][j] + range_num_unfinished_flows_tenant_x[x][j]
                    statistics[range_name[j] + '_num_finished_flows_tenant_' + str(x)] = range_num_finished_flows_tenant_x[x][j]
                    statistics[range_name[j] + '_num_unfinished_flows_tenant_' + str(x)] = range_num_unfinished_flows_tenant_x[x][j]

            total = (range_num_finished_flows[j] + range_num_unfinished_flows[j])
            # total_tenant_1 = (range_num_finished_flows_tenant_1[j] + range_num_unfinished_flows_tenant_1[j])
            # total_tenant_2 = (range_num_finished_flows_tenant_2[j] + range_num_unfinished_flows_tenant_2[j])
            if range_num_finished_flows[j] != 0:
                statistics[range_name[j] + '_flows_completed_fraction'] = float(range_num_finished_flows[j]) / float(total)
                statistics[range_name[j] + '_mean_fct_ns'] = np.mean(range_completed_duration[j])
                statistics[range_name[j] + '_median_fct_ns'] = np.median(range_completed_duration[j])
                statistics[range_name[j] + '_99th_fct_ns'] = np.percentile(range_completed_duration[j], 99)
                statistics[range_name[j] + '_99.9th_fct_ns'] = np.percentile(range_completed_duration[j], 99.9)
                statistics[range_name[j] + '_mean_fct_ms'] = statistics[range_name[j] + '_mean_fct_ns'] / 1000000
                statistics[range_name[j] + '_median_fct_ms'] = statistics[range_name[j] + '_median_fct_ns'] / 1000000
                statistics[range_name[j] + '_99th_fct_ms'] = statistics[range_name[j] + '_99th_fct_ns'] / 1000000
                statistics[range_name[j] + '_99.9th_fct_ms'] = statistics[range_name[j] + '_99.9th_fct_ns'] / 1000000
                statistics[range_name[j] + '_throughput_mean_Gbps'] = np.mean(range_completed_throughput[j])
                statistics[range_name[j] + '_throughput_median_Gbps'] = np.median(range_completed_throughput[j])
                statistics[range_name[j] + '_throughput_99th_Gbps'] = np.percentile(range_completed_throughput[j], 99)
                statistics[range_name[j] + '_throughput_99.9th_Gbps'] = np.percentile(range_completed_throughput[j], 99.9)
                statistics[range_name[j] + '_throughput_1th_Gbps'] = np.percentile(range_completed_throughput[j], 1)
                statistics[range_name[j] + '_throughput_0.1th_Gbps'] = np.percentile(range_completed_throughput[j], 0.1)
            else:
                statistics[range_name[j] + '_flows_completed_fraction'] = 0

            for x in range(number_of_tenants):
                if range_num_finished_flows_tenant_x[x][j] + range_num_unfinished_flows_tenant_x[x][j] != 0:
                    if range_num_finished_flows_tenant_x[x][j] != 0:
                        statistics[range_name[j] + '_flows_completed_fraction_tenant_' + str(x)] = float(range_num_finished_flows_tenant_x[x][j]) / float(range_num_finished_flows_tenant_x[x][j] + range_num_unfinished_flows_tenant_x[x][j])
                        statistics[range_name[j] + '_mean_fct_ns_tenant_' + str(x)] = np.mean(range_completed_duration_tenant_x[x][j])
                        statistics[range_name[j] + '_median_fct_ns_tenant_' + str(x)] = np.median(range_completed_duration_tenant_x[x][j])
                        statistics[range_name[j] + '_99th_fct_ns_tenant_' + str(x)] = np.percentile(range_completed_duration_tenant_x[x][j], 99)
                        statistics[range_name[j] + '_99.9th_fct_ns_tenant_' + str(x)] = np.percentile(range_completed_duration_tenant_x[x][j], 99.9)
                        statistics[range_name[j] + '_mean_fct_ms_tenant_' + str(x)] = statistics[range_name[j] + '_mean_fct_ns_tenant_' + str(x)] / 1000000
                        statistics[range_name[j] + '_median_fct_ms_tenant_' + str(x)] = statistics[range_name[j] + '_median_fct_ns_tenant_' + str(x)] / 1000000
                        statistics[range_name[j] + '_99th_fct_ms_tenant_' + str(x)] = statistics[range_name[j] + '_99th_fct_ns_tenant_' + str(x)] / 1000000
                        statistics[range_name[j] + '_99.9th_fct_ms_tenant_' + str(x)] = statistics[range_name[j] + '_99.9th_fct_ns_tenant_' + str(x)] / 1000000
                        statistics[range_name[j] + '_throughput_mean_Gbps_tenant_' + str(x)] = np.mean(range_completed_throughput_tenant_x[x][j])
                        statistics[range_name[j] + '_throughput_median_Gbps_tenant_' + str(x)] = np.median(range_completed_throughput_tenant_x[x][j])
                        statistics[range_name[j] + '_throughput_99th_Gbps_tenant_' + str(x)] = np.percentile(range_completed_throughput_tenant_x[x][j], 99)
                        statistics[range_name[j] + '_throughput_99.9th_Gbps_tenant_' + str(x)] = np.percentile(range_completed_throughput_tenant_x[x][j], 99.9)
                        statistics[range_name[j] + '_throughput_1th_Gbps_tenant_' + str(x)] = np.percentile(range_completed_throughput_tenant_x[x][j], 1)
                        statistics[range_name[j] + '_throughput_0.1th_Gbps_tenant_' + str(x)] = np.percentile(range_completed_throughput_tenant_x[x][j], 0.1)
                    else:
                        statistics[range_name[j] + '_flows_completed_fraction_tenant_' + str(x)] = 0
            small = []
            large = []
            
            for x in range(54):
                if range_name[j] + "_mean_fct_ms_tenant_" + str(x) in statistics:
                    if x < 27:
                        small.append(statistics[range_name[j] + "_mean_fct_ms_tenant_" + str(x)])
                    else:
                        large.append(statistics[range_name[j] + "_mean_fct_ms_tenant_" + str(x)])
            print(small+large)
            # print(large)
            print(range_name[j], "small mean:", np.mean(small))
            print(range_name[j], "large mean:", np.mean(large))
            # if range_num_finished_flows_tenant_1[j] != 0:
            #     statistics[range_name[j] + '_flows_completed_fraction_tenant_1'] = float(range_num_finished_flows_tenant_1[j]) / float(total_tenant_1)
            #     statistics[range_name[j] + '_mean_fct_ns_tenant_1'] = np.mean(range_completed_duration_tenant_1[j])
            #     statistics[range_name[j] + '_median_fct_ns_tenant_1'] = np.median(range_completed_duration_tenant_1[j])
            #     statistics[range_name[j] + '_99th_fct_ns_tenant_1'] = np.percentile(range_completed_duration_tenant_1[j], 99)
            #     statistics[range_name[j] + '_99.9th_fct_ns_tenant_1'] = np.percentile(range_completed_duration_tenant_1[j], 99.9)
            #     statistics[range_name[j] + '_mean_fct_ms_tenant_1'] = statistics[range_name[j] + '_mean_fct_ns_tenant_1'] / 1000000
            #     statistics[range_name[j] + '_median_fct_ms_tenant_1'] = statistics[range_name[j] + '_median_fct_ns_tenant_1'] / 1000000
            #     statistics[range_name[j] + '_99th_fct_ms_tenant_1'] = statistics[range_name[j] + '_99th_fct_ns_tenant_1'] / 1000000
            #     statistics[range_name[j] + '_99.9th_fct_ms_tenant_1'] = statistics[range_name[j] + '_99.9th_fct_ns_tenant_1'] / 1000000
            #     statistics[range_name[j] + '_throughput_mean_Gbps_tenant_1'] = np.mean(range_completed_throughput_tenant_1[j])
            #     statistics[range_name[j] + '_throughput_median_Gbps_tenant_1'] = np.median(range_completed_throughput_tenant_1[j])
            #     statistics[range_name[j] + '_throughput_99th_Gbps_tenant_1'] = np.percentile(range_completed_throughput_tenant_1[j], 99)
            #     statistics[range_name[j] + '_throughput_99.9th_Gbps_tenant_1'] = np.percentile(range_completed_throughput_tenant_1[j], 99.9)
            #     statistics[range_name[j] + '_throughput_1th_Gbps_tenant_1'] = np.percentile(range_completed_throughput_tenant_1[j], 1)
            #     statistics[range_name[j] + '_throughput_0.1th_Gbps_tenant_1'] = np.percentile(range_completed_throughput_tenant_1[j], 0.1)
            # else:
            #     statistics[range_name[j] + '_flows_completed_fraction_tenant_1'] = 0

            # if range_num_finished_flows_tenant_2[j] != 0:
            #     statistics[range_name[j] + '_flows_completed_fraction_tenant_2'] = float(range_num_finished_flows_tenant_2[j]) / float(total_tenant_2)
            #     statistics[range_name[j] + '_mean_fct_ns_tenant_2'] = np.mean(range_completed_duration_tenant_2[j])
            #     statistics[range_name[j] + '_median_fct_ns_tenant_2'] = np.median(range_completed_duration_tenant_2[j])
            #     statistics[range_name[j] + '_99th_fct_ns_tenant_2'] = np.percentile(range_completed_duration_tenant_2[j], 99)
            #     statistics[range_name[j] + '_99.9th_fct_ns_tenant_2'] = np.percentile(range_completed_duration_tenant_2[j], 99.9)
            #     statistics[range_name[j] + '_mean_fct_ms_tenant_2'] = statistics[range_name[j] + '_mean_fct_ns_tenant_2'] / 1000000
            #     statistics[range_name[j] + '_median_fct_ms_tenant_2'] = statistics[range_name[j] + '_median_fct_ns_tenant_2'] / 1000000
            #     statistics[range_name[j] + '_99th_fct_ms_tenant_2'] = statistics[range_name[j] + '_99th_fct_ns_tenant_2'] / 1000000
            #     statistics[range_name[j] + '_99.9th_fct_ms_tenant_2'] = statistics[range_name[j] + '_99.9th_fct_ns_tenant_2'] / 1000000
            #     statistics[range_name[j] + '_throughput_mean_Gbps_tenant_2'] = np.mean(range_completed_throughput_tenant_2[j])
            #     statistics[range_name[j] + '_throughput_median_Gbps_tenant_2'] = np.median(range_completed_throughput_tenant_2[j])
            #     statistics[range_name[j] + '_throughput_99th_Gbps_tenant_2'] = np.percentile(range_completed_throughput_tenant_2[j], 99)
            #     statistics[range_name[j] + '_throughput_99.9th_Gbps_tenant_2'] = np.percentile(range_completed_throughput_tenant_2[j], 99.9)
            #     statistics[range_name[j] + '_throughput_1th_Gbps_tenant_2'] = np.percentile(range_completed_throughput_tenant_2[j], 1)
            #     statistics[range_name[j] + '_throughput_0.1th_Gbps_tenant_2'] = np.percentile(range_completed_throughput_tenant_2[j], 0.1)
            # else:
            #     statistics[range_name[j] + '_flows_completed_fraction_tenant_2'] = 0
#         print(statistics["all_flows_completed_fraction"])
        # Print raw results
#         print('Writing to result file flow_completion.statistics...')
        to_sort = []
        to_sort_t1 = []
        to_sort_t2 = []
        print("tenant 1")
        for i in range(0, len(flow_ids)):
#             if (source_ids[i] >= 7.9) and (source_ids[i] < 13.9):
            if (flow_ids[i] >=1000000) and ( total_size_bytes[i] <= 100000):
                to_sort.append(duration[i])
                if (int(source_ids[i]) % 144 < 63.9):
                    to_sort_t1.append(duration[i])
                else:
                    to_sort_t2.append(duration[i])
        to_sort.sort()
        to_sort_t1.sort()
        to_sort_t2.sort()
#         print(to_sort)
        with open(analysis_folder_path + '/flow_completion.statistics', 'w+') as outfile:
            for key, value in sorted(statistics.items()):
                outfile.write(str(key) + "=" + str(value) + "\n")

        # with open(analysis_folder_path + "/fct_cdf.stat", "w+") as outfile:
        #     outfile.write("[")
        #     for i in range(len(to_sort) - 1):
        #         item = to_sort[i]
        #         outfile.write("%s," % item)
        #     item = to_sort[len(to_sort) - 1]
        #     outfile.write("%s]\n" % item)

        # with open(analysis_folder_path + "/fct_cdf_t1.stat", "w+") as outfile:
        #     outfile.write("[")
        #     for i in range(len(to_sort_t1) - 1):
        #         item = to_sort_t1[i]
        #         outfile.write("%s," % item)
        #     item = to_sort[len(to_sort_t1) - 1]
        #     outfile.write("%s]\n" % item)

        # with open(analysis_folder_path + "/fct_cdf_t2.stat", "w+") as outfile:
        #     outfile.write("[")
        #     for i in range(len(to_sort_t2) - 1):
        #         item = to_sort_t2[i]
        #         outfile.write("%s," % item)
        #     item = to_sort[len(to_sort_t2) - 1]
        #     outfile.write("%s]\n" % item)

# Call analysis functions
analyze_flow_completion()
