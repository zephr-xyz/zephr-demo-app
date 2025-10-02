# Zephr Demo Application
Android demo application to demonstrate the capabilities of the Zephr SDK.

## Getting Started

To build and run the demo app:

 1. Clone or download this repository.
 2. Open the project in Android Studio.
 3. Create a `secrets.properties` file in your project root direction and populate it following the steps in the next section.
 4. Sync your project with Gradle.
 5. Build and run the app on a physical Android device (an Android emulated device does not receive GNSS measurements so cannot produce a Zephr position).

## Authenticating with Secrets Gradle Plugin
 There are a few steps needed to authenticate for some packages required in the app. We are using [secrets gradle plugin](https://github.com/google/secrets-gradle-plugin) to authenticate for certain APIs, so you should place the following for each of these packages in a file titled `secrets.properties` in your project root directory:

 ### Zephr API
 If you do not have credentials, head to https://zephr.xyz/developer-portal to get started.
 ```
 # API credentials
API_USERNAME=<YOUR_ZEPHR_USERNAME>
API_PASSWORD=<YOUR_ZEPHR_PASSWORD>
# Zephr Overture API Base URL
API_BASE_URL=https://api.zephr.xyz
 ```

 ### Google Maps API
 You will need to provide your own Google Maps API key here.
 ```
 MAPS_API_KEY=<YOUR_MAPS_API_KEY>
 ```
