#include <Smartcar.h>

BrushedMotor leftMotor(smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);
SimpleCar car(control);

const int TRIGGER_PIN = 5;
const int ECHO_PIN = 18;
const unsigned int MAX_DISTANCE = 100;
SR04 sensor(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE);


void setup() {

}

void loop(){
    unsigned int distance = sensor.getDistance();
    if (distance && distance < 20){
        car.setSpeed(0);
    } else {
        car.setSpeed(40);
    }
}
