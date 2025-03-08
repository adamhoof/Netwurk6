## What is it?

SpudrNet6 is intuitive tool for network communication simulation. It is designed for visualizing and experimenting with basics of network environments of small to medium scale. It offers the ability to build network topology by placing network devices and connections between them, start and pause simulation of DHCP, ARP, IP and RIP in real-time with nice logger to capture ongoing operations. The goal is to deepen the user understanding of the beforementioned network communication principles.

## Demo video

https://github.com/user-attachments/assets/08e09fbf-a004-4fe6-ac9e-8b95fa8c0107

---

### Functionality and control

* **GUI**
  * **Element placement**: The simulation allows you to place network elements, namely PCs, routers and switches, on the application workspace using the "PC", "Router, "Switch" buttons.
  * **Import and export**: The network configuration can be exported using the "Options"->"Save" menu and then re-imported from the main page using the "Load" button (sample configurations are in the main/resources folder).
  * **Element Connections**: You can create connections between elements by clicking on the "Connector" button and clicking on the 2 desired devices to arrange the network topology.
  * **Simulation Flow**: It is possible to start and pause the simulation by clicking "Start" and "Pause" buttons.
  * **Traffic Visualization**: The simulation visualizes the packet movement along the link to indicate the current data flow in real time.
  * **Log window**: The simulation contains a log window, where the current network operations or error messages are listed (e.g. when the user tries to connect the device to itself...).

    ---
* **Networking and subnetting**
  * **LAN**: Placing a router creates a default LAN for that router with a default subnet.
  * **Subnetting**: Connecting a switch to the router creates a new subnet that the router manages. Connecting the PC and the router does not create a new subnet, the PC is added to the aforementioned default subnet.
  * **WAN**: When two routers are connected, a WAN is created.

    ---
* **Device configuration**
  * **Router**: The router is configured by default (automatic subnet management, routing tables, arp cache, interfaces).
  * **PC**: In order for the PC to participate in LAN communication, it must go through the DORA process (automatic DHCP configuration) to obtain an IP address.
  * **Switch**: No configuration required, automatically maintains a CAM table.

    ---
* **Simulation function**
  * **Protocols**: The simulation implements the protocols: RIP, DHCP, ARP, IP.
  * **Inter-WAN routing**: It is provided by dynamic RIP (Routing Information Protocol), which is triggered every 30 seconds.
  * **Switching**: Switches maintain a dynamic CAM (Content Addressable Memory) table for efficient packet forwarding based on MAC addresses.
  * **LAN Communication**: Random communication between 2 PCs is selected. If both have passed the DHCP configuration, the communication is initiated and the network elements ensure the correct message delivery to the end PC.
