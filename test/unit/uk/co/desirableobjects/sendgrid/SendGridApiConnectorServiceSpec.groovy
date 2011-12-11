package uk.co.desirableobjects.sendgrid

import grails.plugin.spock.UnitSpec
import spock.lang.Shared

import groovyx.net.http.RESTClient
import grails.converters.JSON
import uk.co.desirableobjects.sendgrid.exception.MissingCredentialsException
import spock.lang.Unroll

class SendGridApiConnectorServiceSpec extends UnitSpec {

    @Shared SendGridApiConnectorService sendGridApiConnectorService
    @Shared static private Map postData = [:]

    private static final String USERNAME = 'antony'
    private static final String PASSWORD = 'password'

    def setupSpec() {
        
        mockConfig ''
        
        sendGridApiConnectorService = new SendGridApiConnectorService()

        RESTClient.metaClass.post = { Map params ->
            postData = params.body
            return [data:JSON.parse('{"response":"success"}')]
        }

        sendGridApiConnectorService.sendGrid = new RESTClient()

    }
    
    def 'connector provides username and password to api'() {

        given:
            mockConfig """
                sendgrid {
                    username = '${USERNAME}'
                    password = '${PASSWORD}'
                }
            """

        when:
            sendGridApiConnectorService.post([:])
        
        then:
            postData.api_user == USERNAME
            postData.api_key == PASSWORD

    }

    @Unroll('No authentication configured (configuration was #config)')
    def 'No authentication configured'() {

        given:
            mockConfig config

        when:
            sendGridApiConnectorService.post([:])

        then:
            thrown MissingCredentialsException

        where:
            config << ['', 'sendgrid { }', 'sendgrid { username = "" }']

    }

    @Unroll('Configuration #config overrides API URL to be #expectedUri')
    def 'API URL can be overriden by config'() {

        given:
            mockConfig config

        when:
            sendGridApiConnectorService = new SendGridApiConnectorService()

        then:
            sendGridApiConnectorService.sendGrid.uri.toString() == expectedUri

        where:
            config                                        | expectedUri
            ''                                            | 'https://sendgrid.com/api/'
            'sendgrid { } '                               | 'https://sendgrid.com/api/'
            'sendgrid { api.url = "http://example.net" }' | 'http://example.net'
            'sendgrid.api.url = "http://example.net" '    | 'http://example.net'

    }

}