## Setup
1. On a Windows pc press `Win + R` then type `taskschd.msc` and enter.
2. Click on `Import Task` on the right side and import the `checkin-ps1.xml` or `checkin-exe.xml` from this directory and continue.
3. Place the `checkin.ps1` or `checkin.exe` file at `C:\Checkin\` and you're good to go.

> When deploying this to lab pcs, this process could be automated and the files placed in a restricted filespace so only with administrator priveleges you can modify any of this. Effectively restricting students from changing anything.

## How it works
1. For `checkin-ps1.xml`

The Windows Task Scheduler will periodically run `C:\Checkin\checkin.ps1` every 5 minutes, this is the lowest interval available for periodic tasks. The script fetches our mac address and sends an HTTP POST request to `/checkin` with a body like:
```json
{"macAddress": "A1-B2-C3-D4-E5-F6"}
```

2. Or for `checkin-exe.xml`

The Windows Task Scheduler will run `C:\Checkin\checkin.exe` at system startup. The program runs in the background and periodically sends a checkin at a specified interval.

You can for now view the checkins easily by visiting `localhost:8080/checkins` in your browser. They can also be filtered with the `old` query param like `localhost:8080/checkins?old=5`, this will retreive all checkins older than 5 minutes.

You can also manually send HTTP post requests to `/checkin` with the correct body to test the system!

The backend will then update this checkin in the database for further use! Check the code in `CheckinController.java` and corresponding code in `CheckinService.java`!
