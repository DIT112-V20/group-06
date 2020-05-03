/*********
  Rui Santos
  Complete project details at https://randomnerdtutorials.com  
*********/

// Load Wi-Fi library
#include <WiFi.h>
#include <Smartcar.h>

BrushedMotor leftMotor(smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);

const int TRIGGER_PIN = 5;
const int ECHO_PIN = 18;
const unsigned int MAX_DISTANCE = 100;
SR04 sensor(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE);

const int GYROSCOPE_OFFSET = 12;
GY50 gyro(GYROSCOPE_OFFSET);

const unsigned long leftPulsesPerMeter = 943;
const unsigned long rightPulsesPerMeter = 981;
DirectionalOdometer leftOdometer (smartcarlib::pins::v2::leftOdometerPins, [](){
    leftOdometer.update();
  },
  leftPulsesPerMeter);
DirectionalOdometer rightOdometer (smartcarlib::pins::v2::rightOdometerPins, [](){
    rightOdometer.update();
  },
  rightPulsesPerMeter);

/**
 * NETWORK DETAILS SETUP FOR WIFI CONNECTION
 */
  
// Replace with your network credentials
const char* ssid     = "TheGaulle";
const char* password = "canihaz#";

// Set web server port number to 80
WiFiServer server(80);

// Variable to store the HTTP request
String header;

SmartCar car(control, gyro, leftOdometer, rightOdometer);

void setup() {

Serial.begin(115200);

  // Connect to Wi-Fi network with SSID and password
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  // Print local IP address and start web server
  Serial.println("");
  Serial.println("WiFi connected.");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  server.begin();

}

void loop() {

/**
 * WIFI CONNECTION SETUP
 */

 WiFiClient client = server.available();   // Listen for incoming clients

  if (client) {                             // If a new client connects,
    Serial.println("New Client.");          // print a message out in the serial port
    String currentLine = "";                // make a String to hold incoming data from the client
    while (client.connected()) {            // loop while the client's connected
      if (client.available()) {             // if there's bytes to read from the client,
        char c = client.read();             // read a byte, then
        Serial.write(c);                    // print it out the serial monitor
        header += c;
        if (c == '\n') {                    // if the byte is a newline character
          // if the current line is blank, you got two newline characters in a row.
          // that's the end of the client HTTP request, so send a response:
          if (currentLine.length() == 0) {
            // HTTP headers always start with a response code (e.g. HTTP/1.1 200 OK)
            // and a content-type so the client knows what's coming, then a blank line:
            client.println("HTTP/1.1 200 OK");
            client.println("Content-type:text/html");
            client.println("Connection: close");
            client.println();
            
            // turns the GPIOs on and off
            if (header.indexOf("GET /dance?id=1") >= 0) {
              Serial.println("Start Spinning");
            } else if (header.indexOf("GET /26/off") >= 0) {
              Serial.println("Stop Spinning");
            } else if (header.indexOf("GET /27/on") >= 0) {
              Serial.println("GPIO 27 on");
            } else if (header.indexOf("GET /27/off") >= 0) {
              Serial.println("GPIO 27 off");
            }
            
            // Display the HTML web page
            client.println("<!DOCTYPE html><html>");
            client.println("<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
            client.println("<link rel=\"icon\" href=\"data:,\">");
            // CSS to style the on/off buttons 
            // Feel free to change the background-color and font-size attributes to fit your preferences
            client.println("<style>html { font-family: Helvetica; display: inline-block; margin: 0px auto; text-align: center;}");
            client.println(".button { background-color: #4CAF50; border: none; color: white; padding: 16px 40px;");
            client.println("text-decoration: none; font-size: 30px; margin: 2px; cursor: pointer;}");
            client.println(".button2 {background-color: #555555;}</style></head>");
            
            // Web Page Heading
            client.println("<body><h1>ESP32 Web Server</h1>");
               
            client.println("</body></html>");
            
            // The HTTP response ends with another blank line
            client.println();
            // Break out of the while loop
            break;
          } else { // if you got a newline, then clear currentLine
            currentLine = "";
          }
        } else if (c != '\r') {  // if you got anything else but a carriage return character,
          currentLine += c;      // add it to the end of the currentLine
        }
      }
    }
    // Clear the header variable
    header = "";
    // Close the connection
    client.stop();
    Serial.println("Client disconnected.");
    Serial.println("");
  }

/**
 * DANCE MOVES 
 */
  
    unsigned int distance = sensor.getDistance();
    
    if (distance != 0 && distance < 20){
      car.setSpeed(0);
    } else {
      int danceID = random(1, 5);
      handleInput(danceID);
    }

    delay(1000);
}

/**
 * DANCE MOVE LOGIC
 */

/** 
 *  Takes an integer and switch case determines which dance to perform
 */
void handleInput(int danceID) {
  switch (danceID) {
    case 1:
      spin();
      break;
    case 2:
      shuffle(30);
      break;
    case 3:
      shake(30);
      break;
    case 4:
      macarena(30);
      break;
    default:
      break;
  }
}

/**
 * Spins the car on the spot
 */
void spin() {
    rotateOnSpot(360, 50);   
}

/**
 * Rotate the car on spot at the specified degrees with the certain speed
 * @param degrees   The degrees to rotate on spot. Positive values for clockwise negative for counter-clockwise.
 * @param speed     The speed to rotate
 * 
 * Title: smartCar shield rotateOnSpot
 * Author: Dimitrios Platis
 * Date: 2020-04-20
 * Availability: https://github.com/platisd/smartcar_shield/blob/master/examples/Car/rotateOnSpot/rotateOnSpot.ino
 */
void rotateOnSpot(int targetDegrees, int speed) {
    speed = smartcarlib::utils::getAbsolute(speed);
    targetDegrees %= 360; /* puts it on a (-360,360) scale */
    
    if (!targetDegrees)
        return; /* if the target degrees is 0, don't bother doing anything */
    
    /* Let's set opposite speed for the motors on opposite sides, so it rotates on spot */
    if (targetDegrees > 0) { /* positive value means we should rotate clockwise */
        car.overrideMotorSpeed(speed, -speed); /* left motors spin forward, right motors spin backward */
    } else { /* rotate counter clockwise */
        car.overrideMotorSpeed(-speed, speed); /* left motors spin backward, right motors spin forward */
    }
    
    const auto initialHeading = car.getHeading(); /* the initial heading we'll use as offset to calculate the absolute displacement */
    int degreesTurnedSoFar = 0; /* this variable will hold the absolute displacement from the beginning of the rotation */
    
    while (abs(degreesTurnedSoFar) < abs(targetDegrees)) { /* while absolute displacement hasn't reached the (absolute) target, keep turning */
        car.update(); /* update to integrate the latest heading sensor readings */
        auto currentHeading = car.getHeading(); /* in the scale of 0 to 360 */
        
        if ((targetDegrees < 0) && (currentHeading > initialHeading)) { /* if we are turning left and the current heading is larger than the initial one 
                                                                        (e.g. started at 10 degrees and now we are at 350), we need to substract 360, 
                                                                        so to eventually get a signed displacement from the initial heading (-20) */
            currentHeading -= 360; 
        } else if ((targetDegrees > 0) && (currentHeading < initialHeading)) { /* if we are turning right and the heading is smaller than the initial one 
                                                                               (e.g. started at 350 degrees and now we are at 20), 
                                                                               so to get a signed displacement (+30) */
            currentHeading += 360;
        }
        
        degreesTurnedSoFar = initialHeading - currentHeading; /* degrees turned so far is initial heading minus current 
                                                              (initial heading is at least 0 and at most 360. To handle the "edge" cases we substracted or added 
                                                              360 to currentHeading) */
    }
    
    car.setSpeed(0); /* we have reached the target, so stop the car */
}

/**
 * Shuffle dance move
 * Car moves forwards and backwards (at set intervals) at the specified speed.
 */
void shuffle(int speed) {
  const int shortDistance = 5;
  const int mediumDistance = 10;
  const int longDistance = 20;
  
  long startingPoint = leftOdometer.getDistance();
  bool danceIsFinished = false;
  int steps = 1;
  
  car.setSpeed(speed);
  
  while(!danceIsFinished) {
    if ((steps == 1 || steps == 5) && (leftOdometer.getDirection() == 1) && ((car.getDistance() - startingPoint) == mediumDistance)) {
      car.setSpeed(0); 
      delay(1000);
      car.setSpeed(speed *-1); /* backwards*/
      steps++;
    } else if ((steps == 2 || steps == 4) && (leftOdometer.getDirection() == -1) && ((car.getDistance() - startingPoint) == shortDistance)) {
      car.setSpeed(0); 
      delay(1000);
      car.setSpeed(speed); /* forwards */
      steps++;
    } else if ((steps == 3) && (leftOdometer.getDirection() == 1) && ((car.getDistance() - startingPoint) == longDistance)) {
      car.setSpeed(0); 
      delay(1000);
      car.setSpeed(speed *-1);
      steps++;
    } else if ((steps == 6) && ((car.getDistance() - startingPoint) == 0)) {
      danceIsFinished = true;
      car.setSpeed(0);
    }
  }
}

/**
 * Changes the direction the car moves in
 */
void changeDirection(int speed) {
  if (leftOdometer.getDirection() == 1) {
    car.setSpeed(speed * -1);
  } else {
    car.setSpeed(speed);
  }
}

/**
 * Shake dance move
 */
void shake(int speed) {
  long startingPoint = 0;
  int steps = 1; 
  int repeats = 0;
  
  while (repeats != 3){
    if (steps == 1) {
      startingPoint = car.getDistance();
      car.setAngle(-45);      
      car.setSpeed(speed * -1); /* going backwards, start of 'shake'*/
      steps++;
    } else if ((steps == 2 || steps == 4) && (car.getDistance() - startingPoint) == -20) {
      changeDirection(speed); /* going forwards, left side of "V"*/
      steps++;
    } else if (steps == 3 && (car.getDistance() - startingPoint) == 0) {
      car.setAngle(45);
      changeDirection(speed); /* going backwards, right side of "v"*/
      steps++;
    } else if (steps == 5 && (car.getDistance() - startingPoint) == 0) {
      steps = 1;
      repeats++; 
      car.setAngle(0);
      car.setSpeed(0);
      delay(1000);
    }
  } 
  
  car.setSpeed(0);
}

void macarena(int speed) {
  long startingPoint = 0;
  int steps = 1;
  int repeats = 0;

  while (repeats != 3) {
    if (steps == 1) {
      startingPoint = car.getDistance();
      car.setSpeed(speed);
      steps++;
    } else if ((steps == 2 || steps == 4) && (car.getDistance() - startingPoint) == 15) {
      car.setSpeed(0);
      delay(1000);
      car.setSpeed(speed * -1);
      steps++;
    } else if ((steps == 3) && (car.getDistance() - startingPoint == 0)){
      car.setSpeed(0);
      delay(1000);
      car.setSpeed(speed);
      steps++;
    } else if ((steps == 5) && (car.getDistance() - startingPoint == 0)) {
      car.setSpeed(0);
      delay(1000);
      rotateOnSpot(90,50);
      repeats++;
    }
  }
  car.setSpeed(0); 
}
