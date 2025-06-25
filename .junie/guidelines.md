# Project Guidelines

This is a placeholder of the project guidelines for Junie.
Replace this text with any project-level instructions for Junie, e.g.:

* The project structure
  * This must be a multiproject project
  * It will contain a folder for the backend project
  * and another for the FE project
* Run tests to check the correctness of the proposed solution
* Build the project before submitting the result
* The build tooling for the backend project if gradle
* The build tooling for the FE project can be what bests works witht he tools
* The backend project must use quarkus
* The front-end project can use React 
* The backend project must be in java
* The front-end project must be in TypeScript
* Both FE and backend must be deployed in docker containers
* Both should have a GitHub workflow that aligns with the best practice CI/CD for each projects technology
* Each workflow should run on any push to the repo 
  * Perform basic builds and unit tests, linting
  * On MR requests these jobs should be run again.
    * Deployments to an environment for testing will be expanded on later
  * The quarkus build for deployment (not for local testing) should build as a native executable
*  For the backend testing should
  * Make use of Quarkus Dev services as appropriate
  * Include unit tests 
* Quarkus features and extensions should be used
