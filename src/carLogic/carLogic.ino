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

SmartCar car(control, gyro, leftOdometer, rightOdometer);

void setup() {
    Serial.begin(9600);
}

void loop() {
    unsigned int distance = sensor.getDistance();
    
    if (distance != 0 && distance < 20){
      car.setSpeed(0);
    } else {
      int danceID = random(1, 4);
      handleInput(danceID);
    }
}

/** 
 *  Takes an integer and switch case determines which dance to perform
 */
void handleInput(int danceID) {
  switch (danceID) {
    case 1:
      spin(50);
      break;
    case 2:
      shuffle(50);
      break;
    case 3:
      shake(50);
      break;
    default:
      break;
  }
}

/**
 * Spins the car on the spot
 */
void spin(int speed) {
    car.setSpeed(speed);
    control.overrideMotorSpeed(100, -100);    
}

/**
 * This method rotates the car to the specified degree
 */
void rotate(int degrees) {
    car.setAngle(degrees);
}

/**
 * Shuffle dance move
 * Car moves forwards and backwards (at set intervals) at the specified speed.
 */
void shuffle(int speed) {
  const int shortDistance = 5;
  const int mediumDistance = 10;
  const int longDistance = 20;
  
  const long startingPoint = leftOdometer.getDistance();
  bool danceIsFinished = false;
  int steps = 1;
  
  car.setSpeed(speed);
  
  while(!danceIsFinished) {
    if ((steps == 1 || steps == 5) && (leftOdometer.getDirection() == 1) && ((car.getDistance() - startingPoint) == mediumDistance)) {
      changeDirection(speed); /* backwards*/
      steps++;
    } else if ((steps == 2 || steps == 4) && (leftOdometer.getDirection() == -1) && ((car.getDistance() - startingPoint) == shortDistance)) {
      changeDirection(speed); /* forwards */
      steps++;
    } else if ((steps == 3) && (leftOdometer.getDirection() == 1) && ((car.getDistance() - startingPoint) == longDistance)) {
      changeDirection(speed); /* backwards*/
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
      startingPoint = leftOdometer.getDistance();
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
    }
  } 
  
  car.setSpeed(0);
}
