import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class WeatherAPI extends Specification {

    @Shared apiKey = 'b2ce5b9466a4cdcec5e7a6bf11465c5a'
    @Shared lang = "en"
    @Shared baseUrl = 'https://api.openweathermap.org'
    @Shared path = '/data/2.5/forecast/daily'
    @Shared RequestWrapper requestWrapper
    @Shared defaultQueryParams

    def setupSpec() {
        // Shared RequestWrapper initialization
        requestWrapper = new RequestWrapper(baseUrl)
    }

    def setup() {
        // Set up common query parameters that are reused in multiple tests
        defaultQueryParams = [lang: lang, appid: apiKey]
    }

    @Unroll
    def "Positive test cases for: #cityName, #days, #expectedPopulation, #expectedTimezone, #expectedCode "() {
        when: "The API request is sent"
        // Merge common query params with specific ones
        def queryParams = defaultQueryParams + [q: cityName, cnt: days]
        def result = requestWrapper.sendGetRequest(path, queryParams)

        def statusLine = result.statusLine
        def responseJson = result.responseJson

        then: "Ensure that a status line is received"
        statusLine != null

        and: "The response should contain forecast data for the expected number of days"
        assert responseJson != null
        assert responseJson.list.size() == days

        and: "The city and country in the response should match the requested city and country"
        def cityNameResponse = responseJson.city.name
        def countryResponse = responseJson.city.country
        cityNameResponse == cityName.split(",")[0]
        countryResponse == cityName.split(",")[1]

        and: "Validate the population and timezone data"
        def populationResponse = responseJson.city.population
        def timezoneResponse = responseJson.city.timezone
        populationResponse == expectedPopulation
        timezoneResponse == expectedTimezone

        and: "Ensure that 'message' field exists and validate its value"
        def messageResponse = responseJson.message
        messageResponse != null
        messageResponse < expectedMessage

        and: "Ensure that 'cod' field exists and validate its value"
        def codeResponse = responseJson.cod
        codeResponse != null
        codeResponse == expectedCode

        and: "Validate the forecast data is returned in the correct order"
        responseJson.list.eachWithIndex { day, index ->
            def date = new Date(day.dt * 1000L) // Convert date from Unix timestamp
            def temp = day.temp.day - 273.15 // Convert temperature from Kelvin to Celsius
            def description = day.weather[0].description

            temp.round(2) != null
            description != null
        }

        where:
        cityName      | days | expectedPopulation | expectedTimezone | expectedCode | expectedMessage
        'Budapest,HU' | 7    | 1696128            | 7200             | '200'         | 6
        'Paris,FR'    | 5    | 2138551            | 7200             | '200'         | 6
        'Berlin,DE'   | 3    | 1000000            | 7200             | '200'         | 6
        'London,GB'   | 1    | 1000000            | 3600             | '200'         | 6
        'New York,US' | 10   | 8175133            | -14400           | '200'         | 6
        'Canberra,AU' | 15   | 327700             | 36000            | '200'         | 6
        'Vienna,AT'   | 16   | 1000000            | 7200             | '200'         | 6
    }

    @Unroll
    def "Negative test cases for #cityName, #days, #expectedCode, #appId"() {
        when: "The API request is sent"
        def queryParams = defaultQueryParams + [q: cityName, cnt: days, appid: appId]
        def result = requestWrapper.sendGetRequest(path, queryParams)

        def statusLine = result.statusLine
        def responseJson = result.responseJson

        then: "Ensure that a status line is received"
        statusLine != null

        and: "Check the status code to be 400/404/401"
        def statusCode = statusLine.statusCode
        statusCode == expectedCode

        and: "Check the error message"
        if (statusCode != 200) {
            assert responseJson.message == expectedError
        }

        where:
        cityName      | days    | expectedCode | appId                             | expectedError
        "Moscow,RU"   | "apple" | 400          | "b2ce5b9466a4cdcec5e7a6bf11465c5a" | "apple is not a number" // Invalid day parameter
        ""            | 3       | 400          | 'b2ce5b9466a4cdcec5e7a6bf11465c5a' | "Nothing to geocode"    // Empty city name
        "fporek,__"   | 9       | 404          | 'b2ce5b9466a4cdcec5e7a6bf11465c5a' | "city not found"        // Invalid city and country
        "Beijing,CH"  | 0       | 400          | 'b2ce5b9466a4cdcec5e7a6bf11465c5a' | "cnt from 1 to 17"      // Boundary value (lower)
        "Madrid,ES"   | 18      | 400          | 'b2ce5b9466a4cdcec5e7a6bf11465c5a' | "cnt from 1 to 17"      // Boundary value (upper)
        "Киев,UA"     | 3       | 200          | 'b2ce5b9466a4cdcec5e7a6bf11465c5a' | "Bad request"           // Different language
        "Szeged,HU"   | 3       | 401          | 'b2ccdca'                          | "Invalid API key. Please see https://openweathermap.org/faq#error401 for more info."       // Wrong API key
        "PL,Warsaw"   | 4       | 404          | "b2ce5b9466a4cdcec5e7a6bf11465c5a" | "city not found"        // Switched order for cityName
        "Belgrade,HU" | 8       | 404          | "b2ce5b9466a4cdcec5e7a6bf11465c5a" | "city not found"        // Wrong country code
    }

    def "Test cases for: #latitude and #longitude works properly"() {
        when: "The API request is sent"
        def queryParams = defaultQueryParams + [lat: latitude, lon: longitude, cnt: days]
        def result = requestWrapper.sendGetRequest(path, queryParams)

        def statusLine = result.statusLine
        def responseJson = result.responseJson

        then: "Ensure that a status line is received"
        statusLine != null

        and: "The coordinates should relate to the given city with the proper data"
        assert responseJson != null
        assert responseJson.list.size() == days
        responseJson.city.name == expectedCityName
        responseJson.city.population == expectedPopulation
        responseJson.city.timezone == expectedTimezone
        assert responseJson.message < expectedMessage
        responseJson.cod == expectedCode

        where:
        latitude | longitude | days | expectedPopulation | expectedTimezone |  expectedCode | expectedMessage | expectedCityName
        47.4979  | 19.0402   | 7    | 1696128            | 7200             | '200'         | 6               | "Budapest"
        48.8534  | 2.3488    | 5    | 2138551            | 7200             | '200'         | 6               | 'Paris'
        52.5244  | 13.4105   | 3    | 1000000            | 7200             | '200'         | 6               | 'Berlin'
        51.5085  | -0.1257   | 1    | 1000000            | 3600             | '200'         | 6               | 'London'
        40.7143  | -74.006   | 10   | 8175133            | -14400           | '200'         | 6               | 'New York'
        -35.2835 | 149.1281  | 15   | 327700             | 36000            | '200'         | 6               | 'Canberra'
        48.2085  | 16.3721   | 16   | 1000000            | 7200             | '200'         | 6               | 'Vienna'
    }
}

