import spock.lang.Specification
import spock.lang.Unroll

class PokemonAPI extends Specification {

    def responseJson
    def encounterJson

    @Unroll
    def "Check the Pokemon data"() {
        given: "A Pokemon name"
        def pokemonName = pokemonNameParam

        when: "We send a request to fetch Pokemon data"
        def baseUrl = "https://pokeapi.co"
        def path = "/api/v2/pokemon/${pokemonName}"
        def http = RequestWrapper.createHttpBuilder(baseUrl)
        def result = RequestWrapper.sendGetRequest(http, path, [:])
        responseJson = result.responseJson

        then: "The response should be valid"
        assert result.statusLine != null : "No status line received from API call."
        result.statusLine.statusCode == 200
        responseJson.name == expectedPokemonName

        and: "The response data should match the expected data"
        responseJson.height == expectedHeight
        responseJson.id == expectedId
        responseJson.base_experience == expectedXp

        where:
        pokemonNameParam  |expectedPokemonName| expectedHeight | expectedId | expectedXp
        "ditto"           |"ditto"             | 3              | 132        | 101
        "pikachu"         |"pikachu"           | 4              | 25         | 112
        "bulbasaur"       |"balbasaur"         | 7              | 1          | 64
        "charizard"       |"charizard"         | 17             | 6          | 267
    }


    @Unroll
    def "Check the location area of the Pokemons"() {
        given: "A Pokemon name"
        def pokemonName = pokemonNameParam

        when: "We send a request to fetch encounter data"
        def baseUrl = "https://pokeapi.co"
        def path = "/api/v2/pokemon/${pokemonName}/encounters"
        def http = RequestWrapper.createHttpBuilder(baseUrl)
        def result = RequestWrapper.sendGetRequest(http, path, [:])
        encounterJson = result.responseJson

        then: "The encounter response should be valid"
        assert result.statusLine != null : "No status line received from API call."
        result.statusLine.statusCode == 200

        and: "The location information should match the expected locations"
        def actualLocations = encounterJson.collect { it.location_area.name }
        actualLocations.containsAll(expectedLocations)


        where:
        pokemonNameParam  | expectedLocations
        "ditto"           | ["sinnoh-route-218-area", "johto-route-34-area", "johto-route-35-area", "johto-route-47-area",
                             "kanto-route-13-area", "kanto-route-14-area", "kanto-route-15-area", "cerulean-cave-1f",
                             "cerulean-cave-2f", "cerulean-cave-b1f", "kanto-route-23-area", "pokemon-mansion-b1f",
                             "desert-underpass-area", "giant-chasm-forest", "giant-chasm-forest-cave", "pokemon-village-area",
                             "johto-safari-zone-zone-wetland", "alola-route-9-police-station", "konikoni-city-area",
                             "mount-hokulani-main", "mount-hokulani-east", "mount-hokulani-west"]
        "pikachu"         | ["trophy-garden-area", "pallet-town-area", "kanto-route-2-south-towards-viridian-city",
                             "viridian-forest-area", "power-plant-area", "hoenn-safari-zone-sw", "hoenn-safari-zone-se",
                             "kalos-route-3-area", "santalune-forest-area", "slateport-city-contest-hall",
                             "verdanturf-town-contest-hall", "fallarbor-town-contest-hall", "lilycove-city-contest-hall",
                             "alola-route-1-east", "alola-route-1-west", "hauoli-city-shopping-district",
                             "heahea-city-surf-association"]
        "bulbasaur"       | ["cerulean-city-area", "pallet-town-area", "lumiose-city-area", "alola-route-2-main"]
        "charizard"       | [""] // charizard has no location in the API db!
    }
}




