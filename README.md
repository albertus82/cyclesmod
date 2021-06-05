CyclesMod
=========

[![Latest release](https://img.shields.io/github/release/albertus82/cyclesmod.svg)](https://github.com/albertus82/cyclesmod/releases/latest)
[![Build status](https://github.com/albertus82/cyclesmod/workflows/build/badge.svg)](https://github.com/albertus82/cyclesmod/actions)
[![Build status](https://ci.appveyor.com/api/projects/status/github/albertus82/cyclesmod?branch=master&svg=true)](https://ci.appveyor.com/project/albertus82/cyclesmod)
[![Known Vulnerabilities](https://snyk.io/test/github/albertus82/cyclesmod/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/albertus82/cyclesmod?targetFile=pom.xml)

## :checkered_flag: Works with [**Grand Prix Circuit**](#grand-prix-circuit) too! 

[**The Cycles - International Grand Prix Racing**](https://www.mobygames.com/game/cycles-international-grand-prix-racing) is a motorcycle simulation video game developed by *Distinctive Software* and published by *Accolade*.

Dating back to 1989, it was distributed for the most popular platforms of the time, from the Commodore 64 to the IBM PC (DOS), and is now easily available as *Abandonware*. 

Nowadays it cannot be executed natively on modern operating systems, but it runs [fine](https://www.dosbox.com/comp_list.php?showID=348) in [**DOSBox**](https://www.dosbox.com).

**CyclesMod** is a *mod* for the DOS version of this game, which allows you to **change the configurations of the bikes**, which are normally not modifiable by the player:

![Screenshot](https://user-images.githubusercontent.com/8672431/120505649-75f69500-c3c5-11eb-8294-8d8a522b7aba.png)

The game allows to choose three different bikes: 125, 250 or 500cc. The motorcycle operating parameters are stored in the **`BIKES.INF`** binary file in the game directory. *CyclesMod* loads and interprets this file, and provides a graphical interface for inspecting and modifying the configuration of the bikes. On save, the program generates a new `BIKES.INF` containing the modified configuration.


## Download

Download the [latest release](https://github.com/albertus82/cyclesmod/releases/latest) from the [releases page](https://github.com/albertus82/cyclesmod/releases).


## Installation

* **Windows**: if you downloaded a ZIP package, simply unpack the archive; otherwise run the installer (EXE) to install the application.

  If the OS complains with a ***Windows protected your PC*** popup, you may need to click ***Run anyway*** to proceed with the installation.

  ![Windows protected your PC](https://user-images.githubusercontent.com/8672431/31048995-7145b034-a62a-11e7-860b-c477237145ce.png)

  In order to enable the *Run anyway* button, you may need to open the *Properties* of the installer, tab *General*, section *Security* (if available), and tick the ***Unblock*** option.
  > This workaround is required because the installer executables are not *signed*, and there are no free certificates I can use to sign them.
* **Linux** & **macOS**: unpack the archive.

**This application requires [Java SE Runtime Environment (JRE)](https://www.java.com) v1.8 (or newer) to run.**


## Usage

The main application window has three tabs, one for each motorcycle category: 125, 250 and 500 cc. Each tab is divided into three groups:

* General **Settings**
* **Gearbox**
* **Power** curve

There is also a graph of the power/torque curve that is generated in real time from the engine power values present in the relevant group.

Each group has several fields with descriptive labels. Customized values are shown in bold typeface to highlight the fact that the value is different from the default one. Anyhow the default value of a field is always available as tooltip, just move the mouse over the field and wait for the suggestion to appear.

When *CyclesMod* is launched without arguments, all the fields are preloaded with the default values for all motorcycles.

Through the menu bar it is possible to:

* open an existing `BIKES.INF` file;
* save the current settings in a `BIKES.INF` file;
* restore the default settings for one or all bikes (any unsaved customization will be lost);
* modify or draw the power curve in a special dialog box;
* change the numeral system (decimal/hexadecimal);
* change the interface language;
* view *CyclesMod* version, license and acknowledgements.

After modifying the desired properties, simply use the *Save* or *Save as...* function (*File* menu) to save the `BIKES.INF` file in the game directory. The file will almost certainly already be there, so you will be prompted to overwrite it, so **make sure you have a backup copy of the file you will overwrite**. You can now run the game in a DOS emulator such as *DOSBox*. The configuration is reloaded every time you start a new race, so you don't need to completely restart the game.

#### Restoration of the original `BIKES.INF` file

If you want to restore the original configurations of all the bikes, simply use the *Reset* function (*Edit* menu), which will reset the settings like the original `BIKES.INF` file supplied with the game. After that, use the *Save* or *Save as...* function to re-create the original version of the `BIKES.INF` file.


## Bike configuration

### General settings

Each bike has eight general settings that determine the following characteristics:

* **Gears count**: the number of gears of the gearbox. Valid value range: `0`-`9`.

* **RPM redline**: engine speed beyond which, after a variable time determined by the *Overrev grace period* setting and the difficulty level chosen, the engine breaks down. At difficulty levels 1 and 2 (the lowest), this value also determines the upshift speed. Valid value range: `8500`-`32767` RPM (values less than `8500` are considered equal to `8500`).

* **RPM limit**: maximum speed that the engine can reach (limiter). Valid value range: `768`-`14335` RPM. Note that, once the limit is reached, the motorcycle continues to accelerate with the engine power set for the limit RPM, therefore, to effectively limit the speed to a certain value, it is necessary to set to zero the power values around this RPM limit.

* **Overrev tolerance**: grace period during which the engine does not fail despite running at a higher speed than the *RPM redline*; the value is expressed in a linear unit of measurement of time which varies according to the difficulty level. Valid value range: `0`-`32767`. The following list can be useful to determine the value based on the desired tolerance in seconds, depending on the difficulty level:

   * Level 1/5 (Beg.): The engine never fails unless `0` is set.
   * Level 2/5: 1 sec. = `50` (automatic gearbox is still active).
   * Level 3/5: 1 sec. = `80`.
   * Level 4/5: 1 sec. = `120`.
   * Level 5/5 (Pro): 1 sec. = `160`.

* **Grip**: skid threshold, which determines the speed with which it is possible to take curves. Valid values between `0` (the bike skids immediately and does not turn at all) and `65535` (the bike never skids).

* **Braking speed**: determines the stopping time of the motorcycle. Valid values between `0` (the bike does not brake at all; on the contrary, friction and aerodynamic resistance are eliminated) and `65535` (the bike stops instantly on brake).

* **Spin threshold**: ease with which the bike spins while skidding around a corner. Valid values between `0` (the bike spins at the first hint of skidding) and `32767` (the bike never spins).

* **RPM downshift**: significant only for difficulty levels 1 and 2 that involve automatic transmission; it determines the speed below which the automatic transmission engages a lower gear, if available. Valid values between `0` (never downshifts) and `32767` RPM (downshifts continuously, in fact it makes shifting impossible).

> There are also three unused values related to a *pit stop* feature that was never implemented in this game, but that is present in its ancestor: [**Grand Prix Circuit**](#grand-prix-circuit).

### Gearbox

Gear ratios can be configured for each individual gear. Higher values correspond to shorter ratios. The permitted values are between `0` and `65535` for gears 1 to 9, while for *N* (neutral) gear the value is irrelevant.

### Power curve

The engine power curve is constructed from the values present in this group. Each value represents the engine power in *hp* (or something similar) at a given engine speed indicated on the label associated with the field containing the value. Allowed values are between `0` and `255` hp. The resulting curve is graphically represented in a dedicated frame.

It is also possibile to draw and modify the curve using the pointing device from the *Power Graph* window, reachable from the *Edit* menu or simply double clicking on the graph in the main window.


## Grand Prix Circuit

[**Grand Prix Circuit**](https://www.mobygames.com/game/grand-prix-circuit) is a car racing game developed by *Distinctive Software* and published by *Accolade* like **The Cycles**, and in fact it is its ancestor, since it came out the previous year (1988).

The cars featured in *Grand Prix Circuit* and the bikes featured in *The Cycles* share the very same configuration scheme; there's only one biggest difference: the car settings of *Grand Prix Circuit* are stored inside the game executable files instead of an external file, therefore the only way to edit them is to patch the executable itself. *CyclesMod* is able to do this.

Simply *Open* your favorite original executable file of *Grand Prix Circuit* (e.g. `GPEGA.EXE`) and start modifying the car configurations. When you are satisfied and want to try your setup on a circuit, issue the *Save* command and choose a new name for your patched executable (e.g. `GPEGAX.EXE`) and eventually run the game using this new EXE. Like *The Cycles*, *Grand Prix Circuit* is also [compatible](https://www.dosbox.com/comp_list.php?showID=376) with DOSBox.

**Note that *CyclesMod* is able to open only the original executables of *Grand Prix Circuit***, it cannot open patched versions, so the only way to save the settings in order to work on them later is to *Export* them as a *CFG* file using the appropriate functionality. This way, you can resume your work by reopening an original executable, and then importing the previously exported *CFG* file.


## Command line version

If you prefer to operate without a graphical interface, an almost full-featured command line version of *CyclesMod* is available. It can be executed using the following executables:

* Windows: `CyclesMod.exe`
* Linux: `cyclesmod.sh`
* macOS: `cyclesmod.command`

Once executed, the program first checks for the existence of a text file called `BIKES.CFG`; if not present, it creates a default one mapping the original `BIKES.INF` binary file. The `BIKES.CFG` file is basically a plain text translation of the `BIKES.INF` file; opening it with a text editor, it is possible to directly access the motorcycle parameters, which are quite self-explanatory and divided into the usual three groups: *settings*, *gearbox* and *power*. Thus, initially the `BIKES.CFG` file will contain the game defaults derived directly from the original `BIKES.INF` file.

Next, the program reads the `BIKES.CFG` file contents and eventually produces a new `BIKES.INF` file, making a backup of the existing one, if any. At this point it is sufficient to copy the generated `BIKES.INF` file into the game directory. Starting the game you will then be able to experiment with the changes made to the configuration.

To make further changes to the bikes, simply open the `BIKES.CFG` file, modify the parameters of interest, save the file and re-run *CyclesMod*. The program will detect the existence of the `BIKES.CFG` file and will produce a new `BIKES.INF` containing the changes made. In case of errors, appropriate console messages will be displayed.

**If you want to restore the original `BIKES.INF` file shipped with the game, simply delete the `BIKES.CFG` file and run the program without arguments: it will automatically generate default `BIKES.CFG` and `BIKES.INF` files.**


## Acknowledgements

Icon designed by [Everaldo Coelho](http://www.everaldo.com).

This application uses or includes portions of the following third party software:

|Component                   |Author                     |License                                                     |Home page                                              |
|----------------------------|---------------------------|------------------------------------------------------------|-------------------------------------------------------|
|Eclipse Platform & SWT      |Eclipse Foundation         |[License](https://www.eclipse.org/legal/epl-2.0/)           |[Home page](https://www.eclipse.org)                   |
|Inno Setup                  |Jordan Russell             |[License](https://jrsoftware.org/files/is/license.txt)      |[Home page](https://jrsoftware.org/isinfo.php)         |
|Launch4j                    |Grzegorz Kowal             |[License](https://opensource.org/licenses/BSD-3-Clause)     |[Home page](http://launch4j.sourceforge.net)           |
|Picocli                     |Remko Popma                |[License](https://git.io/JUqAY)                             |[Home page](https://picocli.info)                      |
|Reflections                 |ronmamo                    |[License](https://git.io/Jtp8i)                             |[Home page](https://git.io/Jtp81)                      |
|universalJavaApplicationStub|Tobias Fischer             |[License](https://git.io/JUqAq)                             |[Home page](https://git.io/JUqAF)                      |
