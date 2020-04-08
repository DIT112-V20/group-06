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
