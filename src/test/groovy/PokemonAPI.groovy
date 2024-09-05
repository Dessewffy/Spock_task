import spock.lang.Specification

class PokemonAPI extends Specification {

    def responseJson
    def encounterJson


    def cleanup() {
        // After every test clean up
        responseJson = null
        encounterJson = null
    }

    def "Check the pokemon data"() {
        given: "A Pokemon name"
        def pokemonName = pokemonNameParam // I set it in the where block

        when: "We send a request to fetch Pokemon data"
        def url = "https://pokeapi.co/api/v2/pokemon/${pokemonName}"
        def http = RequestWrapper.createHttpBuilder(url)
        def result = RequestWrapper.sendGetRequest(http)
        responseJson = result.responseJson

        then: "The response should be valid"
        result.statusLine.statusCode == 200
        responseJson.name == pokemonName

        and: "Print the important information"
        def name = responseJson.name
        def height = responseJson.height
        def pokemonId = responseJson.id
        def xp = responseJson.base_experience

        println("")
        println("Pokemon name: ${name}")
        println("Pokemon height: ${height}")
        println("Pokemon ID: ${pokemonId}")
        println("Pokemon experience: ${xp}")
        println("")

        where:
        pokemonNameParam << ["ditto", "pikachu", "bulbasaur", "charizard"] // POkemon names
    }

    def "Check the location area of the Pokemons"() {
        given: "A Pokemon name"
        def pokemonName = pokemonNameParam // I set it in the where block

        when: "We send a request to fetch encounter data"
        def encounterUrl = "https://pokeapi.co/api/v2/pokemon/${pokemonName}/encounters" // I have to change the url
        def http = RequestWrapper.createHttpBuilder(encounterUrl)
        def result = RequestWrapper.sendGetRequest(http)
        encounterJson = result.responseJson

        then: "The encounter response should be valid"
        result.statusLine.statusCode == 200

        and: "Print the location information"
        println("")
        println("Encounter locations for ${pokemonName}:")
        encounterJson.each { encounter ->
            println("Location: ${encounter.location_area.name}")
        }
        println("")

        where:
        pokemonNameParam << ["ditto", "pikachu", "bulbasaur","charizard"] // POkemon names
    }

}



