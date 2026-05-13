# 🎭 FaceRecognitionSystem
 
> Real-time facial recognition system built with Java 21, Spring Boot 3, OpenCV 4.9 and JavaCV — featuring live webcam capture, LBPH-based recognition, dynamic enrollment and automatic model retraining.
 
---
 
## 📋 Table of Contents
 
- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [System Flow](#system-flow)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Usage](#usage)
- [Database Schema](#database-schema)
- [Services](#services)
- [Known Limitations](#known-limitations)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)
---
 
## Overview
 
**FaceRecognitionSystem** is a desktop application that performs real-time facial detection and recognition using a standard webcam. It leverages OpenCV's Haar Cascade classifier for face detection and the LBPH (Local Binary Patterns Histograms) algorithm for recognition.
 
The application is built on top of Spring Boot, applying a clean Service-Oriented Architecture with full dependency injection, JPA-backed persistence and an `ExecutorService`-driven processing pipeline to keep the UI thread non-blocking.
 
| Status | Component |
|--------|-----------|
| ✅ Working | Camera capture |
| ✅ Working | Face detection (Haar Cascade) |
| ✅ Working | Face recognition (LBPH) |
| ✅ Working | PostgreSQL persistence |
| ✅ Working | Real-time rendering |
| 🔬 Planned | TensorFlow / FaceNet embeddings |
 
---
 
## Architecture
 
```
┌──────────────────────────────────────────────────────────┐
│                      Spring Boot App                      │
│                                                          │
│  ┌───────────────┐   ┌─────────────────┐                 │
│  │ CameraService │──▶│FaceDetectionSvc │                 │
│  └───────────────┘   └────────┬────────┘                 │
│         │                     │                          │
│         │             ┌───────▼──────────┐               │
│         └────────────▶│FaceRecognitionSvc│               │
│                        └───────┬──────────┘              │
│                                │                         │
│              ┌─────────────────▼──────────────┐          │
│              │         PersonRepository        │          │
│              │         (Spring Data JPA)       │          │
│              └─────────────────┬──────────────┘          │
│                                │                         │
│                         ┌──────▼──────┐                  │
│                         │  PostgreSQL  │                  │
│                         └─────────────┘                  │
└──────────────────────────────────────────────────────────┘
         │                         │
   [Webcam Input]           [data/faces/]
                            (Image Dataset)
```
 
**Design Patterns applied:**
- Service Layer Pattern
- Repository Pattern (Spring Data JPA)
- Dependency Injection (Spring IoC)
---
 
## Tech Stack
 
| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.3.5 |
| Computer Vision | OpenCV | 4.9.0 |
| CV Bindings | JavaCV / JavaCPP Presets | 1.5.10 |
| Database | PostgreSQL | latest |
| ORM | Spring Data JPA / Hibernate | — |
| Utilities | Lombok | — |
| Concurrency | ExecutorService | JDK built-in |
 
---
 
## Features
 
| Feature | Description |
|---------|-------------|
| **Live Detection** | Detects faces in real time using Haar Cascade classifier |
| **LBPH Recognition** | Identifies registered persons with confidence scoring |
| **Dynamic Enrollment** | Register new persons on-the-fly via keyboard shortcut |
| **Auto Retraining** | Model retrains automatically after each new enrollment |
| **Dataset Management** | Captures and organises 20 facial images per person under `data/faces/{label}/` |
| **Persistence** | Stores persons and their label mappings in PostgreSQL |
| **Multithreading** | Capture and recognition run on a dedicated `ExecutorService` thread |
| **Overlay Rendering** | Draws bounding boxes and name labels directly on the video feed |
 
---
 
## System Flow
 
```
Application Startup
        │
        ▼
OpenCV Native Libraries Loaded
        │
        ▼
Webcam Initialised (640×480)
        │
        ▼
┌──── Frame Capture Loop ────────────────────────────────┐
│                                                        │
│  Frame ──▶ Grayscale Conversion                        │
│                   │                                    │
│                   ▼                                    │
│          Haar Cascade Detection                        │
│                   │                                    │
│          ┌────────▼──────────┐                         │
│          │  Face(s) Found?   │                         │
│          └──┬─────────────┬──┘                         │
│            YES             NO                          │
│             │               └──▶ (continue loop)       │
│             ▼                                          │
│   Resize (200×200) + Histogram Equalisation            │
│             │                                          │
│             ▼                                          │
│   LBPH Predict → confidence < 50 → Known Person        │
│             │                                          │
│             ▼                                          │
│   Render Bounding Box + Name Label                     │
│                                                        │
└────────────────────────────────────────────────────────┘
```
 
---
 
## Project Structure
 
```
FaceRecognitionSystem/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/facerecognition/
│       │       ├── FaceRecognitionApplication.java
│       │       ├── config/
│       │       ├── model/
│       │       │   └── Person.java
│       │       ├── repository/
│       │       │   └── PersonRepository.java
│       │       └── service/
│       │           ├── CameraService.java
│       │           ├── FaceDetectionService.java
│       │           ├── FaceRecognitionService.java
│       │           └── TensorFlowService.java   ← mock
│       └── resources/
│           ├── application.properties
│           └── haarcascade_frontalface_default.xml
├── data/
│   └── faces/
│       └── {label}/                             ← per-person image sets
├── pom.xml
└── README.md
```
 
---
 
## Prerequisites
 
- **Java 21** (JDK)
- **Maven 3.8+**
- **PostgreSQL 14+** running locally or via Docker
- A functioning **webcam**
---
 
## Getting Started
 
### 1. Clone the repository
 
```bash
git clone https://github.com/your-org/FaceRecognitionSystem.git
cd FaceRecognitionSystem
```
 
### 2. Set up the database
 
```sql
CREATE DATABASE face_recognition;
```
 
Hibernate's `ddl-auto` will handle table creation on first run.
 
### 3. Configure application properties
 
```properties
# src/main/resources/application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/face_recognition
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```
 
### 4. Build and run
 
```bash
mvn clean install
mvn spring-boot:run
```
 
> **Note for Linux users:** Make sure your user has permission to access `/dev/video0`.
 
---
 
## Configuration
 
| Property | Default | Description |
|----------|---------|-------------|
| Camera resolution width | `640` | Frame capture width in pixels |
| Camera resolution height | `480` | Frame capture height in pixels |
| Face image size | `200×200` | Normalised face size before training |
| Images per person | `20` | Number of facial samples captured on enrolment |
| Dataset path | `data/faces/` | Root directory for face image sets |
| LBPH confidence threshold | `50` | Max confidence value to accept a match |
| Cascade file | `haarcascade_frontalface_default.xml` | Pre-trained Haar Cascade |
 
---
 
## Usage
 
Once the application is running, a window will open showing the live webcam feed.
 
### Keyboard Commands
 
| Key | Action |
|-----|--------|
| `C` | Enrol a new person — prompts for a name, captures 20 face samples and retrains the model |
| `Q` | Quit the application gracefully |
 
### Recognition Output
 
- **Green rectangle + name label** → person recognised with confidence below threshold
- **Red rectangle + "Unknown"** → face detected but not recognised (confidence ≥ 50)
> Confidence score semantics: a **lower** value means **higher** similarity to a trained face.
 
---
 
## Database Schema
 
```sql
CREATE TABLE persons (
    id     BIGSERIAL    PRIMARY KEY,
    name   VARCHAR(255) NOT NULL,
    label  INTEGER      NOT NULL UNIQUE
);
```
 
Each row maps a human-readable `name` to an integer `label` that corresponds to a folder under `data/faces/{label}/`.
 
---
 
## Services
 
### `CameraService`
Responsible for initialising the webcam device, capturing frames in a loop and managing the camera lifecycle (open / release).
 
### `FaceDetectionService`
Loads the Haar Cascade XML file, runs face detection on each grayscale frame and applies pre-processing (resize + histogram equalisation) before passing ROIs downstream.
 
### `FaceRecognitionService`
Core orchestrator. Trains the LBPH model from the on-disk dataset, performs per-frame predictions, manages enrolment (dataset capture + DB write), triggers retraining and handles overlay rendering.
 
### `TensorFlowService` *(mock)*
Placeholder for a future deep-learning pipeline. Currently returns random embeddings and performs no real inference. Intended for integration with TensorFlow or FaceNet.
 
---
 
## Known Limitations
 
- LBPH accuracy degrades significantly under low or uneven lighting conditions.
- Large out-of-plane head rotations (profile views) may cause false negatives.
- `TensorFlowService` does not perform real AI inference yet.
- Dataset capture requires manual interaction (press `C` and stay still in front of the camera).
---
 
## Roadmap
 
- [ ] Replace LBPH with FaceNet / ArcFace deep embeddings via TensorFlow or ONNX Runtime
- [ ] Persist facial embeddings as pgvector columns for fast cosine similarity search
- [ ] Expose REST API endpoints for remote recognition use cases
- [ ] Add anti-spoofing detection (liveness check)
- [ ] Multi-camera support
- [ ] Automated dataset augmentation (brightness, rotation jitter)
- [ ] Improve dataset capture pipeline (quality scoring, blur detection)
---
 
## Contributing
 
1. Fork the repository
2. Create your feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m 'feat: add my feature'`
4. Push to the branch: `git push origin feature/my-feature`
5. Open a Pull Request
Please follow [Conventional Commits](https://www.conventionalcommits.org/) for commit messages.
