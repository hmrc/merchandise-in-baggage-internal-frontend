
# merchandise-in-baggage-internal-frontend

**Who uses the repo/service**

Customs agent for business travellers carrying commercial goods for both import or export.

**How to start the service locally**

`sbt run` This will only start the service as standalone but unable to interact with any other services including Backend and DataBase

SM profile : MERCHANDISE_IN_BAGGAGE_ALL
`sm MERCHANDISE_IN_BAGGAGE_ALL` This will start all the required services to complete a journey

`local url` http://localhost:8282/declare-commercial-goods/import-export-choice

**How to run tests**

`sbt test` will run all the tests, including unit, UI and consumer contract tests. The consumer tests will generate
contract files stored in the project root directory folder `pact`
The generated contracts test will then being used from the Backend contract verifier by running the script:
`checkincheck.sh`. However, currently contracts test only runs for local build.
