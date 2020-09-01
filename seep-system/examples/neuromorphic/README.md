### Follow instructions to run the event-based corner detection query

1. Go to /lib folder and replace the jar inside with the current snapshot of the system.
Build the seep-system by Maven commands. To avoid Maven test, run ./frontier.sh script to generate system jar file for simplicity.

2. Then compile the query with Ant targets. Run the query at root directory.

3. The compiled query, ready to run in Frontier is under /dist.

###Change query settings:

Replication factor at Base class

Batch size at Source class

Reference image at Sink class. 


###  Example commands to run this query in the local mode
This requires 4 terminals: 

Master node:
```
java -jar lib/seep-system-0.0.1-SNAPSHOT.jar Master `pwd`/dist/neuromorphic.jar Base
```
   
Worker nodes:
```
java -jar lib/seep-system-0.0.1-SNAPSHOT.jar Worker 3501
java -jar lib/seep-system-0.0.1-SNAPSHOT.jar Worker 3502
java -jar lib/seep-system-0.0.1-SNAPSHOT.jar Worker 3503
```   
Add ```2>&1 | tee operatorName.log``` to store the log file for each node

Refer to Frontier documents when running at other modes.