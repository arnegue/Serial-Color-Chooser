# Summary #
This is a small repository which contains a project with a color chooser. The chosen color will be send in the format as given in my repository [PIC16F1825 PWM USART LED](https://bitbucket.org/arnegue/pic16f1825-pwm-usart-led/overview).

# Used library #
For this project i used the javax.comm-libary. The jar-file in this repo is NOT enough for the installation of the library! Use Google for it ;)

# Before starting #
Before starting your program, please select in first in your Mainfile your desired COM-Port (Line 35 ``openConnection(PORT)``)

# Starting the program # 
Start as you like your program in your favourite IDE or with command line itself.

# Choosing the color #
Go to the Register ``HSV`` and change the saturation-value to 100 %. After that you can choose every RGB-Value which will be sent via UART.