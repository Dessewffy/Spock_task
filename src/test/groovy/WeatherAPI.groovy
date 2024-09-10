import groovy.util.logging.Log

import java.text.SimpleDateFormat
import spock.lang.Specification
import spock.lang.Unroll

class WeatherAPI extends Specification {
    @Unroll
    def "Positive test cases for: #cityName, #days, #expectedPopulation, #expectedTimezone, #expectedCode "() {
        given: "An API key, language, and HTTPBuilder instance"
        def apiKey = 'b2ce5b9466a4cdcec5e7a6bf11465c5a'
        def lang = "en"
        def baseUrl = 'https://api.openweathermap.org'

        and: "Create the HTTPBuilder using RequestWrapper"
        def http = RequestWrapper.createHttpBuilder(baseUrl)

        when: "The API request is sent"
        def result = RequestWrapper.sendGetRequest(http,
                '/data/2.5/forecast/daily',
                [q: cityName, cnt: days, lang: lang, appid: apiKey])

        def statusLine = result.statusLine
        def responseJson = result.responseJson

        then: "Ensure that we received a status line"
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
        cityName      | days | expectedPopulation | expectedTimezone |  expectedCode | expectedMessage
        'Budapest,HU' | 7    | 1696128            | 7200             | '200'         | 0.5
        'Paris,FR'    | 5    | 2138551            | 7200             | '200'         | 0.5
        'Berlin,DE'   | 3    | 1000000            | 7200             | '200'         | 0.5
        'London,GB'   | 1    | 1000000            | 3600             | '200'         | 0.5
    }

    @Unroll
    def "Negative test cases for #cityName, #days,#expectedCode, #appId"() {
        given: "An API key, language, and HTTPBuilder instance"
        def apiKey = appId
        def lang = "en"
        def baseUrl = 'https://api.openweathermap.org'

        and: "Create the HTTPBuilder using RequestWrapper"
        def http = RequestWrapper.createHttpBuilder(baseUrl)

        try {
            when: "The API request is sent"
            def result = RequestWrapper.sendGetRequest(http,
                    '/data/2.5/forecast/daily',
                    [q: cityName, cnt: days, lang: lang, appid: apiKey])

            def statusLine = result.statusLine
            def responseJson = result.responseJson

            then: "Ensure that we received a status line"
            statusLine != null

            and: "Check the status code to be 400 or 404"
            def statusCode = statusLine.statusCode
            assert statusCode == expectedCode

        } catch (Exception e) {
            then: "Handle unexpected exceptions"
            assert false: "Unexpected exception occurred: ${e.message}"
        }

        where:
        cityName      | days    | expectedCode | appId
        "Moscow,RU"   | "apple" | 400 |"b2ce5b9466a4cdcec5e7a6bf11465c5a"// Invalid day parameter
        ""            | 3       | 400 |'b2ce5b9466a4cdcec5e7a6bf11465c5a'// Empty city name
        "fporek,__"   | 9       | 404 |'b2ce5b9466a4cdcec5e7a6bf11465c5a'// Invalid city and country
        "Beijing,CH"  | 0       | 400 |'b2ce5b9466a4cdcec5e7a6bf11465c5a'// Boundary value (lower)
        "Madrid,ES"   | 18      | 400 |'b2ce5b9466a4cdcec5e7a6bf11465c5a'// Boundary value (upper)
        "Киев,UA"     | 3       | 200 |'b2ce5b9466a4cdcec5e7a6bf11465c5a'// Different language
        "Szeged,HU"   | 3       | 401 |'b2ccdca'// wrong API key

    }
}