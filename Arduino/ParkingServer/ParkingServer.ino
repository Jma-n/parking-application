#include <SPI.h>

//Radio Head Library:
#include <RH_RF95.h>

// We need to provide the RFM95 module's chip select and interrupt pins to the
// rf95 instance below.On the SparkFun ProRF those pins are 12 and 6 respectively.
RH_RF95 rf95(12, 6);

int LED = 13;  //Status LED on pin 13

float frequency = 864.1;
//float frequency = 921.2;




boolean usb = false;
boolean debug = false;
boolean heartbeat = false;

void setup() {
  pinMode(LED, OUTPUT);
  initUSB();

  Serial1.begin(9600);


  while (!Serial1) {
    usbout("Waiting for Serial1 ...");
    delay(500);
  }


  //Initialize the Radio.
  if (rf95.init() == false) {
    usbout("Radio Init Failed - Freezing");
    Serial1.println("Radio Init Failed - Freezing");
    while (1)
      ;
  } else {
    // An LED indicator to let us know radio initialization has completed.
    Serial1.println("Receiver up!");
    usbout("Receiver up!");
    blink(5);
  }

  rf95.setFrequency(frequency);
  rf95.setModemConfig(RH_RF95::Bw125Cr45Sf2048);
  //rf95.setSpreadingFactor(12);

  // The default transmitter power is 13dBm, using PA_BOOST.
  // If you are using RFM95/96/97/98 modules which uses the PA_BOOST transmitter pin, then
  // you can set transmitter powers from 5 to 23 dBm:
  rf95.setTxPower(20, false);

  //Last message from setup
  usbout("Enter loop. Waiting for LORA messages ...");
  if (!debug) {
    usbout("Close USB");
    USBDevice.detach();
    //For unknown reason the tx led is on
    digitalWrite(PIN_LED_RXL, HIGH);
    digitalWrite(PIN_LED_TXL, HIGH);
  }
}

int rssi = 0;
int snr = 0;
int hbcount = 0;

void loop() {


  if (rf95.available()) {
    // Should be a message for us now
    uint8_t buf[RH_RF95_MAX_MESSAGE_LEN];
    uint8_t len = sizeof(buf);

    if (rf95.recv(&buf[0], &len)) {
      //indicate received packet
      blink(3);
      
      rssi = rf95.lastRssi();
      snr  = rf95.lastSNR();
      

      //Terminate string (may be not necessary)
      buf[len] = 0;

      //For debug reasons print message to SerialUSB

      if (usb && debug) {
        
       
        SerialUSB.print((char*)buf);
        SerialUSB.print(":rssi:");
        SerialUSB.print(rssi, DEC);
        SerialUSB.print(":snr:");
        SerialUSB.println(snr, DEC);
        
      }

      //This will be received by the raspberryPi via ttyS0
      //Content of buf should be: <mac>:<state>
      //Pi will get: "<mac>:<state>:<rssi>"
      Serial1.print((char*)buf);
      Serial1.print(":");
      Serial1.println(rssi, DEC);


      //send ack to sender with leading mac of sender followed by an "ACK"
      //If the sender will not receive this message the sender may resend his message
      //! Currently no implementation on client side

      if ((len+4) <= RH_RF95_MAX_MESSAGE_LEN){
        strcpy((char*)&buf[len], ":ACK");
        rf95.send(buf, len + 4);
        rf95.waitPacketSent();
      }






    } else {
      if (debug)
        usbout("Receive failed");
      Serial1.println("Receive failed");
    }
  }
  
  
  if (heartbeat) {

    delay(100);
    //return in loop() will continue loop()
    if (hbcount++ < 50)
      return;
    hbcount=0;
    usbout("Heartbeat");
    Serial1.println("Heartbeat");
    blink(1);
  }

}  //end loop


void blink(int nr) {
  for (int i = 0; i < nr; i++) {
    digitalWrite(LED, HIGH);
    delay(200);
    digitalWrite(LED, LOW);
    delay(200);
  }
}



void usbout(const char* message) {
  if (!usb)
    return;
  SerialUSB.println(message);
}


void initUSB() {

  
  SerialUSB.begin(19200);

   int count=0;
   
   while (!SerialUSB) {
   
   if (count++ > 10){
    return;
   } 
   
   delay(500);
   blink(1);
    
  }

  usb=true;
 
  SerialUSB.println("RFM Server!");
  SerialUSB.println("Enter \'d..h\' for debug..heartbeat");

  //flush
  while (SerialUSB.available()){
    SerialUSB.read();
  }

  SerialUSB.setTimeout(5000);
  count=15;
  char input[16];
  
  while(count >= 0){
    input[count]=0;
    count--;
  }
  blink(10);
  SerialUSB.readBytesUntil('\n',&input[0], 15);
  
  count=15;
  
  while(count >= 0){
    if (input[count] == 'd') {
      debug = true;
      SerialUSB.println("debug enabled");
    }
    if (input[count] == 'h') {
      heartbeat = true;
      SerialUSB.println("heartbeat");
    }
    count--;


  }
  
}
