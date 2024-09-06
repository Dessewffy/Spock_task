import spock.lang.Specification
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class ReqRes extends Specification {
    def "POST request test with ReqresAPI"() {
        given:
        def baseUrl = "https://reqres.in"
        def http = RequestWrapper.createHttpBuilder(baseUrl)

        and: "The request body to be sent"
        def requestBody = [
                name: "morpheus",
                job: "leader"
        ]

        when: "The API POST request is sent"
        def result = RequestWrapper.sendPostRequest(http, '/api/users', requestBody)
        def statusCode = result.statusLine.statusCode
        def responseJson = result.responseJson

        then: "Ensure that we received a status code"
        assert statusCode != null : "No status code received from API call."

        then: "The status code should be 201 (Created)"
        statusCode == 201

        and: "The response should contain the posted data"
        responseJson != null
        responseJson.name == "morpheus"
        responseJson.job == "leader"
    }

    def "PUT request test with ReqresAPI"() {
        given:
        def baseUrl = "https://reqres.in"
        def endpoint = "/api/users/2"
        def url = new URL(baseUrl + endpoint)
        def connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = 'PUT'
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")

        and: "The request body to be sent"
        def requestBody = [
                name: "morpheus",
                job: "zion resident"
        ]
        def jsonRequestBody = JsonOutput.toJson(requestBody)

        when: "The API PUT request is sent"
        connection.outputStream.withWriter('UTF-8') { writer ->
            writer.write(jsonRequestBody)
        }

        def responseCode = connection.responseCode
        def responseMessage = connection.inputStream.text

        and: "Parse the JSON response"
        def jsonSlurper = new JsonSlurper()
        def responseJson = jsonSlurper.parseText(responseMessage)

        then: "Ensure that we received a status code"
        assert responseCode != null : "No status code received from API call."

        then: "The status code should be 200 (OK)"
        responseCode == 200

        and: "The response should contain the updated data"
        responseJson != null

        and: "Check that the 'name' value is correct"
        responseJson.name == "morpheus"
        responseJson.job == "zion resident"
        cleanup:
        connection.disconnect()
    }

    def "DELETE request test with ReqresAPI"() {
        given:
        def baseUrl = "https://reqres.in"
        def endpoint = "/api/users/2"
        def url = new URL(baseUrl + endpoint)
        def connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = 'DELETE'
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")

        when: "The API PUT request is sent"
        def responseCode = connection.responseCode

        then: "Ensure that we received a status code"
        assert responseCode != null : "No status code received from API call."

        then: "The status code should be 204 ()"
        responseCode == 204

        cleanup:
        connection.disconnect()
    }
}


