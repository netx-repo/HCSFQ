## Contents
- projects: contains the parameters for different algorithms (e.g., AFQ, SP-PIFO, HCSFQ)<br>
- src: source code<br>
- temp: the log files are saved here<br>
- results: the results are generated here<br> 

## How to setup and run the simulator<br>
- Software dependencies<br>
  - Java 8<br>
  - Python 2<br>
- Building<br>
  - `mvn clean compile assembly:single`<br>
- Running<br>
  - `java -jar -ea NetBench.jar [config_dir]`<br> 