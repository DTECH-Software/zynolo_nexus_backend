# Meeting Room Booking Service

Basic Spring Boot service scaffold for future meeting room booking requirements.

## Local Run

```bash
mvn -DskipTests spring-boot:run
```

## Build

```bash
mvn -DskipTests clean package
```

## Endpoints

Health:

```text
GET /api/v1/meeting-room/health
```

Meeting Room Master:

```text
POST /api/v1/meeting-room/rooms/reference-data
POST /api/v1/meeting-room/rooms/filter-list
POST /api/v1/meeting-room/rooms
POST /api/v1/meeting-room/rooms/view
POST /api/v1/meeting-room/rooms/update
POST /api/v1/meeting-room/rooms/active-status
```

Default port: `8095`.
