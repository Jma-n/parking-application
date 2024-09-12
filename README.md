For the Arduino Code: 
ParkingClient.ino :This Arduino-based project is part of a LoRa-enabled smart parking system that detects parking spot availability, monitors battery voltage, and transmits data wirelessly to a receiver (e.g., Raspberry Pi). The system uses a SparkFun board with an ultrasonic sensor for distance measurement and operates with low power consumption for extended battery life.

Key Components
SparkFun LoRa Module: Handles data transmission using the LoRa protocol.
Ultrasonic Sensor: Measures the distance to detect vehicle presence in the parking spot.
Battery Monitoring: Tracks battery voltage to ensure operational stability.
Power Management: The system enters deep sleep mode between cycles to conserve energy.
Features
LoRa Communication
The system uses LoRa to send data about parking spot status and battery voltage. It broadcasts at 864.1 MHz with a transmit power of 20 dBm.

Unique Device ID
A unique ID is generated for each device based on the microcontroller's hardware identifier, allowing multiple devices to be differentiated within the network.

Parking Spot Detection (Ultrasonic Sensor)
The ultrasonic sensor measures the distance to an object. If the distance is below a predefined threshold, the parking spot is marked as occupied; otherwise, it is considered free.

Battery Voltage Monitoring
The system monitors the battery voltage using an analog pin. If the voltage falls below critical thresholds (e.g., 3.0V and 2.5V), the system encodes and transmits the battery status along with the parking status.

Low Power Operation
After each data transmission cycle, the system enters deep sleep for 10 seconds, conserving battery power. This is managed using the Watchdog timer.

Flash Storage for Configurations
The system stores and retrieves the loop interval value from Flash memory, ensuring persistent configuration even after reboots. The interval can be updated via serial commands.

Code Breakdown
setup()
Initializes:
Serial communication
LoRa module (sets the broadcast frequency and modem configuration)
Distance sensor and battery monitor
Reads the saved loop interval from Flash memory.
loop()
Checks for any serial commands (e.g., updating the loop interval).
Measures the distance to determine parking status:
Occupied if distance is below threshold.
Free if no object is detected.
Measures the battery voltage and encodes the parking and battery statuses.
Sends the data via LoRa if the status has changed since the last transmission.
Enters deep sleep for 10 seconds to save power.
Distance Measurement
The ultrasonic sensor measures the distance between the sensor and the nearest object using the NewPing library. If the distance is within the defined range, it updates the parking status.

Battery Voltage Measurement
The system reads the battery voltage through a voltage divider circuit connected to an analog pin. It checks if the voltage is below the low or critical thresholds, ensuring that low power levels are reported.

State Encoding
Parking status and battery voltage are combined into a single integer using the formula:

makefile
Code kopieren
encodedStatus = parkingStatus * 1000 + (batteryVoltage * 100)
This encoded status is sent via LoRa to the receiver.

LoRa Transmission
The system sends a message in the format:

ruby
Code kopieren
<uniqueID>:<parkingStatus>:<batteryVoltage>
If the packet is successfully sent, an acknowledgment is received.

Flash Memory Operations
The loop interval is stored and retrieved from Flash memory, allowing persistent configuration using:

save interval <value> to save a new interval.
show interval to display the current saved interval.
Serial Commands
You can interact with the system via the USB serial connection with the following commands:

save interval <value>
Saves a new loop interval (in milliseconds) to Flash storage.

show interval
Displays the current saved interval.



ParkingServer.ino: 
