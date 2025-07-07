# Project Guidelines for Junie AI

## ⚠️ CRITICAL REQUIREMENTS - ALWAYS FOLLOW THESE ⚠️

### 1. PROMPT HANDLING - MANDATORY FOR EVERY REQUEST
- **IMMEDIATELY** update `prompts.txt` in the root folder with each new prompt
  - Format: `## [Ordinal Number] Prompt` (e.g., "## Forty-sixth Prompt")
  - Include the FULL text of the prompt
  - ALWAYS append to the end of the file
  - Do NOT include implementation details or responses
  - This is a REQUIRED step for EVERY prompt

### 2. BUILD VALIDATION - REQUIRED BEFORE SUBMISSION
- Run `./gradlew build` at the root level before submitting changes
  - Ensure all tests pass
  - **MANDATORY**: Verify that all checkstyle rules pass
    - Fix ANY checkstyle violations before submission
    - Run `./gradlew checkstyleMain checkstyleTest` to specifically check for style violations

## PROJECT SPECIFICATIONS

### 3. Project Structure
- **Multi-project Repository**:
  - Backend project folder
  - Frontend project folder
  - Robo-demo project folder (Kotlin)

### 4. Technology Stack

#### 4.1 Backend
- **Language**: Java 21
- **Framework**: Quarkus
  - MUST use Virtual Threads where appropriate
  - Use Quarkus features and extensions
- **Build Tool**: Gradle
  - gradlew is in the root folder, not in the backend project
- **Deployment**: Docker container with native executable

#### 4.2 Frontend
- **Language**: TypeScript
- **Framework**: React
- **Build Tool**: Compatible with chosen technologies
- **Deployment**: Static files for web server deployment

#### 4.3 Robo-demo
- **Language**: Kotlin
- **Build Tool**: Gradle (part of main build)
- **Testing**: Cucumber/Gherkin

### 5. Testing Requirements

#### 5.1 Backend Testing
- **Unit Tests**: Required for all components
- **BDD Testing**: Cucumber
  - Step definitions in Java
  - RestAssured for API testing
  - Quarkus testing tools
- **Development Services**: Use Quarkus Dev Services

#### 5.2 Testing Validation
- Run tests to verify solutions
- **Checkstyle Validation**:
  - Run `./gradlew checkstyleMain checkstyleTest` to verify style compliance
  - Fix ALL style violations before submitting code
  - Style violations will cause build failures in CI/CD pipelines

### 6. CI/CD and Deployment

#### 6.1 GitHub Workflows
- All projects must have GitHub workflows
- Workflow actions:
  - Build
  - Test
  - Lint
- Run on push and merge requests

### 7. Documentation Requirements

#### 7.1 README Files
- Required in:
  - Root directory
  - Backend project
  - Frontend project
  - Robo-demo project
- Must include:
  - Local run instructions
  - Local test instructions
  - Tech stack details
  - Testing approach
  - API overview
