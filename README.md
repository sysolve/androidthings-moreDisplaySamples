# moreDisplaySamples

Android Things 更多显示相关的案例
----

在入门开发配件包中，有提供红、绿、蓝三色的LED，那么我们是不是可以通过三种颜色的组合，显示我们需要的颜色呢？答案是肯定的。如下图连接三种颜色的LED。

![使用入门配件包中的三种颜色的LED显示彩色](https://github.com/sysolve/androidthings-cameraCar/blob/master/photos/photo_3led.jpg)

仅仅能让三种颜色亮起来是不够的，我们需要分别控制每种颜色的亮度，通过三种颜色的比例，才能达到显示所需颜色的效果。

对LED的亮度控制，可以通过控制电流的方式，但一是不方便数字电路动态调节，二是亮度和电流的关系不是线性的，电流过小或过大时LED的颜色还会产生色差。所以通常不用电流来调节LED的亮度。

我们通常会采用PWM来调节LED的亮度，通过改变对LED供电的占空比来改变亮度。也就是说，我们的LED灯不是一直亮的，二是间歇的亮、暗、亮、暗，通过控制亮和暗的时间比例来调节亮度。只要我们切换的比较快，是看不出来灯是在闪烁切换的，一般要求频率>60Hz。本例中，我们设置为100Hz。

```Java
PeripheralManagerService service = new PeripheralManagerService();

pwmRed = service.openPwm(BoardSpec.getInstance().getPwm(0));

pwmRed.setPwmDutyCycle(30);   //percent, 0-100
pwmRed.setPwmFrequencyHz(100);
pwmRed.setEnabled(true);
```

在上面的代码中，我们设置了红色LED，使用第一个PWM脚(PIN_12)。

我们将刷新频率（PwmFrequencyHz）设置成了100Hz。

占空比是30:70，即PwmDutyCycle=30，相当于LED我们显示了30%的亮度。

同样我们可以将绿色LED连接在第二个PWM脚上(PIN_33)。

之后是蓝色LED，这时候问题来了，我们的开发板（无论是树莓派3B还是NXP Pico）只有2个PWM脚！

这时候当我们了解了PWM的原理，我们可以用GPIO，做一个SoftPWM，也就是说，通过程序设置GPIO为高电平，持续一段时间a，再设置为低电平，再持续一段时间b。a:b就是我们前面说的占空比。a+b就是我们一个周期的时间，而1秒/(a+b)就是我们设置的刷新频率。

我们可以试算一下，100Hz的刷新频率，即a+b=1000毫秒/100=10毫秒。那么要设置PwmDutyCycle=30，即a=3毫秒，b=7毫秒。毫秒级的GPIO控制，在Android Java层面还是可以胜任的，那么我们就可以动手写我们的SoftPwm了。具体代码可以参见Github上的源码，这里不再详述。

![面包板连线图](https://github.com/sysolve/androidthings-cameraCar/blob/master/photos/schematics_color_led_touch_button.jpg)

代码中增加了一个触摸按钮模块，切换三色灯全亮还是渐变两种显示模块，按钮模块功能和按键一样，都是通过GPIO输入信号，模块需要接3.3V或5V电源，GND和一个GPIO，例子代码中我们接在PIN_40上。

下图是三种颜色的灯全亮的效果。

![三色灯全亮效果](https://github.com/sysolve/androidthings-cameraCar/blob/master/photos/photo_3led_on.jpg)

下图是颜色渐变显示的效果，做个纸筒套在上面，颜色从纸反射出来，混色的效果更好。可以套个纸筒，反射的颜色会混合在一起。

![三色灯颜色渐变效果](https://github.com/sysolve/androidthings-cameraCar/blob/master/photos/photo_3led_color.jpg)

![三色灯颜色渐变效果-动画](https://github.com/sysolve/androidthings-cameraCar/blob/master/photos/color_change_3led.webp)

如果没有入门开发配件包，或者觉得使用3个LED连接电阻比较麻烦，那么我们可以用一个三色LED模块，其实就是集成了三色LED和电阻的模块，效果如下图。

使用一个全彩LED模块更方便，模块和电路原理图如下：

![全彩LED模块](https://github.com/sysolve/androidthings-cameraCar/blob/master/photos/module_color_led.jpg)

全彩LED模块的结构，相当于集成了红绿蓝三个LED

数码管显示模块
----
本例中，我们还引入了一个四色数码管模块，当按下触摸按钮时，我们会显示按下按钮的次数。

接线很简单，VCC连接到+5V，GND接开发板的GND，CLK连接到开发板的PIN38，DIO连接到开发板的PIN36即可。

4位数码管模块连接图
控制显示的代码也很简单：

```Java
mSegmentDisplay =new NumericDisplay(

        BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_36),         //DIO: Data

        BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_38)          //CLK: Clock

);

mSegmentDisplay.setBrightness(1.0f);  //设置亮度

mSegmentDisplay.setColonEnabled(true);  //是否显示冒号

mSegmentDisplay.display("1234");  //显示数字
```

OLED显示模块
----

本例中，我们还引入了一个OLED显示模块，可以显示128*64个像素点，我们可以用它来显示图形和文字。

OLED显示模块
模块采用IIC接口，估连接到开发板的IIC接口即可，连接图如下：

OLED显示模块
为不影响案例代码在未连接OLED模块时正常运行，OLED显示的代码目前是注释掉的，连接好模块，将代码取消注释即可。

```Java
//要显示OLED显示屏，先连接好开发板到OLED屏的IIC连接线，再取消下面行的注释。开启OLED显示屏，会导致蓝色灯显示闪烁

//oledScreen = new OledScreen(this);
```

例子参照官网Samples代码，有三种显示演示：显示变化的点，显示十字交叉线，显示移动的小花图片，按下触摸按钮会在三种模式切换。

本例中使用到的模块
----


如需要本例中使用到的以上四种模块，可以从淘宝购买：https://item.taobao.com/item.htm?id=564533484773

树莓派3B开发板和NXP Pico开发板均适用，购买以上模块套装，赠送连接所需杜邦线。