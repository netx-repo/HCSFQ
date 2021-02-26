#!/usr/bin/python

class Router:
    def __init__():
        pass
    def rate_estimator():
        pass
    def packet_dropping():
        pass
    def alpha_estimator():
        pass
    def run(packet):

class Hierarchical(Router):
    def __init__():
        pass

    def recirculation(packet, recir_hdr):
        return

    def run(packet):
        meta.flow_id = self.classify(packet)
        meta.tenant_id = self.classify_tenant(packet)

        ## estimate rate and set packet.label (stage 1) (only for edge switch)
        if packet.current_timestamp - reg.last_timestamp[meta.flow_id] < EPOCH_THRESHOLD:
            reg.count[meta.flow_id] += 1 (or packet.size)
        else:
            packet.label = reg.count[meta.flow_id]
            reg.last_timestamp[meta.flow_id] = packet.current_timestamp
            reg.count[meta.flow_id] = 0
        ## estimate rate of tenant (stage 1)
        if packet.current_timestamp - reg.last_timestamp[meta.tenant_id] < EPOCH_THRESHOLD:
            reg.count[meta.tenant_id] += 1 (or packet.size)
        else:
            packet.label = reg.count[meta.tenant_id]
            reg.last_timestamp[meta.tenant_id] = packet.current_timestamp
            reg.count[meta.tenant_id] = 0
        ## estimate A (stage 1)
        if packet.current_timestamp - reg.last_timestamp_A[0] < EPOCH_THRESHOLD:
            reg.count_A[0] += 1 (or packet.size)
        else:
            meta.A = reg.count_A[0]
            reg.last_timestamp_A[0] = packet.current_timestamp
            reg.count_A[0] = 0

        #### estimate rate and set packet.label -- stash/get the last count (stage 2)
        if packet.current_timestamp - reg.last_timestamp[meta.flow_id] < EPOCH_THRESHOLD:
            packet.label = reg.last_count[meta.flow_id]
        else:
            reg.last_count[meta.flow_id] = packet.label
            reg.last_timestamp[meta.flow_id] = packet.current_timestamp
        #### estimate rate of tenant -- stash/get the last count (stage 2)
        if packet.current_timestamp - reg.last_timestamp[meta.tenant_id] < EPOCH_THRESHOLD:
            packet.label = reg.last_count[meta.tenant_id]
        else:
            reg.last_count[meta.tenant_id] = packet.label
            reg.last_timestamp[meta.tenant_id] = packet.current_timestamp
        #### estimate rate of A -- stash/get the last count (stage 2)
        if packet.current_timestamp - reg.last_timestamp_A[0] < EPOCH_THRESHOLD:
            meta.A = reg.last_count_A[0]
        else:
            reg.last_count_A[0] = meta.A
            reg.last_timestamp_A[0] = packet.current_timestamp

        

class EdgeRouter(Router):
    def __init__():
        pass

    def recirculation(packet, recir_hdr):
        if recir_hdr.type == 1:
            if (recic_hdr.F > C):
                reg.alpha[0] = reg.alpha[0] - delta_C
                meta.alpha = reg.alpha[0]
            else:
                reg.alpha[0] = reg.alpha[0] + delta_C
                meta.alpha = reg.alpha[0]
        else if recir_hdr.type == 2:
            reg.alpha[0] = recir_hdr.alpha
            meta.alpha = reg.alpha[0]

    def run(packet):
        meta.flow_id = self.classify(packet)
        
        ## estimate rate and set packet.label (stage 1) (only for edge switch)
        if packet.current_timestamp - reg.last_timestamp[meta.flow_id] < EPOCH_THRESHOLD:
            reg.count[meta.flow_id] += 1 (or packet.size)
        else:
            packet.label = reg.count[meta.flow_id]
            reg.last_timestamp[meta.flow_id] = packet.current_timestamp
            reg.count[meta.flow_id] = 0

        ## estimate rate of A (stage 1)
        if packet.current_timestamp - reg.last_timestamp_A[0] < EPOCH_THRESHOLD:
            reg.count_A[0] += 1 (or packet.size)
        else:
            meta.A = reg.count_A[0]
            reg.last_timestamp_A[0] = packet.current_timestamp
            reg.count_A[0] = 0

        #### estimate rate and set packet.label -- stash/get the last count (stage 2)
        if packet.current_timestamp - reg.last_timestamp[meta.flow_id] < EPOCH_THRESHOLD:
            packet.label = reg.last_count[meta.flow_id]
        else:
            reg.last_count[meta.flow_id] = packet.label
            reg.last_timestamp[meta.flow_id] = packet.current_timestamp

        #### estimate rate of A -- stash/get the last count (stage 2)
        if packet.current_timestamp - reg.last_timestamp_A[0] < EPOCH_THRESHOLD:
            meta.A = reg.last_count_A[0]
        else:
            reg.last_count_A[0] = meta.A
            reg.last_timestamp_A[0] = packet.current_timestamp

        ## check_probability


        meta.alpha = reg.alpha[0]   #(stage 3)
        meta.randv = modify_field_rng_uniform(0, 15) #(stage 3)
        #### condition on meta.randv at each bit
        meta.p_shl_0 = packet.label << 0    #(stage 3)  
        meta.p_shl_1 = packet.label << 1    #(stage 3)
        meta.p_shl_2 = packet.label << 2    #(stage 3)
        meta.p_shl_3 = packet.label << 3    #(stage 3)

        meta.alpha_shl_4 = meta.alpha << 4 #(stage 4)
        meta.p_sum_01 = meta.p_shl_0 + meta.p_shl_1 #(stage 4)
        meta.p_sum_23 = meta.p_shl_2 + meta.p_shl_3 #(stage 4)

        meta.alpha_times_15 = meta.alpha_shl_4 - meta.alpha #(stage 5)
        meta.p_sum_all = meta.p_sum_01 + meta.p_sum_23 #(stage 5)

        #### check if (meta.alpha_times_15 < meta.p_sum_all)
        meta.minv = min(meta.alpha_times_15, meta.p_sum_all) #(stage 6)
        meta.min_alpha_n_label = min(meta.alpha, packet.label) 
        if (meta.minv == meta.alpha_times_15):               #(stage 7)
            drop() # set a ipv4 routing tag (stage 8?)
            ## drop packet
            estimate A #(total arrival rate)  (stage 1 & 2)
            estimate F #(pick from reg.last_count_F[0])  (stage 8)

            #### estimate rate of F -- stash/get the last count (stage 8)
            if packet.current_timestamp - reg.last_timestamp_F[0] < EPOCH_THRESHOLD:
                meta.F = reg.last_count_F[0]
            else:
                reg.last_count_F[0] = meta.F
                reg.last_timestamp_F[0] = packet.current_timestamp 
        else:
            enqueue() # set a ipv4 routing tag (stage 8?)

            ## accept packet
            estimate A #(total arrival rate)  (stage 1 & 2)
            estimate F #(update reg.count_F[0] and visit reg.last_count_F[0]) (stage 7 & 8)

            ## estimate rate of F (stage 1)
            if packet.current_timestamp - reg.last_timestamp_F[0] < EPOCH_THRESHOLD:
                reg.count_F[0] += 1 (or packet.size)
            else:
                meta.F = reg.count_F[0]
                reg.last_timestamp_F[0] = packet.current_timestamp
                reg.count_F[0] = 0

            #### estimate rate of F -- stash/get the last count (stage 8)
            if packet.current_timestamp - reg.last_timestamp_F[0] < EPOCH_THRESHOLD:
                meta.F = reg.last_count_F[0]
            else:
                reg.last_count_F[0] = meta.F
                reg.last_timestamp_F[0] = packet.current_timestamp
            
        ## estimate alpha
        #### check if (A >= C)
        meta.minv = min(meta.A, C)  #(stage 3)
        if (meta.minv == C):    #(stage 8)  # move to the first time of alpha register access 
            if (reg.congested[0] == FALSE):   #(stage 9)
                ## congested
                reg.congested[0] = TRUE
                reg.start_time[0] = packet.current_timestamp
            else:
                if (packet.current_timestamp > reg.start_time[0] + window_size):
                    packet.recirculate_flag = TRUE
                    recir_hdr.F = meta.F
                    recir_hdr.type = 1
                    reg.start_time[0] = packet.current_timestamp
        else:
            if (reg.congested[0] == TRUE):    #(stage 9)
                ## not congested
                reg.congested[0] = FALSE
                reg.start_time[0] = packet.current_timestamp
                predicate[1] = 1
                predicate[3] = 1
            else:
                if (packet.current_timestamp < reg.start_time[0] + K_c):
                    predicate[2] = 1
                else:
                    reg.start_time[0] = packet.current_timestamp
                    predicate[0] = 1

            switch(predicate):  #(stage 10)
                case 0001: recir_hdr.alpha = reg.tmp_alpha[0]
                           reg.tmp_alpha[0] = 0
                           packet.recirculate_flag = TRUE
                           recir_hdr.type = 2
                case 0010: reg.tmp_alpha[0] = 0
                case 0100: reg.tmp_alpha[0] = max(reg.tmp_alpha[0], packet.label)
                case 1000: reg.tmp_alpha[0] = 0
        
        if (meta.min_alpha_n_label == meta.alpha):
            packet.label = meta.alpha













