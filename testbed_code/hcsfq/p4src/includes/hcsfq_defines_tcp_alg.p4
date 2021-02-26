#define CSFQ_PORT         8888
#define LOWER_BOUND     0
#define UPPER_BOUND     15
#define NUM_FLOWS       600
#define NUM_TENANTS     10
#define RATE_ESTIMATED  1
#ifdef BPS 
    #define EPOCH_THRESHOLD 1000 * 8
    #define WINDOW_SIZE 4382
#else 
    #define EPOCH_THRESHOLD 30720
    #define WINDOW_SIZE 5001
#endif

#define RECIRCULATED    1
#define UPDATE_ALPHA    1
#define UPDATE_TOTAL_ALPHA 1
#define UPDATE_RATE     1
#define bitwidth        1

#ifdef BPS
    #define C               1151456
    #define DELTA_C         4800
    #define INIT_C          215145
#else
    #define C               35000
    #define DELTA_C         123
#endif