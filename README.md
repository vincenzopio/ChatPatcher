# ChatPatcher

![236015280-daba9abc-5254-4691-93ad-6b044557d395](https://github.com/user-attachments/assets/683700dc-d7fc-43ab-ba01-90198bfe82c5)
yep, it's paint.
---

**ChatPatcher** is a Velocity plugin that removes the signed chat messages feature introduced in Minecraft 1.19+
If you’ve encountered errors such as:


- “A plugin tried to cancel a signed chat message”
- “A proxy plugin caused an illegal protocol state”

*you're likely to require this one!*

[for reference](https://github.com/PaperMC/Velocity/issues/804)
---

## Why Use ChatPatcher?

With the release of Minecraft 1.19.1, Mojang introduced mandatory signed chat messages to enhance security and message integrity. While this change aims to reduce abuse and ensure authenticity, it has inadvertently caused compatibility issues for networks relying on Velocity's chat manipulation features. ChatPatcher removes these restrictions, restoring the classic chat handling that many servers have long depended on.

[Learn more about signed chat messages and their implications](https://gist.github.com/kennytv/ed783dd244ca0321bbd882c347892874)  
  
---
## Installation

### Clone the Repository

Clone the ChatPatcher repository using Git:

```bash
git clone https://github.com/vincenzopio/ChatPatcher.git
```

### Navigate to the Project Directory

```bash
cd ChatPatcher
```

### Build the Project

Build the project with your preferred build tool (e.g., Maven or Gradle):

```bash
./gradlew build
```

*Alternatively, if you prefer a pre-built version, download the latest release from the [Releases page](https://github.com/vincenzopio/ChatPatcher/releases).*

---
## Usage

ChatPatcher requires no setup and no dependencies.

Beware! ChatPatcher **DOES NOT** replace Paper side plugins such as NoPopup, NoChatReports, ...

Actually, we don't do any packet modification, we're just tweaking Velocity using reflections (ik, i also hate that, but better than having to maintain a fork).


## Contributing

To contribute:

1. **Fork the repository.**
2. **Create a new branch** for your feature or bugfix:
   ```bash
   git checkout -b feature-name
   ```
3. **Make your changes** and commit them with clear, descriptive messages:
   ```bash
   git commit -m "Add new feature"
   ```
4. **Push your changes** to your fork:
   ```bash
   git push origin feature-name
   ```
5. **Create a pull request** with a detailed description of your changes.

---

## License

This project is licensed under the [MIT License](LICENSE).

---
