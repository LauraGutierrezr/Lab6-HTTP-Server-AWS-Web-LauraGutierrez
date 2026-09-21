# Mini HTTP Server — Networking Lab, Part 2

This project corresponds to the second part of the Networking lab. It consists of a small web application developed in Java that works with a socket-based HTTP server.

The server can handle requests sequentially, serve static files such as HTML, JavaScript, and images, and respond to some services defined directly in the code.

The application can run locally and also on an AWS EC2 instance.

An important characteristic of this lab is that the server **does not use threads or concurrency mechanisms**. It only processes one connection at a time. This makes it possible to observe the limitations of a server before applying scalability solutions.

---

## 1. System Metaphor and Architecture

### Metaphor

To understand how the system works, I compare it to a small office with **one window and one person serving customers**.

The browser represents the user who arrives at the window and makes a request. This request contains information such as the HTTP method and the path that the user wants to access.

The person serving the customers can only process one request at a time. First, the request is received, then it is processed, the response is delivered, and only after that the next person is served.

This represents how my server works sequentially.

Inside the office, there are two types of things that can be delivered:

* **Static resources:** files that already exist, such as `index.html`, `app.js`, and images.
* **Services:** routes that are defined directly in the code and generate a JSON response.

The implemented services are:

```text
/api/greet
/api/square
/api/time
/api/health
```

The browser uses JavaScript to make these requests asynchronously. This allows the page to remain responsive while waiting for a response.

However, this does not change how the server works. The server still has one window and completely handles one request before moving to the next one.

### Architecture

| Component            | Responsibility                                                                                                         |
| -------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| **Browser**          | Displays the page, runs `app.js`, and sends requests to the services using `fetch`.                                    |
| **Internet**         | Allows communication between the browser and the EC2 instance.                                                         |
| **Security Group**   | Controls the ports that can receive connections on the EC2 instance.                                                   |
| **MiniHttpServer**   | This is the Java server. It listens for connections using `ServerSocket`, processes requests, and generates responses. |
| **Static resources** | Contains the HTML, JavaScript, and images used by the page.                                                            |
| **Services**         | Contains the four routes defined directly in the code and their JSON responses.                                        |

---

## 2. Design Decisions

### Sequential Server

I kept the server sequential because it is one of the main parts of the lab.

The server accepts a connection, completely processes the request, sends the response, and closes the connection before accepting the next one.

I did not add threads or a `ThreadPool` because that would change the behavior that this lab is intended to demonstrate.

### Directly Defined Routes

The routes are defined directly in `MiniHttpServer.dispatch(...)`.

The server currently recognizes:

```text
/api/greet
/api/square
/api/time
/api/health
```

I did not use a routing framework because the goal is to understand how an HTTP path can determine which operation the server should perform.

### Content Types

The server uses the file extension to determine the content type that should be sent.

For example:

```text
.html  -> text/html
.js    -> application/javascript
.css   -> text/css
.png   -> image/png
.jpg   -> image/jpeg
.jpeg  -> image/jpeg
```

If the file does not exist or its extension is not supported, the server returns a `404` error.

### Path Protection

Before accessing a file, the server normalizes the path and checks that it remains inside the public resources directory.

This means that a request such as:

```text
../../etc/passwd
```

cannot be used to access files outside the allowed directory.

URL-encoded paths are also taken into account.

### Asynchronous JavaScript Client

The `app.js` file uses `fetch` to communicate with the server.

When a form is submitted, the browser's default behavior is prevented using `event.preventDefault()`.

A loading state is then displayed, and the response is processed when it arrives.

The client distinguishes between:

* An HTTP error response, such as `400` or `404`.
* A connection or network error.
* A successful response.

The page does not reload after making a request.

---

## 3. Project Structure

```text
.
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/lauragutierrez/lab6/
│   │   │   ├── http/
│   │   │   ├── services/
│   │   │   └── util/
│   │   └── resources/
│   │       └── public/
│   │           ├── index.html
│   │           ├── app.js
│   └── test/
│       └── java/com/lauragutierrez/lab6/
├── deploy/
│   ├── mini-http-server.service
│   └── deploy.sh
└── docs/
    └── architecture.svg
```

### Main Folders

* `src/main/java`: contains the server and service code.
* `src/main/resources/public`: contains the resources that can be requested by the browser.
* `src/test/java`: contains the tests.
* `deploy`: contains the files used to deploy the application to EC2.
* `docs`: contains the architecture diagram.
* `pom.xml`: contains the Maven configuration.

---

## 4. Requirements

To run the project locally, I need:

* Java 17 or later.
* Maven 3.8 or later.
* Git.
* `curl` or a browser for testing.

For the AWS deployment, I also need:

* An AWS account approved for the lab.
* An EC2 instance.
* An approved connection method.
* An SSH key if SSH is used.

---

## 5. Installation and Build

First, I clone the repository:

```bash
git clone https://github.com/LauraGutierrezr/Lab6-HTTP-Server-AWS-Web-LauraGutierrez.git
```

Then I enter the project directory:

```bash
cd Lab6-HTTP-Server-AWS-Web-LauraGutierrez
```

To run the tests:

```bash
mvn test
```

To generate the `.jar` file:

```bash
mvn package
```

The generated file is located at:

```text
target/mini-http-server.jar
```

The project does not need external dependencies to run. JUnit 5 is only used for testing.

---

## 6. Local Execution

After building the project, I copy the public resources to the `target` directory:

```bash
mvn package
cp -r src/main/resources/public target/public
cd target
```

Then I start the server:

```bash
java -jar mini-http-server.jar
```

By default, the server uses port `8080`.

I can also change the port:

```bash
PORT=9090 java -jar mini-http-server.jar
```

To test the application locally, I open:

```text
http://localhost:8080/
```

To stop the server, I use:

```text
Ctrl + C
```

### Configuration

The port and the location of the resources can be configured using environment variables.

| Variable     | Default Value | Description                                       |
| ------------ | ------------- | ------------------------------------------------- |
| `PORT`       | `8080`        | Port where the server listens.                    |
| `PUBLIC_DIR` | `public`      | Directory where the static resources are located. |

---

## 7. Application Usage

The main page allows me to perform different operations using the services provided by the server.

### Greeting

Request:

```text
GET /api/greet?name=Laura
```

Valid input:

```text
Laura
```

Response:

```json
{
  "name": "Laura",
  "message": "Hello, Laura!"
}
```

If no name is provided or the value is empty, the server returns a `400` error.

### Square of a Number

Request:

```text
GET /api/square?value=14
```

Response:

```json
{
  "input": 14,
  "square": 196
}
```

If the value does not exist or is not numeric, the server returns a `400` error.

### Server Time

Request:

```text
GET /api/time
```

Response:

```json
{
  "serverTime": "<ISO-8601>",
  "epochMillis": 0
}
```

The time corresponds to the server and not to the browser.

### Server Status

Request:

```text
GET /api/health
```

Response:

```json
{
  "status": "UP"
}
```

This service is used to check that the server is running.

### Static Resources

I can also directly request the application's files, for example:

```text
/
```

```text
/app.js
```

```text
/images/logo.png
```

The server returns each file with its corresponding content type.

If the file does not exist, it returns `404`.

If a method other than `GET` is used, it returns `405`.

---

## 8. Testing

### Automated Tests

The tests are executed with:

```bash
mvn test
```

The tests verify:

* Escaping of values used in JSON.
* Protection against path traversal.
* Content types.
* Valid and invalid service behavior.
* Loading of the main page.
* Loading of the JavaScript file.
* Non-existing files.
* Path traversal attempts.
* The four services.
* `POST` requests.
* Ten consecutive requests within the same server execution.

There is also an integration test that starts the server on an available port and makes real HTTP requests using `HttpClient`.

### Manual Tests

With the server running locally, I can use `curl`:

```bash
curl -i http://localhost:8080/
```

Greeting test:

```bash
curl -i http://localhost:8080/api/greet?name=Laura
```

Invalid number test:

```bash
curl -i http://localhost:8080/api/square?value=abc
```

Time test:

```bash
curl -i http://localhost:8080/api/time
```

Non-existing file:

```bash
curl -i http://localhost:8080/no-such-file.html
```

Method not allowed:

```bash
curl -i -X POST http://localhost:8080/
```

Path traversal test:

```bash
curl -i "http://localhost:8080/../../../../etc/passwd"
```

Ten consecutive requests:

```bash
for i in $(seq 1 10); do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/health
done
```

### Sequential Behavior Test

To verify the server limitation, I open the application in two browser windows.

In the first window, I make a request that takes longer to process. While it is being processed, I make another request from the second window.

The second request should wait until the first one finishes because the server only processes one connection at a time.

Although the second page can remain responsive because of asynchronous JavaScript, the server itself continues to work sequentially.

---

## 9. AWS EC2 Deployment

For the deployment, I use a single EC2 instance.

The application running on EC2 is the same one that I tested locally. The main difference is where the server is running.

### 9.1. Create the Instance

I create a Linux instance using the image and instance size approved for the lab.

The instance is configured in the network indicated by the lab and has a name that makes it easy to identify.

### 9.2. Configure the Security Group

The Security Group allows only the necessary ports.

For SSH:

```text
TCP 22
```

Access is restricted to my IP address when I use SSH.

For the application:

```text
TCP 8080
```

Access depends on the configuration allowed for the lab test.

### 9.3. Build the Application

Before sending the application to EC2, I run:

```bash
mvn package
```

This generates:

```text
target/mini-http-server.jar
```

### 9.4. Send the Application to EC2

I use the deployment script included in the project:

```bash
./deploy/deploy.sh <instance-public-dns-or-ip> <path-to-key.pem> [ssh-user]
```

The script copies the `.jar`, public resources, and service file to the instance.

It also configures the service so that the application can run in the background.

### 9.5. Run as a Service

The application is installed as a `systemd` service.

The location used is:

```text
/opt/mini-http-server
```

The service is configured to start automatically and continue running after closing the session used to connect to EC2.

### 9.6. Test the Application

First, I check the service from the instance itself using:

```text
/api/health
```

Then I access the application from the browser using the public address of the instance:

```text
http://<instance-public-address>:8080/
```

From there, I can use the page and check the services.

The logs are stored at:

```text
/var/log/mini-http-server/server.log
```

To stop the service:

```bash
sudo systemctl stop mini-http-server
```

I do not store passwords, private keys, credentials, private addresses, or AWS access information in the repository.

---

## 10. Evidence and Results

### Services

```text
/api/greet
/api/square
/api/time
/api/health
```

### Errors

* Invalid parameter.
* Non-existing file.
* Method not allowed.
* Path traversal attempt.

### EC2 Execution

I include a screenshot of the application running through the public address of the EC2 instance.


<img width="800" height="385" alt="11 55 39 p m" src="https://github.com/user-attachments/assets/05d2c6c2-c130-47ed-8e12-eb94d89c4483" />

<img width="1306" height="480" alt="Captura de pantalla" src="https://github.com/user-attachments/assets/bc85a0cd-013a-4c1a-9a22-2497e2ad6ff4" />

<img width="674" height="407" alt="Caprut m" src="https://github.com/user-attachments/assets/cf85c9dc-742a-46f0-a850-0db651166691" />


<img width="987" height="637" alt="  a m" src="https://github.com/user-attachments/assets/29bb464e-287a-4976-878b-269323d64a06" />



<img width="563" height="53" alt="Cap42 a m" src="https://github.com/user-attachments/assets/7c4a61cb-84bd-4eb2-8c1c-8467ea6ae51e" />


<img width="512" height="42" alt="Capturala(s) 2 16 33 a m" src="https://github.com/user-attachments/assets/816e8fd4-fbc2-44e8-bf3b-3d262b07ce4f" />



<img width="1246" height="774" alt="Capla(s) 2 12 35 a m" src="https://github.com/user-attachments/assets/c91b2861-fa9e-4517-a0a4-3d004e327054" />

<img width="865" height="779" alt="Capa la(s) 2 13 32 a m" src="https://github.com/user-attachments/assets/e1668c49-fa58-40a0-9e6b-0a047dc1090d" />




###

---

## 11. Known Limitations

This project has several limitations because they are part of the purpose of the lab:

* The server is strictly sequential.
* Only the `GET` method is allowed.
* Service routes are defined directly in the code.
* There is no authentication.
* It does not use a database.
* It does not use TLS.
* The application runs on a single EC2 instance.
* It does not use a load balancer.
* It is not designed to be a production HTTP server.

---

## 12. Author and References

**Author:** Laura Valentina Gutiérrez Rico.

This project was developed for the Networking lab, Part 2: *From a Minimal HTTP Server to a Web Application on AWS*.

The main references used were:

* AWS documentation for launching and connecting to EC2 instances.
* AWS documentation about Security Groups.
* Java SE documentation for `ServerSocket`.
* Java SE documentation for `Path`.
* Java SE documentation for `HttpClient`.
* AI guidance (ChatGPT).
