# 🛠 Development Guide

## Project Structure
```
blockcloud-backend/
├── src/
│   ├── main/
│   │   ├── java/com/blockcloud/
│   │   │   ├── BlockcloudApplication.java
│   │   │   └── HelloController.java
│   │   └── resources/
│   │       └── application.yml
├── build.gradle
└── README.md
```

## Development Tips
- Use Java 21 syntax and features
- All documentation and comments should be in English
- Build: `./gradlew build`
- Test: `./gradlew test`

## Code Style
- Use `@RestController` or `@Controller` as appropriate
- Use Lombok for reducing boilerplate code
- Keep controller logic minimal, delegate to service layers

## Running in Dev Mode
```bash
./gradlew bootRun
```
Hot reload is supported with DevTools if included.
