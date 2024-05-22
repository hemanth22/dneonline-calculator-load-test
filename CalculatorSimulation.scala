import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class CalculatorSimulation extends Simulation {

  // Define the HTTP protocol configuration
  val httpProtocol = http
    .baseUrl("http://www.dneonline.com") // Base URL of the SOAP web service
    .header("Content-Type", "text/xml; charset=UTF-8") // Content-Type header for SOAP

  // Define the SOAP request body
  val addRequestBody =
    """<?xml version="1.0" encoding="utf-8"?>
      |<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      |  <soap:Body>
      |    <Add xmlns="http://tempuri.org/">
      |      <intA>1</intA>
      |      <intB>2</intB>
      |    </Add>
      |  </soap:Body>
      |</soap:Envelope>""".stripMargin

  // Define the scenario
  val scn = scenario("Calculator Add Test")
    .exec(
      http("Add Operation")
        .post("/calculator.asmx")
        .body(StringBody(addRequestBody)).asXml
        .check(status.is(200))
        .check(xpath("//*:AddResult").exists) // Verify that the response contains the expected result
    )

  // Setup the simulation with assertions
  setUp(
    scn.inject(
      // atOnceUsers(10), // Injects a specific number of users at once
      rampUsers(10000).during(3600 seconds)
      //rampUsers(10000) during (3600 seconds) // Gradually injects users over a period of time
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lte(1000), // Asserts that the max response time is less than or equal to 1000 ms
      forAll.failedRequests.count.lte(1) // Asserts that the total number of failed requests is less than or equal to 1
    )
}
