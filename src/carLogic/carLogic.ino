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
const int STOP_ID = 0;
const int SPIN_ID = 1;
const int TWO_STEP_ID = 2;
const int SHAKE_ID = 3;
const int MACARENA_ID = 4;
const int RANDOM_ID = 5;
int activeDanceId = 0;

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
  if (activeDanceId == RANDOM_ID) {
    handleInput(random(1, 5), 30); //MAGIC NUMBER SPEED
  } else {
    handleInput(activeDanceId, 30); //MAGIC NUMBER SPEED
  }
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
    int speed = 30; // Base speed
  
    for (auto i = 0; i < arguments; i++) {
      const auto command = server.argName(i);
      
      if (command == "id") {
        activeDanceId = server.arg(i).toInt(); //MAGIC NUMBER SPEED
      } else if (command == "speed") {
        speed = server.arg(i).toInt();
      }
    }
  
    server.send(200, "text/json", "[{'id':'1'}]");
  });

  server.on("/random", []() {
    const auto arguments = server.args();
    activeDanceId = RANDOM_ID;
    
    server.send(200, "text/json", "[{'id':'1'}]");
  });

  server.on("/stop", []() {
    const auto arguments = server.args();
    activeDanceId = STOP_ID;
    
    server.send(200, "text/json", "[{'id':'1'}]");
  });
  
  server.onNotFound(
    []() { server.send(404, "text/plain", "Unknown command"); });
  
  server.begin();
  Serial.println("HTTP server started");
}

/**
 * OBSTACLE AVOIDANCE
 * Stops the car, then rotates 180 degrees and continues with what it was doing before.
 */
void obstacleAvoidance() {
  unsigned int distance = sensor.getDistance();
  //int speed = car.getSpeed();
  
  if (distance > 0 && distance <= 20){ 
    car.setSpeed(0);
    rotateOnSpot(180, 30); //MAGIC NUMBER SPEED
    delay(1000);
    car.setSpeed(30); //MAGIC NUMBER SPEED
  }
}

/**
 * DANCE MOVE LOGIC
 */

/** 
 *  Takes an integer and switch case determines which dance to perform
 */
void handleInput(int danceID, int speed) {
  switch (danceID) {
    case STOP_ID: // Stop
      car.setAngle(0);
      car.setSpeed(0);
      break;
    case SPIN_ID:
      spin(speed);
      break;
    case TWO_STEP_ID:
      twoStep(speed);
      break;
    case SHAKE_ID:
      shake(speed);
      break;
    case MACARENA_ID:
      macarena(speed);
      break;
    default:
      break;
  }

  delay(1000);
}

/**
 * Spins the car on the spot
 */
void spin(int speed) {
    car.update();
    rotateOnSpot(350, speed);   
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
 * Two-step dance move
 * Car moves forwards and backwards (at set intervals) at the specified speed.
 */
void twoStep(int speed) {
  const int shortDistance = 5;
  const int mediumDistance = 10;
  const int longDistance = 20;
  bool danceIsFinished = false;
  int steps = 1;
  
  car.setSpeed(speed);
  
  while(!danceIsFinished) {
    obstacleAvoidance();
    
    /*Serial.print("mediumDistance = ");
    Serial.print(mediumDistance);
    Serial.print("startingPoint = ");
    Serial.print(startingPoint);
    Serial.print("distance = ");
    Serial.print(car.getDistance());
    Serial.print(", steps = ");
    Serial.println(steps);*/
    
    if ((steps == 1 || steps == 5) && (rightOdometer.getDirection() == 1) && (car.getDistance() == mediumDistance)) {
      car.setSpeed(0); 
      delay(1000);
      car.setSpeed(speed *-1); /* backwards*/
      steps++;
    } else if ((steps == 2 || steps == 4) && (rightOdometer.getDirection() == -1) && (car.getDistance() == shortDistance)) {
      car.setSpeed(0); 
      delay(1000);
      car.setSpeed(speed); /* forwards */
      steps++;
    } else if ((steps == 3) && (rightOdometer.getDirection() == 1) && (car.getDistance() == longDistance)) {
      car.setSpeed(0); 
      delay(1000);
      car.setSpeed(speed *-1);
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
  long startingPoint = 0; //remove later
  int steps = 1; 
  int repeats = 0;
  
  while (repeats != 3){
    obstacleAvoidance();
    
    /*Serial.print("repeats = ");
    Serial.print(repeats);
    Serial.print(", startingPoint = ");
    Serial.print(startingPoint);
    Serial.print(", distance = ");
    Serial.print(car.getDistance());
    Serial.print(", steps = ");
    Serial.println(steps);*/
    
    if (steps == 1) {
      //startingPoint = car.getDistance();
      car.setAngle(-45);      
      car.setSpeed(speed * -1); /* going backwards, start of 'shake'*/
      steps++;
    } else if ((steps == 2 || steps == 4) && (abs(car.getDistance())) == 20) {
      changeDirection(speed); /* going forwards, left side of "V"*/
      steps++;
    } else if (steps == 3 && car.getDistance() == 0) {
      car.setAngle(45);
      changeDirection(speed); /* going backwards, right side of "v"*/
      steps++;
    } else if (steps == 5 && car.getDistance() == 0) {
      steps = 1;
      repeats++; 
      car.setAngle(0);
      car.setSpeed(0);
      delay(1000);
    }
  } 
  
  car.setSpeed(0);
}

/**
 * Macarena dance move
 */
void macarena(int speed) {
  long startingPoint = 0; //remove later
  int steps = 1;
  int repeats = 0;

  while (repeats != 3) {
    obstacleAvoidance();
    
    /*Serial.print("startingPoint = ");
    Serial.print(startingPoint);
    Serial.print(", repeats = ");
    Serial.print(repeats);
    Serial.print(", steps = ");
    Serial.println(steps);*/
    
    if (steps == 1) {
      //startingPoint = car.getDistance();
      car.setSpeed(speed);
      steps++;
    } else if ((steps == 2 || steps == 4) && car.getDistance() == 15) {
      car.setSpeed(0);
      delay(1000);
      car.setSpeed(speed * -1);
      steps++;
    } else if ((steps == 3) && car.getDistance() == 0){
      car.setSpeed(0);
      delay(1000);
      car.setSpeed(speed);
      steps++;
    } else if ((steps == 5) && car.getDistance() == 0) {
      car.setSpeed(0);
      delay(1000);
      rotateOnSpot(90, speed);
      repeats++;
      steps = 1;
      delay(1000);
    }
  }
  car.setSpeed(0); 
}
