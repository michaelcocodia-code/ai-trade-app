AI TRADE - ANDROID APP (simple version)
======================================

What it is: a small app that opens your own server's pages full screen:
  Table   = the flip table
  Charts  = the candle charts
  Chat    = the old chat page
The app keeps no trading data. It only shows what your VPS server shows,
so the VPS and the bridge server must be running.

On first start it asks for: server address, username, password.
These are saved on THIS phone only. Change them any time with the Settings button.

HOW TO GET THE APP FILE (no Android Studio needed)
--------------------------------------------------
GitHub will build the app file for you, free.

1. Go to github.com and make a free account (or sign in).
2. Click New repository. Name it ai-trade-app. Choose PRIVATE. Click Create.
3. On the new repository page click "uploading an existing file".
4. Open this folder on your computer and drag EVERYTHING inside it into the page:
   build.gradle, settings.gradle, gradle.properties, the "app" folder,
   and the ".github" folder (it is hidden-looking because it starts with a dot,
   but it must go too - it holds the build recipe).
5. Click Commit changes.
6. Click the "Actions" tab at the top. A build called "Build APK" starts by itself.
   Wait about 5 minutes until it shows a green tick.
   (If the tab says workflows are disabled, click the green button to enable them,
   then open "Build APK" on the left and click Run workflow.)
7. Click the finished run. At the bottom, under Artifacts, click ai-trade-app-apk.
   A zip downloads. Open it: inside is app-debug.apk.

HOW TO INSTALL IT ON THE PHONE
------------------------------
1. Send app-debug.apk to the phone (Telegram "Saved Messages", Google Drive, or USB).
2. Tap the file on the phone. Android will ask to allow "install unknown apps"
   for the app you opened it from - allow it for this one install.
3. Open AI Trade, enter the server address, username and password.

IF THE BUILD TURNS RED (FAILS)
------------------------------
Click the failed run, click the red step, copy the error text and send it to Claude.

IF .github WILL NOT UPLOAD
--------------------------
In the repository click Add file > Create new file. In the name box type
   .github/workflows/build-apk.yml
(the slashes make the folders), paste the contents of that file, and commit.

THINGS TO KNOW
--------------
* Your server address starts with http, not https. That means the login is sent
  without encryption. Fine for testing on your own, but before sharing the app
  with other people we should add HTTPS together with the separate logins.
* Alerts and sound only work while the app is open. Telegram is still the alert
  that reaches you when the phone is asleep.
* The app file is a "debug" build: fine for you, not for the Play Store.
