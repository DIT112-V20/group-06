# Group-06: DJ SmartCar ![Android CI] ![Arduino CI]

![](https://i.imgur.com/K6XNvpd.png)

## Contents
* [Description](#description)
   * [Demo Video](#demo-video)
* [Installation Guide for Open Source Developers](#installation-guide-for-open-source-developers)
* [User Manual](#user-manual)
* [Resources](#resources)
* [Developers](#developers)

## Description 
DJ SmartCar is a dancing SmartCar controlled by an app that can also play music via Spotify. During these “unprecedented times” we wanted to develop a product that is fun and entertaining, while also showing off the capabilities of the SmartCar. When using our product, the user can choose between two modes: *Dance without Music* or *Dance with Music*. In *Dance without Music*, the user can make the car dance by choosing from four pre-programmed dance moves or a randomized sequence of these moves. In *Dance with Music*, the user connects to Spotify and the car will dance in a randomized sequence to the average tempo of the currently playing track. The car’s speed will change based on the tempo of the current track.

DJ SmartCar is a two-tier system that includes a SmartCar and an Android app. The app sends requests to the SmartCar server via WiFi. To play music and receive track tempo data, the app connects to the Spotify Web API. Please refer to [Resources](#resources) for a list of the hardware and software used during development and in the final product.To see the evolution of the DJ SmartCar software architecture, please refer to the [Class Diagram](https://github.com/DIT112-V20/group-06/wiki/Class-Diagram) Wiki page.

### Demo Video
[![Video thumbnail](http://i3.ytimg.com/vi/GGH_hdqZUTI/hqdefault.jpg)](https://youtu.be/GGH_hdqZUTI)

We have created a "fun" infomercial-style demo video. Please enjoy!

## Installation Guide for Open Source Developers
There are a few things you need to do before getting started:
* Download [Arduino IDE](https://www.arduino.cc/en/Main/Software)
* Download [Android Studio](https://developer.android.com/studio)
* Install Java SDK version >= 11 
* Clone the repository: `https://github.com/DIT112-V20/group-06` 

### SmartCar
1. Open the Arduino IDE and navigate to File > Preferences and paste the following link in the Additional Boards Manager URL section: `https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json`.
2. Navigate to Sketch > Include Library > Manage Libraries, then search and install the *Smartcar shield* library.
3. Go to Tools > Board and select `DOIT ESP32 DEVKIT V1`.
4. Go to Tools > Port and select the port the car is connected to.
5. Change the WiFi credentials on **lines 36** and **37** in `carLogic.ino`.
6. Calibrate the sensors to get `GYROSCOPE_OFFSET` and `LEFT_PULSES_PER_METER` and `RIGHT_PULSES_PER_METER` for the odometers and add the number to the variables in the `carLogic.ino` sketch. In the Arduino IDE:
    - File > Examples > Smartcar shield > Sensors > gyroscope > gyroscopeHeading
    - File > Examples > Smartcar shield > Sensors > odometer > findPulsesPerMeter
7. Open *carLogic.ino* and upload the sketch to the car.

### Android App
8. Open the `src/app` project in Android Studio.
9. Sync the Project with the Gradle Files either by clicking the elephant button in the top right corner or by entering the following command: `./gradlew build --stacktrace`.
10. Register the app on Spotify to get the Client ID and Client Secret. For assistance on finding the fingerprint, refer to the following link: 
https://developer.spotify.com/documentation/android/quick-start/
11. Input the IP address of the car in `BASE_URL` in `RetrofitClient.kt`. To get the IP address, follow the steps below:
    - When the car is connected to the computer, open the serial monitor window. 
    - Turn on the car.
    - Press the EN button on the car.
12. Input the Client ID in `CLIENT_ID` for Spotify in `SpotifyService.kt`.
13. To find out how to acquire authorization for the Spotify Web API, go to: https://developer.spotify.com/documentation/general/guides/authorization-guide/
    - In `Endpoint.kt`, input the authorization on **line 25**.   
    - The `CLIENT_SECRET`variable is also available in `SpotifyService.kt` for those who want to implement the base 64 encoding of the Client ID and Client Secret programmatically.
14. Run the app!

## User Manual
*Thank you for choosing the DJ SmartCar system!*

We hope this product will bring you joy and entertainment for years to come. Before you can get started, there are a few things you need to do: 
1. Download and install the DJ SmartCar app.
2. Download and install the Spotify app.
3. Log in to the Spotify app on your phone.
4. Click *Dance with Music* in the DJ SmartCar app and accept the Spotify terms and conditions. 
5. Turn on the SmartCar.

![](https://i.imgur.com/Ql6TQxW.jpg?1)

Now that you have downloaded the DJ SmartCar and Spotify apps, you can start enjoying the DJ SmartCar system! 

![](https://i.imgur.com/bILHO3g.jpg?1)

The *Dance with Music* mode allows you to play music from Spotify right in the DJ SmartCar app. When pressing the *Play* button, the music will begin and your car will start dancing! When you press *Pause*, both the car and the music will stop. If you want to change the track, press *Pause* then change it in the Spotify app and return to the DJ SmartCar app to start dancing. When the track automatically changes, press *Pause* and then *Play* again to get the new tempo for DJ SmartCar to dance along to. If you press the home button while the car is dancing, the music and car will automatically stop and you will go back to the home screen. Pressing it while the music and car are paused will just take you back to the home screen.

![](https://i.imgur.com/WJRigre.jpg?1)

*You will see this screen once you have selected a dance move.* 

The *Dance without Music* mode features four unique dance moves: *two-step, spin, shake, and macarena.* The *random* button will make the car do these dances in a randomized order. After you have selected a dance move, press the *Start* button to make it dance until you press the *Stop* button.  

To go back to the home screen, simply press the home button in the top right corner. 

*Thank you again for choosing the DJ SmartCar system, the most entertaining SmartCar on the market!* 

## Resources
### Smartcar Platform Hardware
* DOIT ESP32 devkit v1
* TTL level shifters
* GY-50 Gyroscope
* Adjustable DC regulator
* Power switch
* Motor drive
* Bumpers 
* Motors
* Odometers
* Ultrasonic sensor
* Cables
* Battery recharger
* 8x AA recharageable batteries

### Software
* [Android Studio](https://developer.android.com/studio)
* [Arduino IDE](https://www.arduino.cc/en/Main/Software)
* [SmartCar Library](https://www.arduinolibraries.info/libraries/smartcar-shield)
* [Spotify Web API](https://developer.spotify.com/documentation/web-api/)
* [Spotify Android SDK](https://developer.spotify.com/documentation/android/)
* [Postman](https://postman.com) (Only used during development)

## Developers
* Fayona Cowperthwaite (guscowfa@student.gu.se)
* Dominique Deramat (gusderdo@student.gu.se)
* Shonaigh Douglas (gusdoush@student.gu.se)
* Negin Hashmati (gushashne@student.gu.se)
* Jennifer Nord (gusnorjea@student.gu.se)
* Victoria Vu (gusvuvij@student.gu.se)

[Android CI]: https://github.com/DIT112-V20/group-06/workflows/Android%20CI/badge.svg
[Arduino CI]: https://github.com/DIT112-V20/group-06/workflows/Arduino%20CI/badge.svg
