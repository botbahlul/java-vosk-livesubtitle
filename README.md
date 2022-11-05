# java-vosk-livesubtitle

### A java based desktop aplication that can RECOGNIZE any live streaming in 21 languages that supported by VOSK then TRANSLATE and display it as LIVE SUBTITLES

This app was develepoed with Apache Netbeans IDE 15 https://netbeans.apache.org/download/index.html

The speech recognition part is using java vosk api https://github.com/alphacep/vosk-api/tree/master/java/lib/src/main/java/org/vosk

The translation part is using self made GoogleTranslateTranslator class

If you want to build this source to an exe file, you will need jar2exe https://www.jar2exe.com/

That build.gradle file must contain these lines :
```
jar {
    manifest {
        attributes(
                'Main-Class': 'org.vosk.LiveSubtitle'
        )
    }

 duplicatesStrategy = DuplicatesStrategy.INCLUDE
 from { configurations.compileClasspath.collect { it.isDirectory() ? it : zipTree(it) } }

}
```

After Clean And Build with Netbeans, goto that build\distribution folder and extract that zip file.
Then open jar2exe, browse to distributions\java-vosk-livesubtitle\lib folder, select java-vosk-livesubtitle.jar as source that you want to convert to exe
Set JDK version to 1.8
Choose Windows GUI Application if you don't want to see any log exception in DOS interface.
Add all other jar files in that lib folder except that java-vosk-livesubtitle.jar that you already set as main source.
Don't forget to check Create 64bits executivce to avoid libvosk dll errors
Then click Config Internal to set additional compile parameter UTF-8 enconding
Select Custom tab paste this code
```
option -Dfile.encoding=UTF-8
```
click Apply, and Next to start compile
The file java-vosk-livesubtitle.exe will be created on same folder of those jar filers (lib folder)
Now you can run this app in single click.

As usual for best recognizing quality, on windows you will need STEREO MIX or VIRTUAL AUDIO CABLE as RECORDING/INPUT DEVICE 
![image](https://user-images.githubusercontent.com/88623122/199527559-e2609d8c-3479-420d-8c52-806fa56a21f4.png)
![image](https://user-images.githubusercontent.com/88623122/199528286-1ab77dc4-38a9-41f2-9b92-25db352a1ed2.png)
![image](https://user-images.githubusercontent.com/88623122/199528861-22541706-3bdf-427c-8c2f-44174b114e34.png)

and on linux you willl need PAVUCONTROL (by choosing MONITOR of your audio device as INPUT DEVICE)
![image](https://user-images.githubusercontent.com/88623122/199517907-76d61acb-3f07-49b6-8f2f-4b6a2b787eff.png)


### License
MIT
