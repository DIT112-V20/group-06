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
HeadingCar car(control, gyro);

void setup() {

}

void loop(){
    unsigned int distance = sensor.getDistance();
    if (distance != 0 && distance < 20){
        car.setSpeed(0);
    } else {
        /*car.setSpeed(40);*/
        spin();
    }
}
/*
 * Sets the speed to 20 and rotates the car 360 degrees
 */
void spin(){
    car.setSpeed(100);
    rotate(360);
}

void rotate(int degrees){
  car.setAngle(degrees);
}
