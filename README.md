# Multi-Threaded Web Server Project

## Overview

This project involves developing a multi-threaded web server capable of handling multiple simultaneous service requests in parallel. The web server listens on two ports: 5555 and 8888. Port 8888 provides regular HTTP service, while port 5555 redirects any GET requests to Google's homepage. The project is divided into three main parts:

1. **Part A**: Implementing a basic multi-threaded web server.
2. **Part B**: Enhancing the server to handle HTTP requests and send appropriate responses.
3. **Part C**: Adding support for a second port that redirects requests.

## Features

- Multi-threaded to handle multiple clients simultaneously.
- Serves static HTML files and media.
- Supports HTTP/1.0 as defined in RFC 1945.
- Redirects requests on port 5555 to Google's homepage.

## Technologies Used

- Java
- Java NIO (Non-blocking I/O)

## Installation and Setup

### Prerequisites

- Java Development Kit (JDK) installed
- IDE or text editor of choice

### Steps

1. **Clone the Repository**

   ```sh
   git clone https://github.com/yourusername/multi-threaded-web-server.git
   cd multi-threaded-web-server
