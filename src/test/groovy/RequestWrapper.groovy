import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

class RequestWrapper {
    static HTTPBuilder createHttpBuilder(String url) {
        return new HTTPBuilder(url)
    }

    static Map<String, Object> sendGetRequest(HTTPBuilder http, String path = "/", Map<String, Object> queryParams = [:]) {
        def statusLine
        def responseJson

        http.get(path: path, query: queryParams, contentType: ContentType.JSON) { resp, json ->
            statusLine = resp.statusLine
            responseJson = json
        }

        return [statusLine: statusLine, responseJson: responseJson]
    }

    static Map<String, Object> sendPostRequest(HTTPBuilder http, String path = "/", Map<String, Object> body = [:]) {
        def statusLine
        def responseJson

        http.post(
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