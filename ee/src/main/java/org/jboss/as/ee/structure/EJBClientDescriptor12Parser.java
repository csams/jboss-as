/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.ee.structure;

import org.jboss.as.ee.EeMessages;
import org.jboss.as.ee.metadata.EJBClientDescriptorMetaData;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLExtendedStreamReader;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static javax.xml.stream.XMLStreamConstants.ATTRIBUTE;
import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.DTD;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.ENTITY_DECLARATION;
import static javax.xml.stream.XMLStreamConstants.ENTITY_REFERENCE;
import static javax.xml.stream.XMLStreamConstants.NAMESPACE;
import static javax.xml.stream.XMLStreamConstants.NOTATION_DECLARATION;
import static javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION;
import static javax.xml.stream.XMLStreamConstants.SPACE;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * Parser for urn:jboss:ejb-client:1.2:jboss-ejb-client
 *
 * @author Jaikiran Pai
 */
class EJBClientDescriptor12Parser implements XMLElementReader<EJBClientDescriptorMetaData> {

    public static final String NAMESPACE_1_2 = "urn:jboss:ejb-client:1.2";

    public static final EJBClientDescriptor12Parser INSTANCE = new EJBClientDescriptor12Parser();


    private EJBClientDescriptor12Parser() {
    }


    enum Element {
        CLIENT_CONTEXT,
        NODE,
        EJB_RECEIVERS,
        JBOSS_EJB_CLIENT,
        REMOTING_EJB_RECEIVER,
        CLUSTERS,
        CLUSTER,
        CHANNEL_CREATION_OPTIONS,
        CONNECTION_CREATION_OPTIONS,
        PROPERTY,
        // default unknown element
        UNKNOWN;

        private static final Map<QName, Element> elements;

        static {
            Map<QName, Element> elementsMap = new HashMap<QName, Element>();
            elementsMap.put(new QName(NAMESPACE_1_2, "jboss-ejb-client"), Element.JBOSS_EJB_CLIENT);
            elementsMap.put(new QName(NAMESPACE_1_2, "client-context"), Element.CLIENT_CONTEXT);
            elementsMap.put(new QName(NAMESPACE_1_2, "ejb-receivers"), Element.EJB_RECEIVERS);
            elementsMap.put(new QName(NAMESPACE_1_2, "remoting-ejb-receiver"), Element.REMOTING_EJB_RECEIVER);
            elementsMap.put(new QName(NAMESPACE_1_2, "clusters"), Element.CLUSTERS);
            elementsMap.put(new QName(NAMESPACE_1_2, "cluster"), Element.CLUSTER);
            elementsMap.put(new QName(NAMESPACE_1_2, "node"), Element.NODE);
            elementsMap.put(new QName(NAMESPACE_1_2, "channel-creation-options"), Element.CHANNEL_CREATION_OPTIONS);
            elementsMap.put(new QName(NAMESPACE_1_2, "connection-creation-options"), Element.CONNECTION_CREATION_OPTIONS);
            elementsMap.put(new QName(NAMESPACE_1_2, "property"), Element.PROPERTY);
            elements = elementsMap;
        }

        static Element of(QName qName) {
            QName name;
            if (qName.getNamespaceURI().equals("")) {
                name = new QName(NAMESPACE_1_2, qName.getLocalPart());
            } else {
                name = qName;
            }
            final Element element = elements.get(name);
            return element == null ? UNKNOWN : element;
        }
    }

    enum Attribute {
        EXCLUDE_LOCAL_RECEIVER,
        LOCAL_RECEIVER_PASS_BY_VALUE,
        CONNECT_TIMEOUT,
        NAME,
        OUTBOUND_CONNECTION_REF,
        VALUE,
        MAX_ALLOWED_CONNECTED_NODES,
        CLUSTER_NODE_SELECTOR,
        USERNAME,
        SECURITY_REALM,
        INVOCATION_TIMEOUT,
        DEPLOYMENT_NODE_SELECTOR,
        // default unknown attribute
        UNKNOWN;

        private static final Map<QName, Attribute> attributes;

        static {
            Map<QName, Attribute> attributesMap = new HashMap<QName, Attribute>();
            attributesMap.put(new QName("exclude-local-receiver"), EXCLUDE_LOCAL_RECEIVER);
            attributesMap.put(new QName("local-receiver-pass-by-value"), LOCAL_RECEIVER_PASS_BY_VALUE);
            attributesMap.put(new QName("name"), NAME);
            attributesMap.put(new QName("value"), VALUE);
            attributesMap.put(new QName("outbound-connection-ref"), OUTBOUND_CONNECTION_REF);
            attributesMap.put(new QName("connect-timeout"), CONNECT_TIMEOUT);
            attributesMap.put(new QName("max-allowed-connected-nodes"), MAX_ALLOWED_CONNECTED_NODES);
            attributesMap.put(new QName("cluster-node-selector"), CLUSTER_NODE_SELECTOR);
            attributesMap.put(new QName("username"), USERNAME);
            attributesMap.put(new QName("security-realm"), SECURITY_REALM);
            attributesMap.put(new QName("invocation-timeout"), INVOCATION_TIMEOUT);
            attributesMap.put(new QName("deployment-node-selector"), DEPLOYMENT_NODE_SELECTOR);
            attributes = attributesMap;
        }

        static Attribute of(QName qName) {
            final Attribute attribute = attributes.get(qName);
            return attribute == null ? UNKNOWN : attribute;
        }
    }

    @Override
    public void readElement(final XMLExtendedStreamReader reader, final EJBClientDescriptorMetaData ejbClientDescriptorMetaData) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case END_ELEMENT: {
                    return;
                }
                case START_ELEMENT: {
                    final Element element = Element.of(reader.getName());

                    switch (element) {
                        case CLIENT_CONTEXT:
                            this.parseClientContext(reader, ejbClientDescriptorMetaData);
                            break;
                        default:
                            this.unexpectedElement(reader);
                    }
                    break;
                }
                default: {
                    this.unexpectedContent(reader);
                }
            }
        }
        unexpectedEndOfDocument(reader.getLocation());
    }

    private void parseClientContext(final XMLExtendedStreamReader reader, final EJBClientDescriptorMetaData ejbClientDescriptorMetaData) throws XMLStreamException {
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            final String val = reader.getAttributeValue(i);
            switch (attribute) {
                case INVOCATION_TIMEOUT:
                    final Long invocationTimeout = Long.parseLong(val.trim());
                    ejbClientDescriptorMetaData.setInvocationTimeout(invocationTimeout);
                    break;
                case DEPLOYMENT_NODE_SELECTOR:
                    ejbClientDescriptorMetaData.setDeploymentNodeSelector(val.trim());
                    break;
                default:
                    unexpectedContent(reader);
            }
        }

        final Set<Element> visited = EnumSet.noneOf(Element.class);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case END_ELEMENT: {
                    return;
                }
                case START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    if (visited.contains(element)) {
                        this.unexpectedElement(reader);
                    }
                    visited.add(element);
                    switch (element) {
                        case EJB_RECEIVERS:
                            this.parseEJBReceivers(reader, ejbClientDescriptorMetaData);
                            break;
                        case CLUSTERS:
                            this.parseClusters(reader, ejbClientDescriptorMetaData);
                            break;
                        default:
                            this.unexpectedElement(reader);
                    }
                    break;
                }
                default: {
                    unexpectedContent(reader);
                }
            }
        }
        unexpectedEndOfDocument(reader.getLocation());
    }

    private void parseEJBReceivers(final XMLExtendedStreamReader reader, final EJBClientDescriptorMetaData ejbClientDescriptorMetaData) throws XMLStreamException {

        // initialize the local-receiver-pass-by-value to the default true
        Boolean localReceiverPassByValue = null;

        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            final String val = reader.getAttributeValue(i);
            switch (attribute) {
                case EXCLUDE_LOCAL_RECEIVER:
                    final boolean excludeLocalReceiver = Boolean.parseBoolean(val.trim());
                    ejbClientDescriptorMetaData.setExcludeLocalReceiver(excludeLocalReceiver);
                    break;
                case LOCAL_RECEIVER_PASS_BY_VALUE:
                    localReceiverPassByValue = Boolean.parseBoolean(val.trim());
                    break;
                default:
                    unexpectedContent(reader);
            }
        }
        // set the local receiver pass by value into the metadata
        ejbClientDescriptorMetaData.setLocalReceiverPassByValue(localReceiverPassByValue);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case END_ELEMENT: {
                    return;
                }
                case START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case REMOTING_EJB_RECEIVER:
                            this.parseRemotingReceiver(reader, ejbClientDescriptorMetaData);
                            break;
                        default:
                            this.unexpectedElement(reader);
                    }
                    break;
                }
                default: {
                    unexpectedContent(reader);
                }
            }
        }
        unexpectedEndOfDocument(reader.getLocation());
    }

    private void parseRemotingReceiver(final XMLExtendedStreamReader reader, final EJBClientDescriptorMetaData ejbClientDescriptorMetaData) throws XMLStreamException {
        String outboundConnectionRef = null;
        final Set<Attribute> required = EnumSet.of(Attribute.OUTBOUND_CONNECTION_REF);
        final int count = reader.getAttributeCount();
        EJBClientDescriptorMetaData.RemotingReceiverConfiguration remotingReceiverConfiguration = null;
        long connectTimeout = 5000;
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            required.remove(attribute);
            switch (attribute) {
                case OUTBOUND_CONNECTION_REF:
                    outboundConnectionRef = reader.getAttributeValue(i).trim();
                    remotingReceiverConfiguration = ejbClientDescriptorMetaData.addRemotingReceiverConnectionRef(outboundConnectionRef);
                    break;
                case CONNECT_TIMEOUT:
                    connectTimeout = reader.getLongAttributeValue(i);
                    break;
                default:
                    unexpectedContent(reader);
            }
        }
        if (!required.isEmpty()) {
            missingAttributes(reader.getLocation(), required);
        }
        // set the timeout
        remotingReceiverConfiguration.setConnectionTimeout(connectTimeout);

        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case END_ELEMENT: {
                    return;
                }
                case START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case CHANNEL_CREATION_OPTIONS:
                            final Properties channelCreationOptions = this.parseChannelCreationOptions(reader);
                            remotingReceiverConfiguration.setChannelCreationOptions(channelCreationOptions);
                            break;
                        default:
                            this.unexpectedElement(reader);
                    }
                    break;
                }
                default: {
                    unexpectedContent(reader);
                }
            }
        }
        unexpectedEndOfDocument(reader.getLocation());
    }

    private void parseClusters(final XMLExtendedStreamReader reader, final EJBClientDescriptorMetaData ejbClientDescriptorMetaData) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case END_ELEMENT: {
                    return;
                }
                case START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case CLUSTER:
                            this.parseCluster(reader, ejbClientDescriptorMetaData);
                            break;
                        default:
                            this.unexpectedElement(reader);
                    }
                    break;
                }
                default: {
                    unexpectedContent(reader);
                }
            }
        }
        unexpectedEndOfDocument(reader.getLocation());
    }

    private void parseCluster(final XMLExtendedStreamReader reader, final EJBClientDescriptorMetaData ejbClientDescriptorMetaData) throws XMLStreamException {
        final Set<Attribute> required = EnumSet.of(Attribute.NAME);
        final int count = reader.getAttributeCount();
        String clusterName = null;
        String clusterNodeSelector = null;
        long connectTimeout = 5000;
        long maxAllowedConnectedNodes = 10;
        String userName = null;
        String securityRealm = null;
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            required.remove(attribute);
            switch (attribute) {
                case NAME:
                    clusterName = reader.getAttributeValue(i).trim();
                    break;
                case CONNECT_TIMEOUT:
                    connectTimeout = reader.getLongAttributeValue(i);
                    break;
                case CLUSTER_NODE_SELECTOR:
                    clusterNodeSelector = reader.getAttributeValue(i).trim();
                    break;
                case MAX_ALLOWED_CONNECTED_NODES:
                    maxAllowedConnectedNodes = reader.getLongAttributeValue(i);
                    break;
                case USERNAME:
                    userName = reader.getAttributeValue(i).trim();
                    break;
                case SECURITY_REALM:
                    securityRealm = reader.getAttributeValue(i).trim();
                    break;
                default:
                    unexpectedContent(reader);
            }
        }
        if (!required.isEmpty()) {
            missingAttributes(reader.getLocation(), required);
        }
        // add a new cluster config to the client configuration metadata
        final EJBClientDescriptorMetaData.ClusterConfig clusterConfig = ejbClientDescriptorMetaData.newClusterConfig(clusterName);
        clusterConfig.setConnectTimeout(connectTimeout);
        clusterConfig.setNodeSelector(clusterNodeSelector);
        clusterConfig.setMaxAllowedConnectedNodes(maxAllowedConnectedNodes);
        clusterConfig.setSecurityRealm(securityRealm);
        clusterConfig.setUserName(userName);

        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case END_ELEMENT: {
                    return;
                }
                case START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case CONNECTION_CREATION_OPTIONS:
                            final Properties connectionCreationOptions = this.parseConnectionCreationOptions(reader);
                            clusterConfig.setConnectionOptions(connectionCreationOptions);
                            break;
                        case CHANNEL_CREATION_OPTIONS:
                            final Properties channelCreationOptions = this.parseChannelCreationOptions(reader);
                            clusterConfig.setChannelCreationOptions(channelCreationOptions);
                            break;
                        case NODE:
                            this.parseClusterNode(reader, clusterConfig);
                            break;
                        default:
                            this.unexpectedElement(reader);
                    }
                    break;
                }
                default: {
                    unexpectedContent(reader);
                }
            }
        }
        unexpectedEndOfDocument(reader.getLocation());
    }

    private Properties parseConnectionCreationOptions(final XMLExtendedStreamReader reader) throws XMLStreamException {
        final Properties connectionCreationOptions = new Properties();
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case END_ELEMENT: {
                    return connectionCreationOptions;
                }
                case START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case PROPERTY:
                            connectionCreationOptions.putAll(this.parseProperty(reader));
                            break;
                        default:
                            this.unexpectedElement(reader);
                    }
                    break;
                }
                default: {
                    unexpectedContent(reader);
                }
            }
        }
        unexpectedEndOfDocument(reader.getLocation());
        // unreachable
        return connectionCreationOptions;
    }

    private Properties parseChannelCreationOptions(final XMLExtendedStreamReader reader) throws XMLStreamException {
        final Properties channelCreationOptions = new Properties();
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case END_ELEMENT: {
                    return channelCreationOptions;
                }
                case START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case PROPERTY:
                            channelCreationOptions.putAll(this.parseProperty(reader));
                            break;
                        default:
                            this.unexpectedElement(reader);
                    }
                    break;
                }
                default: {
                    unexpectedContent(reader);
                }
            }
        }
        unexpectedEndOfDocument(reader.getLocation());
        // unreachable
        return channelCreationOptions;
    }

    private void parseClusterNode(final XMLExtendedStreamReader reader, final EJBClientDescriptorMetaData.ClusterConfig clusterConfig) throws XMLStreamException {
        final Set<Attribute> required = EnumSet.of(Attribute.NAME);
        final int count = reader.getAttributeCount();
        String nodeName = null;
        long connectTimeout = 5000;
        String userName = null;
        String securityRealm = null;
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            required.remove(attribute);
            switch (attribute) {
                case NAME:
                    nodeName = reader.getAttributeValue(i).trim();
                    break;
                case CONNECT_TIMEOUT:
                    connectTimeout = reader.getLongAttributeValue(i);
                    break;
                case USERNAME:
                    userName = reader.getAttributeValue(i).trim();
                    break;
                case SECURITY_REALM:
                    securityRealm = reader.getAttributeValue(i).trim();
                    break;
                default:
                    unexpectedContent(reader);
            }
        }
        if (!required.isEmpty()) {
            missingAttributes(reader.getLocation(), required);
        }
        // add a new node config to the cluster config
        final EJBClientDescriptorMetaData.ClusterNodeConfig clusterNodeConfig = clusterConfig.newClusterNode(nodeName);
        clusterNodeConfig.setConnectTimeout(connectTimeout);
        clusterNodeConfig.setSecurityRealm(securityRealm);
        clusterNodeConfig.setUserName(userName);

        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case END_ELEMENT: {
                    return;
                }
                case START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case CONNECTION_CREATION_OPTIONS:
                            final Properties connectionCreationOptions = this.parseConnectionCreationOptions(reader);
                            clusterNodeConfig.setConnectionOptions(connectionCreationOptions);
                            break;
                        case CHANNEL_CREATION_OPTIONS:
                            final Properties channelCreationOptions = this.parseChannelCreationOptions(reader);
                            clusterNodeConfig.setChannelCreationOptions(channelCreationOptions);
                            break;
                        default:
                            this.unexpectedElement(reader);
                    }
                    break;
                }
                default: {
                    unexpectedContent(reader);
                }
            }
        }
        unexpectedEndOfDocument(reader.getLocation());
    }

    private Properties parseProperty(final XMLExtendedStreamReader reader) throws XMLStreamException {
        final Set<Attribute> required = EnumSet.of(Attribute.NAME, Attribute.VALUE);
        final int count = reader.getAttributeCount();
        String name = null;
        String value = null;
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            required.remove(attribute);
            switch (attribute) {
                case NAME:
                    name = reader.getAttributeValue(i).trim();
                    break;
                case VALUE:
                    value = reader.getAttributeValue(i).trim();
                    break;
                default:
                    unexpectedContent(reader);
            }
        }
        if (!required.isEmpty()) {
            missingAttributes(reader.getLocation(), required);
        }
        // no child elements allowed
        this.requireNoContent(reader);

        final Properties property = new Properties();
        property.put(name, value);
        return property;
    }

    private static void unexpectedEndOfDocument(final Location location) throws XMLStreamException {
        throw EeMessages.MESSAGES.errorParsingEJBClientDescriptor("Unexpected end of document", location);
    }

    private static void missingAttributes(final Location location, final Set<Attribute> required) throws XMLStreamException {
        final StringBuilder b = new StringBuilder("Missing one or more required attributes:");
        for (Attribute attribute : required) {
            b.append(' ').append(attribute);
        }
        throw EeMessages.MESSAGES.errorParsingEJBClientDescriptor(b.toString(), location);
    }

    /**
     * Consumes the remainder of the current element, throwing an
     * {@link javax.xml.stream.XMLStreamException} if it contains any child
     * elements.
     *
     * @param reader the reader
     * @throws javax.xml.stream.XMLStreamException
     *          if an error occurs
     */
    public static void requireNoContent(final XMLExtendedStreamReader reader) throws XMLStreamException {
        if (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            unexpectedElement(reader);
        }
    }

    /**
     * Throws a XMLStreamException for the unexpected element that was encountered during the parse
     *
     * @param reader the stream reader
     * @throws javax.xml.stream.XMLStreamException
     *
     */
    public static void unexpectedElement(final XMLExtendedStreamReader reader) throws XMLStreamException {
        throw EeMessages.MESSAGES.unexpectedElement(reader.getName(), reader.getLocation());
    }

    private static void unexpectedContent(final XMLStreamReader reader) throws XMLStreamException {
        final String kind;
        switch (reader.getEventType()) {
            case ATTRIBUTE:
                kind = "attribute";
                break;
            case CDATA:
                kind = "cdata";
                break;
            case CHARACTERS:
                kind = "characters";
                break;
            case COMMENT:
                kind = "comment";
                break;
            case DTD:
                kind = "dtd";
                break;
            case END_DOCUMENT:
                kind = "document end";
                break;
            case END_ELEMENT:
                kind = "element end";
                break;
            case ENTITY_DECLARATION:
                kind = "entity declaration";
                break;
            case ENTITY_REFERENCE:
                kind = "entity ref";
                break;
            case NAMESPACE:
                kind = "namespace";
                break;
            case NOTATION_DECLARATION:
                kind = "notation declaration";
                break;
            case PROCESSING_INSTRUCTION:
                kind = "processing instruction";
                break;
            case SPACE:
                kind = "whitespace";
                break;
            case START_DOCUMENT:
                kind = "document start";
                break;
            case START_ELEMENT:
                kind = "element start";
                break;
            default:
                kind = "unknown";
                break;
        }
        final StringBuilder b = new StringBuilder("Unexpected content of type '").append(kind).append('\'');
        if (reader.hasName()) {
            b.append(" named '").append(reader.getName()).append('\'');
        }
        if (reader.hasText()) {
            b.append(", text is: '").append(reader.getText()).append('\'');
        }
        throw EeMessages.MESSAGES.errorParsingEJBClientDescriptor(b.toString(), reader.getLocation());
    }

}
