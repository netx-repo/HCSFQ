#define CSFQ_PORT         8888
#define LOWER_BOUND     0
#define UPPER_BOUND     15
#define NUM_FLOWS       600
#define NUM_TENANTS     10
#define RATE_ESTIMATED  1
#define INCREASE_SPEED  13
#define QDEPTH_THRESHOLD 2000
#ifdef BPS 
    #define EPOCH_THRESHOLD 800 * 100 * 10
    #define WINDOW_SIZE 838200
    // #define WINDOW_SIZE 100
    #define EPOCH_THRESHOLD_FOR_AF 838200
    // #define EPOCH_THRESHOLD 8000
    // #define WINDOW_SIZE 4382
#else 
    #define EPOCH_THRESHOLD 30720
    #define WINDOW_SIZE 5001
#endif

#define RECIRCULATED    1
#define UPDATE_ALPHA    1
#define UPDATE_TOTAL_ALPHA 1
#define UPDATE_RATE     1
#define bitwidth        3

#ifdef BPS
    #define C               115200 * 100 * 10
    // #define C 100000 * 100 * 20
    // #define DELTA_C         480 * 10
    #define DELTA_C         400
    #define INIT_C          21514 * 100 * 10
    
    // #define C               1151456
    // #define DELTA_C         6000
    // #define INIT_C          515145
#else
    #define C               35000
    #define DELTA_C         123
#endif
