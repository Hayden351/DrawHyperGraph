package misc;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Hayden Fields
 */
public class XmlOutput
{
    public static void main (String[] args) throws Exception
    {
        String relativeFilepath = "src/draw/testXml";
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(relativeFilepath));

        
        traversal(doc);
//        NodeList map = doc.getChildNodes();
//        for (int i = 0; i < map.getLength(); i++)
//            System.out.printf("%s", map.item(i));
    }
    public static void traversal(Node node)
    {
        traversal(node, 0, System.out);
    }
    
    public static String repeat(String str, int mult)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mult; i++)
            result.append(str);
        return result.toString();
    }
    
    public static void traversal(Node node, int indent, PrintStream out)
    {
        if (node.getNodeType() == Node.ELEMENT_NODE)
        {
            
            NodeList children = node.getChildNodes();
            if (children.getLength() == 1 && children.item(0).getNodeType() == Node.TEXT_NODE)
            {
                /* old
                out.printf("\n%s</%s>", node.getNodeName(), repeat("    ", indent));
                out.printf("%s", node.getTextContent());
                out.printf("</%s>", node.getNodeName());
                */
                
                
                // println to start on next line from previous tag
                out.println("");
                
                // put down indent
                // put down local name
                // if attribute 
                int min = (indent * 4);
                out.printf("%s<%s", repeat("    ", indent), node.getNodeName());
                int lineLength = (indent * 4) + 1 + node.getNodeName().length();
                Map<String, String> attributes = convert(node.getAttributes());
                Queue<String> queue = new ArrayDeque<>(attributes.keySet());
                boolean attributeWrapOccured = false;
                while (!queue.isEmpty()) // can do this without a queue *shrug*
                {
                    String key = queue.poll();
                    
                    if (min + 40 < lineLength + key.length() + 1 + (1 + attributes.get(key).length() + 1))
                    {
                        out.println();
                        out.printf("%s%s=\"%s\"",  repeat("    ", 4), key, attributes.get(key));
                        lineLength = min + key.length() + 1 + attributes.get(key).length();
                        attributeWrapOccured = true;
                    }
                    else
                    {
                        String text = String.format(" %s=\"%s\"", key, attributes.get(key));
                        out.printf("%s", text);
                        lineLength += text.length();
                    }
                }
                out.printf(">");
                
//                StringBuilder content = new StringBuilder();
//                content.append(repeat("    ", indent));
//                content.append("<").append(node.getNodeName());
//                out.printf(" %s>", , String.join(" ", convert(node.getAttributes()).entrySet().stream().map(x -> String.format("%s=\"%s\"", x.getKey(), x.getValue())).collect(Collectors.toList())));
                if (attributeWrapOccured)
                {
                    out.printf("\n%s%s", repeat("    ", indent + 1), node.getTextContent());
                    out.printf("\n%s</%s>", repeat("    ", indent), node.getNodeName());
                }
                else
                {
                    out.printf("%s", node.getTextContent());
                    out.printf("</%s>", node.getNodeName());
                }
            }
            else
            {
                out.printf("%s<%s>", repeat("    ", indent), node.getNodeName());
                for (int i = 0; i < children.getLength(); i++)
                    traversal(children.item(i), indent + 1, out);
                out.printf("\n%s</%s>", repeat("    ", indent), node.getNodeName());
            }
            
        }
        else if (node.getNodeType() == Node.DOCUMENT_NODE)
        {
            out.printf("<?xml version=\"%s\" encoding=\"%s\"?>\n", ((Document)node).getXmlVersion(), ((Document)node).getInputEncoding());
            traversal(node.getChildNodes().item(0), 0, out);
        }
        else if (node.getNodeType() == Node.TEXT_NODE)
        {
//            System.out.printf("%s", convertToList(node.getTextContent()));
            System.out.printf("%s", node.getTextContent().trim());
        }
    }
    public static List<String> convertToList(String str)
    {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < str.length(); i++)
            if (str.charAt(i) == '\n')
                result.add("\\n");
            else
                result.add(""+str.charAt(i));
        return result;
    }

    private static Map<String, String> convert (NamedNodeMap attributes)
    {
        Map<String, String> result = new HashMap<>();
        
        for (int i = 0; i < attributes.getLength(); i++)
        {
            Node attribute = attributes.item(i);
            result.put(attribute.getNodeName(), attribute.getTextContent());
        }
        
        return result;
    }
}
