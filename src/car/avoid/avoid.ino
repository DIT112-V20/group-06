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
const unsigned long pulsesPerMeter = 600;

DirectionalOdometer leftOdometer (
  smartcarlib::pins::v2::leftOdometerPin, [](){ /* Check if it works as pin, otherwise change to pinS */
    leftOdometer.update();
  },
  pulsesPerMeter);

DirectionalOdometer rightOdometer (
  smartcarlib::pins::v2::rightOdometerPin, [](){
    rightOdometer.update();
  },
  pulsesPerMeter);

SmartCar car(control, gyro, leftOdometer, rightOdometer);

void setup() {

}

void loop(){
    unsigned int distance = sensor.getDistance();
    if (distance != 0 && distance < 20){
        car.setSpeed(0);
    } else {
        /* car.setSpeed(40); */
        spin();
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

void shuffle(){
  long startingPoint = car.getDistance();
  boolean danceIsFinished = false;

  car.setSpeed(50);

  while(!danceIsFinished) {
    car.update(); /* needed? */

    if (odometer.getDirection() == 1 && ((car.getDistance() - startingPoint) >= 20)) {
        changeDirection(); /* backwards*/
    } else if (odometer.getDirection() == -1 && ((car.getDistance() - startingPoint) <= 10) {
        changeDirection(); /* forwards */
    } else {
        danceIsFinished = true;
      }
    }

   /* if ((car.getDistance() - startingPoint) >= 20) {
      car.setSpeed(-50);
      
      if ((car.getDistance() - startingPoint) >= 10) {
        car.setSpeed(50);

        if ((car.getDistance() - startingPoint) >= 30) {
          car.setSpeed(-50);

          if ((car.getDistance() - startingPoint) >= 10) {
             car.setSpeed(50);

             if ((car.getDistance() - startingPoint) >= 20) {
              car.setSpeed(-50);

              if ((car.getDistance() - startingPoint) >= 0) {
                car.setSpeed(0);
              }
            }
          }
        }
      }
    }  */
}

void changeDirection() {
  if (odometer.getDirection() == 1) {
    car.setSpeed(-50);
  } else {
    car.setSpeed(50);
  }
}
