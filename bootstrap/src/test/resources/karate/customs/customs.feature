Feature: Manage customs through the public REST API

  Background:
    * url baseUrl
    * path '/api/v1/customs'
    # Default caller: a Keycloak service account holding platform-writer
    * configure headers = { Authorization: '#("Bearer " + writerToken)' }

  Scenario: A caller without a token is rejected with a problem document
    * configure headers = null
    When method get
    Then status 401
    And match response.code == 'UNAUTHENTICATED'
    And match responseHeaders['WWW-Authenticate'][0] contains 'Bearer'

  Scenario: A reader can list but cannot create
    * configure headers = { Authorization: '#("Bearer " + readerToken)' }
    When method get
    Then status 200

    Given url baseUrl
    And path '/api/v1/customs'
    And request { name: 'Forbidden' }
    When method post
    Then status 403
    And match response.code == 'ACCESS_DENIED'

  Scenario: A created custom can be read back
    Given request { name: 'Demo' }
    When method post
    Then status 201
    And match response == { id: '#uuid', name: 'Demo', version: 0 }
    And match responseHeaders['Location'][0] == '#regex .*/api/v1/customs/' + response.id
    * def id = response.id

    Given url baseUrl
    And path '/api/v1/customs', id
    When method get
    Then status 200
    And match response.name == 'Demo'

  Scenario: Renaming bumps the version and is never hidden by a stale cache entry
    Given request { name: 'Before' }
    When method post
    Then status 201
    * def id = response.id

    # Warm the cache twice: the second read is a hit
    Given url baseUrl
    And path '/api/v1/customs', id
    When method get
    Then status 200
    Given url baseUrl
    And path '/api/v1/customs', id
    When method get
    Then match response.name == 'Before'

    Given url baseUrl
    And path '/api/v1/customs', id
    And request { name: 'After' }
    When method put
    Then status 200
    And match response == { id: '#(id)', name: 'After', version: 1 }

    Given url baseUrl
    And path '/api/v1/customs', id
    When method get
    Then status 200
    And match response == { id: '#(id)', name: 'After', version: 1 }

  Scenario: A deleted custom is gone
    Given request { name: 'Ephemeral' }
    When method post
    Then status 201
    * def id = response.id

    Given url baseUrl
    And path '/api/v1/customs', id
    When method delete
    Then status 204

    Given url baseUrl
    And path '/api/v1/customs', id
    When method get
    Then status 404
    And match response.code == 'CUSTOM_NOT_FOUND'
    And match responseHeaders['Content-Type'][0] contains 'application/problem+json'

  Scenario: Listing is paginated
    Given request { name: 'Paged' }
    When method post
    Then status 201

    Given url baseUrl
    And path '/api/v1/customs'
    And param size = 1
    When method get
    Then status 200
    And match response.size == 1
    And match response.items == '#[1]'
    And match response.totalElements == '#? _ >= 1'

  Scenario Outline: Invalid names are rejected with a problem document
    Given request { name: '<name>' }
    When method post
    Then status 400
    And match response.code == 'VALIDATION_FAILED'
    And match response.errors[0].field == 'name'

    Examples:
      | name                      |
      |                           |
      | abcdefghijklmnopqrstuvwxy |
