# SkyAutoLightGift

SkyAutoLightGift is an open-source project designed to automatically send light gifts to every friend on the constellation in the game "Sky: Children of the Light". This application uses accessibility services to obtain the position of each friend's name (TextView) on the constellation and utilizes an ESP32 to simulate gamepad operations to complete the gift-sending process.

This application has been tested on NetEase China Server version 0.12.7.

## Features

- Automatically recognizes friend names on the constellation
- Simulates clicks to enter the friend's menu
- Automatically sends light gifts (requires ESP32 assistance)

## How It Works

1. **Recognize friend name positions**: Since the friend positions on the Sky constellation are random, the program first uses accessibility services to obtain the position of each friend's name (TextView) on the constellation.
2. **Enter the friend's menu**:
   - Quickly clicks the center and the edges of the TextView to enter the menu before the game responds.
   - Checks if the screen only contains the friend's name (excluding the "心火" prompt); if so, the menu has been successfully entered.
4. **Send the light gift**: Uses the ESP32 to simulate pressing the gamepad X button to send the gift, then presses the B button to return to the constellation.

## Disclaimer

This application achieves automatic gift-sending through simulated operations and does not modify game memory, but using third-party tools can lead to account bans. The developer is not responsible for any loss resulting from the use of this application.

## Usage

1. Clone this repository to your local machine:
    ```bash
    git clone https://github.com/milkycandy/SkyAutoLightGift.git
    ```
2. Import `SkyAutoLighting.ino` into Arduino IDE, compile and upload it to the ESP32.
3. Connect your phone and the ESP32 to the same WiFi. The ESP32 will print its local IP address via serial output (baud rate: 115200).
4. Modify the hardcoded ESP32 IP address in `FloatingWindowService` to the actual local network address.
5. Import the project into Android Studio, connect your phone, then build and run the application.
6. Click the start button; the application will guide you to enable accessibility services and overlay permissions.
7. After the overlay appears, open the Sky constellation, navigate to the page where you want to send light gifts, and click the start button.

## Areas for Improvement

1. **Automatic Page Turning**: Plan to implement automatic page turning to send light gifts to the next page of friends after finishing the current page.
2. **Interrupt Gift Sending**: Plan to add a cancel button to stop automatic gift-sending.

## Known Issues

- If the constellation group name is not "好友" or "挚友", the program may mistakenly click the constellation name as a friend.
- If a friend's name contains "心火", the friend will be ignored by the program.
- Does not work on NetEase MuMu Emulator (unable to obtain screen layout).
- Many more...

## Contributing

If you have any questions or suggestions, please submit an Issue or contact [ohayo@milkycandy.cn](mailto:ohayo@milkycandy.cn).

## License

This project is open-source under the GPL-3.0 license. For details, please refer to the [LICENSE](LICENSE) file.