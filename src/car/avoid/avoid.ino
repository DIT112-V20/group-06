#include <Smartcar.h>

BrushedMotor leftMotor(smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);

const int TRIGGER_PIN = 5;
const int ECHO_PIN = 18;
const unsigned int MAX_DISTANCE = 100;
SR04 sensor(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE);

const int GYROSCOPE_OFFSET = 32;
GY50 gyro(GYROSCOPE_OFFSET);
const unsigned long leftPulsesPerMeter = 943;
const unsigned long rightPulsesPerMeter = 981;

DirectionalOdometer leftOdometer (
  smartcarlib::pins::v2::leftOdometerPins, [](){ /* Check if it works as pin, otherwise change to pinS */
    leftOdometer.update();
  },
  leftPulsesPerMeter);

DirectionalOdometer rightOdometer (
  smartcarlib::pins::v2::rightOdometerPins, [](){
    rightOdometer.update();
  },
  rightPulsesPerMeter);

SmartCar car(control, gyro, leftOdometer, rightOdometer);

void setup() {

    Serial.begin(9600);
    
}

void loop(){
    unsigned int distance = sensor.getDistance();
    if (distance != 0 && distance < 20){
        car.setSpeed(0);
    } else {
        /* car.setSpeed(40); */
        shuffle();
    }
}


/* Sets the speed to 75 and spins the car on the spot */
void spin(){
 
    car.setSpeed(75);
    control.overrideMotorSpeed(100, -100);    
}

/* This method rotates the car to the specified degree */
void rotate(int degrees){
    car.setAngle(degrees);
}

void shuffle() {

  const long startingPoint = leftOdometer.getDistance();
  bool danceIsFinished = false;
  int steps = 1;

  car.setSpeed(30);
  while(!danceIsFinished) {
/*    car.update();
 *     
 */

 Serial.println(leftOdometer.getDistance());

    if ((steps == 1 || steps == 5) && (leftOdometer.getDirection() == 1) && ((car.getDistance() - startingPoint) == 20)) {
    changeDirection(); /* backwards*/
    steps++;
  } else if ((steps == 2 || steps == 4) && (leftOdometer.getDirection() == -1) && ((car.getDistance() - startingPoint) == 10)) {
    changeDirection(); /* forwards */
    steps++;
  } else if ((steps == 3) && (leftOdometer.getDirection() == 1) && ((car.getDistance() - startingPoint) == 30)) {
    changeDirection(); /* backwards*/
    steps++;
  } else if ((steps == 6) && ((car.getDistance() - startingPoint) == 0)) {
    danceIsFinished = true;
    car.setSpeed(0);
  }
  
  }
}

void changeDirection() {
  if (leftOdometer.getDirection() == 1) {
    car.setSpeed(-30);
  } else {
    car.setSpeed(30);
  }
}
