#include <WiFi.h>
#include <AsyncTCP.h>
#include <ESPAsyncWebServer.h>
#include <BleGamepad.h>

const char* ssid = "Xiaomi 14 Pro";
const char* password = "TainakaRitsu23333";

AsyncWebServer server(80);
BleGamepad bleGamepad("SkyAutoLighting By LED", "Espressif", 100);

void setup() {
  Serial.begin(115200);

  // Connect to Wi-Fi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("Connected to WiFi");

  Serial.print("ESP32 IP Address: ");
  Serial.println(WiFi.localIP());

  // Start BLE Gamepad
  bleGamepad.begin();

  // Define the route for /pressY
  server.on("/pressXtwice", HTTP_GET, [](AsyncWebServerRequest *request){
    if (bleGamepad.isConnected()) {
      Serial.println("Press X");
      bleGamepad.press(BUTTON_4);
      delay(20);
      bleGamepad.release(BUTTON_4);
      delay(20);
      bleGamepad.press(BUTTON_4);
      delay(20);
      bleGamepad.release(BUTTON_4);
    }
    request->send(200, "text/plain", "X Button Pressed");
  });

  // Define the route for /pressB
  server.on("/pressB", HTTP_GET, [](AsyncWebServerRequest *request){
    if (bleGamepad.isConnected()) {
      Serial.println("Press B");
      bleGamepad.press(BUTTON_2);
      delay(20);
      bleGamepad.release(BUTTON_2);
    }
    request->send(200, "text/plain", "B Button Pressed");
  });

  server.on("/sendLightAndBack", HTTP_GET, [](AsyncWebServerRequest *request){
    if (bleGamepad.isConnected()) {
      Serial.println("sendLightAndBack");
      bleGamepad.press(BUTTON_4);
      delay(30);
      bleGamepad.release(BUTTON_4);
      delay(200);
      bleGamepad.press(BUTTON_4);
      delay(30);
      bleGamepad.release(BUTTON_4);
      delay(400);
      bleGamepad.press(BUTTON_2);
      delay(30);
      bleGamepad.release(BUTTON_2);
    }
    request->send(200, "text/plain", "sendLightAndBack");
  });

  // Start server
  server.begin();
}

void loop() {
  // Nothing to do here
}
