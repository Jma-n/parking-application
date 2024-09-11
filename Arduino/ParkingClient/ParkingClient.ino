
#include <RH_RF95.h>
#include <SPI.h>
#include <ArduinoLowPower.h>
#include <ArduinoUniqueID.h>
#include <NewPing.h>
#include <FlashStorage_SAMD.h>
#include <Adafruit_SleepyDog.h>

// Definiere eine Struktur für die gespeicherten Daten
struct DataStorage {
  unsigned long interval;
};

// Erstelle ein FlashStorage-Objekt
FlashStorage(flash_store, DataStorage);

// RFM95 Modul (CS, Interrupt)
RH_RF95 rf95(12, 6);

#define TRIGGER_ECHO_PIN 4
#define MAX_DISTANCE 250
#define LOOP_LIMIT 100
#define STAT_NOTFREE 180

#define BATT_PIN A3  // Analog pin connected to the voltage divider
#define R1 68     // Resistor R1 value in ohms
#define R2 20     // Resistor R2 value in ohms
#define LOW_BATTERY_THRESHOLD 3.0
#define CRITICAL_BATTERY_THRESHOLD 2.5

NewPing sonar(TRIGGER_ECHO_PIN, TRIGGER_ECHO_PIN, MAX_DISTANCE);

char uniqueID[17];
float frequency = 864.1;  // Broadcast frequency

int oldStatus = -1;
unsigned long interval = 10000;  // Default interval value

void setup() {
  pinMode(TRIGGER_ECHO_PIN, OUTPUT);

  SerialUSB.begin(9600);

  // Versuche, eine serielle Verbindung aufzubauen
  bool serialPortOpened = false;
  for (int i = 0; i < 3; i++) {
    SerialUSB.begin(9600);
    unsigned long startMillis = millis();
    while (!SerialUSB && (millis() - startMillis) < 2000) {
    }
    if (SerialUSB) {
      serialPortOpened = true;
      break;
    }
  }

  SerialUSB.println("SerialUSB ready");

  setUniqueID();
  SerialUSB.println(uniqueID);

  UniqueIDdump(SerialUSB);
  UniqueID8dump(SerialUSB);
  SerialUSB.println("RFM ParkingClient!");

  if (!rf95.init()) {
    SerialUSB.println("Radio Init Failed - Freezing");
    while (1);
  } else {
    SerialUSB.println("Transmitter up!");
  }

  rf95.setFrequency(frequency);
  rf95.setModemConfig(RH_RF95::Bw125Cr45Sf2048);
  rf95.setTxPower(20, false);

  // Lese den gespeicherten Intervall aus dem Flash-Speicher
  interval = readIntervalFromFlash();
  SerialUSB.print("Using interval: ");
  SerialUSB.println(interval);
}

int currentState = -1;
float batteryVoltage;

void loop() {
  if (SerialUSB.available() > 0) {
    String command = SerialUSB.readStringUntil('\n');
    command.trim();  // Entfernt unnötige Leerzeichen

    if (command.startsWith("save interval")) {
      String intervalStr = command.substring(13);
      unsigned long newInterval = intervalStr.toInt();
      saveIntervalToFlash(newInterval);
      interval = newInterval;  // Aktualisiere den aktuellen Intervall
    } else if (command.equals("show interval")) {
      unsigned long savedInterval = readIntervalFromFlash();
      SerialUSB.print("Current saved interval: ");
      SerialUSB.println(savedInterval);
    }
  }

  SerialUSB.println("Start loop");

  int distance = measureDistance();

  if (distance == 0 || distance == MAX_DISTANCE) {
    currentState = 102;  // No sensor detected or sensor malfunction
    SerialUSB.println("No sensor detected or sensor malfunction");
  } else if (distance >= STAT_NOTFREE) {
    currentState = 0;
  } else {
    currentState = 1;
  }

  SerialUSB.print("Current State: ");
  SerialUSB.println(currentState);
  SerialUSB.print("Old State: ");
  SerialUSB.println(oldStatus);

  batteryVoltage = measureBatteryVoltage();
  SerialUSB.print("Battery Voltage: ");
  SerialUSB.println(batteryVoltage);

  // Kodierung des Statuswerts mit Spannung und Zustand
  if (currentState != oldStatus) {
    int status = kodierenStatus(currentState, batteryVoltage);
    sendState(status);
  } else {
    SerialUSB.print("Do not send update");
  }

  oldStatus = currentState;

   delay(1000);  // Give some time to detach USB

 //Gehe in den Deep-Sleep-Modus für 10 Sekunden (10000 Millisekunden)
 Watchdog.sleep(10000); // Schlafen für 10 Sekunden
  

  SerialUSB.println("Woke up from deep sleep!");
}

int measureDistance() {
  unsigned int uS = sonar.ping();
  unsigned int distanceCM = uS / US_ROUNDTRIP_CM;
  SerialUSB.print("Distanz: ");
  SerialUSB.print(distanceCM);
  SerialUSB.println(" cm");
  return distanceCM;
}

void sendState(int state) {
  // Status und Batteriestatus extrahieren
  int status = state / 1000;
  float battery = (state % 1000) / 100.0;

  // Nachricht zusammenstellen
  char message[32];
  sprintf(message, "%s:%d:%.2f", uniqueID, status, battery);
  SerialUSB.print("Sending state: ");
  SerialUSB.println(message);

  rf95.send((uint8_t *)message, strlen(message));
  if (!rf95.waitPacketSent()) {
    SerialUSB.println("Failed to send packet");
  } else {
    SerialUSB.println("State sent!");
  }
}

void setUniqueID() {
  sprintf(uniqueID, "%02X%02X%02X%02X%02X%02X%02X%02X",
          UniqueID8[0],
          UniqueID8[1],
          UniqueID8[2],
          UniqueID8[3],
          UniqueID8[4],
          UniqueID8[5],
          UniqueID8[6],
          UniqueID8[7]);
}

float measureBatteryVoltage() {
    int sensorValue = analogRead(BATT_PIN);
    float voltage = 3.3 * sensorValue / 1024;
    SerialUSB.println(voltage);
    
    float batteryVoltage = voltage * ((R1 + R2) / (float)R1);
    SerialUSB.println(sensorValue);
    return batteryVoltage;
}

int kodierenStatus(int zustand, float spannung) {
  int spannungInHundert = (int)(spannung * 100);
  return zustand * 1000 + spannungInHundert;
}

// Funktionen zum Lesen und Schreiben des Intervalls im Flash-Speicher
unsigned long readIntervalFromFlash() {
    DataStorage data;
    flash_store.read(data);  // Lese die gesamte Struktur aus dem Flash-Speicher
    SerialUSB.print("Read interval from flash: ");
    SerialUSB.println(data.interval);  // Zeigt den ausgelesenen Wert an
    return data.interval;
}

void saveIntervalToFlash(unsigned long intervalToSave) {
    DataStorage data;
    data.interval = intervalToSave;
    flash_store.write(data);  // Schreibe die gesamte Struktur in den Flash-Speicher
    SerialUSB.println("Interval saved to flash.");
    SerialUSB.print("Saved interval: ");
    SerialUSB.println(intervalToSave);  // Zeigt den gespeicherten Wert an
}