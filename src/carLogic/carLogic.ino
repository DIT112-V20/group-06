#include <ESPmDNS.h>
#include <Smartcar.h>
#include <WiFi.h>
#include <WebServer.h>

/* SMARTCAR VARIABLES */
const int TRIGGER_PIN = 5;
const int ECHO_PIN = 18;
const unsigned int MAX_DISTANCE = 100;
const int GYROSCOPE_OFFSET = 12;
const unsigned long LEFT_PULSES_PER_METER = 943;
const unsigned long RIGHT_PULSES_PER_METER = 981;
const unsigned short SPIN_ID = 1;
const unsigned short TWO_STEP_ID = 2;
const unsigned short SHAKE_ID = 3;
const unsigned short MACARENA_ID = 4;
const unsigned short RANDOM_ID = 5;
int dance_speed = 30;
unsigned int ms_delay = 0;

BrushedMotor leftMotor(smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);
SR04 sensor(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE);
GY50 gyro(GYROSCOPE_OFFSET);
DirectionalOdometer leftOdometer (smartcarlib::pins::v2::leftOdometerPins, [](){
    leftOdometer.update();
  }, LEFT_PULSES_PER_METER);
DirectionalOdometer rightOdometer (smartcarlib::pins::v2::rightOdometerPins, [](){
    rightOdometer.update();
  }, RIGHT_PULSES_PER_METER);
SmartCar car(control, gyro, leftOdometer, rightOdometer);

/* NETWORK VARIABLES */
// Replace with your network credentials
const char* ssid     = "TheGaulle";
const char* password = "canihaz#";
WebServer server(80);

void setup() {
  Serial.begin(115200);
  serverSetup();
}

void loop() {
  server.handleClient();
  car.update();
}

/**
 * SERVER SETUP
 */
void serverSetup() {
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  Serial.println("");
  
  // Wait for connection
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.print("Connected to ");
  Serial.println(ssid);
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  if (MDNS.begin("djsmartcar")) {
    Serial.println("MDNS responder started");
  }
  
  server.on("/dance", []() {
    const auto arguments = server.args();
    int danceId = 0;
  
    for (auto i = 0; i < arguments; i++) {
      const auto command = server.argName(i);
      
      if (command == "id") {
        danceId = server.arg(i).toInt();
      } else if (command == "speed") {
        dance_speed = server.arg(i).toInt();
      } else if (command == "delay") {
        ms_delay = server.arg(i).toInt();
      }
    }
    
    handleInput(danceId);
    server.send(200, "text/json", "");
  });
  
  server.onNotFound([]() { server.send(404, "text/json", ""); });
  
  server.begin();
  Serial.println("HTTP server started");
}

/**
 * OBSTACLE AVOIDANCE
 * Stops the car, then rotates 180 degrees and continues with what it was doing before.
 * Not used in dance moves, feel free to enable it by using it in the dance-loops.
 */
void obstacleAvoidance() {
  unsigned int distance = sensor.getDistance();
  unsigned int distanceToObstacle = 20;
  unsigned int degreesToRotate = 180;
  
  if (distance > 0 && distance <= distanceToObstacle){ 
    car.setSpeed(0);
    rotateOnSpot(degreesToRotate);
    delay(ms_delay);
    car.setSpeed(dance_speed);
  }

  Serial.println("Hi, I avoided an obstacle!");
}

/**
 * DANCE MOVE LOGIC
 */

/** 
 *  Takes an integer and switch case determines which dance to perform.
 */
void handleInput(int danceID) {
  switch (danceID) {
    case SPIN_ID:
      spin();
      break;
    case TWO_STEP_ID:
      twoStep();
      break;
    case SHAKE_ID:
      shake();
      break;
    case MACARENA_ID:
      macarena();
      break;
    default:
      break;
  }
}

/**
 * Spin dance move
 * Rotates the car on the spot, i.e. tank turn.
 */
void spin() {
    car.update();
    rotateOnSpot(350);
}

/**
 * Rotate the car on spot at the specified degrees
 * @param degrees   The degrees to rotate on spot. Positive values for clockwise negative for counter-clockwise.
 * 
 * Title: smartCar shield rotateOnSpot
 * Author: Dimitrios Platis
 * Date: 2020-04-20
 * Availability: https://github.com/platisd/smartcar_shield/blob/master/examples/Car/rotateOnSpot/rotateOnSpot.ino
 */
void rotateOnSpot(int targetDegrees) {
    int speed = smartcarlib::utils::getAbsolute(dance_speed);
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
 * Two-step dance move
 * Car moves forwards and backwards (at set intervals).
 */
void twoStep() {
  const unsigned int shortDistance = 5;
  const unsigned int mediumDistance = 10;
  const unsigned int longDistance = 20;
  const int forward = 1;
  const int backward = -1;
  bool danceIsFinished = false;
  unsigned int steps = 1;
  
  car.setSpeed(dance_speed);
  
  while(!danceIsFinished) {
    if ((steps == 1 || steps == 5) && (rightOdometer.getDirection() == forward) && (car.getDistance() == mediumDistance)) {
      car.setSpeed(0); 
      delay(ms_delay);
      car.setSpeed(dance_speed *-1); /* backwards*/
      steps++;
    } else if ((steps == 2 || steps == 4) && (rightOdometer.getDirection() == backward) && (car.getDistance() == shortDistance)) {
      car.setSpeed(0); 
      delay(ms_delay);
      car.setSpeed(dance_speed); /* forwards */
      steps++;
    } else if ((steps == 3) && (rightOdometer.getDirection() == forward) && (car.getDistance() == longDistance)) {
      car.setSpeed(0); 
      delay(ms_delay);
      car.setSpeed(dance_speed *-1);
      steps++;
    } else if ((steps == 6) && (car.getDistance() == 0)) {
      danceIsFinished = true;
      car.setSpeed(0);
    }
  }
}

/**
 * Changes the direction the car moves in
 */
void changeDirection() {
  if (leftOdometer.getDirection() == 1) {
    car.setSpeed(dance_speed * -1);
  } else {
    car.setSpeed(dance_speed);
  }
}

/**
 * Shake dance move
 */
void shake() {
  unsigned int lengthOfV = 20;
  unsigned int steps = 1;
  bool danceIsFinished = false;
  
  while (!danceIsFinished){
    if (steps == 1) {
      car.setAngle(-45);      
      car.setSpeed(dance_speed * -1); /* going backwards, start of 'shake'*/
      steps++;
    } else if ((steps == 2 || steps == 4) && (abs(car.getDistance())) == lengthOfV) {
      changeDirection(); /* going forwards, left side of "V"*/
      steps++;
    } else if (steps == 3 && car.getDistance() == 0) {
      car.setAngle(45);
      changeDirection(); /* going backwards, right side of "v"*/
      steps++;
    } else if (steps == 5 && car.getDistance() == 0) {
      car.setAngle(0);
      car.setSpeed(0);
      danceIsFinished = true;
    }
  }
}

/**
 * Macarena dance move
 */
void macarena() {
  unsigned int distance = 15;
  unsigned int steps = 1;
  int repeats = 0;

  while (repeats != 3) {
    if (steps == 1) {
      car.setSpeed(dance_speed);
      steps++;
    } else if ((steps == 2 || steps == 4) && car.getDistance() == distance) {
      car.setSpeed(0);
      delay(ms_delay);
      car.setSpeed(dance_speed * -1);
      steps++;
    } else if ((steps == 3) && car.getDistance() == 0){
      car.setSpeed(0);
      delay(ms_delay);
      car.setSpeed(dance_speed);
      steps++;
    } else if ((steps == 5) && car.getDistance() == 0) {
      car.setSpeed(0);
      delay(ms_delay);
      
      if (repeats != 2) {
        rotateOnSpot(90);
        delay(ms_delay);
      }
      
      repeats++;
      steps = 1;
    }
  }
  car.setSpeed(0); 
}
