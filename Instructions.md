# C2 Framework

A lightweight Java Spring Boot framework with static pages and Python agents, Dockerized for easy deployment.

---

## Features

- Command & Control
- Real-time Keylogging
- Screenshot Capture
- File Transfer / Exfiltration
- Remote files download/upload
- Agent Management


---

## Requirements

- Java 21
- Maven
- Docker & Docker Compose
- Git

---

## Getting Started

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/<username>/C2.git
cd C2
```

## BUILDING IMAGE OR PULLING IMAGE
```bash
# you can build image locally
docker build -t c2 .
#or alternatively u can pull from my dockerhub
docker pull kash1dev2/c2:latest
```

## RUNNING IMAGE
```bash
# Start everything
docker-compose up -d

# Stop everything
docker-compose down

# Check logs
docker-compose logs -f
```
## RUNNING AGENT ON VICTIM
```bash
#In repository there is a folder in C2/python_agents
pip install -r requirements.txt
python <agent>.py
# make sure to change ip address in agent if you want to use via apps like ngrock
```

## ⚠️ Legal & Ethical Note
- These features are potentially malicious.
- Real-time keyloggers, screenshots, and remote execution without user consent are illegal.
- Use them only in educational, lab, or testing environments.
