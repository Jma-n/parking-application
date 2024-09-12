For the Arduino Code: 
ParkingClient.ino :This Arduino-based project is part of a LoRa-enabled smart parking system that detects parking spot availability, monitors battery voltage, and transmits data wirelessly to a receiver (e.g., Raspberry Pi). The system uses a SparkFun board with an ultrasonic sensor for distance measurement and operates with low power consumption for extended battery life.

## Key Components

- **SparkFun LoRa Module**: Handles data transmission using the LoRa protocol.
- **Ultrasonic Sensor**: Measures the distance to detect vehicle presence in the parking spot.
- **Battery Monitoring**: Tracks battery voltage to ensure operational stability.
- **Power Management**: The system enters deep sleep mode between cycles to conserve energy.

## Features

1. **LoRa Communication**  
   The system uses LoRa to send data about parking spot status and battery voltage. It broadcasts at **864.1 MHz** with a transmit power of **20 dBm**.

2. **Unique Device ID**  
   A unique ID is generated for each device based on the microcontroller's hardware identifier, allowing multiple devices to be differentiated within the network.

3. **Parking Spot Detection (Ultrasonic Sensor)**  
   The ultrasonic sensor measures the distance to an object. If the distance is below a predefined threshold, the parking spot is marked as **occupied**; otherwise, it is considered **free**.

4. **Battery Voltage Monitoring**  
   The system monitors the battery voltage using an analog pin. If the voltage falls below critical thresholds (e.g., **3.0V** and **2.5V**), the system encodes and transmits the battery status along with the parking status.

5. **Low Power Operation**  
   After each data transmission cycle, the system enters **deep sleep** for 10 seconds, conserving battery power. This is managed using the **Watchdog timer**.

6. **Flash Storage for Configurations**  
   The system stores and retrieves the loop interval value from **Flash memory**, ensuring persistent configuration even after reboots. The interval can be updated via serial commands.

## Code Breakdown

### `setup()`

- Initializes:
  - Serial communication
  - LoRa module (sets the broadcast frequency and modem configuration)
  - Distance sensor and battery monitor
- Reads the saved loop interval from **Flash memory**.

### `loop()`

- Checks for any serial commands (e.g., updating the loop interval).
- Measures the distance to determine parking status:
  - **Occupied** if distance is below threshold.
  - **Free** if no object is detected.
- Measures the **battery voltage** and encodes the parking and battery statuses.
- Sends the data via LoRa if the status has changed since the last transmission.
- Enters **deep sleep** for 10 seconds to save power.

### Distance Measurement

The ultrasonic sensor measures the distance between the sensor and the nearest object using the **NewPing library**. If the distance is within the defined range, it updates the parking status.

### Battery Voltage Measurement

The system reads the battery voltage through a **voltage divider** circuit connected to an analog pin. It checks if the voltage is below the **low** or **critical** thresholds, ensuring that low power levels are reported.

### State Encoding

Parking status and battery voltage are combined into a single integer using the formula:




encodedStatus = parkingStatus * 1000 + (batteryVoltage * 100)

This encoded status is sent via LoRa to the receiver.

### LoRa Transmission

The system sends a message in the format:



<uniqueID>:<parkingStatus>:<batteryVoltage>

If the packet is successfully sent, an acknowledgment is received.

### Flash Memory Operations

The loop interval is stored and retrieved from Flash memory, allowing persistent configuration using:
- `save interval <value>` to save a new interval.
- `show interval` to display the current saved interval.

## Serial Commands

You can interact with the system via the USB serial connection with the following commands:

- **`save interval <value>`**  
  Saves a new loop interval (in milliseconds) to Flash storage.

- **`show interval`**  
  Displays the current saved interval.




## ParkingServer.ino: # LoRa Receiver for Smart Parking System

This Arduino code runs on a **SparkFun LoRa Board** and functions as a LoRa receiver for a smart parking system. The receiver listens for LoRa messages from multiple parking sensors, processes the received data, and forwards it to a **Raspberry Pi** via serial communication (`Serial1`). It also provides options for debugging and heartbeat signals over the USB connection.

## Key Components

- **SparkFun LoRa Module (RFM95)**: Communicates wirelessly with the parking sensors using the LoRa protocol.
- **LED Indicator**: Provides visual feedback for received messages and system status.
- **USB Serial Communication**: Optional serial communication via USB for debugging or heartbeat monitoring.

## Features

1. **LoRa Communication**  
   The receiver operates at a frequency of **864.1 MHz** and listens for messages from parking sensors. Upon receiving a message, it extracts the **RSSI** (signal strength) and **SNR** (signal-to-noise ratio) to evaluate signal quality. It can also send acknowledgments (`ACK`) back to the sender.

2. **Serial Communication with Raspberry Pi**  
   The received messages, along with the **RSSI**, are forwarded to the Raspberry Pi using `Serial1` communication. The format sent to the Pi is:  <mac>:<state>:<rssi>

   
This allows the Pi to track the status of parking spots and signal quality.

3. **USB Serial Debugging**  
When debugging is enabled, the receiver can print messages and signal data to the USB serial port (`SerialUSB`). You can enable debugging or heartbeat monitoring by entering the respective commands when prompted via USB.

4. **Message Acknowledgment**  
After receiving a message, the receiver sends an acknowledgment (`ACK`) back to the sensor that transmitted the message. This helps ensure the reliability of communication.

5. **LED Blink Feedback**  
The onboard LED blinks to indicate various system states such as successful message reception or entering the main loop. The LED is also used to signal when the system is waiting for serial communication to initialize.

## Code Breakdown

### `setup()`

- **Pin Initialization**: Sets the LED pin as output.
- **USB Initialization**: Optionally enables USB serial communication for debugging.
- **LoRa Initialization**: Initializes the LoRa module and sets the frequency and modem configuration. If the initialization fails, the system freezes.

### `loop()`

- **LoRa Message Reception**: Continuously checks for incoming LoRa messages. When a message is received:
- The **RSSI** (Received Signal Strength Indicator) and **SNR** (Signal-to-Noise Ratio) values are extracted.
- The message is forwarded to the Raspberry Pi over `Serial1` in the format:

 ```
 <mac>:<state>:<rssi>
 ```

- An acknowledgment (`ACK`) is sent back to the sender to confirm receipt.

- **Debugging**: If debugging is enabled, the received message, RSSI, and SNR are printed to `SerialUSB`.
- **Heartbeat Mode**: If enabled, the system sends a "Heartbeat" message every 5 seconds and flashes the LED to indicate activity.

### **LED Blink Function**

The `blink()` function is used to provide visual feedback by flashing the LED. The number of blinks indicates the current system status.

### **USB Serial Debugging (`usbout()`)**

This function sends messages to the USB serial port (`SerialUSB`) for debugging purposes when the `usb` flag is set to `true`.

### **USB Initialization (`initUSB()`)**

This function initializes the USB serial connection and allows the user to enable debug or heartbeat modes by entering the corresponding commands (`d` for debug, `h` for heartbeat) within a limited time frame. 

Once the USB connection is established, the system waits for user input to enable features. It also provides visual feedback by blinking the LED during this process.

## Configuration Commands

During the initial USB setup, the following commands can be entered via the USB terminal:

- **`d`**: Enables debug mode, which prints received messages, RSSI, and SNR to the USB serial port.
- **`h`**: Enables heartbeat mode, which sends periodic "Heartbeat" messages to `Serial1` and flashes the LED.

## Code Summary

This program listens for LoRa messages from parking sensors, forwards them to a Raspberry Pi via `Serial1`, and sends acknowledgments back to the sensors. It also offers optional debugging and heartbeat modes, which can be activated via USB commands.

