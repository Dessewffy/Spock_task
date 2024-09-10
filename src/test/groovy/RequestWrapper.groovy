import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseException

class RequestWrapper {
    static HTTPBuilder createHttpBuilder(String url) {
        return new HTTPBuilder(url)
    }

    static Map<String, Object> sendGetRequest(HTTPBuilder http, String path = "/", Map<String, Object> queryParams = [:]) {
        def statusLine
        def responseJson

        try {
            http.get(path: path, query: queryParams, contentType: ContentType.JSON) { resp, json ->
                statusLine = resp.statusLine
                responseJson = json
            }
        } catch (HttpResponseException e) {
            // Handle HTTP errors (e.g., 400, 404) and capture the response
            statusLine = e.response.statusLine
            responseJson = e.response.data // JSON body, if any, in error response
        } catch (Exception e) {
            // Handle other unexpected exceptions
            throw new RuntimeException("An unexpected error occurred during the API call: ${e.message}")
        }

        return [statusLine: statusLine, responseJson: responseJson]
    }

    static Map<String, Object> sendPostRequest(HTTPBuilder http, String path = "/", Map<String, Object> body = [:]) {
        def statusLine
        def responseJson

        try {
            http.post(
                    path: path,
                    body: body,
                    requestContentType: ContentType.JSON
            ) { resp, json ->
                statusLine = resp.statusLine
                responseJson = json
            }
        } catch (HttpResponseException e) {
            // Handle HTTP errors (e.g., 400, 404) and capture the response
            statusLine = e.response.statusLine
            responseJson = e.response.data // JSON body, if any, in error response
        } catch (Exception e) {
            // Handle other unexpected exceptions
            throw new RuntimeException("An unexpected error occurred during the API call: ${e.message}")
        }

        return [statusLine: statusLine, responseJson: responseJson]
    }
}
