# SkyAutoLightGift

SkyAutoLightGift is an open-source project designed to automatically send light gifts to every friend on the constellation in the game "Sky: Children of the Light". This application uses accessibility services to obtain the position of each friend's name (TextView) on the constellation and simulates click operations to complete the gift-sending process.

This application has been tested on NetEase China Server version 0.12.5.

## Features

- Automatically recognizes friend names on the constellation
- Simulates clicks to enter the friend's menu
- Automatically clicks the send light gift button

## How It Works

1. **Recognize friend name positions**: Since the friend positions on the Sky constellation are random, the program first uses accessibility services to obtain the position of each friend's name (TextView) on the constellation.
2. **Enter the friend's menu**:
    - Attempts to click the center of the friend's name.
    - If other friend names disappear from the screen, leaving only the target friend's name, the menu has been successfully entered.
    - If it fails to enter the friend's menu, it tries clicking the left and right edges of the name, as the name may sometimes appear on one side of the star.
4. **Send the light gift**: Completes the gift-sending by clicking the absolute screen coordinates.

## Disclaimer

This application achieves automatic gift-sending through simulated clicks and does not modify game memory, but using third-party tools can lead to account bans. The developer is not responsible for any loss resulting from the use of this application.

## Usage

1. Clone this repository to your local machine:
    ```bash
    git clone https://github.com/milkycandy/SkyAutoLightGift.git
    ```
2. Import the project into Android Studio, connect your phone, then build and run the application.
3. Enter the screen coordinates of the send light gift button. Find a position that works for both tall and short characters. You can use the "Pointer location" in Android Developer Options to help determine screen coordinates. Remember to save.
4. Click the start button; the program will guide you to enable accessibility services and overlay permissions.
5. After the overlay appears, open the Sky constellation to the group that you want to send light gifts, and click the start button.

## Areas for Improvement

1. **Send light gift button click**: Currently, clicking the send light gift button requires pre-entering the button's absolute screen coordinates. A better implementation method is needed.
2. **Automatic Page Turning**: Plan to implement automatic page turning to send light gifts to the next page of friends after finishing the current page.
3. **Interrupt Gift Sending**: Plan to add a cancel button to stop automatic gift-sending.

## Known Issues

- If the constellation group name is not "好友" or "挚友", the program may mistakenly click the constellation name as a friend.
- Does not work on NetEase MuMu Emulator  (unable to obtain screen layout).

## Contributing

If you have any ideas or suggestions, please contact [ohayo@milkycandy.cn](mailto:ohayo@milkycandy.cn).

## License

This project is open-source under the GPL-3.0 license. For details, please refer to the [LICENSE](LICENSE) file.
