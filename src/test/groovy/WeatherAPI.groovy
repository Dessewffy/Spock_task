import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import java.text.SimpleDateFormat
import spock.lang.Specification
import spock.lang.Unroll

class WeatherAPI extends Specification {

    @Unroll
    def "should fetch weather forecast for #cityName for #days days"() {
        given: "An API URL and key and the language"
        def apiKey = 'b2ce5b9466a4cdcec5e7a6bf11465c5a'
        def lang = "en"
        def url = "https://api.openweathermap.org/data/2.5/forecast/daily?q=${cityName},&cnt=${days}&lang=${lang}&appid=${apiKey}"

        and: "An HTTPBuilder instance"
        def http = RequestWrapper.createHttpBuilder(url)

        when: "The API request is sent"
        def result = RequestWrapper.sendGetRequest(http)
        def statusLine = result.statusLine
        def responseJson = result.responseJson

        then: "The status code should be 200 (OK)"
        statusLine.statusCode == 200

        and: "The response should contain forecast data"
        responseJson != null
        responseJson.list.size() == days

        and: "Print the forecast data"
        def dateFormat = new SimpleDateFormat('yyyy-MM-dd')
        dateFormat.setTimeZone(TimeZone.getTimeZone('GMT'))

        def cityNameResponse = responseJson.city.name
        responseJson.list.each { day ->
            def date = new Date(day.dt * 1000L) // Convert date from Unix timestamp
            def temp = day.temp.day - 273.15 // Convert temperature from Kelvin to Celsius
            def description = day.weather[0].description

            println "City name: ${cityNameResponse}"
            println "Date: ${dateFormat.format(date)}"
            println "Temperature: ${temp.round(2)}°C"
            println "Description: ${description.capitalize()}"
            println ""

        }

        where:
        cityName     | days //Parametrization
        'Budapest,HU' | 7
        'Paris,FR'    | 5
        'Berlin,DE'   | 3
    }
}

