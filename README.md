# API Capability Test

## Objective

Create a small backend microservice project to demonstrate your ability to design and implement API services, authentication, event-driven communication, message queues, caching/session storage, and database integration.

Java Spring Boot is strongly recommended, but you may use another language or framework if you are more comfortable with it.

The business context is fully up to you. It does not need to match our company domain. What we want to evaluate is how you structure services, define clear responsibilities, handle authentication, and implement service-to-service communication using RabbitMQ.

This test requires a real running implementation, not only diagrams or pseudo-code.

## Main Focus

Your submission should clearly demonstrate these capabilities:

- API design and service structure
- Authentication flow
- Redis usage for authentication/session/token storage
- RabbitMQ usage for event-driven communication
- Message queue handling between services
- Database integration from only one service
- Error handling, validation, and clear documentation

## Minimum Requirements

Please build a small microservice system with exactly these two application services:

1. Service A
   - Exposes one or more API endpoints for the client.
   - Communicates with Service B through RabbitMQ.
   - Must only connect to RabbitMQ as its external infrastructure dependency.
   - Must not connect directly to Redis.
   - Must not connect directly to the database.
   - Must not call Service B through HTTP, gRPC, WebSocket, or any other direct service-to-service protocol.

2. Service B
   - Consumes messages/events from RabbitMQ.
   - Publishes responses/events back through RabbitMQ when needed.
   - Handles authentication logic.
   - Connects to Redis to store authentication/session/token data.
   - Connects to a database.
   - The database may be MySQL or PostgreSQL.
   - Must not expose endpoints that Service A calls directly.

3. RabbitMQ
   - Used as the message queue between Service A and Service B.
   - Service A and Service B should communicate using events/messages, not direct HTTP calls between services.
   - Define clear queue names, routing keys, exchange names, or event contracts.
   - Must support both the authentication flow and one authenticated business flow.

4. Redis
   - Used by Service B for authentication-related storage.
   - Examples: session storage, token storage, refresh token storage, login attempt tracking, or temporary authentication state.

5. Database
   - Only Service B should connect to the database.
   - Store meaningful data related to your chosen use case.
   - Store user/account data if needed for authentication.

## Required Flows

Your implementation must include these flows:

1. Authentication flow
   - Client sends a login request to Service A.
   - Service A publishes an authentication request message to RabbitMQ.
   - Service B consumes the message, validates the credentials using its database, stores authentication/session/token data in Redis, and publishes the authentication result back through RabbitMQ.
   - Service A returns the authentication result to the client.

2. Authenticated business flow
   - Client sends a second request to Service A using the authentication result from the login flow.
   - Service A publishes a business request message to RabbitMQ.
   - Service B validates the authentication/session/token data using Redis.
   - Service B reads or writes data in the database.
   - Service B publishes the result back through RabbitMQ.
   - Service A returns the result to the client.

## Example Flow

You may choose your own business context, but the system should follow this communication pattern:

1. Client calls an endpoint in Service A.
2. Service A validates the request shape and publishes a message to RabbitMQ.
3. Service B consumes the message.
4. Service B processes the request, reads/writes data from the database, and uses Redis when authentication/session data is involved.
5. Service B publishes a result message back to RabbitMQ.
6. Service A receives the result and returns a response to the client.

The implementation may use a request/reply pattern, correlation IDs, temporary reply queues, or named response queues. The chosen approach must be documented.

## Recommended Scope

Keep the project focused and practical. We are not looking for a huge system. A small but well-structured implementation is better than a large unfinished one.

Example ideas:

- User authentication with profile lookup
- Order creation workflow
- Booking or reservation workflow
- Task processing workflow
- Payment simulation workflow
- Notification request workflow
- Any other concept you prefer

## Technical Expectations

Your project should include:

- Two separate application services: Service A and Service B
- RabbitMQ integration
- Redis integration
- MySQL or PostgreSQL integration
- Authentication handled by Service B
- Clear event/message contracts
- Clear API request and response examples
- Input validation
- Error handling for failed authentication, invalid messages, and service errors
- A runnable local setup, preferably using Docker Compose

## Required API Endpoints

Service A must expose at least these client-facing endpoints:

- `POST /auth/login`
- One authenticated business endpoint, for example `POST /orders`, `GET /profile`, or `POST /tasks`

Service B may expose health-check endpoints, but Service A must not use them for business or authentication communication.

## Required Event Contracts

Document and implement at least these message contracts:

- Login request message from Service A to Service B
- Login result message from Service B to Service A
- Authenticated business request message from Service A to Service B
- Business result message from Service B to Service A

Each contract should include:

- Message name or event type
- Queue/exchange/routing key
- JSON payload example
- Success response example
- Error response example
- Correlation ID or equivalent request tracking field

## Acceptance Criteria

Your submission is considered complete when:

- The project can be run locally from the documentation.
- Service A starts without database or Redis configuration.
- Service B starts with RabbitMQ, Redis, and database configuration.
- Login works through RabbitMQ, not direct calls.
- The authenticated business endpoint works through RabbitMQ, not direct calls.
- Redis is used during authentication/session/token handling.
- Only Service B reads from or writes to the database.
- Message contracts and API examples are documented.
- Failure cases are handled and documented, including invalid credentials, invalid/expired authentication, and a failed business request.

## Recommended Stack

Spring Boot is recommended, with:

- Spring Web
- Spring AMQP for RabbitMQ
- Spring Data Redis
- Spring Data JPA
- PostgreSQL or MySQL driver
- Docker Compose for RabbitMQ, Redis, and database setup

Other stacks are allowed if the same requirements are fulfilled clearly.

## What We Will Evaluate

We will mainly look at:

- Service separation and responsibility boundaries
- Correct use of RabbitMQ for event-driven communication
- Correct use of Redis for authentication/session storage
- Correct database ownership by Service B only
- API design and request/response clarity
- Message contract clarity
- Authentication flow quality
- Error handling and edge cases
- Code organization and maintainability
- Local setup completeness
- Documentation quality

## Submission Instructions

Follow these steps to submit your work:

1. Clone this public repository.

   ```bash
   git clone <this-public-repository-url>
   cd <repository-folder>
   ```

2. Create your own public repository in your Git hosting account, for example GitHub, GitLab, or Bitbucket.

3. Change the Git remote from this repository to your own repository.

   ```bash
   git remote remove origin
   git remote add origin <your-public-repository-url>
   ```

4. Create a branch named:

   ```bash
   developers/<your-name>
   ```

   Example:

   ```bash
   developers/irvan-hilmi
   ```

5. Implement the required services and documentation in that branch.

6. Push your work to your own public repository.

   ```bash
   git push -u origin developers/<your-name>
   ```

7. Email your public repository URL back to:

   `it@teleanjarmitraglobal.co.id`

Your email should include:

- Your full name
- Your public repository URL
- The branch name containing your submission
- Any important setup notes, if needed

## Required Documentation

Your submission must include a `README.md` that explains:

- What stack/framework you used
- How to run all services locally
- How to run RabbitMQ, Redis, and the database
- How Service A and Service B communicate through RabbitMQ
- Queue, exchange, routing key, or event names used
- How authentication works
- How Redis is used for authentication/session/token storage
- Which database you used and what tables/entities you created
- API endpoint documentation with example requests and responses
- Any environment variables or setup requirements

## Notes

- Java Spring Boot is recommended, but other technologies are allowed.
- Service A must only connect to RabbitMQ.
- Service B is the only service that may connect to Redis and the database.
- Service-to-service communication should happen through RabbitMQ, not direct HTTP calls.
- Do not put both services in one runtime process. They should be separate applications that can be started independently.
- Do not bypass RabbitMQ by sharing database tables, Redis keys, files, or in-memory state between Service A and Service B.
- Extra points for Docker Compose, tests, clean event contracts, and thoughtful failure handling.

## Goal

The main goal of this test is to understand how you design and build a small backend system involving:

- microservice boundaries
- authentication
- Redis-backed auth/session storage
- RabbitMQ message queues
- event-driven service communication
- database ownership
- reliable local setup and documentation
