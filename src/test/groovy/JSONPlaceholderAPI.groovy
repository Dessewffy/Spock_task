import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class JSONPlaceholderAPI extends Specification {


    @Shared baseURL = "https://jsonplaceholder.typicode.com"

    @Shared RequestWrapper requestWrapper

    def setupSpec() {
        requestWrapper = new RequestWrapper(baseURL)
    }

    def performApiGetRequest(String path) {
        def result = requestWrapper.sendGetRequest(path)
        def statusCode = result.statusLine.statusCode
        def responseJson = result.responseJson
        return [statusCode: statusCode, responseJson: responseJson]
    }

    def performApiPostRequest(String path, Map<String, Object> requestBody) {
        def result = requestWrapper.sendPostRequest(path, requestBody)
        def statusCode = result.statusLine.statusCode
        def responseJson = result.responseJson
        return [statusCode: statusCode, responseJson: responseJson]
    }

    @Unroll
    def "should get post with ID #postId"() {
        when: "The API GET request is sent for post ID #postId"
        def apiResponse = performApiGetRequest("/posts/${postId}")
        def statusCode = apiResponse.statusCode
        def responseJson = apiResponse.responseJson

        then: "Ensure that we received a status code"
        assert statusCode != null : "No status code received from API call."

        then: "The status code should be 200 (Ok)"
        statusCode == 200

        and: "The response should contain the following data for post ID #postId"
        responseJson != null
        responseJson.id == postId
        responseJson.body == expectedBody
        responseJson.title == expectedTitle
        responseJson.userId == expectedUserId

        where:
        postId | expectedBody                                                                                                  | expectedTitle                                                                                     | expectedUserId
        1      | "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto" | "sunt aut facere repellat provident occaecati excepturi optio reprehenderit"                      | 1
        2      | "est rerum tempore vitae\nsequi sint nihil reprehenderit dolor beatae ea dolores neque\nfugiat blanditiis voluptate porro vel nihil molestiae ut reiciendis\nqui aperiam non debitis possimus qui neque nisi nulla" | "qui est esse"                                                                                     | 1
        3      | "et iusto sed quo iure\nvoluptatem occaecati omnis eligendi aut ad\nvoluptatem doloribus vel accusantium quis pariatur\nmolestiae porro eius odio et labore et velit aut" | "ea molestias quasi exercitationem repellat qui ipsa sit aut"                                      | 1
    }

    def "Should fetch post with #userId"() {
        given: "The request body to be sent"
        def requestBody = [
                title: 'foo',
                body : 'bar',
                userId: userId
        ]

        when: "The API POST request is sent"
        def apiResponse = performApiPostRequest('/posts', requestBody)
        def statusCode = apiResponse.statusCode
        def responseJson = apiResponse.responseJson

        then: "Ensure that we received a status code"
        assert statusCode != null : "No status code received from API call."

        then: "The status code should be 201 (Created)"
        statusCode == 201

        and: "The response should contain the posted data"
        responseJson != null
        responseJson.id == expectedId
        responseJson.userId == expecteduserId
        responseJson.body == expectedBody
        responseJson.title == expectedTitle

        where:
        userId | expectedId | expecteduserId | expectedBody | expectedTitle
        1      | 101        | 1              | "bar"        | "foo"
        2      | 101        | 2              | "bar"        | "foo"
    }
}

