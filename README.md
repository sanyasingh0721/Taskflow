# TaskFlow

TaskFlow is a small task tracker for teams. The backend is a REST API written in Kotlin with Spring Boot. The frontend is a React app written in TypeScript.

```
taskflow/
├── backend/               Spring Boot, Kotlin, JWT, JPA, Flyway, H2 or PostgreSQL
├── frontend/              React, TypeScript, Vite, React Query, React Router, Jest
├── postman/               Postman collection and environment for API testing
├── docker-compose.yml     PostgreSQL + backend + frontend in one command
└── .github/workflows/     CI: backend tests, frontend tests and build
```

## Quick start with Docker

You need Docker Desktop installed and running.

```bash
docker compose up --build
```

When the build finishes:

- App: http://localhost:3000
- API docs (Swagger UI): http://localhost:8080/swagger-ui.html
- PostgreSQL: localhost:5432, database `taskflow`, user `taskflow`, password `taskflow`

Log in with `demo@taskflow.dev` / `password123`. To stop everything, press `Ctrl+C`, then run `docker compose down`. To also delete the database, run `docker compose down -v`.

To use your own JWT secret, set `JWT_SECRET` before starting, for example `JWT_SECRET=a-long-random-value-of-at-least-32-characters docker compose up --build`.

## Running locally without Docker

### What you need

- JDK 21 (JDK 17 also works). JDK 25 doesn't work with Gradle 8.10.2.
- Gradle 8.10.2. You only need it once, to generate the wrapper.
- Node.js 20 LTS.

### Backend

```bash
cd backend
gradle wrapper --gradle-version 8.10.2   # first time only
./gradlew bootRun                        # Windows: gradlew.bat bootRun
```

The API runs at http://localhost:8080. It uses an in-memory H2 database by default, so nothing needs to be installed. Demo data is added at startup, so restarting the server resets it.

### Frontend

```bash
cd frontend
cp .env.example .env.local     # Windows: copy .env.example .env.local
npm install
npm run dev
```

Open http://localhost:5173.

## Demo accounts

All demo accounts use the password `password123`.

| Name | Email | Owns |
| --- | --- | --- |
| Demo User | demo@taskflow.dev | Website Launch, Mobile App v2, Customer Onboarding Revamp, Internal Tools (empty) |
| Priya Sharma | priya@taskflow.dev | Design System 2.0 |
| Arjun Mehta | arjun@taskflow.dev | Payments API Migration |
| Neha Verma | neha@taskflow.dev | Q4 Hiring Plan |
| Rahul Iyer | rahul@taskflow.dev | Bug Bash Sprint |

The login page has a **User Details** button that lists these accounts and fills in the form for you.

## Running the tests

```bash
cd backend && ./gradlew test     # Windows: gradlew.bat test
cd frontend && npm test
```

- Backend: unit tests for `TaskService`, `ProjectService` and `AuthService` (JUnit 5 and MockK), plus an end-to-end test that goes through the real security filter, controllers and database.
- Frontend: component and helper tests with Jest and React Testing Library.

Backend test reports are in `backend/build/reports/tests/test/index.html`.

## Continuous integration

`.github/workflows/ci.yml` runs on every push and pull request. It runs the backend tests, then the frontend tests and production build. Both jobs must pass.

## API documentation

Swagger UI is at http://localhost:8080/swagger-ui.html and the raw OpenAPI document is at http://localhost:8080/v3/api-docs. To try protected endpoints, log in through `/api/auth/login`, copy the token, and paste it into the **Authorize** dialog.

To test the API in Postman, import the two files in the `postman` folder. The collection saves the login token and IDs automatically, so run its folders in order.

## Configuration

Every setting has a default for local development. Set an environment variable only when you need to change one.

| Variable | Default | What it does |
| --- | --- | --- |
| `JWT_SECRET` | a local dev value | Signs login tokens. Use at least 32 characters, and set your own value anywhere other than your own machine. |
| `JWT_EXPIRATION_MINUTES` | `60` | How long a login token lasts. |
| `DATABASE_URL` | in-memory H2 | JDBC URL, for example `jdbc:postgresql://localhost:5432/taskflow` |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | `sa` / empty | Database login. |
| `DATABASE_DRIVER` | `org.h2.Driver` | Set to `org.postgresql.Driver` when using PostgreSQL. |
| `CORS_ORIGIN` | `http://localhost:5173` | The frontend address that may call the API. |
| `SEED_DEMO` | `true` | Set to `false` to skip the demo users and data. |

The frontend reads `VITE_API_URL` from `.env.local`. It defaults to `http://localhost:8080`.

## API endpoints

Everything except `/api/auth/*` needs an `Authorization: Bearer <token>` header. Every error uses the same format:

```json
{
  "timestamp": "2026-11-03T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": { "title": "Title is required" }
}
```

| Method | Path | What it does |
| --- | --- | --- |
| POST | `/api/auth/register` | Creates an account and returns a token (201) |
| POST | `/api/auth/login` | Returns a token. Wrong email or password gives 401. |
| GET | `/api/users` | Lists users, used for the assignee dropdown |
| GET | `/api/projects` | Lists your projects |
| POST | `/api/projects` | Creates a project (201) |
| GET | `/api/projects/{id}` | Returns one project. 404 if it doesn't exist, 403 if someone else owns it. |
| GET | `/api/projects/{id}/tasks` | Lists tasks. Supports `status`, `priority`, `assigneeId`, `sort` (`dueDate` or `priority`), `page` and `size`. |
| POST | `/api/projects/{id}/tasks` | Creates a task (201). The due date can't be in the past. |
| GET | `/api/projects/{id}/summary` | Counts tasks by status and shows how many are overdue |
| PUT | `/api/tasks/{id}` | Updates all fields of a task |
| PATCH | `/api/tasks/{id}/status` | Changes only the status |
| DELETE | `/api/tasks/{id}` | Deletes a task (204) |
| GET | `/api/tasks/{id}/activity` | Lists the task's history, newest first |

Error codes: 400 for invalid input, 401 for a missing, invalid or expired token (or wrong login details), 403 for a project or task owned by someone else, 404 for something that doesn't exist, 409 for an email that is already registered, and 500 for unexpected server or database errors.

## Activity log

Every task keeps a history: who created it, who edited it, and who changed its status, with the time of each change. The edit dialog shows this history under the form. Activity rows are deleted with their task.

## Database

The schema is managed by Flyway. The migration in `backend/src/main/resources/db/migration/V1__init.sql` creates every table. Hibernate doesn't change the schema, so a change to an entity needs a new migration file (for example `V2__...sql`). The same migrations run on H2 and PostgreSQL.

## Architecture

On the backend, a request goes through the JWT filter first, which reads the token and works out who is logged in. The controller checks the input and calls a service. The service applies the rules, including the ownership check, and uses a repository to read or write the database. Controllers return DTOs, never database entities. Any error, from any layer, is turned into the same JSON format by `GlobalExceptionHandler`.

On the frontend, pages live in `pages/`, reusable pieces live in `components/`, and all HTTP calls live in `api/`. Components don't call axios directly.

## State management

Server data (projects, tasks, the summary, the user list and the activity log) is managed by React Query. It caches responses, tracks loading and error state for each request, retries failed reads, and refreshes the board after a change. Components don't need their own copies of that logic, so the pages stay short.

The logged-in user and token are kept in a small React Context. Only a few components need them and they change rarely, so a full store would be more than the app needs. Nothing else is global, so there's no second copy of the server data to keep in sync.

## Design patterns

- **Strategy** for sorting. Each sort option is a class in `TaskSortStrategies.kt`. Adding a new sort means adding a class and registering it.
- **Mapper** for converting entities to response objects in `TaskMapper.kt` and `TaskActivity.kt`. Entities never leave the service layer.
- **Repository** for database access, using Spring Data JPA interfaces.

## Design choices

- **Login tokens (JWT) with no server-side sessions.** The server doesn't keep track of logins, which keeps it simple to scale. The downside is that a token can't be revoked before it expires.
- **H2 by default, PostgreSQL in Docker.** The default needs nothing installed. Docker Compose runs PostgreSQL 16, which is the database you'd use in production.
- **Flyway for the schema.** Every change is a numbered SQL file, so the schema is reproducible and reviewable.
- **Priority is stored by position.** This lets SQL sort LOW, MEDIUM and HIGH correctly. The order of the enum values must not change once there's data.
- **Status changes use a dropdown.** Drag-and-drop was not added. The dropdown is simpler to test and works with a keyboard.
- **Forbidden is 403, not 404.** Someone who isn't the owner gets 403, so the API reveals that a project exists. That matches the assignment's error table.
- **Dates use two formats on purpose.** The API uses `yyyy-mm-dd`, which sorts correctly and has no locale issues. Screens show `dd-mm-yyyy`.
- **Jest for frontend tests.** The assignment asks for Jest, so the component tests run on Jest with React Testing Library.

## Assumptions

- Each project has one owner. Projects can't be shared with other users yet, so only the owner can see or change a project's tasks.
- Any logged-in user can be assigned to a task, and can see the list of users. That list includes names and email addresses.
- Due dates are calendar dates with no time zone. A task is overdue when its due date has passed and it isn't done.
- A task's due date may be in the past when it's edited. The "not in the past" rule applies only when creating a task.
- The demo data is for local use and is created on startup. Set `SEED_DEMO=false` anywhere real users will log in.
- Passwords must be 8 to 72 characters. The limit of 72 comes from BCrypt.

## Trade-offs

- **Stateless tokens can't be revoked.** If someone's token is stolen, it works until it expires (60 minutes by default). A revocation list or short-lived refresh tokens would fix that.
- **The board loads up to 100 tasks per project.** The API supports paging, but the board doesn't use it yet. That's fine for the size of team this app is built for.
- **Users can see every other user's name and email.** This is needed for the assignee dropdown. In a real product I would limit it to team members and hide email addresses.
- **The Docker demo password and JWT secret are for local use.** They are written in `docker-compose.yml` so the app starts without setup. Don't deploy them as they are.
- **No drag-and-drop.** The status dropdown meets the assignment and is easier to test.

## Next steps

- Share projects with other users, with roles such as viewer and editor.
- Drag-and-drop between board columns.
- Refresh tokens or a token revocation list.
- Paged loading on the board.