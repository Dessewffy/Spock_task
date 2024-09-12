import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method

class RequestWrapper {
    private HTTPBuilder httpBuilder

    // Constructor to initialize the HTTPBuilder
    RequestWrapper(String baseUrl) {
        this.httpBuilder = new HTTPBuilder(baseUrl)
    }

    // Instance method for sending a GET request
    Map<String, Object> sendGetRequest(String path = "/", Map<String, Object> queryParams = [:]) {
        def statusLine
        def responseJson

        httpBuilder.request(Method.GET, ContentType.JSON) {
            uri.path = path
            uri.query = queryParams

            // Success response handler
            response.success = { resp, json ->
                statusLine = resp.statusLine
                responseJson = json
            }

            // Failure response handler
            response.failure = { resp, json ->
                statusLine = resp.statusLine
                responseJson = json
            }
        }

        return [statusLine: statusLine, responseJson: responseJson]
    }

    // Instance method for sending a POST request
    Map<String, Object> sendPostRequest(String path = "/", Map<String, Object> body = [:]) {
        def statusLine
        def responseJson

        httpBuilder.post(
                path: path,
                body: body,
                requestContentType: ContentType.JSON
        ) { resp, json ->
            statusLine = resp.statusLine
            responseJson = json
        }

        return [statusLine: statusLine, responseJson: responseJson]
    }
}

