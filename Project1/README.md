# Instructions

On the "Scripts" folder, there is a set of scripts that will help to use the program. Those which extension is ".ps1" are for Powershell and those which extension is ".sh" are for Bash. They do the same thing. For generic purposes, we will indicate ".ext" to reference ".ps1" and ".sh".
 - The first thing to do is to execute the script *Compile.ext*.
 - Then, it is possible to execute the script *Server.ext* that will ask some parameters and start running the server program. To stop the server type CTRL-C.
 - Run *Server.ext* how many times you want to open several severs, just use different server ids and ports.
 - Finally, execute the script *Client.ext*, which will ask some parameters related to the server you want to connect to, and the order will be sent to the server if the parameters are valid and nothing in the network blocks the communication.
