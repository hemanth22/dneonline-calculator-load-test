package calculatoronline

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class CalculatorSimulation extends Simulation {

  // Define the HTTP protocol configuration
  val httpProtocol = http
    .baseUrl("http://www.dneonline.com") // Base URL of the SOAP web service
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  // Define the SOAP request body
  val addRequestBody =
    """<?xml version="1.0" encoding="utf-8"?>
      |<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
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
      rampUsers(1).during(5 seconds)
      //rampUsers(10000) during (3600 seconds) // Gradually injects users over a period of time
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lte(1000), // Asserts that the max response time is less than or equal to 1000 ms
      forAll.failedRequests.count.lte(1) // Asserts that the total number of failed requests is less than or equal to 1
    )
}
