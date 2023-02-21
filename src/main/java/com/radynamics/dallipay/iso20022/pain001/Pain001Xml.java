package com.radynamics.dallipay.iso20022.pain001;

import com.radynamics.dallipay.iso20022.Payment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.Iterator;

public class Pain001Xml {
    private Document document;
    private NamespaceContext nsCtx;
    private final static String nsPrefix = "pain";

    public static Pain001Xml read(InputStream pain001) throws Exception {
        var o = new Pain001Xml();
        o.document = readXml(pain001);
        o.nsCtx = new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                if (nsPrefix.equals(prefix)) {
                    return o.document.getDocumentElement().getNamespaceURI();
                }
                return null;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public Iterator<String> getPrefixes(String namespaceURI) {
                return null;
            }
        };
        return o;
    }

    public Pain001Xml remove(Payment[] payments) throws XPathExpressionException {
        for (var p : payments) {
            remove(p);
        }
        return this;
    }

    public boolean isRemovable(Payment p) throws XPathExpressionException {
        return getCdtTrfTxInf(p) != null;
    }

    private Node getCdtTrfTxInf(Payment p) throws XPathExpressionException {
        var expression = createXPath().compile(String.format("//%s:EndToEndId[text()='%s']", nsPrefix, p.getEndToEndId()));
        var node = (Node) expression.evaluate(document, XPathConstants.NODE);
        return node == null ? null : node.getParentNode().getParentNode();
    }

    public Pain001Xml remove(Payment p) throws XPathExpressionException {
        var nodeCdtTrfTxInf = getCdtTrfTxInf(p);
        if (nodeCdtTrfTxInf == null) {
            return this;
        }

        var nodePmtInf = (Element) nodeCdtTrfTxInf.getParentNode();
        nodePmtInf.removeChild(nodeCdtTrfTxInf);

        if (nodePmtInf.getElementsByTagName("CdtTrfTxInf").getLength() == 0) {
            nodePmtInf.getParentNode().removeChild(nodePmtInf);
        }

        updateCtrlSum(p.getAmount() * -1);
        updateNbOfTxs(-1);

        return this;
    }

    private void updateCtrlSum(Double change) throws XPathExpressionException {
        var node = singleNodeOrThrow(String.format("//%s:CtrlSum", nsPrefix));
        var ctrlSum = Double.parseDouble(node.getTextContent());
        node.setTextContent(String.valueOf(ctrlSum + change));
    }

    private void updateNbOfTxs(Integer change) throws XPathExpressionException {
        var node = singleNodeOrThrow(String.format("//%s:NbOfTxs", nsPrefix));
        var nbOfTxs = Integer.parseInt(node.getTextContent());
        node.setTextContent(String.valueOf(nbOfTxs + change));
    }

    private Node singleNodeOrThrow(String xpath) throws XPathExpressionException {
        var expression = createXPath().compile(xpath);
        var node = (Node) expression.evaluate(document, XPathConstants.NODE);
        if (node == null) {
            throw new XPathExpressionException(String.format("Node %s not found", xpath));
        }
        return node;
    }

    private XPath createXPath() {
        var xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(nsCtx);
        return xpath;
    }

    public int countCdtTrfTxInf() throws XPathExpressionException {
        var expression = createXPath().compile(String.format("count(//%s:CdtTrfTxInf)", nsPrefix));
        return ((Double) expression.evaluate(document, XPathConstants.NUMBER)).intValue();
    }

    public void writeTo(File file) throws TransformerException, IOException {
        var stream = new FileOutputStream(file);
        try {
            var tf = TransformerFactory.newInstance();
            var t = tf.newTransformer();
            t.transform(new DOMSource(document), new StreamResult(stream));
        } finally {
            stream.close();
        }
    }

    private static Document readXml(InputStream is) throws ParserConfigurationException, IOException, SAXException {
        var dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setIgnoringComments(false);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setNamespaceAware(true);

        var db = dbf.newDocumentBuilder();
        db.setEntityResolver(new NullResolver());

        return db.parse(is);
    }

    static class NullResolver implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId) {
            return new InputSource(new StringReader(""));
        }
    }
}