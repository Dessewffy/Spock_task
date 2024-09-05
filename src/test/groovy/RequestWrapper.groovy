import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder

class RequestWrapper {
    static HTTPBuilder createHttpBuilder(String url) {
        return new HTTPBuilder(url)
    }
    static Map<String, Object> sendGetRequest(HTTPBuilder http) {
        def statusLine
        def responseJson

        http.get(contentType: ContentType.JSON) { resp, json ->
            statusLine = resp.statusLine
            responseJson = json
        }

        return [statusLine: statusLine, responseJson: responseJson]
    }

}