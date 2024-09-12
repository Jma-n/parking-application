For the Arduino Code: 
ParkingClient.ino :This Arduino-based project is part of a LoRa-enabled smart parking system that detects parking spot availability, measures battery voltage, and transmits the data wirelessly to a receiver (such as a Raspberry Pi). The system is built to work with a SparkFun board using the LoRa protocol and an ultrasonic sensor for distance measurement.

# Key Components

- SparkFun LoRa Module: Used for sending data over LoRa communication.
- Ultrasonic Sensor (NewPing): Measures the distance between the sensor and an object (e.g., a car in a parking spot).
- Battery Voltage Measurement: Monitors battery levels and sends alerts if battery is low.
- Low Power Management: The system enters deep sleep to conserve battery life when idle.

Features
LoRa Communication:

The system uses the LoRa protocol to transmit data from the parking spot sensor to a remote receiver.
It broadcasts at a frequency of 864.1 MHz with a transmit power of 20 dBm.
Unique Device ID:

A unique ID for each device is generated based on the microcontroller's unique hardware identifier, which is used to differentiate multiple devices in the network.
Ultrasonic Sensor (Distance Measurement):

The sensor checks the parking space by measuring the distance to the nearest object.
If the distance is below a certain threshold, the space is marked as occupied; otherwise, it’s free.
Battery Voltage Monitoring:

The system continuously monitors battery voltage through an analog pin and checks if it falls below critical or low thresholds (e.g., 3.0V and 2.5V).
The battery status is encoded along with the parking status and transmitted to the server.
Power Management:

After each cycle of data collection and transmission, the system goes into deep sleep for 10 seconds to conserve power, using the Watchdog timer.
Flash Memory Storage:

The system stores and retrieves an interval value in non-volatile Flash memory, allowing for persistent configuration across reboots.
The interval value can be updated via serial commands.
Code Breakdown
Setup Function:

Initializes the serial communication, LoRa module, and sets the broadcast frequency and modulation parameters.
Reads a previously saved interval value from Flash memory and sets it for the main loop's sleep cycle.
Main Loop:

The system first checks for serial commands to modify the sleep interval.
It then measures the distance using the ultrasonic sensor. Based on the measured distance, it determines the current state (occupied or free).
The system measures battery voltage and encodes the parking status along with battery voltage before sending it over LoRa.
If there’s no change in parking status, it doesn’t send updates.
The system then enters a deep sleep for 10 seconds before waking up for the next loop cycle.
Distance Measurement:

The ultrasonic sensor sends a pulse and measures the time taken for the echo to return, converting it into distance in centimeters.
Battery Voltage Measurement:

The system reads the analog input connected to a voltage divider circuit, calculates the actual battery voltage, and checks if it's below critical levels.
State Encoding:

The system encodes the parking status and battery voltage into a single integer value for efficient transmission. The parking status is in the thousands place, and the battery voltage (multiplied by 100) is in the remainder.
LoRa Transmission:

A message is constructed containing the unique device ID, parking status, and battery voltage, which is then sent over LoRa to the receiver.
It waits for an acknowledgment that the packet was successfully sent.
Flash Memory Operations:

The system can read and write the loop interval value to Flash memory using the FlashStorage library, allowing persistent storage of settings across resets.
Commands
The system can be controlled via serial commands sent through the USB port:

save interval <value>: Updates the loop interval and saves it to Flash storage.
show interval: Displays the current saved interval.


ParkingServer.ino: 
