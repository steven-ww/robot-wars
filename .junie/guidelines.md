# Project Guidelines for Junie AI

## 1. Introduction
This document outlines the requirements and guidelines for the Robot Wars project. Junie AI should follow these guidelines when implementing features and responding to prompts.

## 2. Project Structure
- **Multi-project Setup**:
  - This must be a multi-project repository
  - It must contain a folder for the backend project
  - It must contain a folder for the frontend project

## 3. Technology Stack

### 3.1 Backend
- **Language**: Java
- **Java Version**: 21
- **Framework**: Quarkus
  - Use Virtual Threads when appropriate
  - Use Quarkus features and extensions
- **Build Tool**: Gradle
  - - gradlew is available for use in the root folder, not directly in the backend project
- **Deployment**: Docker container
  - Native executable for production deployment

### 3.2 Frontend
- **Language**: TypeScript
- **Framework**: React
- **Build Tool**: Best tool that works with the chosen technologies
- **Deployment**: Static files deployable to a web server (e.g., S3 bucket)

## 4. Testing Requirements

### 4.1 Backend Testing
- **Unit Tests**: Required for all components
- **BDD Testing**: Cucumber
  - Step definition files must be written in Java
  - Use RestAssured for API interactions
  - Use Quarkus testing tools and strategies
- **Development Services**: Use Quarkus Dev Services as appropriate

### 4.2 Testing Validation
- Run tests to check the correctness of proposed solutions
- Validate that checkstyle rules pass after each change

## 5. CI/CD and Deployment

### 5.1 GitHub Workflows
- Both projects should have GitHub workflows following best practices
- Workflows should run on any push to the repository:
  - Perform basic builds
  - Run unit tests
  - Perform linting
- On merge requests, these jobs should run again
- Deployments to test environments will be expanded on later

## 6. Documentation Requirements

### 6.1 README Files
- Update README files in:
  - Root directory
  - Backend project directory
  - Frontend project directory
- Each README must include:
  - How to run locally
  - How to test locally
  - Tech stack used
  - Testing approach
  - Overview of APIs provided/used

## 7. For each prompt, follow this process

### 7.1 Prompt Handling
- Record all prompts in a `prompts.txt` file in the root folder
  - Update immediately upon receiving each new prompt
  - Use consistent format: "## [Number] Prompt" as heading
  - Include the full text of the prompt
  - Do not include implementation details or responses
  - Ensure sequential numbering (First, Second, Third, etc.)
  - Always append new prompts to the end of the file

### 7.2 Build Process
- Build the project using the "./gradlew build command" at the top level 
  - Ensure all tests pass during the build process
  - Ensure all checkstyle rules pass
