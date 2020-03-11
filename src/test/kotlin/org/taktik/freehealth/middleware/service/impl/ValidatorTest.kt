package org.taktik.freehealth.middleware.service.impl

import be.cin.types.v1.DetailType
import be.cin.types.v1.DetailsType
import be.cin.types.v1.FaultType
import be.cin.types.v1.StringLangType
import be.fgov.ehealth.errors.core.v1.LocalisedStringType
import be.fgov.ehealth.errors.soa.v1.BusinessError
import be.fgov.ehealth.errors.soa.v1.EnvironmentType
import org.junit.Assert
import org.junit.Test
import org.taktik.connector.technical.utils.MarshallerHelper
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.validation.SchemaFactory


class ValidatorTest {
    @Test
    fun instanceOfValidator() {
        val s =
            SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
                .newSchema(this.javaClass.classLoader.getResource("ehealth-kmehr/XSD/recipe/recipe_PP_kmehr_elements-1_19.xsd"))
        Assert.assertNotNull("Not null", s)
    }

    @Test
    fun unmarshalFault() {
        val s = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Fault xmlns:ns2="urn:oasis:names:tc:SAML:2.0:assertion" xmlns:ns3="http://www.w3.org/2000/09/xmldsig#" xmlns:ns4="urn:oasis:names:tc:SAML:2.0:protocol" xmlns:ns5="http://www.w3.org/2001/04/xmlenc#" xmlns:ns6="urn:be:cin:types:v1" xmlns:ns7="urn:be:cin:nippin:memberData:support:v2" xmlns:ns8="urn:be:cin:nippin:memberdata:saml:extension">
              <FaultCode>INPUT_ERROR</FaultCode>
              <FaultSource>IO-BE-500</FaultSource>
              <Message>Mismatched ContactType</Message>
              <Details>
                <Detail>
                  <DetailCode>MISMATCHED_CONTACTTYPE</DetailCode>
                  <DetailSource>Insurability</DetailSource>
                  <Location>*:AttributeQuery/*:Extensions/*:Facet[@id='urn:be:cin:nippin:insurability']/*:Dimension[@id='contactType']</Location>
                  <Message>The value of contactType is hospitalized and member is not hospitalized</Message>
                </Detail>
              </Details>
            </Fault>
        """.trimIndent()

        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val builder = factory.newDocumentBuilder()

        val root = builder.parse(ByteArrayInputStream(s.toByteArray())).documentElement

        val fault = FaultType().apply {
            faultCode = root.getElementsByTagName("FaultCode").item(0)?.textContent
            faultSource = root.getElementsByTagName("FaultSource").item(0)?.textContent
            message = root.getElementsByTagName("Message").item(0)?.let {
                StringLangType().apply {
                    value = it.textContent
                    lang = it.attributes.getNamedItem("lang")?.textContent
                }
            }

            root.getElementsByTagName("Detail").let {
                if (it.length > 0) {
                    details = DetailsType()
                }
                for (i in 0 until it.length) {
                    details.details.add(DetailType().apply {
                        it.item(i).let {
                            detailCode = (it as Element).getElementsByTagName("DetailCode").item(0)?.textContent
                            detailSource = it.getElementsByTagName("DetailSource").item(0)?.textContent
                            location = it.getElementsByTagName("Location").item(0)?.textContent
                            message = it.getElementsByTagName("Message").item(0)?.let {
                                StringLangType().apply {
                                    value = it.textContent
                                    lang = it.attributes.getNamedItem("lang")?.textContent
                                }
                            }
                        }
                    })
                }

            }
        }
    }

    @Test
    fun unmarshalEhboxFault() {
        val s = """
            <?xml version="1.0" encoding="UTF-8"?>
            <soapenv:Fault xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
              <faultcode>soapenv:Server</faultcode>
              <faultstring>803: The specified Quality is invalid; please verify that Quality is a quality recognized by the system.</faultstring>
              <detail>
                <urn:BusinessError xmlns:urn="urn:be:fgov:ehealth:errors:soa:v1" Id="9986eca637bc986c">
                  <Origin>Server</Origin>
                  <Code>803</Code>
                  <Message xml:lang="EN">The specified Quality is invalid; please verify that Quality is a quality recognized by the system.</Message>
                  <urn:Environment>Acceptation</urn:Environment>
                </urn:BusinessError>
              </detail>
            </soapenv:Fault>
        """.trimIndent()

        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val builder = factory.newDocumentBuilder()
        val fault = (builder.parse(ByteArrayInputStream(s.toByteArray())).documentElement)?.let {
            org.taktik.connector.business.domain.ehbox.fault.FaultType().apply {
                faultCode =
                    it.getElementsByTagNameWithOrWithoutNs("http://schemas.xmlsoap.org/soap/envelope/", "faultcode")
                        .item(0)?.textContent
                faultString =
                    it.getElementsByTagNameWithOrWithoutNs("http://schemas.xmlsoap.org/soap/envelope/", "faultstring")
                        .item(0)?.textContent
                details = org.taktik.connector.business.domain.ehbox.fault.DetailsType().apply {
                    (it.getElementsByTagNameWithOrWithoutNs("urn:be:fgov:ehealth:errors:soa:v1", "BusinessError").item(0) as? Element)?.let {
                        details.add(BusinessError().apply {
                            origin =
                                it.getElementsByTagNameWithOrWithoutNs("urn:be:fgov:ehealth:errors:soa:v1", "Origin")
                                    .item(0)?.textContent
                            code =
                                it.getElementsByTagNameWithOrWithoutNs("urn:be:fgov:ehealth:errors:soa:v1", "Code")
                                    .item(0)
                                    ?.textContent
                            id = it.attributes.getNamedItem("Id")?.textContent
                            environment =
                                EnvironmentType.fromValue(it.getElementsByTagNameWithOrWithoutNs("urn:be:fgov:ehealth:errors:soa:v1", "Environment").item(0)?.textContent)
                            messages.add(LocalisedStringType().apply {
                                it.getElementsByTagNameWithOrWithoutNs("urn:be:fgov:ehealth:errors:soa:v1", "Message")
                                    .item(0)?.let {
                                        lang =
                                            it.attributes.getNamedItemWithOrWithoutNs("http://www.w3.org/XML/1998/namespace", "lang")
                                                ?.textContent
                                        value = it.textContent
                                    }
                            })
                        })
                    }
                }
            }
        }

        Assert.assertNotNull("Not null", fault)
    }

    private fun Element.getElementsByTagNameWithOrWithoutNs(ns: String, name: String): NodeList {
        return this.getElementsByTagNameNS(ns, name).let { if (it.length > 0) it else this.getElementsByTagName(name) }
    }

    private fun NamedNodeMap.getNamedItemWithOrWithoutNs(ns: String, name: String): Node? {
        return this.getNamedItemNS(ns, name) ?: this.getNamedItem(name)
    }

}
